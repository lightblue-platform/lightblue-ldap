/*
 Copyright 2014 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.crud.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.common.ldap.DBResolver;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.common.ldap.LdapErrorCode;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.common.ldap.LightblueUtil;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.ldap.translator.EntryTranslatorFromJson;
import com.redhat.lightblue.crud.ldap.translator.ModificationTranslatorFromJson;
import com.redhat.lightblue.crud.ldap.translator.ResultTranslatorToJson;
import com.redhat.lightblue.crud.ldap.translator.SortTranslator;
import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.ServerSideSortRequestControl;
import com.unboundid.ldap.sdk.controls.VirtualListViewRequestControl;

/**
 * {@link CRUDController} implementation for LDAP.
 *
 * @author dcrissman
 */
public class LdapCRUDController implements CRUDController {

    private final DBResolver dbResolver;

    public LdapCRUDController(DBResolver dbResolver) {
        this.dbResolver = dbResolver;
    }

    @Override
    public CRUDInsertionResponse insert(CRUDOperationContext ctx,
            Projection projection) {
        CRUDInsertionResponse response = new CRUDInsertionResponse();
        response.setNumInserted(0);

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);
        LdapFieldNameTranslator fieldNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);

        EntryTranslatorFromJson entryTranslatorFromJson = new EntryTranslatorFromJson(md, fieldNameTranslator);

        //Create Entry instances for each document.
        Map<String, DocCtx> documentToDnMap = new HashMap<>();
        List<com.unboundid.ldap.sdk.Entry> entries = parseDocuments(ctx, fieldNameTranslator, (DocCtx document, String dn) -> {
            com.unboundid.ldap.sdk.Entry entry = entryTranslatorFromJson.translate(document, dn);
            documentToDnMap.put(dn, document);
            return entry;
        });

        //Persist each Entry.
        LDAPConnection connection = getLdapConnection(store);
        for (com.unboundid.ldap.sdk.Entry entry : entries) {
            runInsert(connection, ctx, entry, (LDAPResult) -> response.setNumInserted(response.getNumInserted() + 1));
        }

        projectChanges(projection, ctx, documentToDnMap);

        return response;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx, boolean upsert,
            Projection projection) {
        CRUDSaveResponse response = new CRUDSaveResponse();
        response.setNumSaved(0);

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);
        LdapFieldNameTranslator fieldNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);
        LDAPConnection connection = getLdapConnection(store);

        ModificationTranslatorFromJson modificationTranslator = new ModificationTranslatorFromJson(md, fieldNameTranslator);
        EntryTranslatorFromJson entryTranslator = new EntryTranslatorFromJson(md, fieldNameTranslator);

        //Create Entry instances for each document.
        Map<String, DocCtx> documentToDnMap = new HashMap<>();
        List<com.unboundid.ldap.sdk.Entry> entries = new ArrayList<>();
        List<ModifyRequest> modifications = parseDocuments(ctx, fieldNameTranslator, (DocCtx document, String dn) -> {
            documentToDnMap.put(dn, document);

            SearchResultEntry entity = connection.getEntry(dn);

            if(entity != null){
                return modificationTranslator.translate(document, dn);
            }
            else if(upsert){
                //DNs that do not already exist, need to be created.
                entries.add(entryTranslator.translate(document, dn));
            }
            else {
                document.addError(Error.get(LdapErrorCode.ERR_LDAP_SAVE_ERROR_INS_WITH_NO_UPSERT, "New document, but upsert=false"));
            }

            return null;
        });

        //Persist each change as either an insert or a modify.
        for (ModifyRequest modifyRequest : modifications) {
            execute(ctx, new ExecutionHandler() {

                @Override
                void onSuccess(LDAPResult result) {
                    response.setNumSaved(response.getNumSaved() + 1);
                }

                @Override
                LDAPResult execute() throws LDAPException {
                    return connection.modify(modifyRequest);
                }
            });
        }

        if (upsert && !entries.isEmpty()) {
            for(com.unboundid.ldap.sdk.Entry entry : entries){
                runInsert(connection, ctx, entry, (LDAPResult) -> response.setNumSaved(response.getNumSaved() + 1));
            }
        }

        projectChanges(projection, ctx, documentToDnMap);

        return response;
    }

    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx,
            QueryExpression query, UpdateExpression update,
            Projection projection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CRUDDeleteResponse delete(CRUDOperationContext ctx,
            QueryExpression query) {

        if (query == null) {
            throw new IllegalArgumentException("No query was provided.");
        }

        CRUDDeleteResponse deleteResponse = new CRUDDeleteResponse();
        deleteResponse.setNumDeleted(0);

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);

        SearchRequest searchRequest = buildSearchRequest(store.getBaseDN(), md, query, null, SearchRequest.NO_ATTRIBUTES);

        LDAPConnection connection = getLdapConnection(store);

        runSearch(connection, searchRequest, ctx,
                (SearchResultEntry entry) -> {
                    //LDAP only supports performing 1 delete at a time.
                    execute(ctx, new ExecutionHandler() {

                        @Override
                        void onSuccess(LDAPResult deleteResult) {
                            deleteResponse.setNumDeleted(deleteResponse.getNumDeleted() + 1);
                        }

                        @Override
                        LDAPResult execute() throws LDAPException {
                            return connection.delete(entry.getDN());
                        }
                    });
                });

        return deleteResponse;
    }

    @Override
    public CRUDFindResponse find(CRUDOperationContext ctx,
            QueryExpression query, Projection projection, Sort sort, Long from,
            Long to) {

        if (query == null) {
            throw new IllegalArgumentException("No query was provided.");
        }
        if (projection == null) {
            throw new IllegalArgumentException("No projection was provided");
        }

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);

        CRUDFindResponse response = new CRUDFindResponse();
        response.setSize(0);

        LDAPConnection connection = getLdapConnection(store);

        LdapFieldNameTranslator fieldNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);

        SearchRequest searchRequest = buildSearchRequest(
                store.getBaseDN(),
                md,
                query,
                translateFieldNames(fieldNameTranslator, gatherRequiredFields(md, projection, query, sort)).toArray(new String[0]));
        if (sort != null) {
            searchRequest.addControl(new ServerSideSortRequestControl(false, new SortTranslator(fieldNameTranslator).translate(sort)));
        }
        if ((from != null) && (from > 0)) {
            int endPos = to.intValue() - from.intValue();
            searchRequest.addControl(new VirtualListViewRequestControl(from.intValue(), 0, endPos, 0, null, false));
        }

        ResultTranslatorToJson resultTranslator = new ResultTranslatorToJson(ctx.getFactory().getNodeFactory(), md, fieldNameTranslator);

        List<DocCtx> translatedDocs = new ArrayList<>();
        runSearch(connection, searchRequest, ctx, (SearchResultEntry entry) -> {
            translatedDocs.add(new DocCtx(resultTranslator.translate(entry)));
            response.setSize(response.getSize() + 1);
        });

        ctx.setDocuments(translatedDocs);

        Projector projector = Projector.getInstance(
                Projection.add(
                        projection,
                        new FieldAccessRoleEvaluator(
                                md,
                                ctx.getCallerRoles()).getExcludedFields(FieldAccessRoleEvaluator.Operation.find)
                        ),
                md);
        for (DocCtx document : ctx.getDocumentsWithoutErrors()) {
            document.setOutputDocument(projector.project(document, ctx.getFactory().getNodeFactory()));
        }

        return response;
    }

    @Override
    public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {
        //Do Nothing!!
    }

    @Override
    public MetadataListener getMetadataListener() {
        return new LdapMetadataListener();
    }

    /**
     * Returns a list of the field names that are needed for the operation to be
     * successful.
     * @param md - {@link EntityMetadata}.
     * @param projection - (optional) {@link Projection}.
     * @param query - (optional) {@link QueryExpression}.
     * @param sort - (optional) {@link Sort}.
     * @return list of field names.
     */
    private Set<Path> gatherRequiredFields(EntityMetadata md,
            Projection projection, QueryExpression query, Sort sort) {
        Set<Path> paths = new HashSet<>();

        FieldCursor cursor = md.getFieldCursor();
        while (cursor.next()) {
            Path node = cursor.getCurrentPath();
            String fieldName = node.getLast();

            if (((projection != null) && projection.isFieldRequiredToEvaluateProjection(node))
                    || ((query != null) && query.isRequired(node))
                    || ((sort != null) && sort.isRequired(node))) {
                if (LightblueUtil.isFieldAnArrayCount(fieldName, md.getFields())) {
                    /*
                     * Handles the case of an array count field, which will not actually exist in
                     * the ldap entity.
                     */
                    paths.add(node.mutableCopy().setLast(LightblueUtil.createArrayFieldNameFromCountField(fieldName)).immutableCopy());
                }
                else {
                    paths.add(node);
                }
            }
        }

        return paths;
    }

    /**
     * Translates a <code>Collection</code> of fieldNames into a <code>Set</code> of
     * attributeNames
     * @param property - {@link LdapFieldNameTranslator}.
     * @param fieldNames - <code>Collection</code> of fieldNames to translated
     * @return <code>Set</code> of translated attributeNames.
     */
    private Set<String> translateFieldNames(LdapFieldNameTranslator property, Collection<Path> fieldNames) {
        Set<String> attributes = new HashSet<>();
        for (Path path : fieldNames) {
            attributes.add(property.translateFieldName(path));
        }

        return attributes;
    }

    /**
     * For Insert and Save (and possibly Update), this method will project the results back
     * onto the documents.
     * @param projection - {@link Projection} If null, then nothing will happen.
     * @param ctx - {@link CRUDOperationContext}
     * @param documentToDnMap - Map linking {@link DocCtx} to the DN that represents it.
     */
    private void projectChanges(Projection projection, CRUDOperationContext ctx, Map<String, DocCtx> documentToDnMap) {
        if (projection == null) {
            return;
        }

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        JsonNodeFactory factory = ctx.getFactory().getNodeFactory();
        LdapFieldNameTranslator fieldNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);

        Set<String> requiredAttributeNames = translateFieldNames(fieldNameTranslator, gatherRequiredFields(md, projection, null, null));
        Projector projector = Projector.getInstance(
                Projection.add(
                        projection,
                        new FieldAccessRoleEvaluator(
                                md,
                                ctx.getCallerRoles()).getExcludedFields(FieldAccessRoleEvaluator.Operation.insert)
                        ),
                md);

        Path dnFieldPath = fieldNameTranslator.translateAttributeName(LdapConstant.ATTRIBUTE_DN);

        for (Entry<String, DocCtx> insertedDn : documentToDnMap.entrySet()) {
            String dn = insertedDn.getKey();
            DocCtx document = insertedDn.getValue();
            DocCtx projectionResponseJson = null;

            // If only dn is in the projection, then no need to query LDAP.
            if ((requiredAttributeNames.size() == 1) && requiredAttributeNames.contains(LdapConstant.ATTRIBUTE_DN)) {
                JsonDoc jdoc = new JsonDoc(factory.objectNode());
                jdoc.modify(dnFieldPath, StringType.TYPE.toJson(factory, dn), true);
                projectionResponseJson = new DocCtx(jdoc);
            }
            //TODO: else fetch entity from LDAP and project results.
            //TODO: Probably want to batch fetch as opposed to individual fetches.

            document.setOutputDocument(projector.project(projectionResponseJson, factory));
        }
    }

    /**
     * Returns a connection to ldap.
     * @param store - {@link LdapDataStore} to connect too.
     * @return a connection to ldap
     * @throws RuntimeException when unable to connect to ldap.
     */
    private LDAPConnection getLdapConnection(LdapDataStore store) {
        LDAPConnection connection = null;
        try {
            connection = dbResolver.get(store);
        } catch (LDAPException e) {
            //TODO: throw more relevant exception.
            throw new RuntimeException("Unable to establish connection to LDAP", e);
        }
        return connection;
    }

    private static SearchRequest buildSearchRequest(String baseDn, EntityMetadata md, QueryExpression query, String... attributes) {
        //TODO: Support scopes other than SUB
        return new SearchRequest(
                baseDn,
                SearchScope.SUB,
                new FilterBuilder(LdapCrudUtil.getLdapFieldNameTranslator(md)).build(query),
                attributes);
    }

    private void runSearch(LDAPConnection connection, SearchRequest searchRequest, CRUDOperationContext ctx, SearchResultProcessor searchRunner) {
        execute(ctx, new ExecutionHandler() {

            @Override
            void onSuccess(LDAPResult searchResult) {
                for (SearchResultEntry entry : ((SearchResult) searchResult).getSearchEntries()) {
                    searchRunner.process(entry);
                }
            }

            @Override
            SearchResult execute() throws LDAPException {
                return connection.search(searchRequest);
            }
        });
    }

    private interface SearchResultProcessor {
        void process(SearchResultEntry searchResultEntry);
    }

    private void runInsert(LDAPConnection connection, CRUDOperationContext ctx, com.unboundid.ldap.sdk.Entry entry, InsertResultProcessor processor) {
        execute(ctx, new ExecutionHandler() {

            @Override
            void onSuccess(LDAPResult insertResult) {
                processor.process(insertResult);
            }

            @Override
            LDAPResult execute() throws LDAPException {
                return connection.add(entry);
            }
        });
    }

    private interface InsertResultProcessor {
        void process(LDAPResult result);
    }

    private <T> List<T> parseDocuments(CRUDOperationContext ctx, LdapFieldNameTranslator fieldNameTranslator, DocumentProcessor<T> processor) {
        List<DocCtx> documents = ctx.getDocumentsWithoutErrors();
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);
        FieldAccessRoleEvaluator roles = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());

        List<T> items = new ArrayList<>();
        for (DocCtx document : documents) {
            Set<Path> paths = roles.getInaccessibleFields_Insert(document);
            if ((paths != null) && !paths.isEmpty()) {
                for (Path path : paths) {
                    document.addError(Error.get(CrudConstants.ERR_NO_FIELD_INSERT_ACCESS, path.toString()));
                }
            }

            Path uniqueFieldPath = fieldNameTranslator.translateAttributeName(store.getUniqueAttribute());
            JsonNode uniqueNode = document.get(uniqueFieldPath);
            if (uniqueNode == null) {
                document.addError(Error.get(MetadataConstants.ERR_PARSE_MISSING_ELEMENT, store.getUniqueAttribute()));
            }

            if (document.hasErrors()) {
                continue;
            }

            try {
                T item = processor.process(document, LdapCrudUtil.createDN(store, uniqueNode.asText()));
                if (item != null) {
                    items.add(item);
                }
            } catch (Error e) {
                document.addError(e);
            } catch (Exception e) {
                document.addError(Error.get(e));
            }
        }

        return items;
    }

    private interface DocumentProcessor<T> {
        T process(DocCtx document, String dn) throws Exception;
    }

    private void execute(CRUDOperationContext ctx, ExecutionHandler handler){
        try {
            LDAPResult result = handler.execute();
            if (ResultCode.SUCCESS.equals(result.getResultCode())) {
                handler.onSuccess(result);
            } else {
                ctx.addError(Error.get(
                        LdapErrorCode.ERR_LDAP_UNSUCCESSFUL_RESPONSE,
                        result.getResultCode().toString()));
            }
        } catch (LDAPException e) {
            ctx.addError(Error.get(LdapErrorCode.ERR_LDAP_REQUEST_FAILED, e));
        }
    }

    private abstract class ExecutionHandler {

        abstract LDAPResult execute() throws LDAPException;

        abstract void onSuccess(LDAPResult result);

    }

}

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
import com.unboundid.ldap.sdk.LDAPSearchException;
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

        List<DocCtx> documents = ctx.getDocumentsWithoutErrors();
        if (documents == null || documents.isEmpty()) {
            return response;
        }

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);
        LdapFieldNameTranslator fieldNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);

        FieldAccessRoleEvaluator roles = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());
        EntryTranslatorFromJson entryTranslatorFromJson = new EntryTranslatorFromJson(md, fieldNameTranslator);

        //Create Entry instances for each document.
        List<com.unboundid.ldap.sdk.Entry> entries = new ArrayList<>();
        Map<DocCtx, String> documentToDnMap = new HashMap<>();
        boolean hasError = false;
        for (DocCtx document : documents) {
            Set<Path> paths = roles.getInaccessibleFields_Insert(document);
            if ((paths != null) && !paths.isEmpty()) {
                for (Path path : paths) {
                    document.addError(Error.get("insert", CrudConstants.ERR_NO_FIELD_INSERT_ACCESS, path.toString()));
                    continue;
                }
            }

            Path uniqueFieldPath = fieldNameTranslator.translateAttributeName(store.getUniqueAttribute());
            JsonNode uniqueNode = document.get(uniqueFieldPath);
            if (uniqueNode == null) {
                throw Error.get(MetadataConstants.ERR_PARSE_MISSING_ELEMENT, store.getUniqueAttribute());
            }

            String dn = LdapCrudUtil.createDN(store, uniqueNode.asText());
            documentToDnMap.put(document, dn);
            try {
                entries.add(entryTranslatorFromJson.translate(document, dn));
            } catch (Exception e) {
                document.addError(Error.get(e));
                hasError = true;
            }
        }
        if (hasError) {
            return response;
        }

        //Persist each Entry.
        LDAPConnection connection = getNewLdapConnection(store);
        for (com.unboundid.ldap.sdk.Entry entry : entries) {
            try {
                LDAPResult insertResult = connection.add(entry);
                if (ResultCode.SUCCESS.equals(insertResult.getResultCode())) {
                    response.setNumInserted(response.getNumInserted() + 1);
                } else {
                    ctx.addError(Error.get("ldap:insert",
                            LdapErrorCode.ERR_LDAP_UNSUCCESSFUL_RESPONSE,
                            insertResult.getResultCode().toString()));
                }
            } catch (LDAPException e) {
                ctx.addError(Error.get(LdapErrorCode.ERR_LDAP_REQUEST_FAILED, e));
            }
        }

        projectChanges(projection, ctx, documentToDnMap);

        return response;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx, boolean upsert,
            Projection projection) {
        // TODO Auto-generated method stub
        return null;
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

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);

        CRUDDeleteResponse deleteResponse = new CRUDDeleteResponse();
        deleteResponse.setNumDeleted(0);

        LDAPConnection connection = getNewLdapConnection(store);

        LdapFieldNameTranslator fieldNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);

        //TODO: Support scopes other than SUB
        SearchRequest searchRequest = new SearchRequest(
                store.getBaseDN(),
                SearchScope.SUB,
                new FilterBuilder(fieldNameTranslator).build(query),
                SearchRequest.NO_ATTRIBUTES);

        try {
            SearchResult searchResult = connection.search(searchRequest);
            if (ResultCode.SUCCESS.equals(searchResult.getResultCode())) {
                for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                    //LDAP only supports performing 1 delete at a time.
                    try {
                        LDAPResult deleteResult = connection.delete(entry.getDN());
                        if (ResultCode.SUCCESS.equals(deleteResult.getResultCode())) {
                            deleteResponse.setNumDeleted(deleteResponse.getNumDeleted() + 1);
                        } else {
                            ctx.addError(Error.get("ldap:delete",
                                    LdapErrorCode.ERR_LDAP_UNSUCCESSFUL_RESPONSE,
                                    deleteResult.getResultCode().toString()));
                        }
                    } catch (LDAPException e) {
                        ctx.addError(Error.get(LdapErrorCode.ERR_LDAP_REQUEST_FAILED, e));
                    }
                }
            } else {
                ctx.addError(Error.get("ldap:search",
                        LdapErrorCode.ERR_LDAP_UNSUCCESSFUL_RESPONSE,
                        searchResult.getResultCode().toString()));
            }
        } catch (LDAPSearchException e) {
            ctx.addError(Error.get(LdapErrorCode.ERR_LDAP_REQUEST_FAILED, e));
        }

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

        LDAPConnection connection = getNewLdapConnection(store);

        LdapFieldNameTranslator fieldNameTranslator = LdapCrudUtil.getLdapFieldNameTranslator(md);

        //TODO: Support scopes other than SUB
        SearchRequest request = new SearchRequest(
                store.getBaseDN(),
                SearchScope.SUB,
                new FilterBuilder(fieldNameTranslator).build(query),
                translateFieldNames(fieldNameTranslator, gatherRequiredFields(md, projection, query, sort)).toArray(new String[0]));
        if (sort != null) {
            request.addControl(new ServerSideSortRequestControl(false, new SortTranslator(fieldNameTranslator).translate(sort)));
        }
        if ((from != null) && (from > 0)) {
            int endPos = to.intValue() - from.intValue();
            request.addControl(new VirtualListViewRequestControl(from.intValue(), 0, endPos, 0, null, false));
        }

        try{
            SearchResult searchResult = connection.search(request);

            if (ResultCode.SUCCESS.equals(searchResult.getResultCode())) {
                response.setSize(searchResult.getEntryCount());
                ResultTranslatorToJson resultTranslator = new ResultTranslatorToJson(ctx.getFactory().getNodeFactory(), md, fieldNameTranslator);
                List<DocCtx> translatedDocs = new ArrayList<>();
                for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                    try {
                        translatedDocs.add(new DocCtx(resultTranslator.translate(entry)));
                    } catch (Exception e) {
                        ctx.addError(Error.get(e));
                    }
                }
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
            } else {
                ctx.addError(Error.get("ldap:search",
                        LdapErrorCode.ERR_LDAP_UNSUCCESSFUL_RESPONSE,
                        searchResult.getResultCode().toString()));
            }
        } catch (LDAPSearchException e) {
            ctx.addError(Error.get(LdapErrorCode.ERR_LDAP_REQUEST_FAILED, e));
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
    private void projectChanges(Projection projection, CRUDOperationContext ctx, Map<DocCtx, String> documentToDnMap) {
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

        for (Entry<DocCtx, String> insertedDn : documentToDnMap.entrySet()) {
            DocCtx document = insertedDn.getKey();
            String dn = insertedDn.getValue();
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
     * Returns a new connection to ldap.
     * @param store - {@link LdapDataStore} to connect too.
     * @return a new connection to ldap
     * @throws RuntimeException when unable to connect to ldap.
     */
    private LDAPConnection getNewLdapConnection(LdapDataStore store) {
        LDAPConnection connection = null;
        try {
            connection = dbResolver.get(store);
        } catch (LDAPException e) {
            //TODO: throw more relevant exception.
            throw new RuntimeException("Unable to establish connection to LDAP", e);
        }
        return connection;
    }

}

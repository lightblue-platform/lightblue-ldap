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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.common.ldap.DBResolver;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapDataStore;
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
import com.redhat.lightblue.crud.ldap.translator.FilterTranslator;
import com.redhat.lightblue.crud.ldap.translator.ResultTranslator;
import com.redhat.lightblue.crud.ldap.translator.SortTranslator;
import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.hystrix.ldap.InsertCommand;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.types.IntegerType;
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
public class LdapCRUDController implements CRUDController{

    private final DBResolver dbResolver;

    public LdapCRUDController(DBResolver dbResolver){
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
        LdapDataStore store = getLdapDataStore(md);

        FieldAccessRoleEvaluator roles = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());
        EntryBuilder entryBuilder = new EntryBuilder(md);

        //Create Entry instances for each document.
        List<com.unboundid.ldap.sdk.Entry> entries = new ArrayList<com.unboundid.ldap.sdk.Entry>();
        Map<DocCtx, String> documentToDnMap = new HashMap<DocCtx, String>();
        boolean hasError = false;
        for(DocCtx document : documents){
            List<Path> paths = roles.getInaccessibleFields_Insert(document);
            if((paths != null) && !paths.isEmpty()){
                for(Path path : paths){
                    document.addError(Error.get("insert", CrudConstants.ERR_NO_FIELD_INSERT_ACCESS, path.toString()));
                    continue;
                }
            }

            JsonNode rootNode = document.getRoot();

            JsonNode uniqueNode = rootNode.get(store.getUniqueField());
            if(uniqueNode == null){
                throw new IllegalArgumentException(store.getUniqueField() + " is a required field");
            }

            String dn = createDN(store, uniqueNode.asText());
            documentToDnMap.put(document, dn);
            try{
                entries.add(entryBuilder.build(dn, document));
            }
            catch(Exception e){
                document.addError(Error.get(e));
                hasError = true;
            }
        }
        if(hasError){
            return response;
        }

        //Persist each Entry.
        LDAPConnection connection = getNewLdapConnection(store);
        for(com.unboundid.ldap.sdk.Entry entry : entries){
            InsertCommand command = new InsertCommand(connection, entry);

            LDAPResult result = command.execute();
            if(result.getResultCode() != ResultCode.SUCCESS){
                //TODO: Do something to indicate unsuccessful status
                continue;
            }

            response.setNumInserted(response.getNumInserted() + 1);
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
        // TODO Auto-generated method stub
        return null;
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
        LdapDataStore store = getLdapDataStore(md);

        CRUDFindResponse response = new CRUDFindResponse();
        response.setSize(0);

        LDAPConnection connection = getNewLdapConnection(store);

        try {
            //TODO: Support scopes other than SUB
            SearchRequest request = new SearchRequest(
                    store.getBaseDN(),
                    SearchScope.SUB,
                    new FilterTranslator().translate(query),
                    collectRequiredFields(md, projection, query, sort).toArray(new String[0]));
            if(sort != null){
                request.addControl(new ServerSideSortRequestControl(false, new SortTranslator().translate(sort)));
            }
            if((from != null) && (from > 0)){
                int endPos = to.intValue() - from.intValue();
                request.addControl(new VirtualListViewRequestControl(from.intValue(), 0, endPos, 0, null, false));
            }

            SearchResult result = connection.search(request);

            response.setSize(result.getEntryCount());
            ResultTranslator resultTranslator = new ResultTranslator(ctx.getFactory().getNodeFactory());
            List<DocCtx> translatedDocs = new ArrayList<DocCtx>();
            for(SearchResultEntry entry : result.getSearchEntries()){
                try{
                    translatedDocs.add(resultTranslator.translate(entry, md));
                }
                catch(Exception e){
                    DocCtx erroredDoc = new DocCtx(null);
                    erroredDoc.addError(Error.get(e));
                    translatedDocs.add(erroredDoc);
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
            for (DocCtx document : ctx.getDocuments()) {
                document.setOutputDocument(projector.project(document, ctx.getFactory().getNodeFactory()));
            }
        }
        catch (LDAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {
        //Do Nothing!!
    }

    @Override
    public MetadataListener getMetadataListener() {
        return new MetadataListener() {

            @Override
            public void beforeUpdateEntityInfo(Metadata m, EntityInfo ei, boolean newEntity) {
                //Do Nothing!!
            }

            /**
             * Ensure that dn and objectClass are on the entity.
             */
            @Override
            public void beforeCreateNewSchema(Metadata m, EntityMetadata md) {
                Fields fields = md.getEntitySchema().getFields();
                if(!fields.has(LdapConstant.FIELD_DN)){
                    fields.addNew(new SimpleField(LdapConstant.FIELD_DN, StringType.TYPE));
                }
                if(!fields.has(LdapConstant.FIELD_OBJECT_CLASS)){
                    fields.addNew(new ArrayField(LdapConstant.FIELD_OBJECT_CLASS, new SimpleArrayElement(StringType.TYPE)));
                    fields.addNew(new SimpleField(LightblueUtil.createArrayCountFieldName(LdapConstant.FIELD_OBJECT_CLASS), IntegerType.TYPE));
                }
            }

            @Override
            public void afterUpdateEntityInfo(Metadata m, EntityInfo ei, boolean newEntity) {
                //Do Nothing!!
            }

            @Override
            public void afterCreateNewSchema(Metadata m, EntityMetadata md) {
                //Do Nothing!!
            }
        };
    }

    /**
     * Shortcut method to get and return the {@link LdapDataStore} on the passed in
     * {@link EntityMetadata}.
     * @param md - {@link EntityMetadata}
     * @return {@link LdapDataStore}
     * @throws IllegalArgumentException if an {@link LdapDataStore} is not set
     * on the {@link EntityMetadata}.
     */
    private LdapDataStore getLdapDataStore(EntityMetadata md){
        DataStore store = md.getDataStore();
        if(!(store instanceof LdapDataStore)){
            throw new IllegalArgumentException("DataStore of type " + store.getClass() + " is not supported.");
        }
        return (LdapDataStore) store;
    }

    /**
     * Creates and returns a unique DN.
     * @param store - {@link LdapDataStore} to use as the BaseDN and field that
     * is used to represent uniqueness.
     * @param uniqueValue - value that makes the entity unique.
     * @return a string representation of the DN.
     */
    private String createDN(LdapDataStore store, String uniqueValue){
        return store.getUniqueField() + "=" + uniqueValue + "," + store.getBaseDN();
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
    private Set<String> collectRequiredFields(EntityMetadata md,
            Projection projection, QueryExpression query, Sort sort){
        Set<String> fields = new HashSet<String>();

        FieldCursor cursor = md.getFieldCursor();
        while(cursor.next()) {
            Path node = cursor.getCurrentPath();
            String fieldName = node.getLast();

            if(((projection != null) && projection.isFieldRequiredToEvaluateProjection(node))
                    || ((query != null) && query.isRequired(node))
                    || ((sort != null) && sort.isRequired(node))) {
                if(LightblueUtil.isFieldAnArrayCount(fieldName, md.getFields())){
                    /*
                     * Handles the case of an array count field, which will not actually exist in
                     * the ldap entity.
                     */
                    fields.add(LightblueUtil.createArrayFieldNameFromCountField(fieldName));
                }
                else{
                    fields.add(fieldName);
                }
            }
        }

        return fields;
    }

    /**
     * For Insert and Save (and possibly Update), this method will project the results back
     * onto the documents.
     * @param projection - {@link Projection} If null, then nothing will happen.
     * @param ctx - {@link CRUDOperationContext}
     * @param documentToDnMap - Map linking {@link DocCtx} to the DN that represents it.
     */
    private void projectChanges(Projection projection, CRUDOperationContext ctx, Map<DocCtx, String> documentToDnMap) {
        if(projection == null){
            return;
        }

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        JsonNodeFactory factory = ctx.getFactory().getNodeFactory();
        Set<String> requiredFields = collectRequiredFields(md, projection, null, null);
        Projector projector = Projector.getInstance(
                Projection.add(
                        projection,
                        new FieldAccessRoleEvaluator(
                                md,
                                ctx.getCallerRoles()).getExcludedFields(FieldAccessRoleEvaluator.Operation.insert)
                        ),
                        md);

        for(Entry<DocCtx, String> insertedDn : documentToDnMap.entrySet()){
            DocCtx document = insertedDn.getKey();
            String dn = insertedDn.getValue();
            DocCtx projectionResponseJson = null;

            // If only dn is in the projection, then no need to query LDAP.
            if((requiredFields.size() == 1) && requiredFields.contains(LdapConstant.FIELD_DN)){
                ObjectNode node = factory.objectNode();
                node.set(LdapConstant.FIELD_DN, StringType.TYPE.toJson(factory, dn));
                projectionResponseJson = new DocCtx(new JsonDoc(node));
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
        }
        catch (LDAPException e) {
            //TODO: throw more relevant exception.
            throw new RuntimeException("Unable to establish connection to LDAP", e);
        }
        return connection;
    }

}

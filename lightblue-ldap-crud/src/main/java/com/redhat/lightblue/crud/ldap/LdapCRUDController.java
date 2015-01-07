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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.ldap.DBResolver;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.hystrix.ldap.InsertCommand;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;

public class LdapCRUDController implements CRUDController{

    private final DBResolver dbResolver;

    public LdapCRUDController(DBResolver dbResolver){
        this.dbResolver = dbResolver;
    }

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

        //TODO Revisit Projection
        //FieldAccessRoleEvaluator roleEval = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());
        /*Projection combinedProjection = Projection.add(
                projection,
                roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.insert));*/

        /*        Projector projector = null;
        if(combinedProjection != null){
            projector = Projector.getInstance(combinedProjection, md);
        }*/

        try {
            LDAPConnection connection = dbResolver.get(store);

            for(DocCtx document : documents){
                //document.setOriginalDocument(document);
                JsonNode rootNode = document.getRoot();

                JsonNode uniqueNode = rootNode.get(store.getUniqueField());
                if(uniqueNode == null){
                    throw new IllegalArgumentException(store.getUniqueField() + " is a required field");
                }

                Entry entry = new Entry(createDN(store, uniqueNode.asText()));

                Iterator<Map.Entry<String, JsonNode>> nodeIterator = rootNode.fields();
                while(nodeIterator.hasNext()){
                    Map.Entry<String, JsonNode> node = nodeIterator.next();
                    if("dn".equalsIgnoreCase(node.getKey())){
                        throw new IllegalArgumentException(
                                "DN should not be included as it's value will be derived from the metadata.basedn and" +
                                " the metadata.uniqueattr. Including the DN as an insert attribute is confusing.");
                    }

                    JsonNode valueNode = node.getValue();
                    if(valueNode.isArray()){
                        List<String> values = new ArrayList<String>();
                        for(JsonNode string : valueNode){
                            values.add(string.asText());
                        }
                        entry.addAttribute(new Attribute(node.getKey(), values));
                    }
                    else{
                        entry.addAttribute(new Attribute(node.getKey(), node.getValue().asText()));
                    }
                }

                InsertCommand command = new InsertCommand(connection, entry);

                LDAPResult result = command.execute();
                if(result.getResultCode() != ResultCode.SUCCESS){
                    //TODO: Do something to indicate unsuccessful status
                    continue;
                }

                /*if(projector != null){
                    JsonDoc jsonDoc = null; //TODO: actually populate field.
                    document.setOutputDocument(projector.project(jsonDoc, ctx.getFactory().getNodeFactory()));
                }
                else{*/
                //document.setOutputDocument(new JsonDoc(new ObjectNode(ctx.getFactory().getNodeFactory())));
                //}

                response.setNumInserted(response.getNumInserted() + 1);
            }
        }
        catch (LDAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return response;
    }

    public CRUDSaveResponse save(CRUDOperationContext ctx, boolean upsert,
            Projection projection) {
        // TODO Auto-generated method stub
        return null;
    }

    public CRUDUpdateResponse update(CRUDOperationContext ctx,
            QueryExpression query, UpdateExpression update,
            Projection projection) {
        // TODO Auto-generated method stub
        return null;
    }

    public CRUDDeleteResponse delete(CRUDOperationContext ctx,
            QueryExpression query) {
        // TODO Auto-generated method stub
        return null;
    }

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

        try {
            LDAPConnection connection = dbResolver.get(store);

            Filter filter = new FilterTranslator().translate(query);
            SearchRequest request = new SearchRequest(store.getBaseDN(), SearchScope.SUB, filter, "*");
            SearchResult result = connection.search(request);

            response.setSize(result.getEntryCount());
            ctx.setDocuments(new LdapTranslator(ctx.getFactory().getNodeFactory()).translate(result, md));

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

    public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {
        // TODO Auto-generated method stub
    }

    public MetadataListener getMetadataListener() {
        return null;
    }

    private LdapDataStore getLdapDataStore(EntityMetadata md){
        DataStore store = md.getDataStore();
        if(!(store instanceof LdapDataStore)){
            throw new IllegalArgumentException("DataStore of type " + store.getClass() + " is not supported.");
        }
        return (LdapDataStore) store;
    }

    private String createDN(LdapDataStore store, String uniqueValue){
        return store.getUniqueField() + "=" + uniqueValue + "," + store.getBaseDN();
    }

}

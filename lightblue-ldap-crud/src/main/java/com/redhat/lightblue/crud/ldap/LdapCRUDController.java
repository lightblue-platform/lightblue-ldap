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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.common.ldap.DBResolver;
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
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;

public class LdapCRUDController implements CRUDController{

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapCRUDController.class);

    private final DBResolver dbResolver;

    public LdapCRUDController(DBResolver dbResolver){
        this.dbResolver = dbResolver;
    }

    public CRUDInsertionResponse insert(CRUDOperationContext ctx,
            Projection projection) {
        CRUDInsertionResponse response = new CRUDInsertionResponse();
        List<DocCtx> documents = ctx.getDocumentsWithoutErrors();
        if (documents == null || documents.isEmpty()) {
            response.setNumInserted(0);
            return response;
        }

        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());

        FieldAccessRoleEvaluator roleEval = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());
        Projection combinedProjection = Projection.add(
                projection,
                roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.insert));

        Projector projector = null;
        if(combinedProjection != null){
            projector = Projector.getInstance(combinedProjection, md);
        }

        try {
            LDAPConnection connection = dbResolver.get(md.getDataStore());

            for(DocCtx document : documents){
                Entry entry = new Entry(""); //TODO populate Entry

                InsertCommand command = new InsertCommand(connection, entry);

                LDAPResult result = command.execute();
                if(result.getResultCode() != ResultCode.SUCCESS){
                    //TODO: Do something to indicate unsuccessful status
                }

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
        // TODO Auto-generated method stub
        return null;
    }

    public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {
        // TODO Auto-generated method stub
    }

    public MetadataListener getMetadataListener() {
        return null;
    }

}

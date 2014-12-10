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

import com.redhat.lightblue.common.ldap.DBResolver;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonDoc;

public class LdapCRUDController implements CRUDController{

    private final DBResolver dbResolver;

    public LdapCRUDController(DBResolver dbResolver){
        this.dbResolver = dbResolver;
    }

    public CRUDInsertionResponse insert(CRUDOperationContext ctx,
            Projection projection) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

}

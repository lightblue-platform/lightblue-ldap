package com.redhat.lightblue.crud.ldap;

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

    public MetadataListener getMetadataListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {
        // TODO Auto-generated method stub

    }

}

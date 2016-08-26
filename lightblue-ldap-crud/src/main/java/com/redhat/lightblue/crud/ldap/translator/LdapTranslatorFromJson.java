package com.redhat.lightblue.crud.ldap.translator;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.ldap.LdapErrorCode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.BinaryType;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonNodeCursor;
import com.unboundid.util.StaticUtils;

public abstract class LdapTranslatorFromJson<T> extends NonPersistedPredefinedFieldTranslatorFromJson<T> {

    public LdapTranslatorFromJson(EntityMetadata entityMetadata) {
        super(entityMetadata);

    }

    @Override
    protected void translateObjectArray(ArrayField field, JsonNodeCursor cursor, T target) {
        throw Error.get(LdapErrorCode.ERR_UNSUPPORTED_FEATURE_OBJECT_ARRAY, field.getFullPath().toString());
    }

    @Override
    protected Object fromJson(Type type, JsonNode node){
        if(type instanceof DateType){
            return StaticUtils.encodeGeneralizedTime((Date)type.fromJson(node));
        }
        else if(type instanceof BinaryType){
            return type.fromJson(node);
        }
        else{
            return super.fromJson(type, node).toString();
        }
    }

}

package com.redhat.lightblue.crud.ldap.translator;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.ldap.LdapErrorCode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.translator.NonPersistedPredefinedFieldTranslatorFromJson;
import com.redhat.lightblue.metadata.types.BinaryType;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import com.unboundid.util.StaticUtils;

public abstract class LdapTranslatorFromJson<T> extends NonPersistedPredefinedFieldTranslatorFromJson<T> {

    public LdapTranslatorFromJson(EntityMetadata entityMetadata) {
        super(entityMetadata);

    }

    @Override
    protected Object createInstanceFor(Path path) {
        throw Error.get(LdapErrorCode.ERR_UNSUPPORTED_FEATURE, path.toString());
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

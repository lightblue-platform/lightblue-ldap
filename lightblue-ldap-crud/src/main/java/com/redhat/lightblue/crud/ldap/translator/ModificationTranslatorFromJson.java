package com.redhat.lightblue.crud.ldap.translator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.BinaryType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;

public class ModificationTranslatorFromJson extends LdapTranslatorFromJson<List<Modification>> {

    private final LdapFieldNameTranslator fieldNameTranslator;
    private final Set<Path> modifiedPaths = new HashSet<>();

    public ModificationTranslatorFromJson(EntityMetadata entityMetadata, LdapFieldNameTranslator fieldNameTranslator) {
        super(entityMetadata);
        this.fieldNameTranslator = fieldNameTranslator;
    }

    public ModifyRequest translate(JsonDoc document, String dn) {
        Error.push(LdapConstant.ATTRIBUTE_DN + "=" + dn);
        try {
            return new ModifyRequest(dn, translate(document));
        } finally {
            Error.pop();
        }
    }

    private List<Modification> translate(JsonDoc document) {
        List<Modification> modifications = new ArrayList<>();
        translate(document, modifications);
        return modifications;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void translate(SimpleField field, JsonNode node, Object target) {
        String attributeName = fieldNameTranslator.translateFieldName(field.getFullPath());

        Type type = field.getType();
        Object o = fromJson(type, node);
        if(type instanceof BinaryType) {
            ((List<Modification>) target).add(new Modification(ModificationType.REPLACE, attributeName, (byte[])o));
        } else {
            ((List<Modification>) target).add(new Modification(ModificationType.REPLACE, attributeName, o.toString()));
        }

        modifiedPaths.add(field.getFullPath());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void translate(ArrayField field, List<Object> items, Object target) {
        ArrayElement arrayElement = field.getElement();
        Type arrayElementType = arrayElement.getType();
        String attributeName = fieldNameTranslator.translateFieldName(field.getFullPath());

        if(arrayElementType instanceof BinaryType){
            List<byte[]> bytes = new ArrayList<>();
            for(Object item : items){
                bytes.add((byte[])item);
            }
            ((List<Modification>) target).add(new Modification(ModificationType.REPLACE, attributeName, bytes.toArray(new byte[0][])));
        }
        else{
            List<String> values = new ArrayList<>();
            for(Object item : items){
                values.add(item.toString());
            }
            ((List<Modification>) target).add(new Modification(ModificationType.REPLACE, attributeName, values.toArray(new String[0])));
        }

        modifiedPaths.add(field.getFullPath());
    }

}

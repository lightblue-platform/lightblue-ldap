/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.crud.ldap.translator;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.BinaryType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.unboundid.ldap.sdk.Entry;

/**
 * Translates Lightblue json into populated instances of {@link Entry} for LDAP interaction.
 *
 * @author dcrissman
 */
public class EntryTranslatorFromJson extends LdapTranslatorFromJson<Entry>{

    private final LdapFieldNameTranslator fieldNameTranslator;

    public EntryTranslatorFromJson(EntityMetadata entityMetadata, LdapFieldNameTranslator fieldNameTranslator){
        super(entityMetadata);
        this.fieldNameTranslator = fieldNameTranslator;
    }

    public Entry translate(JsonDoc document, String dn){
        Entry entry = new Entry(dn);
        translate(document, entry);
        return entry;
    }

    @Override
    public void translate(JsonDoc document, Entry target){
        Error.push(LdapConstant.ATTRIBUTE_DN + "=" + target.getDN());
        try{
            super.translate(document, target);
        }
        finally{
            Error.pop();
        }
    }

    @Override
    protected void translate(SimpleField field, JsonNode node, Object target) {
        String attributeName = fieldNameTranslator.translateFieldName(field.getFullPath());

        if(LdapConstant.ATTRIBUTE_DN.equalsIgnoreCase(attributeName)){
            //DN is derived using metadata.uniqueattr, providing it is confusing.
            throw Error.get(MetadataConstants.ERR_INVALID_FIELD_REFERENCE, LdapConstant.ATTRIBUTE_DN);
        }

        Type type = field.getType();
        Object o = fromJson(type, node);
        if(type instanceof BinaryType) {
            ((Entry) target).addAttribute(attributeName, (byte[])o);
        }
        else{
            ((Entry) target).addAttribute(attributeName, o.toString());
        }
    }

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
            ((Entry) target).addAttribute(attributeName, bytes.toArray(new byte[0][]));
        }
        else{
            List<String> values = new ArrayList<>();
            for(Object item : items){
                values.add(item.toString());
            }
            ((Entry) target).addAttribute(attributeName, values);
        }
    }

}

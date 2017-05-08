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

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.crud.NonPersistedPredefinedFieldTranslatorToJson;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.BinaryType;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.SearchResultEntry;

/**
 * Translator to convert UnboundID {@link SearchResultEntry} into json that Lightblue can understand.
 *
 * @author dcrissman
 */
public class ResultTranslatorToJson extends NonPersistedPredefinedFieldTranslatorToJson<SearchResultEntry>{

    private final LdapFieldNameTranslator fieldNameTranslator;
    private final Path dnPath;

    public ResultTranslatorToJson(JsonNodeFactory factory, EntityMetadata entityMetadata, LdapFieldNameTranslator fieldNameTranslator) {
        super(factory, entityMetadata);
        this.fieldNameTranslator = fieldNameTranslator;
        dnPath = fieldNameTranslator.translateAttributeName(LdapConstant.ATTRIBUTE_DN);
    }

    @Override
    public JsonDoc translate(SearchResultEntry entry){
        Error.push(LdapConstant.ATTRIBUTE_DN + "=" + entry.getDN());
        try{
            JsonDoc jdoc = super.translate(entry);
            jdoc.modify(dnPath, toJson(StringType.TYPE, entry.getDN()), true);
            return jdoc;
        }
        finally{
            Error.pop();
        }
    }

    @Override
    protected void appendToJsonNode(Object value, ContainerNode<?> targetNode, FieldCursor cursor) {
        Path fieldPath = cursor.getCurrentPath();

        if(dnPath.equals(fieldPath)){
            //DN is not technically an attribute and can be skipped.
            return;
        }

        super.appendToJsonNode(value, targetNode, cursor);
    }

    @Override
    protected JsonNode translate(ReferenceField field, Object o) {
        throw new UnsupportedOperationException("ReferenceField type not currently supported.");
    }

    @Override
    protected JsonNode translate(SimpleField field, Object o) {
        Type type = field.getType();
        Attribute attr = (Attribute) o;

        Object value = null;
        if(type instanceof DateType){
            value = attr.getValueAsDate();
        }
        else if(type instanceof BinaryType){
            value = attr.getValueByteArray();
        }
        else{
            value = attr.getValue();
        }

        if(value == null){
            throw new NullPointerException("Unable to convert LDAP attribute to json resulting in a null value: " + attr.getName());
        }

        return toJson(field.getType(), value);
    }

    @Override
    protected Object getValueFor(Object value, Path path) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof SearchResultEntry)) {
            throw new IllegalArgumentException("Only SearchResultEntry objects are supported: " + value.getClass());
        }

        String attributeName = fieldNameTranslator.translateFieldName(path);
        return ((SearchResultEntry) value).getAttribute(attributeName);
    }

    @Override
    protected List<String> getSimpleArrayValues(Object o, SimpleArrayElement simpleArrayElement) {
        Attribute attr = (Attribute) o;
        return Arrays.asList(attr.getValues());
    }

    @Override
    protected int getSizeOf(Object o) {
        Attribute attr = (Attribute) o;
        return attr.getValues().length;
    }

}

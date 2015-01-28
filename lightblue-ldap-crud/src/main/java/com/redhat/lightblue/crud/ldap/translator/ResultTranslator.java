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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.common.ldap.LightblueUtil;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.BinaryType;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.SearchResultEntry;

/**
 * Translator to convert UnboundID {@link SearchResultEntry} into json that Lightblue can understand.
 *
 * @author dcrissman
 */
public class ResultTranslator {

    private final JsonNodeFactory factory;
    private final EntityMetadata md;
    private final LdapFieldNameTranslator fieldNameTranslator;
    private final Path dnPath;

    public ResultTranslator(JsonNodeFactory factory, EntityMetadata md, LdapFieldNameTranslator fieldNameTranslator){
        this.factory = factory;
        this.md = md;
        this.fieldNameTranslator = fieldNameTranslator;
        dnPath = fieldNameTranslator.translateAttributeName(LdapConstant.ATTRIBUTE_DN);
    }

    public DocCtx translate(SearchResultEntry entry){
        FieldCursor cursor = md.getFieldCursor();
        Fields fields = md.getFields();

        if (cursor.firstChild()) {
            ObjectNode node = toJson(entry, cursor, fields);
            node.set(dnPath.toString(), StringType.TYPE.toJson(factory, entry.getDN()));
            return new DocCtx(new JsonDoc(node));
        }

        //TODO: What to do in case of a null value here?
        return null;
    }

    private ObjectNode toJson(SearchResultEntry entry, FieldCursor fieldCursor, Fields fields){
        ObjectNode node = factory.objectNode();
        String entityName = md.getEntityInfo().getName();

        do {
            Path fieldPath = fieldCursor.getCurrentPath();

            if(dnPath.matches(fieldPath)){
                //DN is not handled as a normal attribute, can be skipped.
                continue;
            }
            else if(LightblueUtil.isFieldObjectType(fieldPath.toString())){
                node.set(fieldPath.toString(), StringType.TYPE.toJson(factory, entityName));
            }
            else{
                appendToJsonNode(entry, fieldCursor, node, fields);
            }

        } while(fieldCursor.nextSibling());

        return node;
    }

    private void appendToJsonNode(SearchResultEntry entry, FieldCursor fieldCursor, ObjectNode targetNode, Fields fields){
        FieldTreeNode field = fieldCursor.getCurrentNode();
        String fieldName = field.getName();
        Path path = fieldCursor.getCurrentPath();

        String attributeName = fieldNameTranslator.translateFieldName(path);
        Attribute attr = entry.getAttribute(attributeName);

        JsonNode value = null;

        if(LightblueUtil.isFieldAnArrayCount(fieldName, fields)){
            /*
             * This case will be handled by the array itself, allowing this to
             * process runs the risk of nulling out the correct value.
             */
            return;
        }
        else if (field instanceof ObjectField) {
            value = toJson((ObjectField)field, fieldCursor, entry);
        }
        else if(attr != null){
            if (field instanceof SimpleField) {
                value = toJson((SimpleField)field, attr);
            }
            else if (field instanceof ArrayField){
                value = toJson((ArrayField)field, attr, fieldCursor);
                targetNode.set(
                        LightblueUtil.createArrayCountFieldName(fieldName),
                        IntegerType.TYPE.toJson(factory, attr.getValues().length));
            }
            else if (field instanceof ReferenceField) {
                value = toJson((ReferenceField)field, attr);
            }
            else{
                throw new UnsupportedOperationException("Unknown Field type: " + field.getClass().getName());
            }
        }

        targetNode.set(fieldName, value);
    }

    private JsonNode toJson(SimpleField field, Attribute attr){
        Type type = field.getType();

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

        return field.getType().toJson(factory, value);
    }

    private JsonNode toJson(ObjectField field, FieldCursor fieldCursor, SearchResultEntry entry){
        if(!fieldCursor.firstChild()){
            //TODO: Should an exception be thrown here?
            return null;
        }

        JsonNode node = toJson(entry, fieldCursor, field.getFields());

        fieldCursor.parent();

        return node;
    }

    private JsonNode toJson(ArrayField field, Attribute attr, FieldCursor fieldCursor){
        if(!fieldCursor.firstChild()){
            //TODO: Should an exception be thrown here?
            return null;
        }

        FieldTreeNode node = fieldCursor.getCurrentNode();

        ArrayElement arrayElement = field.getElement();
        ArrayNode valueNode = factory.arrayNode();

        if (arrayElement instanceof SimpleArrayElement) {
            String[] values = attr.getValues();
            for(String value : values){
                valueNode.add(node.getType().toJson(factory, value));
            }
        }
        else if(arrayElement instanceof ObjectArrayElement){
            throw new UnsupportedOperationException("Object ArrayField type is not currently supported.");
        }
        else{
            throw new UnsupportedOperationException("ArrayElement type is not supported: " + node.getClass().getName());
        }

        fieldCursor.parent();
        return valueNode;
    }

    private JsonNode toJson(ReferenceField field, Attribute attr){
        throw new UnsupportedOperationException("ReferenceField type not currently supported.");
    }

}

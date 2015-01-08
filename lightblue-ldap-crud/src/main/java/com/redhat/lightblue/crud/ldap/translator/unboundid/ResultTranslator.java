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
package com.redhat.lightblue.crud.ldap.translator.unboundid;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.BooleanType;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.JsonDoc;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;

/**
 * Translator to convert UnboundID {@link SearchResultEntry} into json that Lightblue can understand.
 *
 * @author dcrissman
 */
public class ResultTranslator {

    private final JsonNodeFactory factory;

    public ResultTranslator(JsonNodeFactory factory){
        this.factory = factory;
    }

    public List<DocCtx> translate(SearchResult result, EntityMetadata md){
        List<DocCtx> docs = new ArrayList<DocCtx>();
        for(SearchResultEntry entry : result.getSearchEntries()){
            docs.add(translate(entry, md));
        }
        return docs;
    }

    public DocCtx translate(SearchResultEntry entry, EntityMetadata md){
        FieldCursor cursor = md.getFieldCursor();
        String entityName = md.getEntityInfo().getName();
        if (cursor.firstChild()) {
            return new DocCtx(new JsonDoc(toJson(entry, cursor, entityName)));
        }

        return null;
    }

    private JsonNode toJson(SearchResultEntry entry, FieldCursor fieldCursor, String entityName){
        ObjectNode node = factory.objectNode();

        do {
            FieldTreeNode field = fieldCursor.getCurrentNode();
            String fieldName = field.getName();
            if(fieldName.endsWith("#")){
                /*
                 * This case will be handled by the array itself, allowing this to
                 * process runs the risk of nulling out the correct value.
                 */
                continue;
            }

            Attribute attr = entry.getAttribute(fieldName);

            JsonNode value = null;
            if(attr != null){
                if (field instanceof SimpleField) {
                    value = toJson((SimpleField)field, attr);
                }
                else if (field instanceof ObjectField) {
                    value = toJson((ObjectField)field, attr);
                }
                else if (field instanceof ArrayField){
                    value = toJson((ArrayField)field, attr, fieldCursor);
                    node.set(fieldName + "#", IntegerType.TYPE.toJson(factory, attr.getValues().length));
                }
                else if (field instanceof ReferenceField) {
                    value = toJson((ReferenceField)field, attr);
                }
                else{
                    throw new UnsupportedOperationException("Unknown Field type: " + field.getClass().getName());
                }
            }
            else if(fieldName.equalsIgnoreCase("objectType")){
                value = StringType.TYPE.toJson(factory, entityName);
            }

            node.set(fieldName, value);
        } while(fieldCursor.nextSibling());

        node.set("dn", StringType.TYPE.toJson(factory, entry.getDN()));
        return node;
    }

    private JsonNode toJson(SimpleField field, Attribute attr){
        Type type = field.getType();

        Object value = null;
        if(type instanceof StringType){
            value = attr.getValue();
        }
        else if(type instanceof IntegerType){
            value =  attr.getValueAsInteger();
        }
        else if(type instanceof BooleanType){
            value =  attr.getValueAsBoolean();
        }
        else if(type instanceof DateType){
            value =  attr.getValueAsDate();
        }
        //TODO BigDecimalType, BigIntegerType, BinaryType, DoubleType, UIDType

        if(value == null){
            throw new UnsupportedOperationException("Unable to convert Type: " + type.getClass().getName());
        }

        return field.getType().toJson(factory, value);
    }

    private JsonNode toJson(ObjectField field, Attribute attr){
        throw new UnsupportedOperationException("ObjectField type not currently supported.");
    }

    private JsonNode toJson(ArrayField field, Attribute attr, FieldCursor fieldCursor){
        if(!fieldCursor.firstChild()){
            return null;
        }

        String[] values = attr.getValues();
        FieldTreeNode node = fieldCursor.getCurrentNode();

        if(!(node instanceof SimpleArrayElement)){
            throw new UnsupportedOperationException("ArrayElement type is not supported: " + node.getClass().getName());
        }

        //TODO: Determine if LDAP would support an ObjectArrayElement

        ArrayNode valueNode = factory.arrayNode();

        for(String value : values){
            valueNode.add(node.getType().toJson(factory, value));
        }

        fieldCursor.parent();
        return valueNode;
    }

    private JsonNode toJson(ReferenceField field, Attribute attr){
        throw new UnsupportedOperationException("ReferenceField type not currently supported.");
    }

}

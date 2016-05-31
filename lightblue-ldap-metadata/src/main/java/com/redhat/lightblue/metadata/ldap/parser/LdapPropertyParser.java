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
package com.redhat.lightblue.metadata.ldap.parser;

import java.util.Map;
import java.util.Map.Entry;

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.ldap.model.LdapMetadata;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.parser.PropertyParser;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * {@link PropertyParser} implementation for LDAP.
 *
 * @author dcrissman
 */
public class LdapPropertyParser <T> extends PropertyParser<T> {

    private static final String FIELDS_TO_ATTRIBUTES = "fieldsToAttributes";
    private static final String FIELD = "field";
    private static final String ATTRIBUTE = "attribute";

    @Override
    public LdapMetadata parseProperty(MetadataParser<T> p, T container, String name) {
        if (!LdapConstant.BACKEND.equals(name)) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
        }

        LdapMetadata ldapMetadata = new LdapMetadata();

        T ldapMetadataNode = p.getMapProperty(container, name);
        
        T fieldToAttributesNode = p.getMapProperty(ldapMetadataNode, FIELDS_TO_ATTRIBUTES);
        if(fieldToAttributesNode != null){
            int n = p.getListSize(fieldToAttributesNode);
            for (int i = 0; i < n; i++) {
                T fieldToAttributeNode = p.getListElement(fieldToAttributesNode, i);
                ldapMetadata.addFieldToAttribute(
                        new Path(p.getRequiredStringProperty(fieldToAttributeNode, FIELD)),
                        p.getRequiredStringProperty(fieldToAttributeNode, ATTRIBUTE));
            }
        }

        return ldapMetadata;
    }

    @Override
    protected T convertProperty(MetadataParser<T> p, Object propertyValue) {
        if(!(propertyValue instanceof LdapMetadata)){
            throw new IllegalArgumentException("Source type " + propertyValue.getClass() + " is not supported.");
        }

        LdapMetadata ldapMetadata = (LdapMetadata) propertyValue;

        T propertyObject = p.newMap();

        Map<Path, String> fieldsToAttributes = ldapMetadata.getFieldsToAttributes();
        if(!fieldsToAttributes.isEmpty()){
            T fieldsToAttributesNode = p.newList();

            for(Entry<Path, String> entry : fieldsToAttributes.entrySet()){
                T fieldToAttributeNode = p.newMap();

                p.setMapProperty(fieldToAttributeNode, FIELD, p.asRepresentation(entry.getKey().toString()));
                p.setMapProperty(fieldToAttributeNode, ATTRIBUTE, p.asRepresentation(entry.getValue()));

                p.addListElement(fieldsToAttributesNode, fieldToAttributeNode);
            }

            p.setMapProperty(propertyObject, FIELDS_TO_ATTRIBUTES, fieldsToAttributesNode);
        }

        return propertyObject;
    }

}

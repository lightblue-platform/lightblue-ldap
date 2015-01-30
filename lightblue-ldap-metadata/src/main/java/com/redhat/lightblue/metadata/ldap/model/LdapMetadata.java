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
package com.redhat.lightblue.metadata.ldap.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.util.Path;

/**
 * Container for special ldap properties parsed from the metadata.json file.
 *
 * @author dcrissman
 *
 * @see com.redhat.lightblue.metadata.ldap.parser.LdapPropertyParser
 */
public class LdapMetadata implements LdapFieldNameTranslator{

    private final Map<Path, String> fieldsToAttributes = new HashMap<Path, String>();

    /**
     * Returns an immutable copy of the internal collection of {@link FieldAttributeMapping}s.
     * @return a collection of {@link FieldAttributeMapping}s.
     */
    public Map<Path, String> getFieldsToAttributes(){
        return new HashMap<Path, String>(fieldsToAttributes);
    }

    @Override
    public String translateFieldName(Path path){
        String attributeName = fieldsToAttributes.get(path);
        if(attributeName != null){
            return attributeName;
        }

        Path last = path.suffix(1);
        attributeName = fieldsToAttributes.get(last);
        if(attributeName != null){
            return attributeName;
        }

        return last.toString();
    }

    @Override
    public Path translateAttributeName(String attributeName){
        for(Entry<Path, String> f2a : fieldsToAttributes.entrySet()){
            if(f2a.getValue().equalsIgnoreCase(attributeName)){
                return f2a.getKey();
            }
        }
        return new Path(attributeName);
    }

    /**
     * Adds a {@link FieldAttributeMapping} to this {@link LdapMetadata}.
     * @param fieldAttributeMapping - {@link FieldAttributeMapping}
     */
    public void addFieldToAttribute(Path fieldPath, String attributeName){
        fieldsToAttributes.put(fieldPath, attributeName);
    }

}

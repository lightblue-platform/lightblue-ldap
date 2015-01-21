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

/**
 * Maps the metadata field name to the Ldap attribute name.
 *
 * @author dcrissman
 */
public class FieldToAttribute {

    private final String fieldName;
    private final String attributeName;

    public String getFieldName() {
        return fieldName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public FieldToAttribute(String fieldName, String attributeName){
        this.fieldName = fieldName;
        this.attributeName = attributeName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attributeName == null) ? 0 : attributeName.hashCode());
        result = prime * result
                + ((fieldName == null) ? 0 : fieldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FieldToAttribute other = (FieldToAttribute) obj;
        if (attributeName == null) {
            if (other.attributeName != null) {
                return false;
            }
        }
        else if (!attributeName.equals(other.attributeName)) {
            return false;
        }
        if (fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        }
        else if (!fieldName.equals(other.fieldName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FieldToAttribute [fieldName=" + fieldName + ", attributeName="
                + attributeName + "]";
    }

}

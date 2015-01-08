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
package com.redhat.lightblue.common.ldap;

/**
 * Utility methods to assist with interacting with the Lightblue framework.
 *
 * @author dcrissman
 */
public final class LightblueUtil {

    public static final String FIELD_OBJECT_TYPE = "objectType";
    public static final String FIELD_MOD_ARRAY_COUNT = "#";

    public static boolean isFieldObjectType(String fieldName){
        return FIELD_OBJECT_TYPE.equalsIgnoreCase(fieldName);
    }

    public static boolean isFieldAnArrayCount(String fieldName){
        if(fieldName == null){
            return false;
        }
        return fieldName.endsWith(FIELD_MOD_ARRAY_COUNT);
    }

    public static boolean isFieldPredefined(String fieldName){
        return isFieldObjectType(fieldName) || isFieldAnArrayCount(fieldName);
    }

    public static String createArrayCountFieldName(String arrayFieldName){
        return arrayFieldName + FIELD_MOD_ARRAY_COUNT;
    }

    private LightblueUtil(){}

}

package com.redhat.lightblue.common.ldap;

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

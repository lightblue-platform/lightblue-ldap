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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LightblueUtilTest {

    @Test
    public void testIsFieldObjectType_True(){
        assertTrue(LightblueUtil.isFieldObjectType(LightblueUtil.FIELD_OBJECT_TYPE));
    }

    @Test
    public void testIsFieldObjectType_False(){
        assertFalse(LightblueUtil.isFieldObjectType("NOT " + LightblueUtil.FIELD_OBJECT_TYPE));
    }

    @Test
    public void testIsFieldAnArrayCount_True(){
        assertTrue(LightblueUtil.isFieldAnArrayCount("somearray" + LightblueUtil.FIELD_MOD_ARRAY_COUNT));
    }

    @Test
    public void testIsFieldAnArrayCount_False(){
        assertFalse(LightblueUtil.isFieldAnArrayCount("somearray"));
    }

    @Test
    public void testIsFieldPredefined_ObjectType_True(){
        assertTrue(LightblueUtil.isFieldPredefined(LightblueUtil.FIELD_OBJECT_TYPE));
    }

    @Test
    public void testIsFieldPredefined_ObjectType_False(){
        assertFalse(LightblueUtil.isFieldPredefined("NOT " + LightblueUtil.FIELD_OBJECT_TYPE));
    }

    @Test
    public void testIsFieldPredefined_Array_True(){
        assertTrue(LightblueUtil.isFieldPredefined("somearray" + LightblueUtil.FIELD_MOD_ARRAY_COUNT));
    }

    @Test
    public void testIsFieldPredefined_Array_False(){
        assertFalse(LightblueUtil.isFieldPredefined("somearray"));
    }

    @Test
    public void testCreateArrayCountFieldName(){
        assertEquals("somearray#", LightblueUtil.createArrayCountFieldName("somearray"));
    }

}

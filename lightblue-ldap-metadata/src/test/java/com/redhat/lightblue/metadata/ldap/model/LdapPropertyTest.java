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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Set;

import org.junit.Test;

public class LdapPropertyTest {

    @Test
    public void testTranslateFieldName(){
        String fieldName = "fakeFieldName";
        String attributeName = "fakeAttributeName";

        LdapProperty property = new LdapProperty();
        property.addFieldToAttribute(new FieldAttributeMapping(fieldName, attributeName));
        property.addFieldToAttribute(new FieldAttributeMapping("anotherField", "anotherAttribute"));

        assertEquals(attributeName, property.translateFieldName(fieldName));
    }

    @Test
    public void testTranslateFieldName_ValueNotPresent(){
        String fieldName = "fakeFieldName";

        assertEquals(fieldName, new LdapProperty().translateFieldName(fieldName));
    }

    @Test
    public void testTranslateAttributeName(){
        String fieldName = "fakeFieldName";
        String attributeName = "fakeAttributeName";

        LdapProperty property = new LdapProperty();
        property.addFieldToAttribute(new FieldAttributeMapping(fieldName, attributeName));
        property.addFieldToAttribute(new FieldAttributeMapping("anotherField", "anotherAttribute"));

        assertEquals(fieldName, property.translateAttributeName(attributeName));
    }

    @Test
    public void testTranslateAttributeName_ValueNotPresent(){
        String attributeName = "fakeAttributeName";

        assertEquals(attributeName, new LdapProperty().translateAttributeName(attributeName));
    }

    @Test
    public void testGetFieldsToAttributes_AssertImmutable(){
        LdapProperty property = new LdapProperty();
        property.addFieldToAttribute(new FieldAttributeMapping("anotherField", "anotherAttribute"));

        Set<FieldAttributeMapping> fieldsToAttributes = property.getFieldsToAttributes();
        assertNotNull(fieldsToAttributes);
        assertNotSame(fieldsToAttributes, property.getFieldsToAttributes());
    }

}

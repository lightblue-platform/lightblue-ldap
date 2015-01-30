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

import static com.redhat.lightblue.ldap.test.Assert.assertMapEquivalent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Map;

import org.junit.Test;

import com.redhat.lightblue.util.Path;

public class LdapMetadataTest {

    @Test
    public void testTranslateFieldName(){
        Path fieldName = new Path("fakeFieldName");
        String attributeName = "fakeAttributeName";

        LdapMetadata metadata = new LdapMetadata();
        metadata.addFieldToAttribute(fieldName, attributeName);
        metadata.addFieldToAttribute(new Path("anotherField"), "anotherAttribute");

        assertEquals(attributeName, metadata.translateFieldName(new Path(fieldName)));
    }

    @Test
    public void testTranslateFieldName_WithPath(){
        Path fieldName = new Path("fakePath.fakeFieldName");
        String attributeName = "fakeAttributeName";

        LdapMetadata metadata = new LdapMetadata();
        metadata.addFieldToAttribute(fieldName, attributeName);

        assertEquals(attributeName, metadata.translateFieldName(new Path(fieldName)));
    }

    @Test
    public void testTranslateFieldName_WithPath_MatchesOnTail(){
        Path fieldName = new Path("fakeFieldName");
        String pathedFieldName = "fakePath." + fieldName;
        String attributeName = "fakeAttributeName";

        LdapMetadata metadata = new LdapMetadata();
        metadata.addFieldToAttribute(fieldName, attributeName);

        assertEquals(attributeName, metadata.translateFieldName(new Path(pathedFieldName)));
    }

    @Test
    public void testTranslateFieldName_ValueNotPresent(){
        String fieldName = "fakeFieldName";

        assertEquals(fieldName, new LdapMetadata().translateFieldName(new Path(fieldName)));
    }

    @Test
    public void testTranslateAttributeName(){
        Path fieldName = new Path("fakeFieldName");
        String attributeName = "fakeAttributeName";

        LdapMetadata metadata = new LdapMetadata();
        metadata.addFieldToAttribute(fieldName, attributeName);
        metadata.addFieldToAttribute(new Path("anotherField"), "anotherAttribute");

        assertEquals(fieldName, metadata.translateAttributeName(attributeName));
    }

    @Test
    public void testTranslateAttributeName_WithPath(){
        Path fieldName = new Path("somePath.fakeFieldName");
        String attributeName = "fakeAttributeName";

        LdapMetadata metadata = new LdapMetadata();
        metadata.addFieldToAttribute(fieldName, attributeName);
        metadata.addFieldToAttribute(new Path("anotherField"), "anotherAttribute");

        assertEquals(fieldName, metadata.translateAttributeName(attributeName));
    }

    @Test
    public void testTranslateAttributeName_ValueNotPresent(){
        String attributeName = "fakeAttributeName";

        assertEquals(attributeName, new LdapMetadata().translateAttributeName(attributeName).toString());
    }

    @Test
    public void testGetFieldsToAttributes_AssertImmutable(){
        LdapMetadata metadata = new LdapMetadata();
        metadata.addFieldToAttribute(new Path("anotherField"), "anotherAttribute");

        Map<Path, String> fieldsToAttributes = metadata.getFieldsToAttributes();
        assertNotNull(fieldsToAttributes);
        assertMapEquivalent(fieldsToAttributes, metadata.getFieldsToAttributes());
        assertNotSame(fieldsToAttributes, metadata.getFieldsToAttributes());
    }

}

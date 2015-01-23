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
package com.redhat.lightblue.metadata.ldap.parser;

import static com.redhat.lightblue.ldap.test.Assert.assertCollectionEquivalent;
import static com.redhat.lightblue.util.JsonUtils.json;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.metadata.ldap.model.FieldToAttribute;
import com.redhat.lightblue.metadata.ldap.model.LdapProperty;
import com.redhat.lightblue.test.MetadataUtil;

public class LdapPropertyParserTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testParse() throws IOException{
        com.redhat.lightblue.metadata.ldap.model.LdapProperty ldapProperty = new LdapPropertyParser<JsonNode>().parse(
                LdapConstant.BACKEND,
                MetadataUtil.createJSONMetadataParser(LdapConstant.BACKEND, null),
                loadJsonNode("./ldap-segment-metadata.json").get("ldap"));

        assertNotNull(ldapProperty);

        Set<FieldToAttribute> fieldsToAttributes = ldapProperty.getFieldsToAttributes();
        assertNotNull(fieldsToAttributes);
        assertEquals(2, fieldsToAttributes.size());

        assertCollectionEquivalent(
                Arrays.asList(new FieldToAttribute("firstName", "givenName"), new FieldToAttribute("lastName", "sn")),
                fieldsToAttributes);
    }

    @Test
    public void testParse_NoProperties() throws IOException{
        LdapProperty ldapProperty = new LdapPropertyParser<JsonNode>().parse(
                LdapConstant.BACKEND,
                MetadataUtil.createJSONMetadataParser(LdapConstant.BACKEND, null),
                json("{\"ldap\": {}}").get("ldap"));

        assertNotNull(ldapProperty);

        assertTrue(ldapProperty.getFieldsToAttributes().isEmpty());
    }

    @Test
    public void testParse_IncorrectBackend(){
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"errorCode\":\"metadata:IllFormedMetadata\",\"msg\":\"fakebackend\"}");

        new LdapPropertyParser<JsonNode>().parse("fakebackend", null, null);
    }

    @Test
    public void testConvert() throws IOException, JSONException{
        LdapProperty ldapProperty = new LdapProperty();
        ldapProperty.addFieldToAttribute(new FieldToAttribute("firstName", "givenName"));
        ldapProperty.addFieldToAttribute(new FieldToAttribute("lastName", "sn"));

        JsonNode node = json("{}");

        new LdapPropertyParser<JsonNode>().convert(
                MetadataUtil.createJSONMetadataParser(LdapConstant.BACKEND, null),
                node,
                ldapProperty);

        JSONAssert.assertEquals("{\"fieldsToAttributes\":[{\"field\":\"lastName\",\"attribute\":\"sn\"},{\"field\":\"firstName\",\"attribute\":\"givenName\"}]}",
                node.toString(), false);
    }

    @Test
    public void testConvert_NoMappings() throws IOException, JSONException{
        LdapProperty ldapProperty = new LdapProperty();

        JsonNode node = json("{}");

        new LdapPropertyParser<JsonNode>().convert(
                MetadataUtil.createJSONMetadataParser(LdapConstant.BACKEND, null),
                node,
                ldapProperty);

        JSONAssert.assertEquals("{}",
                node.toString(), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvert_invalidObject(){
        new LdapPropertyParser<JsonNode>().convert(null, null, new Object());
    }

}

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

import static com.redhat.lightblue.util.JsonUtils.json;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.test.MetadataUtil;
import com.redhat.lightblue.test.metadata.FakeDataStore;

public class LdapDataStoreParserTest {

    private final static String DATABASE = "test";
    private final static String BASE_DN = "dc=example,dc=com";
    private final static String UNIQUE_ATTRIBUTE = "uid";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    public LdapDataStoreParserTest() {
        System.setProperty("ldap.database", DATABASE);
        System.setProperty("ldap.basedn", BASE_DN);
        System.setProperty("ldap.uniqueattr", UNIQUE_ATTRIBUTE);
    }

    @Test
    public void testGetDefaultName(){
        assertEquals(LdapConstant.BACKEND, new LdapDataStoreParser<Object>().getDefaultName());
    }

    @Test
    public void testParse() throws IOException{
        LdapDataStore store = new LdapDataStoreParser<JsonNode>().parse(
                LdapConstant.BACKEND,
                MetadataUtil.createJSONMetadataParser(LdapConstant.BACKEND, null),
                loadJsonNode("./entityinfo-segment-metadata.json").get("entityInfo").get("datastore"));

        assertNotNull(store);
        assertEquals(DATABASE, store.getDatabase());
        assertEquals(BASE_DN, store.getBaseDN());
        assertEquals(UNIQUE_ATTRIBUTE, store.getUniqueAttribute());
    }

    @Test
    public void testParse_IncorrectBackend(){
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"errorCode\":\"metadata:IllFormedMetadata\",\"msg\":\"fakebackend\"}");

        new LdapDataStoreParser<JsonNode>().parse("fakebackend", null, null);
    }

    @Test
    public void testConvert() throws IOException, JSONException{
        LdapDataStore store = new LdapDataStore();
        store.setDatabase(DATABASE);
        store.setBaseDN(BASE_DN);
        store.setUniqueAttribute(UNIQUE_ATTRIBUTE);

        JsonNode node = json("{}");

        new LdapDataStoreParser<JsonNode>().convert(
                MetadataUtil.createJSONMetadataParser(LdapConstant.BACKEND, null),
                node,
                store);

        JSONAssert.assertEquals("{\"database\":\"" + DATABASE + "\",\"basedn\":\"" + BASE_DN + "\",\"uniqueattr\":\"" + UNIQUE_ATTRIBUTE + "\"}",
                node.toString(), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvert_wrongStoreType(){
        new LdapDataStoreParser<JsonNode>().convert(null, null, new FakeDataStore("fake"));
    }

}

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
package com.redhat.lightblue.crud.ldap;

import static com.redhat.lightblue.util.JsonUtils.json;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadResource;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.test.MetadataUtil;
import com.redhat.lightblue.util.JsonDoc;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.util.StaticUtils;

@RunWith(value = Parameterized.class)
public class EntryBuilderTest {

    private final static Date now = new Date();

    @Parameters(name= "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"{\"type\": \"string\"}", quote("teststring"), "teststring"},
                {"{\"type\": \"integer\"}", "4", null},
                {"{\"type\": \"boolean\"}", "true", null},
                {"{\"type\": \"date\"}", quote(DateType.getDateFormat().format(now)), StaticUtils.encodeGeneralizedTime(now)},
                {"{\"type\": \"binary\"}", quote(DatatypeConverter.printBase64Binary("test binary data".getBytes())), "test binary data"},
                {"{\"type\": \"array\", \"items\": {\"type\": \"string\"}}", "[\"hello\",\"world\"]", new String[]{"hello", "world"}},
                {"{\"type\": \"array\", \"items\": {\"type\": \"binary\"}}",
                    "[" + quote(DatatypeConverter.printBase64Binary("hello".getBytes())) + ","
                            + quote(DatatypeConverter.printBase64Binary("world".getBytes())) + "]",
                            new String[]{"hello", "world"}
                },
        });
    }

    private static String quote(String text){
        return '"' + text + '"';
    }

    private final String fieldName = "testfield";
    private final String metadataType;
    private final String crudValue;
    private final Object expectedValue;

    public EntryBuilderTest(String metadataType, String crudValue, Object expectedValue){
        this.metadataType = metadataType;
        this.crudValue = crudValue;
        this.expectedValue = (expectedValue == null) ? crudValue : expectedValue;
    }

    @Test
    public void test() throws Exception{
        String metadata = loadResource("./metadata/entryBuilderTest-metadata-template.json")
                .replaceFirst("#fieldname", fieldName)
                .replaceFirst("#type", metadataType);
        String crud = loadResource("./crud/insert/entryBuilderTest-insert-template.json")
                .replaceFirst("#fieldname", fieldName)
                .replaceFirst("#value", crudValue);

        EntityMetadata md = MetadataUtil.createEntityMetadata(LdapConstant.BACKEND, json(metadata), null, null);
        EntryBuilder builder = new EntryBuilder(md);

        Entry entry = builder.build("uid=someuid,dc=example,dc=com",
                new JsonDoc(json(crud).get("data")));

        if(expectedValue.getClass().isArray()){
            assertArrayEquals((String[]) expectedValue, entry.getAttributeValues("testfield"));
        }
        else{
            assertEquals(expectedValue, entry.getAttributeValue("testfield"));
        }
    }

}

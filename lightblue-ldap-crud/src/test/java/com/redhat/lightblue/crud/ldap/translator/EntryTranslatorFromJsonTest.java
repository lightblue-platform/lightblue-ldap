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
package com.redhat.lightblue.crud.ldap.translator;

import static com.redhat.lightblue.util.JsonUtils.json;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadResource;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapErrorCode;
import com.redhat.lightblue.common.ldap.LightblueUtil;
import com.redhat.lightblue.crud.ldap.model.TrivialLdapFieldNameTranslator;
import com.redhat.lightblue.crud.ldap.translator.EntryTranslatorFromJsonTest.ParameterizedTests;
import com.redhat.lightblue.crud.ldap.translator.EntryTranslatorFromJsonTest.SpecializedTests;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.test.MetadataUtil;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.util.StaticUtils;

@RunWith(Suite.class)
@SuiteClasses({ParameterizedTests.class, SpecializedTests.class})
public class EntryTranslatorFromJsonTest {

    protected static Entry buildEntry(String fieldName, String metadataType, String crudValue) throws Exception{
        String metadata = loadResource("./metadata/entryBuilderTest-metadata-template.json")
                .replaceFirst("#fieldname", fieldName)
                .replaceFirst("#type", metadataType);
        String crud = loadResource("./crud/insert/entryBuilderTest-insert-template.json")
                .replaceFirst("#fieldname", fieldName)
                .replaceFirst("#value", crudValue);

        EntityMetadata md = MetadataUtil.createEntityMetadata(LdapConstant.BACKEND, json(metadata), null, null);
        EntryTranslatorFromJson builder = new EntryTranslatorFromJson(md, new TrivialLdapFieldNameTranslator());

        return builder.translate(new JsonDoc(json(crud).get("data")),
                "uid=someuid,dc=example,dc=com");
    }

    protected static String quote(String text){
        return '"' + text + '"';
    }

    public static class SpecializedTests {

        @Rule
        public ExpectedException expectedEx = ExpectedException.none();

        @After
        public void after(){
            Error.reset();
        }

        @Test
        public void testFieldIsObjectType() throws Exception{
            Entry entry = buildEntry(LightblueUtil.FIELD_OBJECT_TYPE, "{\"type\": \"string\"}", quote("someEntity"));

            assertNotNull(entry);
            assertNull(entry.getAttribute(LightblueUtil.FIELD_OBJECT_TYPE));
        }

        /**
         * This test is kind of hacky as it requires json injection in order to make it work because
         * it requires two fields.
         */
        @Test
        public void testFieldIsSimpleArrayField() throws Exception{
            String arrayFieldName = "someSimpleArray";
            String arrayCountFieldName = LightblueUtil.createArrayCountFieldName(arrayFieldName);
            Entry entry = buildEntry(
                    arrayCountFieldName,
                    "{\"type\": \"integer\"}, " + quote(arrayFieldName) + ": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}",
                    "2");

            assertNotNull(entry);
            assertNull(entry.getAttribute(arrayFieldName));
        }

        @Test
        public void testFieldIsArrayFieldWithoutMatchArray() throws Exception{
            String arrayCountFieldName = LightblueUtil.createArrayCountFieldName("someArray");
            Entry entry = buildEntry(arrayCountFieldName, "{\"type\": \"integer\"}", "2");

            assertNotNull(entry);
            assertNotNull(entry.getAttribute(arrayCountFieldName));
        }

        /**
         * This test is kind of hacky as it requires json injection in order to make it work because
         * it requires two fields.
         * ObjectFields are not currently supported in LDAP, an exception should be thrown indicating as such.
         */
        @Test
        public void testObjectArrayField_ThrowsException() throws Exception{
            String arrayFieldName = "someObjectArray";
            String arrayCountFieldName = LightblueUtil.createArrayCountFieldName(arrayFieldName);

            expectedEx.expect(com.redhat.lightblue.util.Error.class);
            expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"dn=uid=someuid,dc=example,dc=com/translating from json/" + arrayFieldName + "\",\"errorCode\":\"" + LdapErrorCode.ERR_UNSUPPORTED_FEATURE_OBJECT_ARRAY + "\",\"msg\":\"" + arrayFieldName + "\"}");

            buildEntry(
                    arrayCountFieldName,
                    "{\"type\": \"integer\"}, " + quote(arrayFieldName) + ": {\"type\": \"array\", \"items\": {\"type\": \"object\",\"fields\": {\"someField\": {\"type\": \"string\"}}}}",
                    "1," + quote(arrayFieldName) + ":[{\"someField\":\"hello\"}]");
        }

        /**
         * DN fields should never be defined as they are technically not attributes.
         */
        @Test
        public void testFieldIsDN() throws Exception{
            expectedEx.expect(com.redhat.lightblue.util.Error.class);
            expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"dn=uid=someuid,dc=example,dc=com/translating from json/dn\",\"errorCode\":\"metadata:InvalidFieldReference\",\"msg\":\"dn\"}");
            buildEntry(LdapConstant.ATTRIBUTE_DN, "{\"type\": \"string\"}", quote("uid=someuid,dc=example,dc=com"));
        }

    }

    @RunWith(value = Parameterized.class)
    public static class ParameterizedTests {

        private final static Date now = new Date();

        @Parameters(name= "{index}: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"{\"type\": \"string\"}", quote("teststring"), "teststring"},
                    {"{\"type\": \"integer\"}", "4", null},
                    {"{\"type\": \"boolean\"}", "true", null},
                    {"{\"type\": \"bigdecimal\"}", String.valueOf(Double.MAX_VALUE), "1.7976931348623157E+308"},
                    {"{\"type\": \"biginteger\"}", BigInteger.ZERO.toString(), BigInteger.ZERO.toString()},
                    {"{\"type\": \"double\"}", String.valueOf(Double.MAX_VALUE), String.valueOf(Double.MAX_VALUE)},
                    {"{\"type\": \"uid\"}", quote("fake-uid"), "fake-uid"},
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

        @After
        public void after(){
            Error.reset();
        }

        private final String fieldName = "testfield";
        private final String metadataType;
        private final String crudValue;
        private final Object expectedValue;

        public ParameterizedTests(String metadataType, String crudValue, Object expectedValue){
            this.metadataType = metadataType;
            this.crudValue = crudValue;
            this.expectedValue = (expectedValue == null) ? crudValue : expectedValue;
        }

        @Test
        public void test() throws Exception{
            Entry entry = buildEntry(fieldName, metadataType, crudValue);

            assertNotNull(entry);

            if(expectedValue.getClass().isArray()){
                assertArrayEquals((String[]) expectedValue, entry.getAttributeValues("testfield"));
            }
            else{
                assertEquals(expectedValue, entry.getAttributeValue("testfield"));
            }
        }

    }

}

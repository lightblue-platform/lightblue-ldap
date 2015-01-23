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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.ldap.model.LazyLdapFieldNameTranslator;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.types.BigDecimalType;
import com.redhat.lightblue.metadata.types.BigIntegerType;
import com.redhat.lightblue.metadata.types.BinaryType;
import com.redhat.lightblue.metadata.types.BooleanType;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.metadata.types.DoubleType;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.metadata.types.UIDType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.SearchResultEntry;

public class ResultTranslatorTest {

    private final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    @Test
    public void testTranslate_SimpleField_String() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("uid", "john.doe")
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", StringType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"uid\":\"john.doe\",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_Integer() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", "4")
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", IntegerType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"key\":4,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_Boolean() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", "true")
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", BooleanType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"key\":true,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_BigDecimalType() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", String.valueOf(Double.MAX_VALUE))
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", BigDecimalType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"key\":" + String.valueOf(Double.MAX_VALUE) + ",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_BigIntegerType() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", BigInteger.ZERO.toString())
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", BigIntegerType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"key\":" + BigInteger.ZERO + ",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_DoubleType() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", String.valueOf(Double.MAX_VALUE))
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", DoubleType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"key\":" + String.valueOf(Double.MAX_VALUE) + ",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_UIDType() throws JSONException{
        String uuid = UUID.randomUUID().toString();

        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", uuid)
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", UIDType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"key\":" + uuid + ",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test(expected = NullPointerException.class)
    public void testTranslate_SimpleField_NullValue() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("uid")
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", StringType.TYPE)
                );

        new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);
    }

    @Test
    public void testTranslate_SimpleField_BinaryType() throws Exception{
        byte[] bite = new byte[]{1, 2, 3, 'a', 'b', 'c'};

        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", bite)
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", BinaryType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);
        JsonDoc jsonDocument = document.getOutputDocument();

        JsonNode keyNode = jsonDocument.get(new Path("key"));
        assertEquals(bite, keyNode.binaryValue());

        JSONAssert.assertEquals(
                "{\"key\":\"" + keyNode.asText() + "\",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                jsonDocument.toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_Date() throws JSONException{
        //Note in and out data are formatted differently
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("key", "20150109201731.570Z")
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", DateType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"key\":\"20150109T20:17:31.570+0000\",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_ObjectType() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{});

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("objectType", StringType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"objectType\":\"fakeMetadata\",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_AttributeDoesNotExist() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{});

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("absentAttribute", StringType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"absentAttribute\":null,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    /**
     * Both the array itself and a count field should be returned even though the count was not asked for.
     * @see #testTranslate_SimpleArrayElement_CountAskedFor()
     */
    @Test
    public void testTranslate_SimpleArrayElement_CountNotAskedFor() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("objectClass", Arrays.asList("top", "person", "organizationalPerson", "inetOrgPerson"))
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("objectClass", new SimpleArrayElement(StringType.TYPE))
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"objectClass\":[\"top\",\"person\",\"organizationalPerson\",\"inetOrgPerson\"],\"objectClass#\":4,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    /**
     * Essentially the same, but the count is specifically asked for. The only real different is that the count field
     * should be skipped over during processing.
     * @see #testTranslate_SimpleArrayElement_CountNotAskedFor()
     */
    @Test
    public void testTranslate_SimpleArrayElement_CountAskedFor() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("objectClass", Arrays.asList("top", "person", "organizationalPerson", "inetOrgPerson"))
        });

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("objectClass", new SimpleArrayElement(StringType.TYPE)),
                new SimpleField("objectClass#", IntegerType.TYPE)
                );

        DocCtx document = new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);

        assertNotNull(document);

        JSONAssert.assertEquals(
                "{\"objectClass\":[\"top\",\"person\",\"organizationalPerson\",\"inetOrgPerson\"],\"objectClass#\":4,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                document.getOutputDocument().toString(),
                true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_UnknownArrayElement() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                new Attribute("fakeArray", Arrays.asList("top", "person", "organizationalPerson", "inetOrgPerson"))
        });

        @SuppressWarnings("serial")
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("fakeArray", new ArrayElement(){

                    @Override
                    public boolean hasChildren() {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public Iterator<? extends FieldTreeNode> getChildren() {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public FieldTreeNode resolve(Path p, int level) {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }
                }));

        new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_ObjectField() throws JSONException{
        SearchResultEntry result =  new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{new Attribute("fake")});

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ObjectField("fake")
                );

        new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_ReferenceField() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{new Attribute("fake")});

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ReferenceField("fake")
                );

        new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_UnsupportedField() throws JSONException{
        SearchResultEntry result = new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{new Attribute("fake")});

        @SuppressWarnings("serial")
        EntityMetadata md = fakeEntityMetadata("fakeMetadata", new Field("fake"){

            @Override
            public boolean hasChildren() {
                throw new UnsupportedOperationException("Method should never be called.");
            }

            @Override
            public Iterator<? extends FieldTreeNode> getChildren() {
                throw new UnsupportedOperationException("Method should never be called.");
            }

            @Override
            public FieldTreeNode resolve(Path p, int level) {
                throw new UnsupportedOperationException("Method should never be called.");
            }

        });

        new ResultTranslator(factory, md, new LazyLdapFieldNameTranslator()).translate(result);
    }

    protected EntityMetadata fakeEntityMetadata(String name, Field... fields){
        EntityMetadata md = new EntityMetadata(name);
        for(Field f : fields){
            md.getFields().addNew(f);
        }
        return md;
    }

}

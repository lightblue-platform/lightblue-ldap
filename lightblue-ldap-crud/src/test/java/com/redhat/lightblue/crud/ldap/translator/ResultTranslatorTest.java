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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.BooleanType;
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultReference;

public class ResultTranslatorTest {

    private final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    @Test
    public void testTranslate_SimpleField_String() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("uid", "john.doe")
                }));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", StringType.TYPE)
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"uid\":\"john.doe\",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_Integer() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("key", "4")
                }));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", IntegerType.TYPE)
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"key\":4,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_Boolean() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("key", "true")
                }));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", BooleanType.TYPE)
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"key\":true,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_SimpleField_Date() throws JSONException{
        //Note in and out data are formatted differently
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("key", "20150109201731.570Z")
                }));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("key", DateType.TYPE)
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"key\":\"20150109T20:17:31.570+0000\",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_SimpleField_Unknown() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("uid", "john.doe")
                }));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", new Type(){

                    @Override
                    public String getName() {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public boolean supportsEq() {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public boolean supportsOrdering() {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public JsonNode toJson(JsonNodeFactory factory, Object value) {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public Object fromJson(JsonNode value) {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public int compare(Object v1, Object v2) {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }

                    @Override
                    public Object cast(Object v) {
                        throw new UnsupportedOperationException("Method should never be called.");
                    }
                }));

        new ResultTranslator(factory).translate(result, md);
    }

    @Test
    public void testTranslate_ObjectType() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{}));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("objectType", StringType.TYPE)
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"objectType\":\"fakeMetadata\",\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    @Test
    public void testTranslate_AttributeDoesNotExist() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{}));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("absentAttribute", StringType.TYPE)
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"absentAttribute\":null,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    /**
     * Both the array itself and a count field should be returned even though the count was not asked for.
     * @see #testTranslate_SimpleArrayElement_CountAskedFor()
     */
    @Test
    public void testTranslate_SimpleArrayElement_CountNotAskedFor() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("objectClass", Arrays.asList("top", "person", "organizationalPerson", "inetOrgPerson"))
                }));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("objectClass", new SimpleArrayElement(StringType.TYPE))
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"objectClass\":[\"top\",\"person\",\"organizationalPerson\",\"inetOrgPerson\"],\"objectClass#\":4,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    /**
     * Essentially the same, but the count is specifically asked for. The only real different is that the count field
     * should be skipped over during processing.
     * @see #testTranslate_SimpleArrayElement_CountNotAskedFor()
     */
    @Test
    public void testTranslate_SimpleArrayElement_CountAskedFor() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("objectClass", Arrays.asList("top", "person", "organizationalPerson", "inetOrgPerson"))
                }));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("objectClass", new SimpleArrayElement(StringType.TYPE)),
                new SimpleField("objectClass#", IntegerType.TYPE)
                );

        List<DocCtx> documents = new ResultTranslator(factory).translate(result, md);

        assertNotNull(documents);
        assertEquals(1, documents.size());

        JSONAssert.assertEquals(
                "{\"objectClass\":[\"top\",\"person\",\"organizationalPerson\",\"inetOrgPerson\"],\"objectClass#\":4,\"dn\":\"uid=john.doe,dc=example,dc=com\"}",
                documents.get(0).getOutputDocument().toString(),
                true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_UnknownArrayElement() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{
                        new Attribute("fakeArray", Arrays.asList("top", "person", "organizationalPerson", "inetOrgPerson"))
                }));

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

        new ResultTranslator(factory).translate(result, md);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_ObjectField() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{new Attribute("fake")}));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ObjectField("fake")
                );

        new ResultTranslator(factory).translate(result, md);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_ReferenceField() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{new Attribute("fake")}));

        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ReferenceField("fake")
                );

        new ResultTranslator(factory).translate(result, md);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_UnsupportedField() throws JSONException{
        SearchResult result = fakeSearchResult(
                new SearchResultEntry(-1, "uid=john.doe,dc=example,dc=com", new Attribute[]{new Attribute("fake")}));

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

        new ResultTranslator(factory).translate(result, md);
    }

    protected SearchResult fakeSearchResult(SearchResultEntry... entries){
        return new SearchResult(-1, null, "", "", new String[0], Arrays.asList(entries), new ArrayList<SearchResultReference>(), entries.length, 0, null);
    }

    protected EntityMetadata fakeEntityMetadata(String name, Field... fields){
        EntityMetadata md = new EntityMetadata(name);
        for(Field f : fields){
            md.getFields().addNew(f);
        }
        return md;
    }

}

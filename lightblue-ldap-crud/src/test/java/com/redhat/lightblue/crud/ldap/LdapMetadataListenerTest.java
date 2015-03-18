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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

public class LdapMetadataListenerTest {

    private void setupEntityMetadata(EntityMetadata md){
        md.getFields().addNew(new SimpleField("uniqAttr", StringType.TYPE));
        md.setDataStore(new LdapDataStore("db", "baseDN", "uniqAttr"));
    }

    /**
     * No fields are defined, so ensure the fields are automatically created.
     */
    @Test
    public void testBeforeCreateNewSchema(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);

        LdapMetadataListener listener = new LdapMetadataListener();
        listener.beforeCreateNewSchema(null, md);

        assertTrue(md.getFields().has(LdapConstant.ATTRIBUTE_DN));
        assertTrue(md.getFields().has(LdapConstant.ATTRIBUTE_OBJECT_CLASS));
        assertTrue(md.getFields().has(LdapConstant.ATTRIBUTE_OBJECT_CLASS + "#"));
    }

    /**
     * The fields that were defined should remain.
     */
    @Test
    public void testBeforeCreateNewSchema_alreadyHasDefinedDnAndObjectClass(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        SimpleField dnField = new SimpleField(LdapConstant.ATTRIBUTE_DN, StringType.TYPE);
        fields.addNew(dnField);
        ArrayField objectClassField = new ArrayField(LdapConstant.ATTRIBUTE_OBJECT_CLASS, new SimpleArrayElement(StringType.TYPE));
        fields.addNew(objectClassField);
        SimpleField objectClassCountField = new SimpleField(LdapConstant.ATTRIBUTE_OBJECT_CLASS + "#", IntegerType.TYPE);
        fields.addNew(objectClassCountField);

        LdapMetadataListener listener = new LdapMetadataListener();
        listener.beforeCreateNewSchema(null, md);

        assertTrue(md.getFields().has(LdapConstant.ATTRIBUTE_DN));
        assertTrue(md.getFields().has(LdapConstant.ATTRIBUTE_OBJECT_CLASS));
        assertTrue(md.getFields().has(LdapConstant.ATTRIBUTE_OBJECT_CLASS + "#"));

        assertEquals(dnField, md.getFields().getField(LdapConstant.ATTRIBUTE_DN));
        assertEquals(objectClassField, md.getFields().getField(LdapConstant.ATTRIBUTE_OBJECT_CLASS));
        assertEquals(objectClassCountField, md.getFields().getField(LdapConstant.ATTRIBUTE_OBJECT_CLASS + "#"));
    }

    /**
     * This ensures that if the DN is defined within an object in the metadata that the method
     * continues to work as expected.
     */
    @Test
    public void testBeforeCreateNewSchema_DnInObject(){
        String fieldName = "someobject." + LdapConstant.ATTRIBUTE_DN;

        EntityMetadata md = LdapCrudUtilTest.createTestEntityMetadataWithLdapProperty(
                new FakeLdapFieldNameTranslator(fieldName, LdapConstant.ATTRIBUTE_DN));
        setupEntityMetadata(md);
        md.getFields().addNew(new ObjectField("someobject"));

        LdapMetadataListener listener = new LdapMetadataListener();
        listener.beforeCreateNewSchema(null, md);

        assertNotNull(md.resolve(new Path(fieldName)));
    }

    /**
     * This ensures that if the DN is defined within an object in the metadata that the method
     * continues to work as expected.
     */
    @Test
    public void testBeforeCreateNewSchema_ObjectClassAndCountInObject(){
        String fieldName = "someobject." + LdapConstant.ATTRIBUTE_OBJECT_CLASS;

        EntityMetadata md = LdapCrudUtilTest.createTestEntityMetadataWithLdapProperty(
                new FakeLdapFieldNameTranslator(fieldName, LdapConstant.ATTRIBUTE_OBJECT_CLASS));
        setupEntityMetadata(md);
        md.getFields().addNew(new ObjectField("someobject"));

        LdapMetadataListener listener = new LdapMetadataListener();
        listener.beforeCreateNewSchema(null, md);

        assertNotNull(md.resolve(new Path(fieldName)));
        assertNotNull(md.resolve(new Path(fieldName + "# ")));
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedDn_butAsWrongType(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        fields.addNew(new SimpleField(LdapConstant.ATTRIBUTE_DN, IntegerType.TYPE));

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedDn_butAsWrongField(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        fields.addNew(new ArrayField(LdapConstant.ATTRIBUTE_DN));

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedObjectClass_butAsWrongType(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        fields.addNew(new SimpleField(LdapConstant.ATTRIBUTE_OBJECT_CLASS, StringType.TYPE));

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedObjectClass_butAsWrongField(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        fields.addNew(new SimpleField(LdapConstant.ATTRIBUTE_OBJECT_CLASS));

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedObjectClass_butWithWrongElementType(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        fields.addNew(new ArrayField(LdapConstant.ATTRIBUTE_OBJECT_CLASS, new SimpleArrayElement(IntegerType.TYPE)));

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedObjectClass_butWithWrongElementField(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        ArrayField field = new ArrayField(LdapConstant.ATTRIBUTE_OBJECT_CLASS, new ObjectArrayElement());
        fields.addNew(field);

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedObjectClassCount_butAsWrongType(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        fields.addNew(new SimpleField(LdapConstant.ATTRIBUTE_OBJECT_CLASS + "#", StringType.TYPE));

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_alreadyHasDefinedObjectClassCount_butAsWrongField(){
        EntityMetadata md = new EntityMetadata("fake");
        setupEntityMetadata(md);
        Fields fields = md.getEntitySchema().getFields();

        fields.addNew(new ArrayField(LdapConstant.ATTRIBUTE_OBJECT_CLASS + "#"));

        new LdapMetadataListener().beforeCreateNewSchema(null, md);
    }

    @Test(expected = Error.class)
    public void testBeforeCreateNewSchema_uniqAttrNotDefinedInSchema(){
        EntityMetadata md = new EntityMetadata("fake");
        md.setDataStore(new LdapDataStore("db", "baseDN", "uniqAttr"));

        LdapMetadataListener listener = new LdapMetadataListener();
        listener.beforeCreateNewSchema(null, md);
    }

    /** Fake implementation of {@link LdapFieldNameTranslator} for testing purposes. */
    private static final class FakeLdapFieldNameTranslator implements LdapFieldNameTranslator{

        private final String fieldName;
        private final String attributeName;

        public FakeLdapFieldNameTranslator(String fieldName, String attributeName){
            this.fieldName = fieldName;
            this.attributeName = attributeName;
        }

        @Override
        public String translateFieldName(Path path) {
            if(fieldName.equals(path.toString())){
                return attributeName;
            }
            return path.toString();
        }

        @Override
        public Path translateAttributeName(String attributeName) {
            if(this.attributeName.equals(attributeName)) {
                return new Path(fieldName);
            }
            return new Path(attributeName);
        }

    }

}

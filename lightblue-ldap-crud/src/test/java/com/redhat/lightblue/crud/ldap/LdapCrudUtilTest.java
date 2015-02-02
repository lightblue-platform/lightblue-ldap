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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.crud.ldap.model.TrivialLdapFieldNameTranslator;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.test.metadata.FakeDataStore;
import com.redhat.lightblue.util.Path;

public class LdapCrudUtilTest {

    public static EntityMetadata createTestEntityMetadataWithLdapProperty(Object property){
        EntityMetadata md = new EntityMetadata("fake");
        md.getEntityInfo().getProperties().put(LdapConstant.BACKEND, property);

        return md;
    }

    /**
     * Should return an instance of {@link TrivialLdapFieldNameTranslator}
     * because the "ldap" property does not exist, ergo no translations.
     */
    @Test
    public void testGetLdapFieldNameTranslator_NoProperties(){
        EntityMetadata md = new EntityMetadata("fake");

        LdapFieldNameTranslator translator = LdapCrudUtil.getLdapFieldNameTranslator(md);
        assertNotNull(translator);
        assertTrue(translator instanceof TrivialLdapFieldNameTranslator);
    }

    /**
     * An "ldap" property does exist, but the specific implementation is not visible here.
     */
    @Test
    public void testGetLdapFieldNameTranslator(){
        EntityMetadata md = createTestEntityMetadataWithLdapProperty(new FakeLdapFieldNameTranslator());

        LdapFieldNameTranslator translator = LdapCrudUtil.getLdapFieldNameTranslator(md);
        assertNotNull(translator);
        assertTrue(LdapCrudUtil.getLdapFieldNameTranslator(md) instanceof FakeLdapFieldNameTranslator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLdapFieldNameTranslator_NotInstanceofLdapFieldNameTranslator(){
        EntityMetadata md = createTestEntityMetadataWithLdapProperty(new Object());

        LdapCrudUtil.getLdapFieldNameTranslator(md);
    }

    @Test
    public void testGetLdapDataStore(){
        EntityMetadata md = new EntityMetadata("fake");
        md.setDataStore(new LdapDataStore());

        LdapDataStore store = LdapCrudUtil.getLdapDataStore(md);

        assertNotNull(store);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLdapDataStore_WrongDataStore(){
        EntityMetadata md = new EntityMetadata("fake");
        md.setDataStore(new FakeDataStore("fakeDS"));

        LdapCrudUtil.getLdapDataStore(md);
    }

    /** Fake implementation of {@link LdapFieldNameTranslator} for testing purposes. */
    private static final class FakeLdapFieldNameTranslator implements LdapFieldNameTranslator{

        @Override
        public String translateFieldName(Path path) {
            throw new UnsupportedOperationException("method does nothing.");
        }

        @Override
        public Path translateAttributeName(String attributeName) {
            throw new UnsupportedOperationException("method does nothing.");
        }

    }

}

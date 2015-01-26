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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ldap.model.TrivialLdapFieldNameTranslator;
import com.redhat.lightblue.query.CompositeSortKey;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.util.Path;

public class SortTranslatorTest {

    @Test
    public void testSortKey_Desc(){
        String fieldName = "fakeField";

        Sort sort = new SortKey(new Path(fieldName), true);

        com.unboundid.ldap.sdk.controls.SortKey[] translatedSorts = new SortTranslator(new TrivialLdapFieldNameTranslator()).translate(sort);

        assertNotNull(translatedSorts);
        assertEquals(1, translatedSorts.length);
        com.unboundid.ldap.sdk.controls.SortKey translatedSort = translatedSorts[0];
        assertNotNull(translatedSort);

        assertEquals(fieldName, translatedSort.getAttributeName());
        assertTrue(translatedSort.reverseOrder());
    }

    @Test
    public void testSortKey_Asc(){
        String fieldName = "fakeField";

        Sort sort = new SortKey(new Path(fieldName), false);

        com.unboundid.ldap.sdk.controls.SortKey[] translatedSorts = new SortTranslator(new TrivialLdapFieldNameTranslator()).translate(sort);

        assertNotNull(translatedSorts);
        assertEquals(1, translatedSorts.length);
        com.unboundid.ldap.sdk.controls.SortKey translatedSort = translatedSorts[0];
        assertNotNull(translatedSort);

        assertEquals(fieldName, translatedSort.getAttributeName());
        assertFalse(translatedSort.reverseOrder());
    }

    @Test
    public void testCompositeSortKey(){
        String fieldName1 = "fakeField1";
        String fieldName2 = "fakeField2";

        Sort sort = new CompositeSortKey(
                Arrays.asList(
                        new SortKey(new Path(fieldName1), true),
                        new SortKey(new Path(fieldName2), false))
                );

        com.unboundid.ldap.sdk.controls.SortKey[] translatedSorts = new SortTranslator(new TrivialLdapFieldNameTranslator()).translate(sort);

        assertNotNull(translatedSorts);
        assertEquals(2, translatedSorts.length);

        com.unboundid.ldap.sdk.controls.SortKey translatedSort1 = translatedSorts[0];
        assertNotNull(translatedSort1);
        assertEquals(fieldName1, translatedSort1.getAttributeName());
        assertTrue(translatedSort1.reverseOrder());

        com.unboundid.ldap.sdk.controls.SortKey translatedSort2 = translatedSorts[1];
        assertNotNull(translatedSort2);
        assertEquals(fieldName2, translatedSort2.getAttributeName());
        assertFalse(translatedSort2.reverseOrder());
    }

    @SuppressWarnings("serial")
    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedSortType(){
        new SortTranslator(new TrivialLdapFieldNameTranslator()).translate(new Sort(){

            @Override
            public JsonNode toJson() {
                throw new UnsupportedOperationException("Method should never be called.");
            }

        });
    }

}

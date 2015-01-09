package com.redhat.lightblue.crud.ldap.translator.unboundid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.redhat.lightblue.query.CompositeSortKey;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.util.Path;

public class SortTranslatorTest {

    @Test
    public void testSortKey_Desc(){
        String fieldName = "fakeField";

        Sort sort = new SortKey(new Path(fieldName), true);

        com.unboundid.ldap.sdk.controls.SortKey[] translatedSorts = new SortTranslator().translate(sort);

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

        com.unboundid.ldap.sdk.controls.SortKey[] translatedSorts = new SortTranslator().translate(sort);

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

        com.unboundid.ldap.sdk.controls.SortKey[] translatedSorts = new SortTranslator().translate(sort);

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

}

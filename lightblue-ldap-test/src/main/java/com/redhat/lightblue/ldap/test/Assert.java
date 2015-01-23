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
package com.redhat.lightblue.ldap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Custom Asserts
 *
 * @author dcrissman
 */
public final class Assert {

    private Assert(){}

    /**
     * <p>Asserts that the keys and values of the two maps are equal.</p>
     * @param expectedMap
     * @param actualMap
     */
    public static <K,V> void assertMapEquivalent(Map<K,V> expectedMap, Map<K,V> actualMap){
        assertMapEquivalent(expectedMap, actualMap, new DefaultEquivalencyEvaluator());
    }

    /**
     * <p>Asserts that the keys and values of the two maps are equal.</p>
     * @param expectedMap
     * @param actualMap
     * @param evaluator
     */
    public static <K,V> void assertMapEquivalent(Map<K,V> expectedMap, Map<K,V> actualMap, EquivalencyEvaluator evaluator){
        assertBothNullsOrNotNulls(expectedMap, actualMap);

        List<String> collector = new ArrayList<String>();

        try{
            assertEquals("Map size does not match", expectedMap.size(), actualMap.size());
        }
        catch(AssertionError e){
            collector.add(e.getMessage());
        }

        try{
            assertCollectionEquivalent(expectedMap.keySet(), actualMap.keySet(), evaluator);
        }
        catch(AssertionError e){
            collector.add(e.getMessage());
        }

        try{
            assertCollectionEquivalent(expectedMap.values(), actualMap.values(), evaluator);
        }
        catch(AssertionError e){
            collector.add(e.getMessage());
        }

        if(!collector.isEmpty()){
            throw new AssertionError(StringUtils.join(collector, "\n"));
        }
    }

    /**
     * <p>Asserts that the two arrays are equal without regard for Object position.</p>
     * <p>For Example: [5,3,2] is equivalent to [2,3,5].</p>
     * @param expecteds
     * @param actuals
     */
    public static void assertArrayEquivalent(Object[] expecteds, Object[] actuals){
        assertCollectionEquivalent(Arrays.asList(expecteds), Arrays.asList(actuals));
    }

    /**
     * <p>Asserts that the two Lists are equal without regard for Object position.</p>
     * <p>For Example: [5,3,2] is equivalent to [2,3,5].</p>
     * @param expecteds
     * @param actuals
     */
    public static void assertCollectionEquivalent(Collection<?> expecteds, Collection<?> actuals){
        assertCollectionEquivalent(expecteds, actuals, new DefaultEquivalencyEvaluator());
    }

    /**
     * <p>Asserts that the two Lists are equal without regard for Object position.</p>
     * <p>For Example: [5,3,2] is equivalent to [2,3,5].</p>
     * @param expecteds
     * @param actuals
     * @param evaluator
     */
    public static void assertCollectionEquivalent(Collection<?> expecteds, Collection<?> actuals, EquivalencyEvaluator evaluator){
        assertBothNullsOrNotNulls(expecteds, actuals);

        List<String> collector = new ArrayList<String>();

        try{
            assertEquals("Collection size does not match", expecteds.size(), actuals.size());
        }
        catch(AssertionError e){
            collector.add(e.getMessage());
        }

        for(Object expected : expecteds){
            try{
                assertEquals("actuals do not contain the expect number of occurrences: " + expected,
                        occurrencesInCollection(expecteds, expected, evaluator),
                        occurrencesInCollection(actuals, expected, evaluator));
            }
            catch(AssertionError e){
                collector.add(e.getMessage());
            }
        }

        for(Object actual : actuals){
            try{
                assertEquals("actuals contain an unexpected number of occurrences: " + actual,
                        occurrencesInCollection(expecteds, actual, evaluator),
                        occurrencesInCollection(actuals, actual, evaluator));
            }
            catch(AssertionError e){
                collector.add(e.getMessage());
            }
        }

        if(!collector.isEmpty()){
            throw new AssertionError(StringUtils.join(collector, "\n"));
        }
    }

    /**
     * Asserts that both the expected and actual are both either null or not null.
     * @param expected
     * @param actual
     */
    private static void assertBothNullsOrNotNulls(Object expected, Object actual){
        if(expected == null){
            assertNull("Expected actual to be null", actual);
        }
        else{
            assertNotNull("Expected actual to be not null", actual);
        }
    }

    /**
     * Counts the number of occurrences of an item in a collection.
     * @param col - {@link Collection}
     * @param item - item
     * @return number of times the item appears in the collection.
     */
    private static int occurrencesInCollection(Collection<?> col, Object item, EquivalencyEvaluator evaluator){
        int count = 0;
        for(Object obj : col){
            if(evaluator.isEquivalent(obj, item)){
                count ++;
            }
        }
        return count;
    }

}

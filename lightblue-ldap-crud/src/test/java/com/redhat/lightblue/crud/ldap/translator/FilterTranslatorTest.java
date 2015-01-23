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

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ldap.model.NullLdapFieldNameTranslator;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.ContainsOperator;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.UnaryLogicalOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.Filter;

public class FilterTranslatorTest {

    @SuppressWarnings("serial")
    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_UnsupportedFilterType(){
        new FilterTranslator(new NullLdapFieldNameTranslator()).translate(new QueryExpression(){

            @Override
            public JsonNode toJson() {
                throw new UnsupportedOperationException("Method should never be called.");
            }

        });
    }

    @Test
    public void testTranslate_ValueComparisonExpression_Equals(){
        QueryExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._eq, new Value("somevalue"));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(somekey=somevalue)", filter.toString());
    }

    @Test
    public void testTranslate_ValueComparisonExpression_NotEquals(){
        QueryExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._neq, new Value("somevalue"));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(!(somekey=somevalue))", filter.toString());
    }

    @Test
    public void testTranslate_ValueComparisonExpression_GTE(){
        QueryExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._gte, new Value("somevalue"));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(somekey>=somevalue)", filter.toString());
    }

    @Test
    public void testTranslate_ValueComparisonExpression_GT(){
        QueryExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._gt, new Value("somevalue"));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(!(somekey<=somevalue))", filter.toString());
    }

    @Test
    public void testTranslate_ValueComparisonExpression_LTE(){
        QueryExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._lte, new Value("somevalue"));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(somekey<=somevalue)", filter.toString());
    }

    @Test
    public void testTranslate_ValueComparisonExpression_LT(){
        QueryExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._lt, new Value("somevalue"));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(!(somekey>=somevalue))", filter.toString());
    }

    @Test
    public void testTranslate_UnaryLogicalExpression_NOT(){
        QueryExpression query = new UnaryLogicalExpression(
                UnaryLogicalOperator._not,
                new ValueComparisonExpression(new Path("somekey"), BinaryComparisonOperator._eq, new Value("somevalue")));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(!(somekey=somevalue))", filter.toString());
    }

    @Test
    public void testTranslate_NaryRelationalExpression_IN(){
        QueryExpression query = new NaryRelationalExpression(
                new Path("somekey"),
                NaryRelationalOperator._in,
                Arrays.asList(new Value("somevalue"), new Value("someothervalue")));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(|(somekey=somevalue)(somekey=someothervalue))", filter.toString());
    }

    @Test
    public void testTranslate_NaryRelationalExpression_NOT_IN(){
        QueryExpression query = new NaryRelationalExpression(
                new Path("somekey"),
                NaryRelationalOperator._not_in,
                Arrays.asList(new Value("somevalue"), new Value("someothervalue")));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(!(|(somekey=somevalue)(somekey=someothervalue)))", filter.toString());
    }

    @Test
    public void testTranslate_NaryLogicalExpression_AND(){
        QueryExpression query = new NaryLogicalExpression(
                NaryLogicalOperator._and, new ArrayList<QueryExpression>(Arrays.asList(
                        new ValueComparisonExpression(new Path("somekey"), BinaryComparisonOperator._eq, new Value("somevalue")),
                        new ValueComparisonExpression(new Path("someotherkey"), BinaryComparisonOperator._eq, new Value("someothervalue")))));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(&(somekey=somevalue)(someotherkey=someothervalue))", filter.toString());
    }

    @Test
    public void testTranslate_NaryLogicalExpression_OR(){
        QueryExpression query = new NaryLogicalExpression(
                NaryLogicalOperator._or, new ArrayList<QueryExpression>(Arrays.asList(
                        new ValueComparisonExpression(new Path("somekey"), BinaryComparisonOperator._eq, new Value("somevalue")),
                        new ValueComparisonExpression(new Path("someotherkey"), BinaryComparisonOperator._eq, new Value("someothervalue")))));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(|(somekey=somevalue)(someotherkey=someothervalue))", filter.toString());
    }

    @Test
    public void testTranslate_ArrayContainsExpression_ANY(){
        QueryExpression query = new ArrayContainsExpression(
                new Path("somekey"),
                ContainsOperator._any,
                Arrays.asList(new Value("somevalue"), new Value("someothervalue")));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(|(somekey=somevalue)(somekey=someothervalue))", filter.toString());
    }

    @Test
    public void testTranslate_ArrayContainsExpression_ALL(){
        QueryExpression query = new ArrayContainsExpression(
                new Path("somekey"),
                ContainsOperator._all,
                Arrays.asList(new Value("somevalue"), new Value("someothervalue")));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(&(somekey=somevalue)(somekey=someothervalue))", filter.toString());
    }

    @Test
    public void testTranslate_ArrayContainsExpression_NONE(){
        QueryExpression query = new ArrayContainsExpression(
                new Path("somekey"),
                ContainsOperator._none,
                Arrays.asList(new Value("somevalue"), new Value("someothervalue")));

        Filter filter = new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
        assertEquals("(!(&(somekey=somevalue)(somekey=someothervalue)))", filter.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_ArrayMatchExpression(){
        QueryExpression query = new ArrayMatchExpression(null, null);

        new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_FieldComparisonExpression(){
        QueryExpression query = new FieldComparisonExpression(null, null, null);

        new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTranslate_RegexMatchExpression(){
        QueryExpression query = new RegexMatchExpression(null, null, false, false, false, false);

        new FilterTranslator(new NullLdapFieldNameTranslator()).translate(query);
    }

}

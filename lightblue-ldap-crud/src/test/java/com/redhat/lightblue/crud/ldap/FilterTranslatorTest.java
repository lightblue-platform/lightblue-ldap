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

import org.junit.Test;

import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.util.Path;
import com.unboundid.ldap.sdk.Filter;

public class FilterTranslatorTest {

    @Test
    public void testTranslate_ValueEquals(){
        ValueComparisonExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._eq, new Value("somevalue"));

        Filter filter = new FilterTranslator().translate(query);
        assertEquals("(somekey=somevalue)", filter.toString());
    }

    @Test
    public void testTranslate_ValueNotEquals(){
        ValueComparisonExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._neq, new Value("somevalue"));

        Filter filter = new FilterTranslator().translate(query);
        assertEquals("(!(somekey=somevalue))", filter.toString());
    }

    @Test
    public void testTranslate_ValueGTE(){
        ValueComparisonExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._gte, new Value("somevalue"));

        Filter filter = new FilterTranslator().translate(query);
        assertEquals("(somekey>=somevalue)", filter.toString());
    }

    @Test
    public void testTranslate_ValueGT(){
        ValueComparisonExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._gt, new Value("somevalue"));

        Filter filter = new FilterTranslator().translate(query);
        assertEquals("(!(somekey<=somevalue))", filter.toString());
    }

    @Test
    public void testTranslate_ValueLTE(){
        ValueComparisonExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._lte, new Value("somevalue"));

        Filter filter = new FilterTranslator().translate(query);
        assertEquals("(somekey<=somevalue)", filter.toString());
    }

    @Test
    public void testTranslate_ValueLT(){
        ValueComparisonExpression query = new ValueComparisonExpression(
                new Path("somekey"), BinaryComparisonOperator._lt, new Value("somevalue"));

        Filter filter = new FilterTranslator().translate(query);
        assertEquals("(!(somekey>=somevalue))", filter.toString());
    }

}

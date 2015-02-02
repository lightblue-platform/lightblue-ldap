/*
 Copyright 2014 Red Hat, Inc. and/or its affiliates.

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

import java.util.ArrayList;
import java.util.List;

import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.unboundid.ldap.sdk.Filter;

/**
 * Translates a Lightblue {@link QueryExpression} into a UnboundID {@link Filter}.
 *
 * @author dcrissman
 */
public class FilterTranslator {

    private final LdapFieldNameTranslator fieldNameTranslator;

    public FilterTranslator(LdapFieldNameTranslator fieldNameTranslator){
        this.fieldNameTranslator = fieldNameTranslator;
    }

    /**
     * <p>Translates a Lightblue {@link QueryExpression} into a UnboundID {@link Filter}.</p>
     * @param query - {@link QueryExpression}
     * @return {@link Filter}
     */
    //NOTE: This method is internally called recursively.
    public Filter translate(QueryExpression query){
        Filter filter;
        if (query instanceof ArrayContainsExpression) {
            filter = translate((ArrayContainsExpression) query);
        }
        else if (query instanceof ArrayMatchExpression) {
            filter = translate((ArrayMatchExpression) query);
        }
        else if (query instanceof FieldComparisonExpression) {
            filter = translate((FieldComparisonExpression) query);
        }
        else if (query instanceof NaryLogicalExpression) {
            filter = translate((NaryLogicalExpression) query);
        }
        else if (query instanceof NaryRelationalExpression) {
            filter = translate((NaryRelationalExpression) query);
        }
        else if (query instanceof RegexMatchExpression) {
            filter = translate((RegexMatchExpression) query);
        }
        else if (query instanceof UnaryLogicalExpression) {
            filter = translate((UnaryLogicalExpression) query);
        }
        else if (query instanceof ValueComparisonExpression){
            filter = translate((ValueComparisonExpression) query);
        }
        else{
            throw new UnsupportedOperationException("Unsupported QueryExpression type: " + query.getClass());
        }
        return filter;
    }

    private Filter translate(ArrayContainsExpression query){
        String attributeName = fieldNameTranslator.translateFieldName(query.getArray());

        List<Filter> filters = new ArrayList<Filter>();
        for(Value value : query.getValues()){
            filters.add(Filter.createEqualityFilter(attributeName, value.getValue().toString()));
        }

        switch(query.getOp()){
            case _all:
                return Filter.createANDFilter(filters);
            case _any:
                return Filter.createORFilter(filters);
            case _none:
                return Filter.createNOTFilter(Filter.createANDFilter(filters));
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + query.getOp());
        }
    }

    private Filter translate(ArrayMatchExpression query){
        //TODO: Support
        throw new UnsupportedOperationException("Operation not yet supported");
    }

    private Filter translate(FieldComparisonExpression query){
        //TODO: Support
        throw new UnsupportedOperationException("Operation not yet supported");
    }

    private Filter translate(NaryLogicalExpression query){
        List<Filter> filters = new ArrayList<Filter>();
        for(QueryExpression subQuery : query.getQueries()){
            filters.add(translate(subQuery));
        }
        switch (query.getOp()){
            case _and:
                return Filter.createANDFilter(filters);
            case _or:
                return Filter.createORFilter(filters);
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + query.getOp());
        }
    }

    private Filter translate(NaryRelationalExpression query){
        String attributeName = fieldNameTranslator.translateFieldName(query.getField());
        List<Filter> filters = new ArrayList<Filter>();
        for(Value value : query.getValues()){
            filters.add(Filter.createEqualityFilter(attributeName, value.getValue().toString()));
        }

        switch (query.getOp()){
            case _in:
                return Filter.createORFilter(filters);
            case _not_in:
                return Filter.createNOTFilter(Filter.createORFilter(filters));
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + query.getOp());
        }
    }

    private Filter translate(RegexMatchExpression query){
        //TODO: Support
        throw new UnsupportedOperationException("Operation not yet supported");
    }

    private Filter translate(UnaryLogicalExpression query){
        switch(query.getOp()){
            case _not:
                return Filter.createNOTFilter(translate(query.getQuery()));
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + query.getOp());
        }
    }

    private Filter translate(ValueComparisonExpression query){
        String attributeName = fieldNameTranslator.translateFieldName(query.getField());
        String rValue = query.getRvalue().getValue().toString();

        switch(query.getOp()){
            case _eq:
                return Filter.createEqualityFilter(attributeName, rValue);
            case _neq:
                return Filter.createNOTFilter(Filter.createEqualityFilter(attributeName, rValue));
            case _gte:
                return Filter.createGreaterOrEqualFilter(attributeName, rValue);
            case _lte:
                return Filter.createLessOrEqualFilter(attributeName, rValue);
            case _gt: //aka. !lte
                return Filter.createNOTFilter(Filter.createLessOrEqualFilter(attributeName, rValue));
            case _lt: //aka. !gte
                return Filter.createNOTFilter(Filter.createGreaterOrEqualFilter(attributeName, rValue));
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + query.getOp());
        }
    }

}

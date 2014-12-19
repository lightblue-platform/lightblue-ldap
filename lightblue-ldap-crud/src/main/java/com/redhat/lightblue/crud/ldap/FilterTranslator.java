package com.redhat.lightblue.crud.ldap;

import java.util.ArrayList;
import java.util.List;

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

public class FilterTranslator {

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
        String field = query.getArray().toString();

        List<Filter> filters = new ArrayList<Filter>();
        for(Value value : query.getValues()){
            filters.add(Filter.createEqualityFilter(field, value.getValue().toString()));
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
        throw new UnsupportedOperationException("Operation not yet supported");
    }

    private Filter translate(FieldComparisonExpression query){
        throw new UnsupportedOperationException("Operation not yet supported");
    }

    private Filter translate(NaryLogicalExpression query){
        List<Filter> filters = new ArrayList<Filter>();
        for(QueryExpression subQuery : query.getQueries()){
            translate(subQuery);
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
        String field = query.getField().toString();
        List<Filter> filters = new ArrayList<Filter>();
        for(Value value : query.getValues()){
            filters.add(Filter.createEqualityFilter(field, value.getValue().toString()));
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
        throw new UnsupportedOperationException("Operation not yet supported");
    }

    private Filter translate(UnaryLogicalExpression query){
        return null;
    }

    private Filter translate(ValueComparisonExpression query){
        String field = query.getField().toString();
        String rValue = query.getRvalue().getValue().toString();

        switch(query.getOp()){
            case _eq:
                return Filter.createEqualityFilter(field, rValue);
            case _neq:
                return Filter.createNOTFilter(Filter.createEqualityFilter(field, rValue));
            case _gte:
                return Filter.createGreaterOrEqualFilter(field, rValue);
            case _lte:
                return Filter.createLessOrEqualFilter(field, rValue);
            default: //TODO gt, lt
                throw new UnsupportedOperationException("Unsupported operation: " + query.getOp());
        }
    }

}

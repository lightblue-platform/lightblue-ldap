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
            throw new IllegalArgumentException("Unsupported QueryExpression type: " + query.getClass());
        }
        return filter;
    }

    private Filter translate(ArrayContainsExpression query){
        return null;
    }

    private Filter translate(ArrayMatchExpression query){
        return null;
    }

    private Filter translate(FieldComparisonExpression query){
        String field = query.getField().toString();
        String rfield = query.getRfield().toString();

        switch(query.getOp()){
            case _eq:
                return Filter.createEqualityFilter(field, rfield);
            case _neq:
                return Filter.createNOTFilter(Filter.createEqualityFilter(field, rfield));
            case _gte:
                return Filter.createGreaterOrEqualFilter(field, rfield);
            case _lte:
                return Filter.createLessOrEqualFilter(field, rfield);
            default: //TODO gt, lt
                throw new IllegalArgumentException("Unsupported operation: " + query.getOp());
        }
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
                throw new IllegalArgumentException("Unsupported operation: " + query.getOp());
        }
    }

    private Filter translate(NaryRelationalExpression query){
        return null;
    }

    private Filter translate(RegexMatchExpression query){
        return null;
    }

    private Filter translate(UnaryLogicalExpression query){
        return null;
    }

    private Filter translate(ValueComparisonExpression query){
        switch(query.getOp()){
            case _eq:
                return Filter.createEqualityFilter(query.getField().toString(), query.getRvalue().toString());
            default:
                throw new IllegalArgumentException("Unsupported operation: " + query.getOp());
        }
    }

}

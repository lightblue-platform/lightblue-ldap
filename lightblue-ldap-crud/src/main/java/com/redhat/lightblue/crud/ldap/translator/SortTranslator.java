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

import java.util.ArrayList;
import java.util.List;

import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.query.CompositeSortKey;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.SortKey;

/**
 * Translator to convert Lightblue {@link Sort} values into {@link com.unboundid.ldap.sdk.controls.SortKey}s that
 * Unboundid can understand.
 *
 * @author dcrissman
 */
public class SortTranslator {

    private final LdapFieldNameTranslator fieldNameTranslator;

    public SortTranslator(LdapFieldNameTranslator fieldNameTranslator){
        this.fieldNameTranslator = fieldNameTranslator;
    }

    public com.unboundid.ldap.sdk.controls.SortKey[] translate(Sort sort){
        List<com.unboundid.ldap.sdk.controls.SortKey> results = new ArrayList<>();
        doTranslate(sort, results);
        return results.toArray(new com.unboundid.ldap.sdk.controls.SortKey[0]);
    }

    /*
     * Recursive method!
     */
    private void doTranslate(Sort sort, List<com.unboundid.ldap.sdk.controls.SortKey> results){
        if(sort instanceof CompositeSortKey){
            CompositeSortKey comoposite = (CompositeSortKey) sort;
            for(Sort subSort : comoposite.getKeys()){
                doTranslate(subSort, results);
            }
        }
        else if(sort instanceof SortKey){
            SortKey key = (SortKey) sort;
            results.add(new com.unboundid.ldap.sdk.controls.SortKey(fieldNameTranslator.translateFieldName(key.getField()), key.isDesc()));
        }
        else{
            throw new IllegalArgumentException("Unsupported Sort type: " + sort.getClass().getName());
        }
    }

}

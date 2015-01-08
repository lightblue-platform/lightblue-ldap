package com.redhat.lightblue.crud.ldap.translator.unboundid;

import java.util.LinkedHashSet;
import java.util.Set;

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

    public com.unboundid.ldap.sdk.controls.SortKey[] translate(Sort sort){
        Set<com.unboundid.ldap.sdk.controls.SortKey> results = new LinkedHashSet<com.unboundid.ldap.sdk.controls.SortKey>();
        doTranslate(sort, results);
        return results.toArray(new com.unboundid.ldap.sdk.controls.SortKey[0]);
    }

    /*
     * Recursive method!
     */
    private void doTranslate(Sort sort, Set<com.unboundid.ldap.sdk.controls.SortKey> results){
        if(sort instanceof CompositeSortKey){
            CompositeSortKey comoposite = (CompositeSortKey) sort;
            for(Sort subSort : comoposite.getKeys()){
                translate(subSort);
            }
        }
        else if(sort instanceof SortKey){
            SortKey key = (SortKey) sort;
            results.add(new com.unboundid.ldap.sdk.controls.SortKey(key.getField().getLast(), key.isDesc()));
        }
    }

}

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

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.crud.ldap.model.TrivialLdapFieldNameTranslator;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityMetadata;

/**
 * Utility methods for LDAP CRUD operations.
 *
 * @author dcrissman
 */
public final class LdapCrudUtil {

    /**
     * Shortcut method to get and return the {@link LdapFieldNameTranslator} on the passed
     * in {@link EntityMetadata}.
     * @param md - {@link EntityMetadata}.
     * @return {@link LdapFieldNameTranslator}
     * @throws IllegalArgumentException if an invalid object is found.
     */
    public static LdapFieldNameTranslator getLdapFieldNameTranslator(EntityMetadata md){
        Object o = md.getEntityInfo().getProperties().get(LdapConstant.BACKEND);

        if(o == null){
            return new TrivialLdapFieldNameTranslator();
        }

        if(!(o instanceof LdapFieldNameTranslator)){
            throw new IllegalArgumentException("Object of type " + o.getClass() + " is not supported.");
        }
        return (LdapFieldNameTranslator) o;
    }

    /**
     * Shortcut method to get and return the {@link LdapDataStore} on the passed in
     * {@link EntityMetadata}.
     * @param md - {@link EntityMetadata}
     * @return {@link LdapDataStore}
     * @throws IllegalArgumentException if an {@link LdapDataStore} is not set
     * on the {@link EntityMetadata}.
     */
    public static LdapDataStore getLdapDataStore(EntityMetadata md){
        DataStore store = md.getDataStore();
        if(!(store instanceof LdapDataStore)){
            throw new IllegalArgumentException("DataStore of type " + store.getClass() + " is not supported.");
        }
        return (LdapDataStore) store;
    }

    /**
     * Creates and returns a unique DN.
     * @param store - {@link LdapDataStore} to use as the BaseDN and field that
     * is used to represent uniqueness.
     * @param uniqueValue - value that makes the entity unique.
     * @return a string representation of the DN.
     */
    public static String createDN(LdapDataStore store, String uniqueValue){
        return store.getUniqueAttribute() + "=" + uniqueValue + "," + store.getBaseDN();
    }

    private LdapCrudUtil(){}

}

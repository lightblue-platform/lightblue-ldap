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
package com.redhat.lightblue.metadata.ldap.parser;

import com.redhat.lightblue.common.ldap.LdapConstant;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Error;

/**
 * {@link DataStoreParser} implementation for LDAP.
 *
 * @author dcrissman
 */
public class LdapDataStoreParser<T> implements DataStoreParser<T> {

    private final static String DATABASE = "database";
    private final static String BASEDN = "basedn";
    private final static String UNIQUE_FIELD = "uniqueattr";

    @Override
    public LdapDataStore parse(String name, MetadataParser<T> p, T node) {
        if (!LdapConstant.BACKEND.equals(name)) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
        }

        LdapDataStore dataStore = new LdapDataStore();
        dataStore.setDatabase(p.getRequiredStringProperty(node, DATABASE));
        dataStore.setBaseDN(p.getRequiredStringProperty(node, BASEDN));
        dataStore.setUniqueAttribute(p.getRequiredStringProperty(node, UNIQUE_FIELD));

        return dataStore;
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, DataStore store) {
        if(!(store instanceof LdapDataStore)){
            throw new IllegalArgumentException("DataStore of type " + store.getClass() + " is not supported.");
        }

        LdapDataStore ds = (LdapDataStore) store;
        p.putString(emptyNode, DATABASE, ds.getDatabase());
        p.putString(emptyNode, BASEDN, ds.getBaseDN());
        p.putString(emptyNode, UNIQUE_FIELD, ds.getUniqueAttribute());
    }

    @Override
    public String getDefaultName() {
        return LdapConstant.BACKEND;
    }

}

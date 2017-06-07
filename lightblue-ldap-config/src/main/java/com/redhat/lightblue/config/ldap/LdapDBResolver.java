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
package com.redhat.lightblue.config.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.redhat.lightblue.common.ldap.DBResolver;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.redhat.lightblue.metadata.DataStore;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * {@link DBResolver} that contains the implementation.
 *
 * @author dcrissman
 */
public class LdapDBResolver implements DBResolver{

    private final Set<LdapDataSourceConfiguration> ldapDataSources;

    public LdapDBResolver(Set<LdapDataSourceConfiguration> ldapDataSources){
        this.ldapDataSources = ldapDataSources;
    }

    public LDAPConnection get(DataStore store) throws LDAPException {
        if(!(store instanceof LdapDataStore)){
            throw new IllegalArgumentException("DataStore of type " + store.getClass() + " is not supported.");
        }

        return get(((LdapDataStore)store).getDatabase());
    }

    public LDAPConnection get(String database) throws LDAPException{
        LdapDataSourceConfiguration cnf = findByDatabase(database);
        if(cnf == null){
            throw new IllegalArgumentException("No database for " + database);
        }
        return cnf.getLdapConnection();
    }

    private LdapDataSourceConfiguration findByDatabase(String database){
        if(database == null){
            return null;
        }

        for(LdapDataSourceConfiguration cnf : ldapDataSources){
            if(database.equals(cnf.getDatabaseName())){
                return cnf;
            }
        }
        return null;
    }

	@Override
	public List<LDAPConnection> getConnections() {
		
		List<LDAPConnection> ldapConnections = new ArrayList<>();
		
		for (LdapDataSourceConfiguration ldapDS : ldapDataSources){
			try {
				ldapConnections.add(ldapDS.getLdapConnection());
			} catch (LDAPException e) {
				ldapConnections.add(null);
			}
		}
		
		return Collections.unmodifiableList(ldapConnections);
	}

}

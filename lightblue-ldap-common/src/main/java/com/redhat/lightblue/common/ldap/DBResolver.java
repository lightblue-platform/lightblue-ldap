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
package com.redhat.lightblue.common.ldap;

import java.util.Map;

import com.redhat.lightblue.metadata.DataStore;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * House and maintain connections to LDAP for the various {@link DataStore} instances.
 *
 * @author dcrissman
 */
public interface DBResolver {

    /**
     * Returns a {@link LDAPConnection} based on the backend definition.<br>
     * <b>NOTE:</b> A connection pool may be being used behind the scenes, so if
     * this method is called multiple times you might get a different connection instance
     * to the same database.
     */
    LDAPConnection get(DataStore store) throws LDAPException;
    
    
    /**
     * @return A {@link Map} of LDAP Database name and corresponding connection
     *         status as true/false. The status may be an object of
     *         {@link LDAPException} if no connection is available, or a problem
     *         occurs while creating a new connection to return
     */
    Map<String, Object> getLDAPConnectionsStatus();
}

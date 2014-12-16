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
package com.redhat.lightblue.hystrix.ldap;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPResult;

public class InsertCommand extends AbstractLdapHystrixCommand<LDAPResult>{

    private final Entry entry;

    public InsertCommand(LDAPConnection connection, Entry entry) {
        super(connection, InsertCommand.class.getSimpleName());

        this.entry = entry;
    }

    @Override
    protected LDAPResult run() throws Exception {
        return getConnection().add(entry);
    }

}

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

import java.util.HashSet;

import com.redhat.lightblue.config.ControllerConfiguration;
import com.redhat.lightblue.config.ControllerFactory;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.ldap.LdapCRUDController;

/**
 * {@link ControllerFactory} for LDAP.
 *
 * @author dcrissman
 */
public class LdapControllerFactory implements ControllerFactory{

    public CRUDController createController(ControllerConfiguration cfg, DataSourcesConfiguration ds) {
        return new LdapCRUDController(new LdapDBResolver(
                new HashSet<LdapDataSourceConfiguration>(ds.getDataSourcesByType(LdapDataSourceConfiguration.class).values())));
    }

}

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
package com.redhat.lightblue.crud.ldap.model;

import com.redhat.lightblue.common.ldap.LdapFieldNameTranslator;
import com.redhat.lightblue.util.Path;

/**
 * An implementation of {@link LdapFieldNameTranslator} used by crud when
 * no other implementation can be found.
 *
 * @author dcrissman
 */
public class TrivialLdapFieldNameTranslator implements LdapFieldNameTranslator{

    @Override
    public String translateFieldName(Path path) {
        return path.getLast();
    }

    @Override
    public String translateAttributeName(String attributeName) {
        return attributeName;
    }

}

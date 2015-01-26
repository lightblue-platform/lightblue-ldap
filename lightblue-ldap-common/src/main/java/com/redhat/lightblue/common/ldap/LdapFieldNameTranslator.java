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
package com.redhat.lightblue.common.ldap;

import com.redhat.lightblue.util.Path;

/**
 * Represents a class that can translate back and forth between a fieldName and an LDAP attributeName.
 *
 * @author dcrissman
 */
public interface LdapFieldNameTranslator {

    /**
     * Returns the attributeName with the given fieldName.
     * @param fieldName - metadata field name
     * @return ldap attributeName or the fieldName back at you if no mapping is present.
     */
    public String translateFieldName(Path path);

    /**
     * Returns the fieldName with the given attributeName.
     * @param attributeName - ldap attribute name
     * @return metadata fieldName or the attributeName back at you if no mapping is present.
     */
    public String translateAttributeName(String attributeName);

}

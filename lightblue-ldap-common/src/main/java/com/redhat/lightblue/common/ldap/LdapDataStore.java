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

import com.redhat.lightblue.metadata.DataStore;

public class LdapDataStore implements DataStore {

    private static final long serialVersionUID = 7599798419158041647L;

    private String database;
    private String baseDN;
    private String uniqueField;

    public String getBackend() {
        return LdapConstant.BACKEND;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getUniqueField() {
        return uniqueField;
    }

    public void setUniqueField(String uniqueField) {
        this.uniqueField = uniqueField;
    }

    public LdapDataStore(){}

    public LdapDataStore(String database){
        this.database = database;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseDN == null) ? 0 : baseDN.hashCode());
        result = prime * result
                + ((database == null) ? 0 : database.hashCode());
        result = prime * result
                + ((uniqueField == null) ? 0 : uniqueField.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LdapDataStore other = (LdapDataStore) obj;
        if (baseDN == null) {
            if (other.baseDN != null) {
                return false;
            }
        }
        else if (!baseDN.equals(other.baseDN)) {
            return false;
        }
        if (database == null) {
            if (other.database != null) {
                return false;
            }
        }
        else if (!database.equals(other.database)) {
            return false;
        }
        if (uniqueField == null) {
            if (other.uniqueField != null) {
                return false;
            }
        }
        else if (!uniqueField.equals(other.uniqueField)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LdapDataStore [database=" + database + ", baseDN=" + baseDN
                + ", uniqueField=" + uniqueField + "]";
    }

}

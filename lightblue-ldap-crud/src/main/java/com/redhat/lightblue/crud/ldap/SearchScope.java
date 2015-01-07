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
package com.redhat.lightblue.crud.ldap;

public enum SearchScope {
    BASE_ONLY(com.unboundid.ldap.sdk.SearchScope.BASE),
    ONE_LEVEL_DEEP_ONLY(com.unboundid.ldap.sdk.SearchScope.ONE),
    ALL(com.unboundid.ldap.sdk.SearchScope.SUB),
    SUBTREES_ONLY(com.unboundid.ldap.sdk.SearchScope.SUBORDINATE_SUBTREE);

    private com.unboundid.ldap.sdk.SearchScope scope;

    public com.unboundid.ldap.sdk.SearchScope getSearchScope(){
        return scope;
    }

    private SearchScope(com.unboundid.ldap.sdk.SearchScope scope){
        this.scope = scope;
    }
}

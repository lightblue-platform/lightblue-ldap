package com.redhat.lightblue.common.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;


public interface DBResolver {

    /**
     * Returns a {@link LDAPConnection} based on the backend definition
     */
    LDAPConnection get(LdapDataStore store) throws LDAPException;

}

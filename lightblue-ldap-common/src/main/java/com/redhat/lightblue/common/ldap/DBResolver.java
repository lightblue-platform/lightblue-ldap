package com.redhat.lightblue.common.ldap;

import com.redhat.lightblue.metadata.DataStore;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;


public interface DBResolver {

    /**
     * Returns a {@link LDAPConnection} based on the backend definition.<br>
     * <b>NOTE:</b> A connection pool may be being used behind the scenes, so if
     * this method is called multiple times you might get a different connection instance
     * to the same database.
     */
    LDAPConnection get(DataStore store) throws LDAPException;

}

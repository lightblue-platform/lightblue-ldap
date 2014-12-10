package com.redhat.lightblue.config.ldap;

import java.util.Map;

import com.redhat.lightblue.common.ldap.DBResolver;
import com.redhat.lightblue.common.ldap.LdapDataStore;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

public class LdapDBResolver implements DBResolver{

    private final Map<String, LdapDataSourceConfiguration> ldapDataSources;

    public LdapDBResolver(Map<String, LdapDataSourceConfiguration> ldapDataSources){
        this.ldapDataSources = ldapDataSources;
    }

    public LDAPConnection get(LdapDataStore store) throws LDAPException {
        return get(store.getDatabase());
    }

    public LDAPConnection get(String database) throws LDAPException{
        LdapDataSourceConfiguration cnf = ldapDataSources.get(database);
        if(cnf == null){
            throw new IllegalArgumentException("No database for " + database);
        }
        return cnf.getLdapConnection();
    }

}

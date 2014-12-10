package com.redhat.lightblue.config.ldap;

import java.util.HashMap;

import org.junit.Test;

import com.unboundid.ldap.sdk.LDAPException;

public class LdapDBResolverTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGet_UnknownDatabase() throws LDAPException{
        LdapDBResolver resolver = new LdapDBResolver(new HashMap<String, LdapDataSourceConfiguration>());
        resolver.get("Does Not Exist");
    }

}

package com.redhat.lightblue.common.ldap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LdapDataStoreTest {

    @Test
    public void testGetBackend(){
        assertEquals(LdapDataStore.BACKEND, new LdapDataStore().getBackend());
    }

}

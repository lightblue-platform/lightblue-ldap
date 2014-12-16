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

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFException;

@InMemoryLdapServer
public class LdapDataSourceConfigurationTest {

    @Rule
    public LdapServerExternalResource ldapServer = LdapServerExternalResource.createDefaultInstance();

    @BeforeClass
    public static void beforeClass(){
        System.setProperty("ldap.host", "localhost");
        System.setProperty("ldap.port", String.valueOf(LdapServerExternalResource.DEFAULT_PORT));
    }

    @Test
    public void testGetMetadataDataStoreParser(){
        assertNotNull(new LdapDataSourceConfiguration().getMetadataDataStoreParser());
    }

    @Test
    public void testInitializeFromJson() throws IOException, LDAPException, LDIFException{
        JsonNode ldapDatasourcesNode = loadJsonNode("./ldap-datasources.json");

        LdapDataSourceConfiguration configuration = new LdapDataSourceConfiguration();
        configuration.initializeFromJson(ldapDatasourcesNode.get("ldap"));

        LDAPConnection conn = configuration.getLdapConnection();
        assertNotNull(conn);
    }

    @Test(expected = IllegalStateException.class)
    public void testInitializeFromJson_NullNode() throws IOException, LDAPException, LDIFException{
        LdapDataSourceConfiguration configuration = new LdapDataSourceConfiguration();
        configuration.initializeFromJson(null);
        configuration.getLdapConnection();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetLdapConnection() throws LDAPException{
        new LdapDataSourceConfiguration().getLdapConnection();
    }

}

package com.redhat.lightblue.config.ldap;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFException;

public class LdapDataSourceConfigurationTest {

    @Test
    public void testGetMetadataDataStoreParser(){
        assertNotNull(new LdapDataSourceConfiguration().getMetadataDataStoreParser());
    }
    private InMemoryDirectoryServer server;
    @Before
    public void before() throws Exception {
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=com");
        config.addAdditionalBindCredentials("uid=admin,dc=example,dc=com", "cred");
        InMemoryListenerConfig listenerConfig = new InMemoryListenerConfig("test", null, 38900, null, null, null);
        config.setListenerConfigs(listenerConfig);
        config.setSchema(null); // do not check (attribute) schema
        server = new InMemoryDirectoryServer(config);
        server.startListening();

        server.add("dn: dc=com", "objectClass: top", "objectClass: domain", "dc: com");
        server.add("dn: dc=example,dc=com", "objectClass: top", "objectClass: domain", "dc: example");
    }

    @After
    public void after(){
        server.shutDown(true);
    }

    @Test
    public void testInitializeFromJson() throws IOException, LDAPException, LDIFException{
        JsonNode ldapDatasourcesNode = loadJsonNode("./ldap-datasources.json");

        LdapDataSourceConfiguration configuration = new LdapDataSourceConfiguration();
        configuration.initializeFromJson(ldapDatasourcesNode.get("ldap"));

        assertNotNull(configuration.getLdapConnection());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetLdapConnection() throws LDAPException{
        new LdapDataSourceConfiguration().getLdapConnection();
    }

}

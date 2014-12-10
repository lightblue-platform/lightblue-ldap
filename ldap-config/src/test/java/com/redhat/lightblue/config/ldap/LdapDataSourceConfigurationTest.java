package com.redhat.lightblue.config.ldap;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFException;

@InMemoryLdapServer
public class LdapDataSourceConfigurationTest {

    @SuppressWarnings("serial")
    @Rule
    public LdapServerExternalResource ldapServer = new LdapServerExternalResource(new LinkedHashMap<String, Attribute[]>(){{
        put("dc=com", new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "domain"),
                new Attribute("dc", "com")});
        put("dc=example,dc=com", new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "domain"),
                new Attribute("dc", "example")});
    }});

    @Test
    public void testGetMetadataDataStoreParser(){
        assertNotNull(new LdapDataSourceConfiguration().getMetadataDataStoreParser());
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

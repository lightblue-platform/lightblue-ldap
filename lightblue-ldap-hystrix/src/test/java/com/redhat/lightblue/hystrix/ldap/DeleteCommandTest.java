package com.redhat.lightblue.hystrix.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;

import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

@InMemoryLdapServer
public class DeleteCommandTest {

    @Rule
    public LdapServerExternalResource ldapServer = LdapServerExternalResource.createDefaultInstance();

    private void insertData(LDAPConnection connection) {
        Entry entry = new Entry("uid=john.doe,dc=example,dc=com",
                new Attribute("objectClass", "top", "person", "organizationalPerson", "inetOrgPerson"),
                new Attribute("uid", "john.doe"),
                new Attribute("givenName", "John"),
                new Attribute("sn", "Doe"),
                new Attribute("cn", "John Doe")
                );

        InsertCommand command = new InsertCommand(connection, entry);

        LDAPResult result = command.execute();

        assertNotNull(result);
        assertEquals(ResultCode.SUCCESS, result.getResultCode());
    }

    private SearchResult searchForData(LDAPConnection connection) throws LDAPException {
        SearchRequest searchRequest = new SearchRequest("dc=example,dc=com", SearchScope.SUB, "uid=john.doe");
        SearchCommand command = new SearchCommand(connection, searchRequest);

        return command.execute();
    }

    private void ensureDataExists(LDAPConnection connection) throws LDAPException {
        SearchResult result = searchForData(connection);

        assertNotNull(result);
        assertEquals(ResultCode.SUCCESS, result.getResultCode());
        assertEquals(1, result.getEntryCount());
        SearchResultEntry found = result.getSearchEntry("uid=john.doe,dc=example,dc=com");
        assertNotNull(found);
        assertEquals("John", found.getAttribute("givenName").getValue());
    }

    @Test
    public void testExecute() throws LDAPException {
        LDAPConnection connection = ldapServer.getLDAPConnection();

        try {
            insertData(connection);
            ensureDataExists(connection);

            DeleteRequest deleteRequest = new DeleteRequest("uid=john.doe,dc=example,dc=com");
            DeleteCommand command = new DeleteCommand(connection, deleteRequest);

            LDAPResult result = command.execute();
            assertNotNull(result);
            assertEquals(ResultCode.SUCCESS, result.getResultCode());

            //Verify data is gone
            SearchResult postResult = searchForData(connection);
            assertNotNull(postResult);
            assertEquals(ResultCode.SUCCESS, postResult.getResultCode());
            assertEquals(0, postResult.getEntryCount());

        } finally {
            connection.close();
        }
    }

}

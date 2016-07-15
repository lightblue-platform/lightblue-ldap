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

import static com.redhat.lightblue.test.Assert.assertNoDataErrors;
import static com.redhat.lightblue.test.Assert.assertNoErrors;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.ldap.test.LightblueLdapTestHarness;
import com.redhat.lightblue.test.FakeClientIdentification;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import com.unboundid.ldap.sdk.Attribute;

/**
 * <b>NOTE:</b> This test suite is intended to be run in a certain order. Selectively running unit tests
 * may produce unwanted results.
 *
 * @author dcrissman
 */
public class ITCaseLdapCRUDControllerTest extends LightblueLdapTestHarness {

    private static final String BASEDB_USERS = "ou=Users,dc=example,dc=com";
    private static final String BASEDB_DEPARTMENTS = "ou=Departments,dc=example,dc=com";

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("ldap.person.basedn", BASEDB_USERS);
        System.setProperty("ldap.department.basedn", BASEDB_DEPARTMENTS);

        init();
    }

    private static void init() throws Exception {
        ldapServer.add(BASEDB_USERS, new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "organizationalUnit"),
                new Attribute("ou", "Users")});
        ldapServer.add(BASEDB_DEPARTMENTS, new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "organizationalUnit"),
                new Attribute("ou", "Departments")});
    }

    @Override
    @Before
    public void loadLdapStatically() throws Exception {
        super.loadLdapStatically();
        init();
    }

    public ITCaseLdapCRUDControllerTest() throws Exception {
        super(false);
    }

    @Override
    protected JsonNode[] getMetadataJsonNodes() throws IOException {
        return new JsonNode[]{
                loadJsonNode("./metadata/person-metadata.json"),
                loadJsonNode("./metadata/department-metadata.json")};
    }

    @Override
    public boolean isGrantAnyoneAccess() {
        return false;
    }

    @Test
    public void testInsertMany() throws Exception {
        //Test
        Response response = getLightblueFactory().getMediator().insert(
                createRequest_FromResource(InsertionRequest.class, "./crud/insert/person-insert-many.json"));

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(4, response.getModifiedCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);
        JSONAssert.assertEquals(
                "[{\"dn\":\"uid=junior.doe," + BASEDB_USERS + "\"},{\"dn\":\"uid=john.doe," + BASEDB_USERS + "\"},{\"dn\":\"uid=jane.doe," + BASEDB_USERS + "\"},{\"dn\":\"uid=jack.buck," + BASEDB_USERS + "\"}]",
                entityData.toString(), false);
    }

    @Test
    public void testFindSingle() throws Exception {
        //Setup
        getLightblueFactory().getMediator().insert(
                createRequest_FromResource(InsertionRequest.class, "./crud/insert/person-insert-many.json"));

        //Test
        Response response = getLightblueFactory().getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/person-find-single.json"));

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getMatchCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);
        JSONAssert.assertEquals(
                "[{\"dn\":\"uid=john.doe," + BASEDB_USERS + "\",\"uid\":\"john.doe\",\"objectType\":\"person\",\"objectClass#\":4}]",
                entityData.toString(), true);
    }

    @Test
    public void testDelete() throws Exception {
        //Setup
        getLightblueFactory().getMediator().insert(
                createRequest_FromResource(InsertionRequest.class, "./crud/insert/person-insert-many.json"));

        //Test
        Response response = getLightblueFactory().getMediator().delete(
                createRequest_FromResource(DeleteRequest.class, "./crud/delete/person-delete-simple.json"));

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getModifiedCount());
        
        //Ensure entry was inserted
        Response findResponse = getLightblueFactory().getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/person-find-many.json"));

        assertNotNull(findResponse);
        assertNoErrors(findResponse);
        assertNoDataErrors(findResponse);
        //There were 3, now only 2
        assertEquals(2, findResponse.getMatchCount());
    }

    @Test
    public void testFindMany() throws Exception {
        //Setup
        getLightblueFactory().getMediator().insert(
                createRequest_FromResource(InsertionRequest.class, "./crud/insert/person-insert-many.json"));

        //Test
        Response response = getLightblueFactory().getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/person-find-many.json"));

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(3, response.getMatchCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);

        //Search requests results in desc order, strict mode is enforced to assure this.
        JSONAssert.assertEquals(
                "[{\"dn\":\"uid=junior.doe," + BASEDB_USERS + "\"},{\"dn\":\"uid=john.doe," + BASEDB_USERS + "\"},{\"dn\":\"uid=jane.doe," + BASEDB_USERS + "\"}]",
                entityData.toString(), true);
    }

    @Test
    public void testFindMany_WithPagination() throws Exception {
        //Setup
        getLightblueFactory().getMediator().insert(
                createRequest_FromResource(InsertionRequest.class, "./crud/insert/person-insert-many.json"));

        //Test
        Response response = getLightblueFactory().getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/person-find-many-paginated.json"));

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getMatchCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);

        JSONAssert.assertEquals(
                "[{\"dn\":\"uid=john.doe," + BASEDB_USERS + "\"}]",
                entityData.toString(), true);
    }

    @Test
    public void testInsertWithRoles() throws Exception {
        //Setup
        String insert = AbstractJsonNodeTest.loadResource("./crud/insert/department-insert-template.json")
                .replaceFirst("#cn", "Marketing")
                .replaceFirst("#description", "Department devoted to Marketing")
                .replaceFirst("#members",
                        "cn=John Doe," + BASEDB_USERS + "\",\"cn=Jane Doe," + BASEDB_USERS);

        InsertionRequest insertRequest = createRequest_FromJsonString(InsertionRequest.class, insert);
        insertRequest.setClientId(new FakeClientIdentification("fakeUser", "admin"));

        //Test
        Response response = getLightblueFactory().getMediator().insert(insertRequest);

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getModifiedCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);
        JSONAssert.assertEquals(
                "[{\"dn\":\"cn=Marketing," + BASEDB_DEPARTMENTS + "\"}]",
                entityData.toString(), true);
    }

    @Test
    public void testInsertWithInvalidRoles() throws Exception {
        //Setup
        String insert = AbstractJsonNodeTest.loadResource("./crud/insert/department-insert-template.json")
                .replaceFirst("#cn", "HR")
                .replaceFirst("#description", "Department devoted to HR")
                .replaceFirst("#members", "cn=John Doe," + BASEDB_USERS);

        InsertionRequest insertRequest = createRequest_FromJsonString(InsertionRequest.class, insert);
        insertRequest.setClientId(new FakeClientIdentification("fakeUser"));

        //Test
        Response response = getLightblueFactory().getMediator().insert(insertRequest);

        //Asserts
        assertNotNull(response);
        assertEquals(0, response.getModifiedCount());

        assertNull(response.getEntityData());

        assertNoErrors(response);
        assertEquals(1, response.getDataErrors().size());
        JSONAssert.assertEquals("{\"errors\":[{\"errorCode\":\"crud:insert:NoFieldAccess\",\"msg\":\"member\"}]}",
                response.getDataErrors().get(0).toJson().toString(), false);
    }

    @Test
    public void testFindWithRoles() throws Exception {
        //Setup
        String insert = AbstractJsonNodeTest.loadResource("./crud/insert/department-insert-template.json")
                .replaceFirst("#cn", "Marketing")
                .replaceFirst("#description", "Department devoted to Marketing")
                .replaceFirst("#members",
                        "cn=John Doe," + BASEDB_USERS + "\",\"cn=Jane Doe," + BASEDB_USERS);

        InsertionRequest insertRequest = createRequest_FromJsonString(InsertionRequest.class, insert);
        insertRequest.setClientId(new FakeClientIdentification("fakeUser", "admin"));

        getLightblueFactory().getMediator().insert(insertRequest);

        //Test
        FindRequest findRequest = createRequest_FromResource(FindRequest.class, "./crud/find/department-find-single.json");
        findRequest.setClientId(new FakeClientIdentification("fakeUser", "admin"));

        Response response = getLightblueFactory().getMediator().find(findRequest);

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getMatchCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);
        JSONAssert.assertEquals(
                "[{\"member#\":2,\"member\":[\"cn=John Doe," + BASEDB_USERS + "\",\"cn=Jane Doe," + BASEDB_USERS + "\"],\"cn\":\"Marketing\",\"description\":\"Department devoted to Marketing\"}]",
                entityData.toString(), true);
    }

    @Test
    public void testFindWithInsufficientRoles() throws Exception {
        //Setup
        String insert = AbstractJsonNodeTest.loadResource("./crud/insert/department-insert-template.json")
                .replaceFirst("#cn", "Marketing")
                .replaceFirst("#description", "Department devoted to Marketing")
                .replaceFirst("#members",
                        "cn=John Doe," + BASEDB_USERS + "\",\"cn=Jane Doe," + BASEDB_USERS);

        InsertionRequest insertRequest = createRequest_FromJsonString(InsertionRequest.class, insert);
        insertRequest.setClientId(new FakeClientIdentification("fakeUser", "admin"));

        getLightblueFactory().getMediator().insert(insertRequest);

        //Test
        FindRequest findRequest = createRequest_FromResource(FindRequest.class, "./crud/find/department-find-single.json");
        findRequest.setClientId(new FakeClientIdentification("fakeUser"));

        Response response = getLightblueFactory().getMediator().find(findRequest);

        //Asserts
        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getMatchCount());

        assertNotNull(response.getEntityData());
        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);

        JSONAssert.assertEquals(
                "[{\"cn\":\"Marketing\",\"description\":\"Department devoted to Marketing\"}]",
                entityData.toString(), true);
    }

}

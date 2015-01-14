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

import static com.redhat.lightblue.util.JsonUtils.json;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runners.MethodSorters;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.DataError;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.JsonTranslator;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.mongo.test.MongoServerExternalResource;
import com.redhat.lightblue.mongo.test.MongoServerExternalResource.InMemoryMongoServer;
import com.redhat.lightblue.test.FakeClientIdentification;
import com.redhat.lightblue.util.Error;
import com.unboundid.ldap.sdk.Attribute;

/**
 * <b>NOTE:</b> This test suite is intended to be run in a certain order. Selectively running unit tests
 * may produce unwanted results.
 *
 * @author dcrissman
 */
@InMemoryLdapServer
@InMemoryMongoServer
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ITCaseLdapCRUDControllerTest{

    private static final String BASEDB_USERS = "ou=Users,dc=example,dc=com";
    private static final String BASEDB_DEPARTMENTS = "ou=Departments,dc=example,dc=com";

    @ClassRule
    public static LdapServerExternalResource ldapServer = LdapServerExternalResource.createDefaultInstance();

    @ClassRule
    public static MongoServerExternalResource mongoServer = new MongoServerExternalResource();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    public static LightblueFactory lightblueFactory;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ldapServer.add("ou=Users,dc=example,dc=com",  new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "organizationalUnit"),
                new Attribute("ou", "Users")});
        ldapServer.add("ou=Departments,dc=example,dc=com",  new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "organizationalUnit"),
                new Attribute("ou", "Departments")});

        System.setProperty("ldap.host", "localhost");
        System.setProperty("ldap.port", String.valueOf(LdapServerExternalResource.DEFAULT_PORT));
        System.setProperty("ldap.database", "test");
        System.setProperty("ldap.person.basedn", BASEDB_USERS);
        System.setProperty("ldap.department.basedn", BASEDB_DEPARTMENTS);

        System.setProperty("mongo.host", "localhost");
        System.setProperty("mongo.port", String.valueOf(MongoServerExternalResource.DEFAULT_PORT));
        System.setProperty("mongo.database", "lightblue");

        lightblueFactory = new LightblueFactory(
                new DataSourcesConfiguration(loadJsonNode("./datasources.json")));

        JsonTranslator tx = lightblueFactory.getJsonTranslator();

        Metadata metadata = lightblueFactory.getMetadata();
        metadata.createNewMetadata(tx.parse(EntityMetadata.class, loadJsonNode("./metadata/person-metadata.json")));
        metadata.createNewMetadata(tx.parse(EntityMetadata.class, loadJsonNode("./metadata/department-metadata.json")));
    }

    @AfterClass
    public static void after(){
        lightblueFactory = null;
    }

    private void assertNoErrors(Response response){
        for(Error error : response.getErrors()){
            Exception e = new Exception(error.getMessage(), error);
            e.printStackTrace();
            collector.addError(e);
        }
    }

    private void assertNoDataErrors(Response response){
        for(DataError error : response.getDataErrors()){
            Exception e = new Exception("DataError: " + error.toJson().toString());
            e.printStackTrace();
            collector.addError(e);
        }
    }

    private <T> T createRequest_FromResource(Class<T> type, String jsonFile) throws IOException{
        return createRequest(type, loadJsonNode(jsonFile));
    }

    private <T> T createRequest_FromJsonString(Class<T> type, String jsonString) throws IOException{
        return createRequest(type, json(jsonString));
    }

    private <T> T createRequest(Class<T> type, JsonNode node) throws IOException{
        JsonTranslator tx = lightblueFactory.getJsonTranslator();
        return tx.parse(type, node);
    }

    @Test
    public void series1_phase1_Person_Insert() throws Exception{
        Response response = lightblueFactory.getMediator().insert(
                createRequest_FromResource(InsertionRequest.class, "./crud/insert/person-insert-many.json"));

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
    public void series1_phase2_Person_FindSingle() throws Exception{
        Response response = lightblueFactory.getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/person-find-single.json"));

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
    public void series1_phase2_Person_FindMany() throws Exception{
        Response response = lightblueFactory.getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/person-find-many.json"));

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
    public void series1_phase2_Person_FindMany_WithPagination() throws Exception{
        Response response = lightblueFactory.getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/person-find-many-paginated.json"));

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
    public void series2_phase1_Department_InsertWithRoles() throws Exception{
        String insert = loadResource("./crud/insert/department-insert-template.json")
                .replaceFirst("#cn", "Marketing")
                .replaceFirst("#description", "Department devoted to Marketing")
                .replaceFirst("#members", "\"" + StringUtils.join(Arrays.asList("cn=John Doe," + BASEDB_USERS, "cn=Jane Doe," + BASEDB_USERS), "\",\"") + "\"");

        InsertionRequest insertRequest = createRequest_FromJsonString(InsertionRequest.class, insert);
        insertRequest.setClientId(new FakeClientIdentification("fakeUser", "admin"));

        Response response = lightblueFactory.getMediator().insert(insertRequest);

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
    public void series2_phase1_Department_InsertWithInvalidRoles() throws Exception{
        String insert = loadResource("./crud/insert/department-insert-template.json")
                .replaceFirst("#cn", "HR")
                .replaceFirst("#description", "Department devoted to HR")
                .replaceFirst("#members", "\"cn=John Doe," + BASEDB_USERS + "\"");

        InsertionRequest insertRequest = createRequest_FromJsonString(InsertionRequest.class, insert);
        insertRequest.setClientId(new FakeClientIdentification("fakeUser"));

        Response response = lightblueFactory.getMediator().insert(insertRequest);

        assertNotNull(response);
        assertEquals(0, response.getModifiedCount());

        assertNull(response.getEntityData());

        assertNoErrors(response);
        assertEquals(1, response.getDataErrors().size());
        JSONAssert.assertEquals("{\"errors\":[{\"errorCode\":\"crud:insert:NoFieldAccess\",\"msg\":\"member\"}]}",
                response.getDataErrors().get(0).toJson().toString(), false);
    }

    @Test
    public void series2_phase2_Department_FindWithRoles() throws Exception{
        FindRequest findRequest = createRequest_FromResource(FindRequest.class, "./crud/find/department-find-single.json");
        findRequest.setClientId(new FakeClientIdentification("fakeUser", "admin"));

        Response response = lightblueFactory.getMediator().find(findRequest);

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
    public void series2_phase2_Department_FindWithInsufficientRoles() throws Exception{
        FindRequest findRequest = createRequest_FromResource(FindRequest.class, "./crud/find/department-find-single.json");
        findRequest.setClientId(new FakeClientIdentification("fakeUser"));

        Response response = lightblueFactory.getMediator().find(findRequest);

        assertNotNull(response);
        assertEquals(1, response.getMatchCount());

        assertNoErrors(response);
        assertNoDataErrors(response);

        assertNotNull(response.getEntityData());
        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);

        JSONAssert.assertEquals(
                "[{\"cn\":\"Marketing\",\"description\":\"Department devoted to Marketing\"}]",
                entityData.toString(), true);
    }

}

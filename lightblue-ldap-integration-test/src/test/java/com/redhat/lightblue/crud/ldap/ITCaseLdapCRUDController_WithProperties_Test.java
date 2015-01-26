/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.mongo.test.MongoServerExternalResource;
import com.unboundid.ldap.sdk.Attribute;

/**
 * This test suite is designed to ensure that the LDAP properties work correctly.
 *
 * @author dcrissman
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ITCaseLdapCRUDController_WithProperties_Test extends AbstractLdapCRUDController{

    private static final String BASEDB_CUSTOMERS = "ou=Customers,dc=example,dc=com";

    @BeforeClass
    public static void beforeClass() throws Exception {
        ldapServer.add("ou=Customers,dc=example,dc=com",  new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "organizationalUnit"),
                new Attribute("ou", "Customers")});

        System.setProperty("ldap.host", "localhost");
        System.setProperty("ldap.port", String.valueOf(LdapServerExternalResource.DEFAULT_PORT));
        System.setProperty("ldap.database", "test");
        System.setProperty("ldap.customer.basedn", BASEDB_CUSTOMERS);

        System.setProperty("mongo.host", "localhost");
        System.setProperty("mongo.port", String.valueOf(MongoServerExternalResource.DEFAULT_PORT));
        System.setProperty("mongo.database", "lightblue");

        initLightblueFactory("./datasources.json", "./metadata/customer-metadata.json");
    }

    @Test
    public void test1CustomerInsertWithProperties() throws Exception{
        Response response = lightblueFactory.getMediator().insert(
                createRequest_FromResource(InsertionRequest.class, "./crud/insert/customer-insert-single.json"));

        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getModifiedCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);
        JSONAssert.assertEquals(
                "[{\"id\":\"uid=frodo.baggins," + BASEDB_CUSTOMERS + "\"}]",
                entityData.toString(), false);
    }

    @Test
    public void test2FindCustomerWithProperties() throws Exception{
        Response response = lightblueFactory.getMediator().find(
                createRequest_FromResource(FindRequest.class, "./crud/find/customer-find-single.json"));

        assertNotNull(response);
        assertNoErrors(response);
        assertNoDataErrors(response);
        assertEquals(1, response.getMatchCount());

        JsonNode entityData = response.getEntityData();
        assertNotNull(entityData);
        JSONAssert.assertEquals(
                "[{\"id\":\"uid=frodo.baggins," + BASEDB_CUSTOMERS + "\","
                        + "\"customerId\":\"frodo.baggins\","
                        + "\"firstName\":\"Frodo\","
                        + "\"lastName\":\"Baggins\","
                        + "\"cn\":\"Frodo Baggins\","
                        + "\"interfaces#\":4,"
                        + "\"interfaces\":[\"top\",\"person\",\"organizationalPerson\",\"inetOrgPerson\"]}]",
                        entityData.toString(), true);
    }

}

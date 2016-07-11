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

import static com.redhat.lightblue.util.JsonUtils.json;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.ldap.test.LightblueLdapTestHarness;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import com.unboundid.ldap.sdk.Attribute;

public class ITCaseLdapCRUDController_InvalidMetadata_Test extends LightblueLdapTestHarness {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static final String BASEDB_USERS = "ou=Users,dc=example,dc=com";

    @BeforeClass
    public static void beforeClass() throws Exception {
        ldapServer.add(BASEDB_USERS, new Attribute[]{
                new Attribute("objectClass", "top"),
                new Attribute("objectClass", "organizationalUnit"),
                new Attribute("ou", "Users")});

        System.setProperty("ldap.person.basedn", BASEDB_USERS);
    }

    public ITCaseLdapCRUDController_InvalidMetadata_Test() throws Exception {
        super(false);
    }

    @Override
    protected JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[]{};
    }

    @Test
    public void testMetadata_WithoutUniqueFieldDefined() throws Exception {
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"createNewMetadata(person)\",\"errorCode\":\"ldap:UndefinedUniqueAttribute\",\"msg\":\"uid\"}");

        //Remove the uid field from the fields definition
        String metadataWithoutUniqueField = AbstractJsonNodeTest.loadResource("./metadata/person-metadata.json").replaceFirst("\"uid\": \\{\"type\": \"string\"\\},", "");
        JsonNode nodeWithoutUniqueField = json(metadataWithoutUniqueField, true);

        Metadata metadata = getLightblueFactory().getMetadata();
        metadata.createNewMetadata(getLightblueFactory().getJsonTranslator().parse(EntityMetadata.class, nodeWithoutUniqueField));
    }

}

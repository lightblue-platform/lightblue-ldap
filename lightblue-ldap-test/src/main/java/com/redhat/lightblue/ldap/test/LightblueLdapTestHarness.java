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
package com.redhat.lightblue.ldap.test;

import static com.redhat.lightblue.util.JsonUtils.json;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadResource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.redhat.lightblue.mongo.test.LightblueMongoTestHarness;

@InMemoryLdapServer
public abstract class LightblueLdapTestHarness extends LightblueMongoTestHarness {

    @ClassRule
    public static LdapServerExternalResource ldapServer = LdapServerExternalResource.createDefaultInstance();

    private final boolean loadLdapStatically;

    @BeforeClass
    public static void prepareLdapDatasources() {
        if (System.getProperty("ldap.datasource") == null) {
            System.setProperty("ldap.datasource", "myldapdatasource");
        }
        if (System.getProperty("ldap.host") == null) {
            System.setProperty("ldap.host", "localhost");
        }
        if (System.getProperty("ldap.port") == null) {
            System.setProperty("ldap.port", String.valueOf(ldapServer.getPort()));
        }
        if (System.getProperty("ldap.database") == null) {
            System.setProperty("ldap.database", "test");
        }
    }

    public LightblueLdapTestHarness() throws Exception {
        this(true);
    }

    public LightblueLdapTestHarness(boolean loadLdapStatically) throws Exception {
        super();
        this.loadLdapStatically = loadLdapStatically;
    }

    @Before
    public void loadLdapStatically() throws Exception {
        if (!loadLdapStatically) {
            ldapServer.clear();
        }
    }

    @Override
    protected JsonNode getLightblueCrudJson() throws Exception {
        return json(loadResource("/ldap-lightblue-crud.json", LightblueLdapTestHarness.class), true);
    }

    @Override
    protected JsonNode getLightblueMetadataJson() throws Exception {
        return json(loadResource("/ldap-lightblue-metadata.json", LightblueLdapTestHarness.class), true);
    }

    @Override
    protected JsonNode getDatasourcesJson() throws Exception {
        return json(loadResource("/ldap-datasources.json", LightblueLdapTestHarness.class), true);
    }

}

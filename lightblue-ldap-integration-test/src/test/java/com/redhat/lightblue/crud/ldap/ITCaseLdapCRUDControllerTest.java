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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.redhat.lightblue.DataError;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.JsonTranslator;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.redhat.lightblue.mongo.test.MongoServerExternalResource;
import com.redhat.lightblue.mongo.test.MongoServerExternalResource.InMemoryMongoServer;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

@InMemoryLdapServer
@InMemoryMongoServer
public class ITCaseLdapCRUDControllerTest{

    @ClassRule
    public static LdapServerExternalResource ldapServer = LdapServerExternalResource.createDefaultInstance();

    @ClassRule
    public static MongoServerExternalResource mongoServer = new MongoServerExternalResource();

    public static LightblueFactory lightblueFactory;

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty("ldap.host", "localhost");
        System.setProperty("ldap.port", String.valueOf(LdapServerExternalResource.DEFAULT_PORT));

        System.setProperty("mongo.host", "localhost");
        System.setProperty("mongo.port", String.valueOf(MongoServerExternalResource.DEFAULT_PORT));
        
        lightblueFactory = new LightblueFactory(
                new DataSourcesConfiguration(AbstractJsonNodeTest.loadJsonNode("./datasources.json")));
    }

    @AfterClass
    public static void after(){
        lightblueFactory = null;
    }

    @Test
    public void testInsert() throws Exception{
        JsonTranslator tx = lightblueFactory.getJsonTranslator();
        InsertionRequest insertIequest = tx.parse(InsertionRequest.class, JsonUtils.json("{}"));
        Response response = lightblueFactory.getMediator().insert(insertIequest);

        assertNotNull(response);
        //assertNoErrors(response);
    }

    private void assertNoErrors(Response response){
        if(response.getErrors().isEmpty() && response.getDataErrors().isEmpty()){
            return;
        }

        StringBuilder builder = new StringBuilder();

        if(!response.getErrors().isEmpty()){
            builder.append("Response has Errors: \n");
            for(Error error : response.getErrors()){
                builder.append(error.toJson()+ "\n");
            }
        }

        if(!response.getDataErrors().isEmpty()){
            builder.append("Response has Data Errors: \n");
            for(DataError error : response.getDataErrors()){
                builder.append(error.toJson() + "\n");
            }
        }

        fail(builder.toString());
    }

}

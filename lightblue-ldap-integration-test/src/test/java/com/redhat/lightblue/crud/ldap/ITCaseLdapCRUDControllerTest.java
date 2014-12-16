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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

@InMemoryLdapServer
public class ITCaseLdapCRUDControllerTest{

    @Rule
    public LdapServerExternalResource ldapServer = LdapServerExternalResource.createDefaultInstance();

    @Before
    public void before() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException{
        if(mediator == null){
            LightblueFactory factory = new LightblueFactory(
                    new DataSourcesConfiguration(AbstractJsonNodeTest.loadJsonNode("./datasources.json")));
            mediator = factory.getMediator();
        }
    }

    public ITCaseLdapCRUDControllerTest(){
        System.setProperty("ldap.host", "localhost");
        System.setProperty("ldap.port", String.valueOf(LdapServerExternalResource.DEFAULT_PORT));
    }

    public Mediator mediator;

    @Test
    public void test(){
        assertNotNull(mediator);
    }


}

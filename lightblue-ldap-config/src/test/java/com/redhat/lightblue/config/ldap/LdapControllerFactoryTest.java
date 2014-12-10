package com.redhat.lightblue.config.ldap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.redhat.lightblue.config.DataSourcesConfiguration;

public class LdapControllerFactoryTest {

    @Test
    public void testCreateController(){
        assertNotNull(new LdapControllerFactory().createController(null, mock(DataSourcesConfiguration.class)));
    }

}

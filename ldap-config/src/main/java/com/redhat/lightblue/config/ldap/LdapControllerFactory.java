package com.redhat.lightblue.config.ldap;

import com.redhat.lightblue.config.ControllerConfiguration;
import com.redhat.lightblue.config.ControllerFactory;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.ldap.LdapCRUDController;

public class LdapControllerFactory implements ControllerFactory{

    public CRUDController createController(ControllerConfiguration cfg, DataSourcesConfiguration ds) {
        return new LdapCRUDController();
    }

}

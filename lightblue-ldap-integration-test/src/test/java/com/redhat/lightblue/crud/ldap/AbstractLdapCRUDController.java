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
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.runners.model.MultipleFailureException;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.DataError;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.config.JsonTranslator;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource;
import com.redhat.lightblue.ldap.test.LdapServerExternalResource.InMemoryLdapServer;
import com.redhat.lightblue.util.Error;

@InMemoryLdapServer
public abstract class AbstractLdapCRUDController extends AbstractCRUDController{

    @ClassRule
    public static LdapServerExternalResource ldapServer = LdapServerExternalResource.createDefaultInstance();

    protected void assertNoErrors(Response response) throws MultipleFailureException{
        List<Throwable> errors = new ArrayList<Throwable>();
        for(Error error : response.getErrors()){
            Exception e = new Exception(error.getMessage(), error);
            e.printStackTrace();
            errors.add(e);
        }

        if(!errors.isEmpty()){
            throw new MultipleFailureException(errors);
        }
    }

    protected void assertNoDataErrors(Response response) throws MultipleFailureException{
        List<Throwable> errors = new ArrayList<Throwable>();
        for(DataError error : response.getDataErrors()){
            Exception e = new Exception("DataError: " + error.toJson().toString());
            e.printStackTrace();
            errors.add(e);
        }

        if(!errors.isEmpty()){
            throw new MultipleFailureException(errors);
        }
    }

    protected <T> T createRequest_FromResource(Class<T> type, String jsonFile) throws IOException{
        return createRequest(type, loadJsonNode(jsonFile));
    }

    protected <T> T createRequest_FromJsonString(Class<T> type, String jsonString) throws IOException{
        return createRequest(type, json(jsonString));
    }

    protected <T> T createRequest(Class<T> type, JsonNode node) throws IOException{
        JsonTranslator tx = lightblueFactory.getJsonTranslator();
        return tx.parse(type, node);
    }

}

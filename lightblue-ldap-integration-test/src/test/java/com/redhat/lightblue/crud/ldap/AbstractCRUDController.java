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

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;

import org.junit.AfterClass;
import org.junit.ClassRule;

import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.JsonTranslator;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.mongo.test.MongoServerExternalResource;
import com.redhat.lightblue.mongo.test.MongoServerExternalResource.InMemoryMongoServer;

@InMemoryMongoServer
public abstract class AbstractCRUDController {

    @ClassRule
    public static MongoServerExternalResource mongoServer = new MongoServerExternalResource();

    protected static LightblueFactory lightblueFactory;

    protected static void initLightblueFactory(String datasourcesResourcePath, String... metadataResourcePaths)
            throws Exception {
        lightblueFactory = new LightblueFactory(
                new DataSourcesConfiguration(loadJsonNode(datasourcesResourcePath)));

        JsonTranslator tx = lightblueFactory.getJsonTranslator();

        Metadata metadata = lightblueFactory.getMetadata();
        for(String metadataResourcePath : metadataResourcePaths){
            metadata.createNewMetadata(tx.parse(EntityMetadata.class, loadJsonNode(metadataResourcePath)));
        }
    }

    @AfterClass
    public static void cleanup(){
        lightblueFactory = null;
    }

}

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
package com.redhat.lightblue.config.ldap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.config.DataSourceConfiguration;
import com.redhat.lightblue.metadata.ldap.parser.LdapDataStoreParser;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.RoundRobinServerSet;
import com.unboundid.ldap.sdk.ServerSet;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.SingleServerSet;

/**
 * {@link DataSourceConfiguration} for LDAP.
 *
 * @author dcrissman
 */
public class LdapDataSourceConfiguration implements DataSourceConfiguration{

    private static final long serialVersionUID = 3276072662352275664L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDataSourceConfiguration.class);

    private static final String LDAP_CONFIG_DATABASE = "database";
    private static final String LDAP_CONFIG_BINDABLE_DB = "bindableDn";
    private static final String LDAP_CONFIG_PASSWORD = "password";
    private static final String LDAP_CONFIG_NUMBER_OF_INITIAL_CONNECTIONS = "numberOfInitialConnections";
    private static final String LDAP_CONFIG_MAX_NUMBER_OF_CONNECTIONS = "maxNumberOfConnections";
    private static final String LDAP_SERVER_CONFIG_HOST = "host";
    private static final String LDAP_SERVER_CONFIG_PORT = "port";

    private static final int DEFAULT_NUMBER_OF_INITIAL_CONNECTIONS = 5;
    private static final int DEFAULT_MAX_NUMBER_OF_CONNECTIONS = 10;

    private String databaseName;
    private LDAPConnectionPool connectionPool;

    public String getDatabaseName(){
        return databaseName;
    }

    @SuppressWarnings("rawtypes")
    public Class<LdapDataStoreParser> getMetadataDataStoreParser() {
        return LdapDataStoreParser.class;
    }

    public void initializeFromJson(JsonNode node) {
        if(node == null){
            LOGGER.warn("Attempted to initizlize an LDAP datasource from a null JsonNode.");
            return;
        }

        databaseName = parseJsonNode(node, LDAP_CONFIG_DATABASE, true).asText();

        //TODO Add functionality for other BindRequest Types
        BindRequest bindRequest = new SimpleBindRequest(
                parseJsonNode(node, LDAP_CONFIG_BINDABLE_DB, true).asText(),
                parseJsonNode(node, LDAP_CONFIG_PASSWORD, true).asText());

        int initialConnections = DEFAULT_NUMBER_OF_INITIAL_CONNECTIONS;
        JsonNode initialConnectionsNode = parseJsonNode(node, LDAP_CONFIG_NUMBER_OF_INITIAL_CONNECTIONS, false);
        if(initialConnectionsNode != null){
            initialConnections = initialConnectionsNode.asInt(DEFAULT_NUMBER_OF_INITIAL_CONNECTIONS);
        }

        int maxConnections = DEFAULT_MAX_NUMBER_OF_CONNECTIONS;
        JsonNode maxConnectionsNode = parseJsonNode(node, LDAP_CONFIG_MAX_NUMBER_OF_CONNECTIONS, false);
        if(maxConnectionsNode != null){
            maxConnections = maxConnectionsNode.asInt(DEFAULT_MAX_NUMBER_OF_CONNECTIONS);
        }

        JsonNode serversNode = parseJsonNode(node, "servers", true);
        Map<String, Integer> hostPortMap = new HashMap<String, Integer>();
        if(serversNode.isArray()){
            Iterator<JsonNode> serversIterator = serversNode.elements();
            while(serversIterator.hasNext()){
                JsonNode serverNode = serversIterator.next();
                hostPortMap.put(
                        parseJsonNode(serverNode, LDAP_SERVER_CONFIG_HOST, true).asText(),
                        parseJsonNode(serverNode, LDAP_SERVER_CONFIG_PORT, true).asInt());
            }
        }
        else{
            throw new IllegalArgumentException("Unable to parse 'servers' for ldap database " + databaseName
                    + ". Must be an instance of an array.");
        }

        String[] hosts = hostPortMap.keySet().toArray(new String[0]);
        if(hostPortMap.isEmpty()){
            throw new IllegalArgumentException("At least 1 server must be provided for ldap database " + databaseName);
        }

        ServerSet serverSet;
        if(hostPortMap.size() == 1){
            serverSet = new SingleServerSet(hosts[0], hostPortMap.get(hosts[0]));
        }
        else{
            int[] ports = new int[hosts.length];
            for(int x = 0; x < ports.length; x++){
                ports[x] = hostPortMap.get(hosts[x]);
            }

            //TODO Add support for other ServerSet types.
            serverSet = new RoundRobinServerSet(hosts, ports);
        }

        try{
            connectionPool = new LDAPConnectionPool(serverSet, bindRequest, initialConnections, maxConnections);
        }
        catch(LDAPException e) {
            throw new LdapConfigException("Unable to connect to ldap server(s).", e);
        }
    }

    public LDAPConnection getLdapConnection() throws LDAPException{
        if(connectionPool == null){
            throw new IllegalStateException("Class has not yet been initialized");
        }
        return connectionPool.getConnection();
    }

    private JsonNode parseJsonNode(JsonNode node, String key, boolean required){
        JsonNode parsedNode = node.get(key);
        if(required && (parsedNode == null)){
            throw new IllegalArgumentException("Unable to find required field '" + key + "' for ldap connection.");
        }
        return parsedNode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((databaseName == null) ? 0 : databaseName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LdapDataSourceConfiguration other = (LdapDataSourceConfiguration) obj;
        if (databaseName == null) {
            if (other.databaseName != null) {
                return false;
            }
        }
        else if (!databaseName.equals(other.databaseName)) {
            return false;
        }
        return true;
    }

}

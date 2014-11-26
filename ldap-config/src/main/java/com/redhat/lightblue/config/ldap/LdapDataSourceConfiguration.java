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

public class LdapDataSourceConfiguration implements DataSourceConfiguration{

    private static final long serialVersionUID = 3276072662352275664L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDataSourceConfiguration.class);

    private static final int DEFAULT_NUMBER_OF_INITIAL_CONNECTIONS = 5;
    private static final int DEFAULT_MAX_NUMBER_OF_CONNECTIONS = 10;

    private String databaseName;
    private LDAPConnectionPool connectionPool;

    public String getDatabaseName(){
        return databaseName;
    }

    public Class<LdapDataStoreParser> getMetadataDataStoreParser() {
        return LdapDataStoreParser.class;
    }

    public void initializeFromJson(JsonNode node) {
        if(node == null){
            LOGGER.warn("Attempted to initizlize an LDAP datasource from a null JsonNode.");
            return;
        }

        databaseName = parseJsonNode(node, "database", true).asText();

        //TODO Add functionality for other BindRequest Types
        BindRequest bindRequest = new SimpleBindRequest(
                parseJsonNode(node, "bindableDn", true).asText(),
                parseJsonNode(node, "password", true).asText());

        int initialConnections = DEFAULT_NUMBER_OF_INITIAL_CONNECTIONS;
        JsonNode initialConnectionsNode = parseJsonNode(node, "numberOfInitialConnections", false);
        if(initialConnectionsNode != null){
            initialConnections = initialConnectionsNode.asInt(DEFAULT_NUMBER_OF_INITIAL_CONNECTIONS);
        }

        int maxConnections = DEFAULT_MAX_NUMBER_OF_CONNECTIONS;
        JsonNode maxConnectionsNode = parseJsonNode(node, "maxNumberOfConnections", false);
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
                        parseJsonNode(serverNode, "host", true).asText(),
                        parseJsonNode(serverNode, "port", true).asInt());
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

}

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
package com.redhat.lightblue.ldap.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;

public class LdapServerExternalResource extends ExternalResource {

    public static final String DEFAULT_BASE_DN = "dc=com";
    public static final String DEFAULT_BINDABLE_DN = "uid=admin,dc=example,dc=com";
    public static final String DEFAULT_PASSWORD = "password";
    public static final int DEFAULT_PORT = 38900;

    /**
     * Creates a simple instance of {@link LdapServerExternalResource} with
     * "dc=example,dc=com" already existing.
     * @return simple instance of {@link LdapServerExternalResource}.
     */
    @SuppressWarnings("serial")
    public static LdapServerExternalResource createDefaultInstance() {
        return new LdapServerExternalResource(null, new LinkedHashMap<String, Attribute[]>() {
            {
                put("dc=com", new Attribute[]{
                        new Attribute("objectClass", "top"),
                        new Attribute("objectClass", "domain"),
                        new Attribute("dc", "com")});
                put("dc=example,dc=com", new Attribute[]{
                        new Attribute("objectClass", "top"),
                        new Attribute("objectClass", "domain"),
                        new Attribute("dc", "example")});
            }
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Inherited
    @Documented
    public @interface InMemoryLdapServer {
        String[] baseDns() default {DEFAULT_BASE_DN};

        BindCriteria[] bindCriteria() default {@BindCriteria()};

        String name() default "test";

        int port() default DEFAULT_PORT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @Documented
    public @interface BindCriteria {
        String bindableDn() default DEFAULT_BINDABLE_DN;

        String password() default DEFAULT_PASSWORD;
    }

    private InMemoryDirectoryServer server = null;
    private InMemoryLdapServer imlsAnnotation = null;
    private final LinkedHashMap<String, Attribute[]> preloadDnData;
    private final Schema schema;

    public LdapServerExternalResource() {
        this(null, null);
    }

    public LdapServerExternalResource(Schema schema, LinkedHashMap<String, Attribute[]> preload) {
        this.schema = schema;
        this.preloadDnData = preload;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        imlsAnnotation = description.getAnnotation(InMemoryLdapServer.class);
        if ((imlsAnnotation == null) && description.isTest()) {
            imlsAnnotation = description.getTestClass().getAnnotation(InMemoryLdapServer.class);
        }

        if (imlsAnnotation == null) {
            throw new IllegalStateException("@InMemoryLdapServer must be set on suite or test level.");
        }

        return super.apply(base, description);
    }

    @Override
    protected void before() throws Throwable {
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(imlsAnnotation.baseDns());

        for (BindCriteria bindCriteria : imlsAnnotation.bindCriteria()) {
            config.addAdditionalBindCredentials(bindCriteria.bindableDn(), bindCriteria.password());
        }

        InMemoryListenerConfig listenerConfig = new InMemoryListenerConfig(
                imlsAnnotation.name(), null, imlsAnnotation.port(), null, null, null);
        config.setListenerConfigs(listenerConfig);
        config.setSchema(schema); // do not check (attribute) schema

        server = new InMemoryDirectoryServer(config);
        server.startListening();

        if (preloadDnData != null) {
            for (Entry<String, Attribute[]> entry : preloadDnData.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected void after() {
        if (server != null) {
            server.shutDown(true);
            server = null;
        }
    }

    public void add(String dn, Attribute... attributes) throws LDIFException, LDAPException {
        server.add(dn, attributes);
    }

    /**
     * @return the actual name used by the ldap server.
     */
    public String getName() {
        return imlsAnnotation.name();
    }

    /**
     * @return the actual port used by the ldap server.
     */
    public int getPort() {
        return imlsAnnotation.port();
    }

    /**
     * @return the actual basedns used by the ldap server.
     */
    public String[] getBaseDNs() {
        return imlsAnnotation.baseDns();
    }

}

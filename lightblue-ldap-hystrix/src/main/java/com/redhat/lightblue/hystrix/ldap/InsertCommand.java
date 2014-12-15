package com.redhat.lightblue.hystrix.ldap;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPResult;

public class InsertCommand extends AbstractLdapHystrixCommand<LDAPResult>{

    private final Entry entry;

    public InsertCommand(LDAPConnection connection, Entry entry) {
        super(connection, InsertCommand.class.getSimpleName());

        this.entry = entry;
    }

    @Override
    protected LDAPResult run() throws Exception {
        return getConnection().add(entry);
    }

}

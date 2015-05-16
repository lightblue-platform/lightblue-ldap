package com.redhat.lightblue.hystrix.ldap;

import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPResult;

public class DeleteCommand extends AbstractLdapHystrixCommand<LDAPResult> {

    private final DeleteRequest deleteRequest;

    public DeleteCommand(LDAPConnection connection, DeleteRequest deleteRequest) {
        super(connection, DeleteCommand.class.getSimpleName());

        this.deleteRequest = deleteRequest;
    }

    @Override
    protected LDAPResult run() throws Exception {
        return getConnection().delete(deleteRequest);
    }

}

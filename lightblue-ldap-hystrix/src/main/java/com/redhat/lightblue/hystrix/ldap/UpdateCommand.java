package com.redhat.lightblue.hystrix.ldap;

import java.util.List;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;

public class UpdateCommand extends AbstractLdapHystrixCommand<LDAPResult> {

    private final String dn;
    private final List<Modification> mods;

    public UpdateCommand(LDAPConnection connection, String dn, List<Modification> mods) {
        super(connection, UpdateCommand.class.getSimpleName());
        this.dn = dn;
        this.mods = mods;
    }

    @Override
    protected LDAPResult run() throws Exception {
        return getConnection().modify(dn, mods);
    }
}

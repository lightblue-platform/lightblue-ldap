package com.redhat.lightblue.hystrix.ldap;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.unboundid.ldap.sdk.LDAPConnection;

public abstract class AbstractLdapHystrixCommand<T> extends HystrixCommand<T>{

    public static final String GROUPKEY = "ldap";

    private final LDAPConnection connection;

    public LDAPConnection getConnection(){
        return connection;
    }

    public AbstractLdapHystrixCommand(LDAPConnection connection, String commandKey){
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(GROUPKEY)).
                andCommandKey(HystrixCommandKey.Factory.asKey(GROUPKEY + ":" + commandKey)));

        this.connection = connection;
    }

}

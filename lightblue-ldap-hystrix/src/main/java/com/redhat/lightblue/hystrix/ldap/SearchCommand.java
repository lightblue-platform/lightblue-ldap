package com.redhat.lightblue.hystrix.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;

public class SearchCommand extends AbstractLdapHystrixCommand<SearchResult>{

    private final SearchRequest searchRequest;

    public SearchCommand(LDAPConnection connection, SearchRequest searchRequest) {
        super(connection, SearchCommand.class.getSimpleName());

        this.searchRequest = searchRequest;
    }

    @Override
    protected SearchResult run() throws Exception {
        return getConnection().search(searchRequest);
    }

}

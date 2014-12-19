package com.redhat.lightblue.metadata.ldap.enums;

public enum SearchScope {
    BASE_ONLY(com.unboundid.ldap.sdk.SearchScope.BASE),
    ONE_LEVEL_DEEP_ONLY(com.unboundid.ldap.sdk.SearchScope.ONE),
    ALL(com.unboundid.ldap.sdk.SearchScope.SUB),
    SUBTREES_ONLY(com.unboundid.ldap.sdk.SearchScope.SUBORDINATE_SUBTREE);

    private com.unboundid.ldap.sdk.SearchScope scope;

    public com.unboundid.ldap.sdk.SearchScope getSearchScope(){
        return scope;
    }

    private SearchScope(com.unboundid.ldap.sdk.SearchScope scope){
        this.scope = scope;
    }
}

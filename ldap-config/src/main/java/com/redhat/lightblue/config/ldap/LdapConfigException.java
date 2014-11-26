package com.redhat.lightblue.config.ldap;

public class LdapConfigException extends RuntimeException {

    private static final long serialVersionUID = -1592146043661826825L;

    public LdapConfigException(String message) {
        super(message);
    }

    public LdapConfigException(Throwable cause) {
        super(cause);
    }

    public LdapConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}

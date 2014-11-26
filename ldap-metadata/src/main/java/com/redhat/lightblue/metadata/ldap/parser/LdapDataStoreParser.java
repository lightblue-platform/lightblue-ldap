package com.redhat.lightblue.metadata.ldap.parser;

import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.ldap.LdapDataStore;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Error;

public class LdapDataStoreParser<T> implements DataStoreParser<T> {

    public static final String NAME = "ldap";

    public DataStore parse(String name, MetadataParser<T> p, T node) {
        if (!NAME.equals(name)) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
        }

        LdapDataStore dataStore = new LdapDataStore();

        return dataStore;
    }

    public void convert(MetadataParser<T> p, T emptyNode, DataStore object) {
        // TODO Auto-generated method stub

    }

    public String getDefaultName() {
        return NAME;
    }

}

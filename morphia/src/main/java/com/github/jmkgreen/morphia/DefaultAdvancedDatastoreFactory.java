package com.github.jmkgreen.morphia;

import com.mongodb.Mongo;

/**
 * Default AdvancedDatastoreFactory that meets most requirements.
 *
 * @since 1.2.4
 */
public class DefaultAdvancedDatastoreFactory implements AdvancedDatastoreFactory {

    /**
     * Returns a new instance of {@link DatastoreImpl} which implements {@link AdvancedDatastore}.
     *
     * @since 1.2.4
     * @param morphia
     * @param mongo
     * @param dbName
     * @param username
     * @param password
     * @return
     */
    public AdvancedDatastore create(Morphia morphia, Mongo mongo, String dbName, String username, char[] password) {
        return new DatastoreImpl(morphia, mongo,  dbName,  username,  password);
    }
}

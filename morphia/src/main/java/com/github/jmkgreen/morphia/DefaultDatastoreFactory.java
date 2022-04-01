package com.github.jmkgreen.morphia;

import com.mongodb.Mongo;

/**
 * Default factory implementation that satisfies most requirements.
 *
 * @since 1.2.4
 */
public class DefaultDatastoreFactory implements DatastoreFactory {

    /**
     * Creates a new Datastore instance using the {@link DatastoreImpl DatastoreImpl}.
     *
     * @since 1.2.4
     * @param morphia
     * @param mongo
     * @param dbName
     * @param username
     * @param password
     * @return new instance
     */
    public Datastore create(Morphia morphia, Mongo mongo, String dbName, String username, char[] password) {
        return new DatastoreImpl(morphia, mongo,  dbName, username, password);
    }
}

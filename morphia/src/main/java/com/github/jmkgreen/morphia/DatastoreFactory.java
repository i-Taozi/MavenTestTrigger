package com.github.jmkgreen.morphia;

import com.mongodb.Mongo;

/**
 * Used by {@link Morphia} to create {@link Datastore} instances.
 *
 * Supply your own to Morphia to deviate.
 *
 * @since 1.2.4
 */
public interface DatastoreFactory {

    /**
     * Create a new instance of {@link Datastore}.
     *
     * @since 1.2.4
     * @param morphia
     * @param mongo
     * @param dbName
     * @param username
     * @param password
     * @return
     */
    public Datastore create(Morphia morphia, Mongo mongo, String dbName, String username, char[] password);
}

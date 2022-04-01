package com.github.jmkgreen.morphia.mapping.cache;

/**
 * Enable implementation of EntityCacheFactories.
 */
public interface EntityCacheFactory {

    /**
     * Create and return an instance of an {@link EntityCache}.
     * @return new EntityCache.
     */
    public EntityCache create();
}

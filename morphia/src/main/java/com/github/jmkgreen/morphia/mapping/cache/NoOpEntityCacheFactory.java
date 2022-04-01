package com.github.jmkgreen.morphia.mapping.cache;

/**
 * Allows creation of NoOp cache.
 *
 * @author James Green
 */
public class NoOpEntityCacheFactory implements EntityCacheFactory {

    /**
     * Returns a new instance of {@link NoOpEntityCache}.
     * @return
     */
    public EntityCache create() {
        return new NoOpEntityCache();
    }
}

package com.github.jmkgreen.morphia.mapping.cache;

import com.github.jmkgreen.morphia.Key;

/**
 * This cache intentionally stores nothing in order to keep memory use low.
 *
 * It is an alternative to BasicEntityCache.
 *
 * @author James Green
 */
public class NoOpEntityCache implements EntityCache {
    private static final EntityCacheStatistics stats = new EntityCacheStatistics();

    /**
     * Returns null under all circumstances.
     *
     * @param k
     * @return
     */
    public Boolean exists(Key<?> k) {
        return null;
    }

    /**
     * Intentionally does nothing.
     *
     * @param k
     * @param exists
     */
    public void notifyExists(Key<?> k, boolean exists) {
    }

    /**
     * Intentionally returns null.
     *
     * @param k
     * @param <T>
     * @return
     */
    public <T> T getEntity(Key<T> k) {
        return null;
    }

    /**
     * Intentionally returns null.
     *
     * @param k
     * @param <T>
     * @return
     */
    public <T> T getProxy(Key<T> k) {
        return null;
    }

    /**
     * Intentionally does nothing.
     *
     * @param k
     * @param t
     * @param <T>
     */
    public <T> void putProxy(Key<T> k, T t) {
    }

    /**
     * Intentionally does nothing.
     *
     * @param k
     * @param t
     * @param <T>
     */
    public <T> void putEntity(Key<T> k, T t) {
    }

    /**
     * Intentionally does nothing.
     */
    public void flush() {
    }

    /**
     * Returns a final static instance of {@link EntityCacheStatistics}.
     * @return
     */
    public EntityCacheStatistics stats() {
        return stats;
    }
}

/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Concurrency {
    /**
     * Number of threads, defaults to {@code Runtime.getRuntime().availableProcessors()}.
     * 
     * @return number of threads
     */
    int threads() default -1;

    /**
     * Maximum number of threads, defaults to {@code 12}.
     * 
     * @return maximum number of threads
     */
    int maxThreads() default 12;

    /**
     * Multiplicator for {@link #threads()}, defaults to {@code 1}.
     * 
     * @return thread multiplicator
     */
    float factor() default 1f;
}

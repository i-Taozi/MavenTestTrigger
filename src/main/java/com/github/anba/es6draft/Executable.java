/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft;

import com.github.anba.es6draft.runtime.internal.RuntimeInfo;
import com.github.anba.es6draft.runtime.internal.Source;

/**
 * <h1>15 ECMAScript Language: Scripts and Modules</h1>
 * <ul>
 * <li>15.1 Scripts
 * <li>15.2 Modules
 * </ul>
 */
public interface Executable {
    /**
     * Returns the source information for this object.
     * 
     * @return the source or {@code null} if no source available
     */
    Source getSource();

    /**
     * Returns the runtime object.
     * 
     * @return the runtime object
     */
    RuntimeInfo.RuntimeObject getRuntimeObject();
}

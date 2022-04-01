/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

import com.github.anba.es6draft.ast.scope.Scope;

/**
 * Base interface for {@link Node} objects which hold any {@link Scope} information
 */
public interface ScopedNode extends Node {
    /**
     * Returns the scope object for this node.
     * 
     * @return the scope object
     */
    Scope getScope();
}

/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast.scope;

import com.github.anba.es6draft.ast.WithStatement;

/**
 * Scope class for {@link WithStatement} nodes.
 */
public interface WithScope extends Scope {
    @Override
    WithStatement getNode();
}

/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

/**
 * <h1>12 ECMAScript Language: Expressions</h1><br>
 * <h2>12.2 Primary Expression</h2>
 * <ul>
 * <li>12.2.6 Object Initializer
 * </ul>
 */
public interface PropertyName extends ClassElementName {
    /**
     * Returns the string representation for this property name or <code>null</code> if no string representation is
     * available.
     * 
     * @return the string representation or {@code null}
     */
    String getName();

    @Override
    default PropertyName toPropertyName() {
        return this;
    }
}

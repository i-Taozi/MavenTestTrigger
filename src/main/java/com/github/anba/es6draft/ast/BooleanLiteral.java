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
 * <li>12.2.4 Literals
 * </ul>
 */
public final class BooleanLiteral extends ValueLiteral<Boolean> {
    private final boolean value;

    public BooleanLiteral(long beginPosition, long endPosition, boolean value) {
        super(beginPosition, endPosition);
        this.value = value;
    }

    /**
     * Returns the boolean literal's {@code boolean} value.
     * 
     * @return the boolean value
     */
    public boolean booleanValue() {
        return value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public <R, V> R accept(NodeVisitor<R, V> visitor, V value) {
        return visitor.visit(this, value);
    }

    @Override
    public <V> int accept(IntNodeVisitor<V> visitor, V value) {
        return visitor.visit(this, value);
    }

    @Override
    public <V> void accept(VoidNodeVisitor<V> visitor, V value) {
        visitor.visit(this, value);
    }
}

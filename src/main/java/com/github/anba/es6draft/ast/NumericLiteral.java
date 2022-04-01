/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

import static com.github.anba.es6draft.runtime.AbstractOperations.ToString;

/**
 * <h1>12 ECMAScript Language: Expressions</h1><br>
 * <h2>12.2 Primary Expression</h2>
 * <ul>
 * <li>12.2.4 Literals
 * </ul>
 */
public final class NumericLiteral extends ValueLiteral<Double> implements PropertyName {
    private final double value;

    public NumericLiteral(long beginPosition, long endPosition, double value) {
        super(beginPosition, endPosition);
        this.value = value;
    }

    /**
     * Returns {@code true} if the value can be represented as an {@code int}.
     * 
     * @return {@code true} if the value is an integer
     */
    public boolean isInt() {
        double value = this.value;
        int intValue = (int) value;
        return intValue == value && (intValue != 0 || Double.doubleToRawLongBits(value) == 0L);
    }

    /**
     * Returns the numeric literal's {@code int} value.
     * 
     * @return the integer value
     */
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns the numeric literal's {@code double} value.
     * 
     * @return the double value
     */
    public double doubleValue() {
        return value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String getName() {
        return ToString(value);
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

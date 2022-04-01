/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

/**
 * <h1>12 ECMAScript Language: Expressions</h1>
 */
public abstract class Expression extends AstNode {
    private int parentheses = 0;

    protected Expression(long beginPosition, long endPosition) {
        super(beginPosition, endPosition);
    }

    /**
     * Returns {@code true} if this expression is enclosed in parentheses.
     * 
     * @return {@code true} if parenthesized
     */
    public boolean isParenthesized() {
        return parentheses != 0;
    }

    /**
     * Adds one more layer of parentheses around this expression.
     */
    public void addParentheses() {
        parentheses += 1;
    }

    /**
     * Returns the number of parentheses enclosing this expression.
     * 
     * @return the number of parentheses
     */
    public int getParentheses() {
        return parentheses;
    }

    /**
     * Returns a {@link Expression} instance representing this node as a no-value-expression, i.e. an expression which
     * returns no completion value.
     * 
     * @return the expression no-value node
     */
    public Expression emptyCompletion() {
        return this;
    }
}

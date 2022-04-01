/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

import com.github.anba.es6draft.ast.scope.WithScope;

/**
 * <h1>13 ECMAScript Language: Statements and Declarations</h1>
 * <ul>
 * <li>13.11 The with Statement
 * </ul>
 */
public final class WithStatement extends Statement implements ScopedNode {
    private final WithScope scope;
    private final Expression expression;
    private Statement statement;

    public WithStatement(long beginPosition, long endPosition, WithScope scope, Expression expression,
            Statement statement) {
        super(beginPosition, endPosition);
        this.scope = scope;
        this.expression = expression;
        this.statement = statement;
    }

    @Override
    public WithScope getScope() {
        return scope;
    }

    /**
     * Returns the <code>with</code>-statement's expression node.
     * 
     * @return the expression node
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Returns the <code>with</code>-statement's statement node.
     * 
     * @return the statement node
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * Sets the <code>with</code>-statement's statement node.
     * 
     * @param statement
     *            the new statement node
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
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

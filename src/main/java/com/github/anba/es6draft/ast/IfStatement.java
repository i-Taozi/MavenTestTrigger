/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

/**
 * <h1>13 ECMAScript Language: Statements and Declarations</h1>
 * <ul>
 * <li>13.6 The if Statement
 * </ul>
 */
public final class IfStatement extends Statement {
    private final Expression test;
    private Statement then;
    private Statement otherwise;

    public IfStatement(long beginPosition, long endPosition, Expression test, Statement then, Statement otherwise) {
        super(beginPosition, endPosition);
        this.test = test;
        this.then = then;
        this.otherwise = otherwise;
    }

    public Expression getTest() {
        return test;
    }

    public Statement getThen() {
        return then;
    }

    public void setThen(Statement then) {
        this.then = then;
    }

    public Statement getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(Statement otherwise) {
        this.otherwise = otherwise;
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

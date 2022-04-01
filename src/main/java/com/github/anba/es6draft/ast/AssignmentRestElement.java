/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

/**
 * <h1>12 ECMAScript Language: Expressions</h1><br>
 * <h2>12.14 Assignment Operators</h2>
 * <ul>
 * <li>12.14.5 Destructuring Assignment
 * </ul>
 */
public final class AssignmentRestElement extends AstNode implements AssignmentElementItem {
    private final LeftHandSideExpression target;

    public AssignmentRestElement(long beginPosition, long endPosition, LeftHandSideExpression target) {
        super(beginPosition, endPosition);
        this.target = target;
    }

    /**
     * Returns the left-hand side expression target.
     * 
     * @return the target expression
     */
    public LeftHandSideExpression getTarget() {
        return target;
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

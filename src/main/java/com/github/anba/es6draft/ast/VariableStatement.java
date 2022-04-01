/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

import java.util.List;

/**
 * <h1>13 ECMAScript Language: Statements and Declarations</h1><br>
 * <h2>13.3 Declarations and the Variable Statement</h2>
 * <ul>
 * <li>13.3.2 Variable Statement
 * </ul>
 */
public final class VariableStatement extends Statement {
    private final List<VariableDeclaration> elements;

    public VariableStatement(long beginPosition, long endPosition, List<VariableDeclaration> elements) {
        super(beginPosition, endPosition);
        this.elements = elements;
    }

    /**
     * Returns the list of variable declaration elements.
     * 
     * @return the variable declarations
     */
    public List<VariableDeclaration> getElements() {
        return elements;
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

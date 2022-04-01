/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

import java.util.EnumSet;
import java.util.Set;

/**
 * <h1>13 ECMAScript Language: Statements and Declarations</h1>
 * <ul>
 * <li>13.7 Iteration Statements
 * </ul>
 */
public abstract class IterationStatement extends BreakableStatement {
    protected IterationStatement(long beginPosition, long endPosition, EnumSet<Abrupt> abrupt, Set<String> labelSet) {
        super(beginPosition, endPosition, abrupt, labelSet);
    }

    /**
     * Returns <code>true</code> if this node is the target of a <code>ContinueStatement</code>.
     * 
     * @return <code>true</code> if this node is the target of a <code>ContinueStatement</code>
     */
    public final boolean hasContinue() {
        return getAbrupt().contains(Abrupt.Continue);
    }

    /**
     * Returns this {@code IterationStatement}'s statement node.
     * 
     * @return the statement node
     */
    public abstract Statement getStatement();

    /**
     * Sets this {@code IterationStatement}'s statement node.
     * 
     * @param statement
     *            the new statement node
     */
    public abstract void setStatement(Statement statement);
}

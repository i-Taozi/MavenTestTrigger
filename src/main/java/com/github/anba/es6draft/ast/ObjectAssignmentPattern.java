/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

import java.util.List;

/**
 * <h1>12 ECMAScript Language: Expressions</h1><br>
 * <h2>12.14 Assignment Operators</h2>
 * <ul>
 * <li>12.14.5 Destructuring Assignment
 * </ul>
 */
public final class ObjectAssignmentPattern extends AssignmentPattern {
    private final List<AssignmentProperty> properties;
    private final AssignmentRestProperty rest;

    public ObjectAssignmentPattern(long beginPosition, long endPosition, List<AssignmentProperty> properties,
            AssignmentRestProperty rest) {
        super(beginPosition, endPosition);
        this.properties = properties;
        this.rest = rest;
    }

    /**
     * Returns the assignment properties of this object assignment pattern.
     * 
     * @return the assignment properties
     */
    public List<AssignmentProperty> getProperties() {
        return properties;
    }

    /**
     * Returns the optional assignment rest property.
     * 
     * @return the assignment rest property or {@code null}
     */
    public AssignmentRestProperty getRest() {
        return rest;
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

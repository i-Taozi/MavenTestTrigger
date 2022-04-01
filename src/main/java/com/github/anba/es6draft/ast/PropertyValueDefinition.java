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
public final class PropertyValueDefinition extends PropertyDefinition {
    private final PropertyName propertyName;
    private final Expression propertyValue;

    public PropertyValueDefinition(long beginPosition, long endPosition, PropertyName propertyName,
            Expression propertyValue) {
        super(beginPosition, endPosition);
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Override
    public PropertyName getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the property value expression.
     * 
     * @return the property value
     */
    public Expression getPropertyValue() {
        return propertyValue;
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

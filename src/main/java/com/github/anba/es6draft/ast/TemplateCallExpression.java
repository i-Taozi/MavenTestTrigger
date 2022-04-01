/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

/**
 * <h1>12 ECMAScript Language: Expressions</h1><br>
 * <h2>12.3 Left-Hand-Side Expressions</h2>
 * <ul>
 * <li>12.3.7 Tagged Templates
 * </ul>
 */
public final class TemplateCallExpression extends Expression {
    private final Expression base;
    private final TemplateLiteral template;

    public TemplateCallExpression(long beginPosition, long endPosition, Expression base, TemplateLiteral template) {
        super(beginPosition, endPosition);
        this.base = base;
        this.template = template;
    }

    /**
     * Returns the template call's base expression.
     * 
     * @return the callee expression
     */
    public Expression getBase() {
        return base;
    }

    /**
     * Returns the tagged template literal.
     * 
     * @return the template literal
     */
    public TemplateLiteral getTemplate() {
        return template;
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

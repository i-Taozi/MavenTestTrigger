/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler.assembler;

/**
 * Label class for jump, goto, and switch instructions.
 */
public class Jump {
    private final org.objectweb.asm.Label label;
    private boolean resolved;
    private boolean target;
    private Type[] stack;

    public Jump() {
        this.label = new org.objectweb.asm.Label();
    }

    final void setStack(Type[] stack) {
        this.stack = stack;
    }

    final org.objectweb.asm.Label label() {
        assert !resolved;
        resolved = true;
        return label;
    }

    final org.objectweb.asm.Label target() {
        target = true;
        return label;
    }

    final Type[] stack() {
        return stack;
    }

    /**
     * Returns {@code true} if this label is resolved, otherwise {@code false}.
     * 
     * @return {@code true} if this label is resolved
     */
    public final boolean isResolved() {
        return resolved;
    }

    /**
     * Returns {@code true} if the label is the target of a jump instruction.
     * 
     * @return {@code true} if the label is a target label
     */
    public final boolean isTarget() {
        return target;
    }
}

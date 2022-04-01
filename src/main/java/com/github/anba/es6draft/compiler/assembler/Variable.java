/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler.assembler;

/**
 * Object to represent a parameter or local variable.
 * 
 * @param <T>
 *            the variable type
 */
public final class Variable<T> implements MutableValue<T> {
    private final String name;
    private final Type type;
    private final int slot;
    private boolean alive = true;

    Variable(String name, Type type, int slot) {
        this.name = name;
        this.type = type;
        this.slot = slot;
    }

    /**
     * Returns the variable name.
     * 
     * @return the variable name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the variable type.
     * 
     * @return the variable type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the bytecode index slot.
     * 
     * @return the bytecode slot
     */
    int getSlot() {
        return slot < 0 ? -slot : slot;
    }

    /**
     * Returns {@code true} if a slot is required for this variable.
     * 
     * @return {@code true} if a slot is required
     */
    boolean hasSlot() {
        return slot >= 0;
    }

    /**
     * Marks this variable as dead.
     */
    void free() {
        alive = false;
    }

    /**
     * Returns {@code true} if the variable is in scope.
     * 
     * @return {@code true} if the variable is in scope
     */
    boolean isAlive() {
        return alive;
    }

    /**
     * Performs an unchecked type cast.
     * 
     * @param <U>
     *            the target type
     * @return this variable
     */
    @SuppressWarnings("unchecked")
    public <U extends T> Variable<U> uncheckedCast() {
        return (Variable<U>) this;
    }

    @Override
    public void load(InstructionAssembler assembler) {
        assembler.load(this);
    }

    @Override
    public void store(InstructionAssembler assembler) {
        assembler.store(this);
    }

    @Override
    public String toString() {
        return String.format("Variable<name=%s, type=%s, slot=%d, alive=%b>", name, type, slot, alive);
    }
}

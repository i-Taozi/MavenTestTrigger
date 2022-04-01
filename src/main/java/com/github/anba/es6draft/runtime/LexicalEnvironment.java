/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime;

import static com.github.anba.es6draft.runtime.internal.Errors.newReferenceError;

import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.Reference;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.builtins.FunctionObject;

/**
 * <h1>8 Executable Code and Execution Contexts</h1><br>
 * <h2>8.1 Lexical Environments</h2>
 * <ul>
 * <li>8.1.2 Lexical Environment Operations
 * </ul>
 */
public final class LexicalEnvironment<RECORD extends EnvironmentRecord> {
    private final ExecutionContext cx;
    private final LexicalEnvironment<?> outer;
    private final RECORD envRec;

    private LexicalEnvironment(ExecutionContext cx, RECORD envRec) {
        assert envRec instanceof GlobalEnvironmentRecord;
        this.cx = cx;
        this.outer = null;
        this.envRec = envRec;
    }

    /**
     * Creates a new lexical environment.
     * 
     * @param outer
     *            the outer lexical environment
     * @param envRec
     *            the environment record
     */
    public LexicalEnvironment(LexicalEnvironment<?> outer, RECORD envRec) {
        this.cx = outer.cx;
        this.outer = outer;
        this.envRec = envRec;
    }

    @Override
    public String toString() {
        return String.format("%s: {envRec=%s}", getClass().getSimpleName(), envRec);
    }

    public RECORD getEnvRec() {
        return envRec;
    }

    public LexicalEnvironment<?> getOuter() {
        return outer;
    }

    /**
     * Returns a clone of the declarative {@link LexicalEnvironment}.
     * <p>
     * [Called from generated code]
     * 
     * @param e
     *            the source lexical environment
     * @return the cloned lexical environment
     */
    public static LexicalEnvironment<DeclarativeEnvironmentRecord> cloneDeclarativeEnvironment(
            LexicalEnvironment<DeclarativeEnvironmentRecord> e) {
        DeclarativeEnvironmentRecord envRec = new DeclarativeEnvironmentRecord(e.envRec);
        return new LexicalEnvironment<>(e.outer, envRec);
    }

    /**
     * Retrieves the binding value of the first {@link EnvironmentRecord} which has a binding for {@code name}. If no
     * such binding exists a ReferenceError is thrown.
     * 
     * @param lex
     *            the lexical environment
     * @param name
     *            the identifier name
     * @param strict
     *            the strict mode flag
     * @return the resolved identifier value
     */
    static Object getIdentifierValueOrThrow(LexicalEnvironment<?> lex, String name, boolean strict) {
        for (LexicalEnvironment<?> env = lex; env != null; env = env.outer) {
            Object value = env.envRec.getBindingValueOrNull(name, strict);
            if (value != null) {
                return value;
            }
        }
        throw newReferenceError(lex.cx, Messages.Key.UnresolvableReference, name);
    }

    /**
     * 8.1.2.1 GetIdentifierReference (lex, name, strict)
     * 
     * @param lex
     *            the lexical environment
     * @param name
     *            the identifier name
     * @param strict
     *            the strict mode flag
     * @return the resolved identifier reference
     */
    public static Reference<?, String> getIdentifierReference(LexicalEnvironment<?> lex, String name, boolean strict) {
        /* steps 2-5 */
        for (LexicalEnvironment<?> env = lex; env != null; env = env.outer) {
            Reference<? extends EnvironmentRecord, String> ref = env.envRec.getReferenceOrNull(name, strict);
            if (ref != null) {
                return ref;
            }
        }
        /* step 1 */
        return new Reference.UnresolvableReference(name, strict);
    }

    /**
     * 8.1.2.2 NewDeclarativeEnvironment (E)
     * 
     * @param e
     *            the outer lexical environment
     * @return the new declarative environment
     */
    public static LexicalEnvironment<DeclarativeEnvironmentRecord> newDeclarativeEnvironment(LexicalEnvironment<?> e) {
        /* step 2 */
        DeclarativeEnvironmentRecord envRec = new DeclarativeEnvironmentRecord(e.cx, false);
        /* steps 1, 3-4 */
        LexicalEnvironment<DeclarativeEnvironmentRecord> env = new LexicalEnvironment<>(e, envRec);
        /* step 5 */
        return env;
    }

    /**
     * 8.1.2.2 NewDeclarativeEnvironment (E)
     * 
     * @param e
     *            the outer lexical environment
     * @return the new declarative environment
     */
    public static LexicalEnvironment<DeclarativeEnvironmentRecord> newCatchDeclarativeEnvironment(
            LexicalEnvironment<?> e) {
        /* step 2 */
        DeclarativeEnvironmentRecord envRec = new DeclarativeEnvironmentRecord(e.cx, true);
        /* steps 1, 3-4 */
        LexicalEnvironment<DeclarativeEnvironmentRecord> env = new LexicalEnvironment<>(e, envRec);
        /* step 5 */
        return env;
    }

    /**
     * 8.1.2.3 NewObjectEnvironment (O, E)
     * 
     * @param o
     *            the script object
     * @param e
     *            the outer lexical environment
     * @return the new object environment
     */
    public static LexicalEnvironment<ObjectEnvironmentRecord> newObjectEnvironment(ScriptObject o,
            LexicalEnvironment<?> e) {
        return newObjectEnvironment(o, e, false);
    }

    /**
     * 8.1.2.3 NewObjectEnvironment (O, E)
     * 
     * @param o
     *            the script object
     * @param e
     *            the outer lexical environment
     * @param withEnvironment
     *            the withEnvironment flag
     * @return the new object environment
     */
    public static LexicalEnvironment<ObjectEnvironmentRecord> newObjectEnvironment(ScriptObject o,
            LexicalEnvironment<?> e, boolean withEnvironment) {
        /* step 2 */
        ObjectEnvironmentRecord envRec = new ObjectEnvironmentRecord(e.cx, o, withEnvironment);
        /* steps 1, 3-4 */
        LexicalEnvironment<ObjectEnvironmentRecord> env = new LexicalEnvironment<>(e, envRec);
        /* step 5 */
        return env;
    }

    /**
     * 8.1.2.4 NewFunctionEnvironment ( F, newTarget )
     * 
     * @param f
     *            the function object
     * @param newTarget
     *            the newTarget constructor object or {@code null}
     * @param thisValue
     *            the function this-binding or {@code null}
     * @return the new function environment
     */
    public static LexicalEnvironment<FunctionEnvironmentRecord> newFunctionEnvironment(FunctionObject f,
            Constructor newTarget, Object thisValue) {
        /* steps 1-2 (not applicable) */
        LexicalEnvironment<?> e = f.getEnvironment();
        /* steps 4-10 */
        FunctionEnvironmentRecord envRec = new FunctionEnvironmentRecord(e.cx, f, newTarget, thisValue);
        /* steps 3, 11-12 */
        LexicalEnvironment<FunctionEnvironmentRecord> env = new LexicalEnvironment<>(e, envRec);
        /* step 13 */
        return env;
    }

    /**
     * 8.1.2.4 NewFunctionEnvironment ( F, newTarget )
     * 
     * @param f
     *            the function object
     * @param newTarget
     *            the newTarget constructor object
     * @return the new function environment
     */
    public static LexicalEnvironment<FunctionEnvironmentRecord> newFunctionEnvironment(FunctionObject f,
            Constructor newTarget) {
        /* steps 1-2 (not applicable) */
        LexicalEnvironment<?> e = f.getEnvironment();
        /* steps 4-10 */
        FunctionEnvironmentRecord envRec = new FunctionEnvironmentRecord(e.cx, f, newTarget);
        /* steps 3, 11-12 */
        LexicalEnvironment<FunctionEnvironmentRecord> env = new LexicalEnvironment<>(e, envRec);
        /* step 13 */
        return env;
    }

    /**
     * 8.1.2.5 NewGlobalEnvironment ( G, thisValue )
     * 
     * @param cx
     *            the default execution context
     * @param g
     *            the global object
     * @param thisValue
     *            the global this value
     * @return the new global environment
     */
    public static LexicalEnvironment<GlobalEnvironmentRecord> newGlobalEnvironment(ExecutionContext cx, ScriptObject g,
            ScriptObject thisValue) {
        /* steps 2-8 */
        GlobalEnvironmentRecord globalRec = new GlobalEnvironmentRecord(cx, g, thisValue);
        /* steps 1, 9-10 */
        LexicalEnvironment<GlobalEnvironmentRecord> env = new LexicalEnvironment<>(cx, globalRec);
        /* step 11 */
        return env;
    }

    /**
     * 8.1.2.6 NewModuleEnvironment (E)
     * 
     * @param e
     *            the outer lexical environment
     * @return the new module environment
     */
    public static LexicalEnvironment<ModuleEnvironmentRecord> newModuleEnvironment(LexicalEnvironment<?> e) {
        /* step 2 */
        ModuleEnvironmentRecord envRec = new ModuleEnvironmentRecord(e.cx);
        /* steps 1, 3-4 */
        LexicalEnvironment<ModuleEnvironmentRecord> env = new LexicalEnvironment<>(e, envRec);
        /* step 5 */
        return env;
    }
}

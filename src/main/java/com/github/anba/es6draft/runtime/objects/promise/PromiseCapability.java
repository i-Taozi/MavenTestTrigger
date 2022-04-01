/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects.promise;

import static com.github.anba.es6draft.runtime.types.Undefined.UNDEFINED;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.internal.ScriptException;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.ScriptObject;

/**
 * <h1>25 Control Abstraction Objects</h1><br>
 * <h2>25.4 Promise Objects</h2><br>
 * <h3>25.4.1 Promise Abstract Operations</h3>
 * <ul>
 * <li>25.4.1.1 PromiseCapability Records
 * </ul>
 */
public final class PromiseCapability<PROMISE extends ScriptObject> {
    /** [[Promise]] */
    private final PROMISE promise;

    /** [[Resolve]] */
    private final Callable resolve;

    /** [[Reject]] */
    private final Callable reject;

    /**
     * Creates a new PromiseCapability record.
     * 
     * @param promise
     *            the promise object
     * @param resolve
     *            the resolve function
     * @param reject
     *            the reject function
     */
    public PromiseCapability(PROMISE promise, Callable resolve, Callable reject) {
        assert promise != null && resolve != null && reject != null;
        this.promise = promise;
        this.resolve = resolve;
        this.reject = reject;
    }

    /**
     * Returns the [[Promise]] field of this PromiseCapability record.
     * 
     * @return the promise object
     */
    public PROMISE getPromise() {
        return promise;
    }

    /**
     * Returns the [[Resolve]] field of this PromiseCapability record.
     * 
     * @return the resolve function
     */
    public Callable getResolve() {
        return resolve;
    }

    /**
     * Returns the [[Reject]] field of this PromiseCapability record.
     * 
     * @return the reject function
     */
    public Callable getReject() {
        return reject;
    }

    /**
     * 25.4.1.1.1 IfAbruptRejectPromise (value, capability)
     * 
     * @param <PROMISE>
     *            the promise type
     * @param cx
     *            the execution context
     * @param e
     *            the script exception
     * @param capability
     *            the promise capability record
     * @return the promise capability
     */
    public static <PROMISE extends ScriptObject> PROMISE IfAbruptRejectPromise(ExecutionContext cx, ScriptException e,
            PromiseCapability<PROMISE> capability) {
        /* step 1.a */
        capability.getReject().call(cx, UNDEFINED, e.getValue());
        /* step 1.b */
        return capability.getPromise();
    }
}

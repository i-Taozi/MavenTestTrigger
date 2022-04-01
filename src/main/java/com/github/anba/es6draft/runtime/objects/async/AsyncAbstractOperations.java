/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects.async;

import static com.github.anba.es6draft.runtime.objects.promise.PromiseAbstractOperations.PromiseBuiltinCapability;
import static com.github.anba.es6draft.runtime.objects.promise.PromisePrototype.PerformPromiseThen;
import static com.github.anba.es6draft.runtime.types.Undefined.UNDEFINED;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.RuntimeInfo;
import com.github.anba.es6draft.runtime.objects.promise.PromiseCapability;
import com.github.anba.es6draft.runtime.objects.promise.PromiseObject;
import com.github.anba.es6draft.runtime.types.Undefined;
import com.github.anba.es6draft.runtime.types.builtins.BuiltinFunction;

/**
 * <h1>Async Functions</h1>
 * <ul>
 * <li>Abstract Operations
 * </ul>
 */
public final class AsyncAbstractOperations {
    private AsyncAbstractOperations() {
    }

    /**
     * 2.2 AsyncFunctionStart(promiseCapability, asyncFunctionBody)
     * 
     * @param cx
     *            the execution context
     * @param promiseCapability
     *            the promise capability
     * @param asyncFunctionBody
     *            the function body
     */
    public static void AsyncFunctionStart(ExecutionContext cx, PromiseCapability<PromiseObject> promiseCapability,
            RuntimeInfo.Function asyncFunctionBody) {
        /* steps 1-7 */
        AsyncObject asyncObject = new AsyncObject(promiseCapability);
        asyncObject.start(cx, asyncFunctionBody);
    }

    /**
     * 2.3 AsyncFunctionAwait(value)
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the await value
     */
    public static void AsyncFunctionAwait(ExecutionContext cx, Object value) {
        /* step 1 */
        Async asyncObject = cx.getCurrentAsync();
        assert asyncObject != null;
        /* step 2 */
        PromiseCapability<PromiseObject> promiseCapability = PromiseBuiltinCapability(cx);
        /* step 3 */
        promiseCapability.getResolve().call(cx, UNDEFINED, value);
        /* steps 4, 6 */
        AwaitedFulfilled onFulfilled = new AwaitedFulfilled(cx.getRealm(), asyncObject);
        /* steps 5, 7 */
        AwaitedRejected onRejected = new AwaitedRejected(cx.getRealm(), asyncObject);
        /* step 8 */
        PromiseCapability<PromiseObject> throwawayCapability = PromiseBuiltinCapability(cx);
        /* step 9 */
        // TODO: [[PromiseIsHandled]]?
        /* step 10 */
        PerformPromiseThen(cx, promiseCapability.getPromise(), onFulfilled, onRejected, throwawayCapability);
        /* steps 11-13 (implemented in generated code) */
    }

    /**
     * 2.4 AsyncFunction Awaited Fulfilled
     */
    public static final class AwaitedFulfilled extends BuiltinFunction {
        private final Async asyncObject;

        public AwaitedFulfilled(Realm realm, Async asyncObject) {
            super(realm, ANONYMOUS, 1);
            this.asyncObject = asyncObject;
            createDefaultFunctionProperties();
        }

        @Override
        public Undefined call(ExecutionContext callerContext, Object thisValue, Object... args) {
            ExecutionContext calleeContext = calleeContext();
            Object value = argument(args, 0);
            /* steps 1-7 */
            asyncObject.resume(calleeContext, value);
            return UNDEFINED;
        }
    }

    /**
     * 2.5 AsyncFunction Awaited Rejected
     */
    public static final class AwaitedRejected extends BuiltinFunction {
        private final Async asyncObject;

        public AwaitedRejected(Realm realm, Async asyncObject) {
            super(realm, ANONYMOUS, 1);
            this.asyncObject = asyncObject;
            createDefaultFunctionProperties();
        }

        @Override
        public Undefined call(ExecutionContext callerContext, Object thisValue, Object... args) {
            ExecutionContext calleeContext = calleeContext();
            Object reason = argument(args, 0);
            /* steps 1-7 */
            asyncObject._throw(calleeContext, reason);
            return UNDEFINED;
        }
    }
}

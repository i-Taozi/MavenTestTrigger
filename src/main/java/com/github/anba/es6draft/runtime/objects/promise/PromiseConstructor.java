/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects.promise;

import static com.github.anba.es6draft.runtime.AbstractOperations.*;
import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
import static com.github.anba.es6draft.runtime.objects.promise.PromiseAbstractOperations.CreateResolvingFunctions;
import static com.github.anba.es6draft.runtime.objects.promise.PromiseAbstractOperations.GetPromiseAllocator;
import static com.github.anba.es6draft.runtime.objects.promise.PromiseAbstractOperations.IsPromise;
import static com.github.anba.es6draft.runtime.objects.promise.PromiseAbstractOperations.NewPromiseCapability;
import static com.github.anba.es6draft.runtime.objects.promise.PromiseCapability.IfAbruptRejectPromise;
import static com.github.anba.es6draft.runtime.types.Undefined.UNDEFINED;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.CompatibilityOption;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.Properties.Accessor;
import com.github.anba.es6draft.runtime.internal.Properties.Attributes;
import com.github.anba.es6draft.runtime.internal.Properties.CompatibilityExtension;
import com.github.anba.es6draft.runtime.internal.Properties.Function;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.internal.ScriptException;
import com.github.anba.es6draft.runtime.internal.ScriptIterator;
import com.github.anba.es6draft.runtime.objects.promise.PromiseAbstractOperations.ResolvingFunctions;
import com.github.anba.es6draft.runtime.types.BuiltinSymbol;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.Type;
import com.github.anba.es6draft.runtime.types.builtins.ArrayObject;
import com.github.anba.es6draft.runtime.types.builtins.BuiltinConstructor;
import com.github.anba.es6draft.runtime.types.builtins.BuiltinFunction;

/**
 * <h1>25 Control Abstraction Objects</h1><br>
 * <h2>25.4 Promise Objects</h2>
 * <ul>
 * <li>25.4.3 The Promise Constructor
 * <li>25.4.4 Properties of the Promise Constructor
 * </ul>
 */
public final class PromiseConstructor extends BuiltinConstructor implements Initializable {
    /**
     * Constructs a new Promise constructor function.
     * 
     * @param realm
     *            the realm object
     */
    public PromiseConstructor(Realm realm) {
        super(realm, "Promise", 1);
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, Properties.class);
        createProperties(realm, this, TryProperty.class);
    }

    /**
     * 25.4.3.1 Promise ( executor )
     */
    @Override
    public Object call(ExecutionContext callerContext, Object thisValue, Object... args) {
        /* step 1 */
        throw newTypeError(calleeContext(), Messages.Key.InvalidCall, "Promise");
    }

    /**
     * 25.4.3.1 Promise ( executor )
     */
    @Override
    public PromiseObject construct(ExecutionContext callerContext, Constructor newTarget, Object... args) {
        ExecutionContext calleeContext = calleeContext();
        Object executor = argument(args, 0);

        /* step 1 (not applicable) */
        /* step 2 */
        if (!IsCallable(executor)) {
            throw newTypeError(calleeContext, Messages.Key.NotCallable);
        }
        /* steps 3-7 */
        PromiseObject promise = OrdinaryCreateFromConstructor(calleeContext, newTarget, Intrinsics.PromisePrototype,
                GetPromiseAllocator(calleeContext.getRealm()));
        /* step 8 */
        ResolvingFunctions resolvingFunctions = CreateResolvingFunctions(calleeContext, promise);
        /* steps 9-10 */
        try {
            /* step 9 */
            ((Callable) executor).call(calleeContext, UNDEFINED, resolvingFunctions.getResolve(),
                    resolvingFunctions.getReject());
        } catch (ScriptException e) {
            /* step 10 */
            resolvingFunctions.getReject().call(calleeContext, UNDEFINED, e.getValue());
        }
        /* step 11 */
        return promise;
    }

    /**
     * 25.4.4 Properties of the Promise Constructor
     */
    public enum Properties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final int length = 1;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final String name = "Promise";

        /**
         * 25.4.4.2 Promise.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static final Intrinsics prototype = Intrinsics.PromisePrototype;

        /**
         * 25.4.4.1 Promise.all ( iterable )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param iterable
         *            the iterable
         * @return the promise object
         */
        @Function(name = "all", arity = 1)
        public static Object all(ExecutionContext cx, Object thisValue, Object iterable) {
            /* steps 1-2 */
            if (!Type.isObject(thisValue)) {
                throw newTypeError(cx, Messages.Key.NotObjectType);
            }
            ScriptObject c = Type.objectValue(thisValue);
            /* step 3 */
            PromiseCapability<?> promiseCapability = NewPromiseCapability(cx, c);
            /* steps 4-5 */
            ScriptIterator<?> iterator;
            try {
                /* step 4 */
                iterator = GetIterator(cx, iterable);
            } catch (ScriptException e) {
                /* step 5 */
                return IfAbruptRejectPromise(cx, e, promiseCapability);
            }
            /* steps 6-9 */
            try {
                return PerformPromiseAll(cx, iterator, c, promiseCapability);
            } catch (ScriptException e) {
                /* step 8 */
                try {
                    iterator.close(e);
                } catch (ScriptException inner) {
                    return IfAbruptRejectPromise(cx, inner, promiseCapability);
                }
                return IfAbruptRejectPromise(cx, e, promiseCapability);
            }
        }

        /**
         * 25.4.4.3 Promise.race ( iterable )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param iterable
         *            the iterable
         * @return the promise object
         */
        @Function(name = "race", arity = 1)
        public static Object race(ExecutionContext cx, Object thisValue, Object iterable) {
            /* steps 1-2 */
            if (!Type.isObject(thisValue)) {
                throw newTypeError(cx, Messages.Key.NotObjectType);
            }
            ScriptObject c = Type.objectValue(thisValue);
            /* step 3 */
            PromiseCapability<?> promiseCapability = NewPromiseCapability(cx, c);
            /* steps 4-5 */
            ScriptIterator<?> iterator;
            try {
                /* step 4 */
                iterator = GetIterator(cx, iterable);
            } catch (ScriptException e) {
                /* step 5 */
                return IfAbruptRejectPromise(cx, e, promiseCapability);
            }
            /* steps 6-9 */
            try {
                return PerformPromiseRace(cx, iterator, c, promiseCapability);
            } catch (ScriptException e) {
                /* step 8 */
                try {
                    iterator.close(e);
                } catch (ScriptException inner) {
                    return IfAbruptRejectPromise(cx, inner, promiseCapability);
                }
                return IfAbruptRejectPromise(cx, e, promiseCapability);
            }
        }

        /**
         * 25.4.4.4 Promise.reject ( r )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param r
         *            the rejected value
         * @return the new promise object
         */
        @Function(name = "reject", arity = 1)
        public static Object reject(ExecutionContext cx, Object thisValue, Object r) {
            /* steps 1-2 */
            if (!Type.isObject(thisValue)) {
                throw newTypeError(cx, Messages.Key.NotObjectType);
            }
            /* step 3 */
            PromiseCapability<?> promiseCapability = NewPromiseCapability(cx, Type.objectValue(thisValue));
            /* step 4 */
            promiseCapability.getReject().call(cx, UNDEFINED, r);
            /* step 5 */
            return promiseCapability.getPromise();
        }

        /**
         * 25.4.4.5 Promise.resolve ( x )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param x
         *            the resolved value
         * @return the new promise object
         */
        @Function(name = "resolve", arity = 1)
        public static Object resolve(ExecutionContext cx, Object thisValue, Object x) {
            /* steps 1-2 */
            if (!Type.isObject(thisValue)) {
                throw newTypeError(cx, Messages.Key.NotObjectType);
            }
            /* step 3 */
            if (IsPromise(x)) {
                Object constructor = Get(cx, (PromiseObject) x, "constructor");
                if (constructor == thisValue) { // SameValue
                    return x;
                }
            }
            /* step 4 */
            PromiseCapability<?> promiseCapability = NewPromiseCapability(cx, Type.objectValue(thisValue));
            /* step 5 */
            promiseCapability.getResolve().call(cx, UNDEFINED, x);
            /* step 6 */
            return promiseCapability.getPromise();
        }

        /**
         * 25.4.4.6 get Promise [ @@species ]
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the species object
         */
        @Accessor(name = "get [Symbol.species]", symbol = BuiltinSymbol.species, type = Accessor.Type.Getter)
        public static Object species(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            return thisValue;
        }
    }

    /**
     * Properties of the Promise Constructor
     */
    @CompatibilityExtension(CompatibilityOption.PromiseTry)
    public enum TryProperty {
        ;

        /**
         * Promise.try ( callbackfn )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param callbackfn
         *            the callback function
         * @return the new promise object
         */
        @Function(name = "try", arity = 1)
        public static Object _try(ExecutionContext cx, Object thisValue, Object callbackfn) {
            /* step 2 */
            if (!Type.isObject(thisValue)) {
                throw newTypeError(cx, Messages.Key.NotObjectType);
            }
            /* step 1 */
            ScriptObject constructor = Type.objectValue(thisValue);
            /* step 3 */
            PromiseCapability<ScriptObject> promiseCapability = NewPromiseCapability(cx, constructor);
            /* steps 4-7 */
            Object value;
            try {
                value = Call(cx, callbackfn, UNDEFINED);
            } catch (ScriptException e) {
                /* step 6 */
                // FIXME: spec bug - cannot assert non-fallible
                promiseCapability.getReject().call(cx, UNDEFINED, e.getValue());
                /* step 8 */
                return promiseCapability.getPromise();
            }
            /* step 7 */
            // FIXME: spec bug - cannot assert non-fallible
            promiseCapability.getResolve().call(cx, UNDEFINED, value);
            /* step 8 */
            return promiseCapability.getPromise();
        }
    }

    /**
     * 25.4.4.1.1 Runtime Semantics: PerformPromiseAll( iteratorRecord, constructor, resultCapability)
     * 
     * @param <PROMISE>
     *            the promise type
     * @param cx
     *            the execution context
     * @param iterator
     *            the iterator object
     * @param constructor
     *            the constructor object
     * @param resultCapability
     *            the new promise capability record
     * @return the new promise object
     */
    public static <PROMISE extends ScriptObject> PROMISE PerformPromiseAll(ExecutionContext cx,
            ScriptIterator<?> iterator, ScriptObject constructor, PromiseCapability<PROMISE> resultCapability) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        ArrayList<Object> values = new ArrayList<>();
        /* step 4 */
        AtomicInteger remainingElementsCount = new AtomicInteger(1);
        /* step 5 */
        int index = 0;
        /* step 6 */
        while (iterator.hasNext()) {
            /* steps 6.a-c, 6.e-g */
            Object nextValue = iterator.next();
            /* step 6.h */
            // Using 'null' instead of undefined to be able to verify that no values are overwritten.
            values.add(null);
            /* step 6.i */
            Object nextPromise = Invoke(cx, constructor, "resolve", nextValue);
            /* steps 6.j-o */
            PromiseAllResolveElementFunction resolveElement = new PromiseAllResolveElementFunction(cx.getRealm(), false,
                    index, values, resultCapability, remainingElementsCount);
            /* step 6.p */
            remainingElementsCount.incrementAndGet();
            /* step 6.q */
            Invoke(cx, nextPromise, "then", resolveElement, resultCapability.getReject());
            /* step 6.r */
            index += 1;
        }
        /* step 6.d */
        if (remainingElementsCount.decrementAndGet() == 0) {
            ArrayObject valuesArray = CreateArrayFromList(cx, values);
            resultCapability.getResolve().call(cx, UNDEFINED, valuesArray);
        }
        return resultCapability.getPromise();
    }

    /**
     * 25.4.4.1.2 Promise.all Resolve Element Functions
     */
    public static final class PromiseAllResolveElementFunction extends BuiltinFunction {
        /** [[AlreadyCalled]] */
        private boolean alreadyCalled;

        /** [[Index]] */
        private final int index;

        /** [[Values]] */
        private final ArrayList<Object> values;

        /** [[Capabilities]] */
        private final PromiseCapability<?> capabilities;

        /** [[RemainingElements]] */
        private final AtomicInteger remainingElements;

        public PromiseAllResolveElementFunction(Realm realm, boolean alreadyCalled, int index, ArrayList<Object> values,
                PromiseCapability<?> capabilities, AtomicInteger remainingElements) {
            super(realm, ANONYMOUS, 1);
            this.alreadyCalled = alreadyCalled;
            this.index = index;
            this.values = values;
            this.capabilities = capabilities;
            this.remainingElements = remainingElements;
            createDefaultFunctionProperties();
        }

        @Override
        public Object call(ExecutionContext callerContext, Object thisValue, Object... args) {
            ExecutionContext calleeContext = calleeContext();
            Object x = argument(args, 0);
            /* steps 1-3 */
            if (alreadyCalled) {
                return UNDEFINED;
            }
            alreadyCalled = true;
            /* step 4 */
            int index = this.index;
            /* step 5 */
            ArrayList<Object> values = this.values;
            /* step 6 */
            PromiseCapability<?> promiseCapability = this.capabilities;
            /* step 7 */
            AtomicInteger remainingElementsCount = this.remainingElements;
            /* step 8 */
            assert values.get(index) == null : String.format("values[%d] = %s", index, values.get(index));
            values.set(index, x);
            /* steps 9-10 */
            if (remainingElementsCount.decrementAndGet() == 0) {
                ArrayObject valuesArray = CreateArrayFromList(calleeContext, values);
                return promiseCapability.getResolve().call(calleeContext, UNDEFINED, valuesArray);
            }
            /* step 11 */
            return UNDEFINED;
        }
    }

    /**
     * 25.4.4.3.1 Runtime Semantics: PerformPromiseRace ( iteratorRecord, constructor, resultCapability )
     * 
     * @param <PROMISE>
     *            the promise type
     * @param cx
     *            the execution context
     * @param iterator
     *            the iterator object
     * @param constructor
     *            the constructor object
     * @param resultCapability
     *            the new promise capability record
     * @return the new promise object
     */
    public static <PROMISE extends ScriptObject> PROMISE PerformPromiseRace(ExecutionContext cx,
            ScriptIterator<?> iterator, ScriptObject constructor, PromiseCapability<PROMISE> resultCapability) {
        /* steps 1-2 (implicit) */
        /* step 3 */
        while (iterator.hasNext()) {
            /* steps 3.a-c, 3.e-g */
            Object nextValue = iterator.next();
            /* step 3.h */
            Object nextPromise = Invoke(cx, constructor, "resolve", nextValue);
            /* step 3.i */
            Invoke(cx, nextPromise, "then", resultCapability.getResolve(), resultCapability.getReject());
        }
        /* step 3.d */
        return resultCapability.getPromise();
    }
}

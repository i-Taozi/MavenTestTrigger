/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects;

import static com.github.anba.es6draft.runtime.AbstractOperations.*;
import static com.github.anba.es6draft.runtime.internal.Errors.newRangeError;
import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
import static com.github.anba.es6draft.runtime.types.builtins.ArrayObject.ArrayCreate;
import static com.github.anba.es6draft.runtime.types.builtins.ArrayObject.DenseArrayCreate;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.Properties.Accessor;
import com.github.anba.es6draft.runtime.internal.Properties.Attributes;
import com.github.anba.es6draft.runtime.internal.Properties.Function;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.internal.ScriptException;
import com.github.anba.es6draft.runtime.internal.ScriptIterator;
import com.github.anba.es6draft.runtime.types.BuiltinSymbol;
import com.github.anba.es6draft.runtime.types.Callable;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.Type;
import com.github.anba.es6draft.runtime.types.builtins.ArrayObject;
import com.github.anba.es6draft.runtime.types.builtins.BuiltinConstructor;

/**
 * <h1>22 Indexed Collections</h1><br>
 * <h2>22.1 Array Objects</h2>
 * <ul>
 * <li>22.1.1 The Array Constructor
 * <li>22.1.2 Properties of the Array Constructor
 * </ul>
 */
public final class ArrayConstructor extends BuiltinConstructor implements Initializable {
    /**
     * Constructs a new Array constructor function.
     * 
     * @param realm
     *            the realm object
     */
    public ArrayConstructor(Realm realm) {
        super(realm, "Array", 1);
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, Properties.class);
    }

    /**
     * 22.1.1.1 Array ( )<br>
     * 22.1.1.2 Array (len)<br>
     * 22.1.1.3 Array (...items )
     */
    @Override
    public ArrayObject call(ExecutionContext callerContext, Object thisValue, Object... args) {
        /* steps 1-6/1-11/1-12 */
        return construct(callerContext, this, args);
    }

    /**
     * 22.1.1.1 Array ( )<br>
     * 22.1.1.2 Array (len)<br>
     * 22.1.1.3 Array (...items )
     */
    @Override
    public ArrayObject construct(ExecutionContext callerContext, Constructor newTarget, Object... args) {
        ExecutionContext calleeContext = calleeContext();
        /* step 1 */
        int numberOfArgs = args.length;
        /* steps 2-3 (not applicable) */
        /* step 4 */
        ScriptObject proto = GetPrototypeFromConstructor(calleeContext, newTarget, Intrinsics.ArrayPrototype);
        if (numberOfArgs == 0) {
            // [22.1.1.1]
            /* step 5 */
            return ArrayCreate(calleeContext, 0, proto);
        } else if (numberOfArgs == 1) {
            // [22.1.1.2]
            Object len = args[0];
            /* steps 5-9 */
            if (!Type.isNumber(len)) {
                return DenseArrayCreate(calleeContext, proto, len);
            } else {
                double llen = Type.numberValue(len);
                long intLen = ToUint32(llen);
                if (intLen != llen) {
                    throw newRangeError(calleeContext, Messages.Key.InvalidArrayLength);
                }
                return ArrayCreate(calleeContext, intLen, proto);
            }
        } else {
            // [22.1.1.3]
            /* steps 5-10 */
            return DenseArrayCreate(calleeContext, proto, args);
        }
    }

    private static final long ARRAY_LENGTH_LIMIT = 0x1F_FFFF_FFFF_FFFFL;

    /**
     * 22.1.2 Properties of the Array Constructor
     */
    public enum Properties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final int length = 1;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final String name = "Array";

        /**
         * 22.1.2.4 Array.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static final Intrinsics prototype = Intrinsics.ArrayPrototype;

        /**
         * 22.1.2.2 Array.isArray ( arg )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param arg
         *            the argument object
         * @return {@code true} if the argument is an array object
         */
        @Function(name = "isArray", arity = 1)
        public static Object isArray(ExecutionContext cx, Object thisValue, Object arg) {
            /* step 1 */
            return IsArray(cx, arg);
        }

        /**
         * 22.1.2.3 Array.of ( ...items )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param items
         *            the element values
         * @return the new array object
         */
        @Function(name = "of", arity = 0)
        public static Object of(ExecutionContext cx, Object thisValue, Object... items) {
            /* steps 1-2 */
            int len = items.length;
            /* step 3 */
            Object c = thisValue;
            /* steps 4-5 */
            ScriptObject a;
            if (IsConstructor(c)) {
                a = ((Constructor) c).construct(cx, len);
            } else {
                a = ArrayCreate(cx, len);
            }
            /* steps 6-7 */
            for (int k = 0; k < len; ++k) {
                int pk = k;
                Object kValue = items[k];
                CreateDataPropertyOrThrow(cx, a, pk, kValue);
            }
            /* step 8 */
            Set(cx, a, "length", len, true);
            /* step 9 */
            return a;
        }

        /**
         * 22.1.2.1 Array.from ( items [ , mapfn [ , thisArg ] ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param items
         *            the source object
         * @param mapfn
         *            the optional mapper function
         * @param thisArg
         *            the optional this-argument for the mapper
         * @return the new array object
         */
        @Function(name = "from", arity = 1)
        public static Object from(ExecutionContext cx, Object thisValue, Object items, Object mapfn, Object thisArg) {
            /* step 1 */
            Object c = thisValue;
            /* steps 2-3 */
            Callable mapper;
            boolean mapping;
            if (Type.isUndefined(mapfn)) {
                mapper = null;
                mapping = false;
            } else {
                if (!IsCallable(mapfn)) {
                    throw newTypeError(cx, Messages.Key.NotCallable);
                }
                mapper = (Callable) mapfn;
                mapping = true;
            }
            /* step 4 */
            Callable usingIterator = GetMethod(cx, items, BuiltinSymbol.iterator.get());
            /* step 5 */
            if (usingIterator != null) {
                /* steps 5.a-b */
                ScriptObject a;
                if (IsConstructor(c)) {
                    a = ((Constructor) c).construct(cx);
                } else {
                    a = ArrayCreate(cx, 0);
                }
                /* step 5.c */
                ScriptIterator<?> iterator = GetIterator(cx, items, usingIterator);
                /* steps 5.d-e */
                long k = 0;
                try {
                    for (; iterator.hasNext(); ++k) {
                        if (k >= ARRAY_LENGTH_LIMIT) {
                            throw newTypeError(cx, Messages.Key.InvalidArrayLength);
                        }
                        long pk = k;
                        Object nextValue = iterator.next();
                        Object mappedValue;
                        if (mapping) {
                            mappedValue = mapper.call(cx, thisArg, nextValue, k);
                        } else {
                            mappedValue = nextValue;
                        }
                        CreateDataPropertyOrThrow(cx, a, pk, mappedValue);
                    }
                } catch (ScriptException e) {
                    iterator.close(e);
                    throw e;
                }
                /* step 5.e.iv */
                assert k <= ARRAY_LENGTH_LIMIT;
                Set(cx, a, "length", k, true);
                return a;
            }
            /* step 6 (note) */
            /* step 7 */
            ScriptObject arrayLike = ToObject(cx, items);
            /* step 8 */
            long len = ToLength(cx, Get(cx, arrayLike, "length"));
            /* steps 9-10 */
            ScriptObject a;
            if (IsConstructor(c)) {
                a = ((Constructor) c).construct(cx, len);
            } else {
                a = ArrayCreate(cx, len);
            }
            /* steps 11-12 */
            for (long k = 0; k < len; ++k) {
                long pk = k;
                Object kValue = Get(cx, arrayLike, pk);
                Object mappedValue;
                if (mapping) {
                    mappedValue = mapper.call(cx, thisArg, kValue, k);
                } else {
                    mappedValue = kValue;
                }
                CreateDataPropertyOrThrow(cx, a, pk, mappedValue);
            }
            /* step 13 */
            Set(cx, a, "length", len, true);
            /* step 14 */
            return a;
        }

        /**
         * 22.1.2.5 get Array [ @@species ]
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
}

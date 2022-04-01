/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.types.builtins;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;

/**
 * <h1>9 Ordinary and Exotic Objects Behaviours</h1>
 * <ul>
 * <li>9.3 Built-in Function Objects
 * </ul>
 */
public final class NativeFunction extends BuiltinFunction {
    // (ExecutionContext, ExecutionContext, Object, Object[]) -> Object
    private final MethodHandle mh;

    private final Class<?> id;

    /**
     * Constructs a new native function.
     * 
     * @param realm
     *            the realm object
     * @param name
     *            the function name
     * @param arity
     *            the function arity
     * @param mh
     *            the method handle to the function code
     */
    public NativeFunction(Realm realm, String name, int arity, MethodHandle mh) {
        this(realm, name, arity, null, mh);
    }

    /**
     * Constructs a new native function.
     * 
     * @param realm
     *            the realm object
     * @param name
     *            the function name
     * @param arity
     *            the function arity
     * @param id
     *            the native function identifier
     * @param mh
     *            the method handle to the function code
     */
    public NativeFunction(Realm realm, String name, int arity, Class<?> id, MethodHandle mh) {
        super(realm, name, arity);
        this.mh = mh;
        this.id = id;
        createDefaultFunctionProperties();
    }

    @Override
    public MethodHandle getCallMethod() {
        MethodHandle mh = MethodHandles.insertArguments(this.mh, 0, getRealm().defaultContext());
        return MethodHandles.dropArguments(mh, 0, NativeFunction.class);
    }

    /**
     * Returns the optional identifier for this native function.
     * 
     * @return the native function id or {@code null} if not available
     */
    public Class<?> getId() {
        return id;
    }

    /**
     * 9.3.1 [[Call]] (thisArgument, argumentsList)
     */
    @Override
    public Object call(ExecutionContext callerContext, Object thisValue, Object... args) {
        try {
            return mh.invokeExact(getRealm().defaultContext(), callerContext, thisValue, args);
        } catch (Throwable e) {
            throw NativeFunction.<RuntimeException> rethrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> E rethrow(Throwable e) throws E {
        throw (E) e;
    }

    /**
     * Returns {@code true} if <var>function</var> is a native function with the requested native identifier.
     * 
     * @param function
     *            the function object
     * @param nativeId
     *            the native function id
     * @return {@code true} if the function is a native function
     */
    public static boolean isNative(Object function, Class<?> nativeId) {
        return function instanceof NativeFunction && ((NativeFunction) function).getId() == nativeId;
    }

    /**
     * Returns {@code true} if <var>function</var> is a native function with the requested native identifier.
     * 
     * @param realm
     *            the function realm
     * @param function
     *            the function object
     * @param nativeId
     *            the native function id
     * @return {@code true} if the function is a native function
     */
    public static boolean isNative(Realm realm, Object function, Class<?> nativeId) {
        return function instanceof NativeFunction && ((NativeFunction) function).getId() == nativeId
                && ((NativeFunction) function).getRealm() == realm;
    }
}

/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects;

import static com.github.anba.es6draft.runtime.AbstractOperations.ToBoolean;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
import static com.github.anba.es6draft.runtime.objects.BooleanObject.BooleanCreate;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Properties.Attributes;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.builtins.BuiltinConstructor;

/**
 * <h1>19 Fundamental Objects</h1><br>
 * <h2>19.3 Boolean Objects</h2>
 * <ul>
 * <li>19.3.1 The Boolean Constructor
 * <li>19.3.2 Properties of the Boolean Constructor
 * </ul>
 */
public final class BooleanConstructor extends BuiltinConstructor implements Initializable {
    /**
     * Constructs a new Boolean constructor function.
     * 
     * @param realm
     *            the realm object
     */
    public BooleanConstructor(Realm realm) {
        super(realm, "Boolean", 1);
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, Properties.class);
    }

    /**
     * 19.3.1.1 Boolean (value)
     */
    @Override
    public Boolean call(ExecutionContext callerContext, Object thisValue, Object... args) {
        /* steps 1-2 */
        return args.length > 0 && ToBoolean(args[0]);
    }

    /**
     * 19.3.1.1 Boolean (value)
     */
    @Override
    public BooleanObject construct(ExecutionContext callerContext, Constructor newTarget, Object... args) {
        ExecutionContext calleeContext = calleeContext();
        /* step 1 */
        boolean b = args.length > 0 && ToBoolean(args[0]);
        /* step 2 (not applicable) */
        /* steps 3-5 */
        return BooleanCreate(calleeContext, b,
                GetPrototypeFromConstructor(calleeContext, newTarget, Intrinsics.BooleanPrototype));
    }

    /**
     * 19.3.2 Properties of the Boolean Constructor
     */
    public enum Properties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final int length = 1;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final String name = "Boolean";

        /**
         * 19.3.2.1 Boolean.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static final Intrinsics prototype = Intrinsics.BooleanPrototype;
    }
}

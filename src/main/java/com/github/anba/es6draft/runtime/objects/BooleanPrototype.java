/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects;

import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.Properties.Function;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.Type;

/**
 * <h1>19 Fundamental Objects</h1><br>
 * <h2>19.3 Boolean Objects</h2>
 * <ul>
 * <li>19.3.3 Properties of the Boolean Prototype Object
 * </ul>
 */
public final class BooleanPrototype extends BooleanObject implements Initializable {
    /**
     * Constructs a new Boolean prototype object.
     * 
     * @param realm
     *            the realm object
     */
    public BooleanPrototype(Realm realm) {
        super(realm, false);
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, Properties.class);
    }

    /**
     * 19.3.3 Properties of the Boolean Prototype Object
     */
    public enum Properties {
        ;

        /**
         * Abstract operation thisBooleanValue(value)
         * 
         * @param cx
         *            the execution context
         * @param value
         *            the value
         * @param method
         *            the method
         * @return the boolean value
         */
        private static boolean thisBooleanValue(ExecutionContext cx, Object value, String method) {
            /* step 1 */
            if (Type.isBoolean(value)) {
                return Type.booleanValue(value);
            }
            /* step 2 */
            if (value instanceof BooleanObject) {
                return ((BooleanObject) value).getBooleanData();
            }
            /* step 3 */
            throw newTypeError(cx, Messages.Key.IncompatibleThis, method, Type.of(value).toString());
        }

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ObjectPrototype;

        /**
         * 19.3.3.1 Boolean.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.Boolean;

        /**
         * 19.3.3.2 Boolean.prototype.toString ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the string representation
         */
        @Function(name = "toString", arity = 0)
        public static Object toString(ExecutionContext cx, Object thisValue) {
            /* steps 1-2 */
            return thisBooleanValue(cx, thisValue, "Boolean.prototype.toString") ? "true" : "false";
        }

        /**
         * 19.3.3.3 Boolean.prototype.valueOf ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the boolean value
         */
        @Function(name = "valueOf", arity = 0)
        public static Object valueOf(ExecutionContext cx, Object thisValue) {
            /* step 1 */
            return thisBooleanValue(cx, thisValue, "Boolean.prototype.valueOf");
        }
    }
}

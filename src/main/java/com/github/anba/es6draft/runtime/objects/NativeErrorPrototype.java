/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects;

import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;

import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.objects.NativeErrorConstructor.ErrorType;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.builtins.OrdinaryObject;

/**
 * <h1>19 Fundamental Objects</h1><br>
 * <h2>19.5 Error Objects</h2>
 * <ul>
 * <li>19.5.6 NativeError Object Structure
 * <ul>
 * <li>19.5.6.3 Properties of the NativeError Prototype Objects
 * </ul>
 * </ul>
 */
public final class NativeErrorPrototype extends OrdinaryObject implements Initializable {
    private final ErrorType type;

    /**
     * Constructs a new NativeError prototype object.
     * 
     * @param realm
     *            the realm object
     * @param type
     *            the native error type
     */
    public NativeErrorPrototype(Realm realm, ErrorType type) {
        super(realm);
        this.type = type;
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, propertiesForType(type));
    }

    private static Class<?> propertiesForType(ErrorType errorType) {
        switch (errorType) {
        case EvalError:
            return EvalErrorPrototypeProperties.class;
        case RangeError:
            return RangeErrorPrototypeProperties.class;
        case ReferenceError:
            return ReferenceErrorPrototypeProperties.class;
        case SyntaxError:
            return SyntaxErrorPrototypeProperties.class;
        case TypeError:
            return TypeErrorPrototypeProperties.class;
        case URIError:
            return URIErrorPrototypeProperties.class;
        case InternalError:
            return InternalErrorPrototypeProperties.class;
        default:
            throw new AssertionError();
        }
    }

    /**
     * 19.5.6.3 Properties of the NativeError Prototype Objects
     */
    public enum EvalErrorPrototypeProperties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ErrorPrototype;

        /**
         * 19.5.6.3.1 NativeError.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.EvalError;

        /**
         * 19.5.6.3.3 NativeError.prototype.name
         */
        @Value(name = "name")
        public static final String name = "EvalError";

        /**
         * 19.5.6.3.2 NativeError.prototype.message
         */
        @Value(name = "message")
        public static final String message = "";
    }

    /**
     * 19.5.6.3 Properties of the NativeError Prototype Objects
     */
    public enum RangeErrorPrototypeProperties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ErrorPrototype;

        /**
         * 19.5.6.3.1 NativeError.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.RangeError;

        /**
         * 19.5.6.3.3 NativeError.prototype.name
         */
        @Value(name = "name")
        public static final String name = "RangeError";

        /**
         * 19.5.6.3.2 NativeError.prototype.message
         */
        @Value(name = "message")
        public static final String message = "";
    }

    /**
     * 19.5.6.3 Properties of the NativeError Prototype Objects
     */
    public enum ReferenceErrorPrototypeProperties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ErrorPrototype;

        /**
         * 19.5.6.3.1 NativeError.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.ReferenceError;

        /**
         * 19.5.6.3.3 NativeError.prototype.name
         */
        @Value(name = "name")
        public static final String name = "ReferenceError";

        /**
         * 19.5.6.3.2 NativeError.prototype.message
         */
        @Value(name = "message")
        public static final String message = "";
    }

    /**
     * 19.5.6.3 Properties of the NativeError Prototype Objects
     */
    public enum SyntaxErrorPrototypeProperties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ErrorPrototype;

        /**
         * 19.5.6.3.1 NativeError.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.SyntaxError;

        /**
         * 19.5.6.3.3 NativeError.prototype.name
         */
        @Value(name = "name")
        public static final String name = "SyntaxError";

        /**
         * 19.5.6.3.2 NativeError.prototype.message
         */
        @Value(name = "message")
        public static final String message = "";
    }

    /**
     * 19.5.6.3 Properties of the NativeError Prototype Objects
     */
    public enum TypeErrorPrototypeProperties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ErrorPrototype;

        /**
         * 19.5.6.3.1 NativeError.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.TypeError;

        /**
         * 19.5.6.3.3 NativeError.prototype.name
         */
        @Value(name = "name")
        public static final String name = "TypeError";

        /**
         * 19.5.6.3.2 NativeError.prototype.message
         */
        @Value(name = "message")
        public static final String message = "";
    }

    /**
     * 19.5.6.3 Properties of the NativeError Prototype Objects
     */
    public enum URIErrorPrototypeProperties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ErrorPrototype;

        /**
         * 19.5.6.3.1 NativeError.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.URIError;

        /**
         * 19.5.6.3.3 NativeError.prototype.name
         */
        @Value(name = "name")
        public static final String name = "URIError";

        /**
         * 19.5.6.3.2 NativeError.prototype.message
         */
        @Value(name = "message")
        public static final String message = "";
    }

    /**
     * 19.5.6.3 Properties of the NativeError Prototype Objects
     */
    public enum InternalErrorPrototypeProperties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ErrorPrototype;

        /**
         * 19.5.6.3.1 NativeError.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.InternalError;

        /**
         * 19.5.6.3.3 NativeError.prototype.name
         */
        @Value(name = "name")
        public static final String name = "InternalError";

        /**
         * 19.5.6.3.2 NativeError.prototype.message
         */
        @Value(name = "message")
        public static final String message = "";
    }
}

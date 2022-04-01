/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects;

import static com.github.anba.es6draft.runtime.AbstractOperations.*;
import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
import static com.github.anba.es6draft.runtime.types.Null.NULL;
import static com.github.anba.es6draft.runtime.types.PropertyDescriptor.FromPropertyDescriptor;
import static com.github.anba.es6draft.runtime.types.PropertyDescriptor.ToPropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

import com.github.anba.es6draft.runtime.AbstractOperations.PropertyKind;
import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.Properties.Attributes;
import com.github.anba.es6draft.runtime.internal.Properties.Function;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.types.Constructor;
import com.github.anba.es6draft.runtime.types.IntegrityLevel;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.Property;
import com.github.anba.es6draft.runtime.types.PropertyDescriptor;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.Symbol;
import com.github.anba.es6draft.runtime.types.Type;
import com.github.anba.es6draft.runtime.types.builtins.ArrayObject;
import com.github.anba.es6draft.runtime.types.builtins.BuiltinConstructor;
import com.github.anba.es6draft.runtime.types.builtins.ImmutablePrototypeObject;
import com.github.anba.es6draft.runtime.types.builtins.OrdinaryObject;

/**
 * <h1>19 Fundamental Objects</h1><br>
 * <h2>19.1 Object Objects</h2>
 * <ul>
 * <li>19.1.1 The Object Constructor
 * <li>19.1.2 Properties of the Object Constructor
 * </ul>
 */
public final class ObjectConstructor extends BuiltinConstructor implements Initializable {
    /**
     * Constructs a new Object constructor function.
     * 
     * @param realm
     *            the realm object
     */
    public ObjectConstructor(Realm realm) {
        super(realm, "Object", 1);
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, Properties.class);
    }

    /**
     * 19.1.1.1 Object ( [ value ] )
     */
    @Override
    public ScriptObject call(ExecutionContext callerContext, Object thisValue, Object... args) {
        ExecutionContext calleeContext = calleeContext();
        Object value = argument(args, 0);
        /* step 1 (not applicable) */
        /* step 2 */
        if (Type.isUndefinedOrNull(value)) {
            return ObjectCreate(calleeContext, Intrinsics.ObjectPrototype);
        }
        /* step 3 */
        return ToObject(calleeContext, value);
    }

    /**
     * 19.1.1.1 Object ( [ value ] )
     */
    @Override
    public ScriptObject construct(ExecutionContext callerContext, Constructor newTarget, Object... args) {
        ExecutionContext calleeContext = calleeContext();
        Object value = argument(args, 0);
        /* step 1 */
        if (newTarget != this) {
            return OrdinaryCreateFromConstructor(calleeContext, newTarget, Intrinsics.ObjectPrototype);
        }
        /* step 2 */
        if (Type.isUndefinedOrNull(value)) {
            return ObjectCreate(calleeContext, Intrinsics.ObjectPrototype);
        }
        /* step 3 */
        return ToObject(calleeContext, value);
    }

    /**
     * 19.1.2 Properties of the Object Constructor
     */
    public enum Properties {
        ;

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.FunctionPrototype;

        @Value(name = "length", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final int length = 1;

        @Value(name = "name", attributes = @Attributes(writable = false, enumerable = false, configurable = true))
        public static final String name = "Object";

        /**
         * 19.1.2.18 Object.prototype
         */
        @Value(name = "prototype", attributes = @Attributes(writable = false, enumerable = false, configurable = false))
        public static final Intrinsics prototype = Intrinsics.ObjectPrototype;

        /**
         * 19.1.2.11 Object.getPrototypeOf ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the prototype object
         */
        @Function(name = "getPrototypeOf", arity = 1)
        public static Object getPrototypeOf(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            ScriptObject obj = ToObject(cx, o);
            /* step 2 */
            ScriptObject proto = obj.getPrototypeOf(cx);
            return proto != null ? proto : NULL;
        }

        /**
         * 19.1.2.7 Object.getOwnPropertyDescriptor ( O, P )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @param p
         *            the property key
         * @return the property descriptor object or undefined
         */
        @Function(name = "getOwnPropertyDescriptor", arity = 2)
        public static Object getOwnPropertyDescriptor(ExecutionContext cx, Object thisValue, Object o, Object p) {
            /* step 1 */
            ScriptObject obj = ToObject(cx, o);
            /* step 2 */
            Object key = ToPropertyKey(cx, p);
            /* step 3 */
            Property desc = obj.getOwnProperty(cx, key);
            /* step 4 */
            return FromPropertyDescriptor(cx, desc);
        }

        /**
         * 19.1.2.8 Object.getOwnPropertyDescriptors ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the object property descriptors array
         */
        @Function(name = "getOwnPropertyDescriptors", arity = 1)
        public static Object getOwnPropertyDescriptors(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            ScriptObject obj = ToObject(cx, o);
            /* step 2 */
            List<?> ownKeys = obj.ownPropertyKeys(cx);
            /* step 3 */
            OrdinaryObject descriptors = ObjectCreate(cx, Intrinsics.ObjectPrototype);
            /* step 4 */
            for (Object key : ownKeys) {
                /* step 4.a */
                Property desc = obj.getOwnProperty(cx, key);
                /* step 4.b */
                Object descriptor = FromPropertyDescriptor(cx, desc);
                /* step 4.c */
                if (!Type.isUndefined(descriptor)) {
                    CreateDataProperty(cx, descriptors, key, descriptor);
                }
            }
            /* step 5 */
            return descriptors;
        }

        /**
         * 19.1.2.9 Object.getOwnPropertyNames ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the own string-valued property keys of <var>o</var>
         */
        @Function(name = "getOwnPropertyNames", arity = 1)
        public static Object getOwnPropertyNames(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            return GetOwnPropertyNames(cx, o);
        }

        /**
         * 19.1.2.10 Object.getOwnPropertySymbols ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the own symbol-valued property keys of <var>o</var>
         */
        @Function(name = "getOwnPropertySymbols", arity = 1)
        public static Object getOwnPropertySymbols(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            return GetOwnPropertySymbols(cx, o);
        }

        /**
         * 19.1.2.2 Object.create ( O, Properties )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @param properties
         *            the properties object
         * @return the new script object
         */
        @Function(name = "create", arity = 2)
        public static Object create(ExecutionContext cx, Object thisValue, Object o, Object properties) {
            /* step 1 */
            if (!Type.isObjectOrNull(o)) {
                throw newTypeError(cx, Messages.Key.NotObjectOrNull);
            }
            /* step 2 */
            OrdinaryObject obj = ObjectCreate(cx, Type.objectValueOrNull(o));
            /* step 3 */
            if (!Type.isUndefined(properties)) {
                return ObjectDefineProperties(cx, obj, properties);
            }
            /* step 4 */
            return obj;
        }

        /**
         * 19.1.2.4 Object.defineProperty ( O, P, Attributes )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @param p
         *            the property key
         * @param attributes
         *            the property descriptor object
         * @return the script object
         */
        @Function(name = "defineProperty", arity = 3)
        public static Object defineProperty(ExecutionContext cx, Object thisValue, Object o, Object p,
                Object attributes) {
            /* step 1 */
            if (!Type.isObject(o)) {
                throw newTypeError(cx, Messages.Key.NotObjectType);
            }
            /* step 2 */
            Object key = ToPropertyKey(cx, p);
            /* step 3 */
            PropertyDescriptor desc = ToPropertyDescriptor(cx, attributes);
            /* step 4 */
            DefinePropertyOrThrow(cx, Type.objectValue(o), key, desc);
            /* step 5 */
            return o;
        }

        /**
         * 19.1.2.3 Object.defineProperties ( O, Properties )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @param properties
         *            the properties object
         * @return the script object
         */
        @Function(name = "defineProperties", arity = 2)
        public static Object defineProperties(ExecutionContext cx, Object thisValue, Object o, Object properties) {
            /* step 1 */
            return ObjectDefineProperties(cx, o, properties);
        }

        /**
         * 19.1.2.19 Object.seal ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the script object
         */
        @Function(name = "seal", arity = 1)
        public static Object seal(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            if (!Type.isObject(o)) {
                return o;
            }
            /* step 2 */
            boolean status = SetIntegrityLevel(cx, Type.objectValue(o), IntegrityLevel.Sealed);
            /* step 3 */
            if (!status) {
                throw newTypeError(cx, Messages.Key.ObjectSealFailed);
            }
            /* step 4 */
            return o;
        }

        /**
         * 19.1.2.6 Object.freeze ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the script object
         */
        @Function(name = "freeze", arity = 1)
        public static Object freeze(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            if (!Type.isObject(o)) {
                return o;
            }
            /* step 2 */
            boolean status = SetIntegrityLevel(cx, Type.objectValue(o), IntegrityLevel.Frozen);
            /* step 3 */
            if (!status) {
                throw newTypeError(cx, Messages.Key.ObjectFreezeFailed);
            }
            /* step 4 */
            return o;
        }

        /**
         * 19.1.2.17 Object.preventExtensions ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the script object
         */
        @Function(name = "preventExtensions", arity = 1)
        public static Object preventExtensions(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            if (!Type.isObject(o)) {
                return o;
            }
            /* step 2 */
            boolean status = Type.objectValue(o).preventExtensions(cx);
            /* step 3 */
            if (!status) {
                throw newTypeError(cx, Messages.Key.ObjectPreventExtensionsFailed);
            }
            /* step 4 */
            return o;
        }

        /**
         * 19.1.2.15 Object.isSealed ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return {@code true} if the object is sealed
         */
        @Function(name = "isSealed", arity = 1)
        public static Object isSealed(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            if (!Type.isObject(o)) {
                return true;
            }
            /* step 2 */
            return TestIntegrityLevel(cx, Type.objectValue(o), IntegrityLevel.Sealed);
        }

        /**
         * 19.1.2.14 Object.isFrozen ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return {@code true} if the object is frozen
         */
        @Function(name = "isFrozen", arity = 1)
        public static Object isFrozen(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            if (!Type.isObject(o)) {
                return true;
            }
            /* step 2 */
            return TestIntegrityLevel(cx, Type.objectValue(o), IntegrityLevel.Frozen);
        }

        /**
         * 19.1.2.13 Object.isExtensible ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return {@code true} if the object is extensible
         */
        @Function(name = "isExtensible", arity = 1)
        public static Object isExtensible(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            if (!Type.isObject(o)) {
                return false;
            }
            /* step 2 */
            return IsExtensible(cx, Type.objectValue(o));
        }

        /**
         * 19.1.2.16 Object.keys ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the object keys array
         */
        @Function(name = "keys", arity = 1)
        public static Object keys(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            ScriptObject obj = ToObject(cx, o);
            /* step 2 */
            List<String> nameList = EnumerableOwnNames(cx, obj);
            /* step 3 */
            return CreateArrayFromList(cx, nameList);
        }

        /**
         * 19.1.2.21 Object.values ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the object values array
         */
        @Function(name = "values", arity = 1)
        public static Object values(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            ScriptObject obj = ToObject(cx, o);
            /* step 2 */
            List<Object> valueList = EnumerableOwnProperties(cx, obj, PropertyKind.Value);
            /* step 3 */
            return CreateArrayFromList(cx, valueList);
        }

        /**
         * 19.1.2.5 Object.entries ( O )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @return the object entries array
         */
        @Function(name = "entries", arity = 1)
        public static Object entries(ExecutionContext cx, Object thisValue, Object o) {
            /* step 1 */
            ScriptObject obj = ToObject(cx, o);
            /* step 2 */
            List<Object> entryList = EnumerableOwnProperties(cx, obj, PropertyKind.KeyValue);
            /* step 3 */
            return CreateArrayFromList(cx, entryList);
        }

        /**
         * 19.1.2.12 Object.is ( value1, value2 )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param value1
         *            the first value
         * @param value2
         *            the second value
         * @return {@code true} if both operands have the same value
         */
        @Function(name = "is", arity = 2)
        public static Object is(ExecutionContext cx, Object thisValue, Object value1, Object value2) {
            /* step 1 */
            return SameValue(value1, value2);
        }

        /**
         * 19.1.2.1 Object.assign ( target, ...sources )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param target
         *            the target object
         * @param sources
         *            the source objects
         * @return the target object
         */
        @Function(name = "assign", arity = 2)
        public static Object assign(ExecutionContext cx, Object thisValue, Object target, Object... sources) {
            /* step 1 */
            ScriptObject to = ToObject(cx, target);
            /* step 2 */
            if (sources.length == 0) {
                return to;
            }
            /* steps 3-4 */
            for (Object nextSource : sources) {
                /* step 4.a */
                if (Type.isUndefinedOrNull(nextSource)) {
                    continue;
                }
                /* step 4.b.i */
                ScriptObject from = ToObject(cx, nextSource);
                /* step 4.b.ii */
                List<?> keys = from.ownPropertyKeys(cx);
                /* step 4.c */
                for (Object nextKey : keys) {
                    Property desc = from.getOwnProperty(cx, nextKey);
                    if (desc != null && desc.isEnumerable()) {
                        Object propValue = Get(cx, from, nextKey);
                        Set(cx, to, nextKey, propValue, true);
                    }
                }
            }
            /* step 5 */
            return to;
        }

        /**
         * 19.1.2.20 Object.setPrototypeOf ( O, proto )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param o
         *            the script object
         * @param proto
         *            the new prototype object
         * @return the script object
         */
        @Function(name = "setPrototypeOf", arity = 2)
        public static Object setPrototypeOf(ExecutionContext cx, Object thisValue, Object o, Object proto) {
            /* step 1 */
            RequireObjectCoercible(cx, o);
            /* step 2 */
            if (!Type.isObjectOrNull(proto)) {
                throw newTypeError(cx, Messages.Key.NotObjectOrNull);
            }
            /* step 3 */
            if (!Type.isObject(o)) {
                return o;
            }
            /* step 4 */
            ScriptObject obj = Type.objectValue(o);
            boolean status = obj.setPrototypeOf(cx, Type.objectValueOrNull(proto));
            /* step 5 */
            if (!status) {
                // provide better error messages for ordinary objects
                if (obj instanceof OrdinaryObject && !(obj instanceof ImmutablePrototypeObject)) {
                    if (!obj.isExtensible(cx)) {
                        throw newTypeError(cx, Messages.Key.NotExtensible);
                    }
                    throw newTypeError(cx, Messages.Key.CyclicProto);
                }
                throw newTypeError(cx, Messages.Key.ObjectSetPrototypeFailed);
            }
            /* step 6 */
            return obj;
        }
    }

    /**
     * 19.1.2.3.1 Runtime Semantics: ObjectDefineProperties ( O, Properties )
     * 
     * @param cx
     *            the execution context
     * @param o
     *            the script object
     * @param properties
     *            the properties object
     * @return the script object
     */
    public static ScriptObject ObjectDefineProperties(ExecutionContext cx, Object o, Object properties) {
        /* step 1 */
        if (!Type.isObject(o)) {
            throw newTypeError(cx, Messages.Key.NotObjectType);
        }
        ScriptObject obj = Type.objectValue(o);
        /* step 2 */
        ScriptObject props = ToObject(cx, properties);
        /* step 3 */
        List<?> keys = props.ownPropertyKeys(cx);
        /* step 4 */
        int initialSize = Math.min(32, keys.size());
        ArrayList<PropertyDescriptor> descriptors = new ArrayList<>(initialSize);
        ArrayList<Object> names = new ArrayList<>(initialSize);
        /* step 5 */
        for (Object nextKey : keys) {
            /* step 5.a */
            Property propDesc = props.getOwnProperty(cx, nextKey);
            /* step 5.b */
            if (propDesc != null && propDesc.isEnumerable()) {
                Object descObj = Get(cx, props, nextKey);
                PropertyDescriptor desc = ToPropertyDescriptor(cx, descObj);
                descriptors.add(desc);
                names.add(nextKey);
            }
        }
        /* step 6 */
        for (int i = 0, size = names.size(); i < size; ++i) {
            /* step 6.a */
            Object p = names.get(i);
            /* step 6.b */
            PropertyDescriptor desc = descriptors.get(i);
            /* step 6.c */
            DefinePropertyOrThrow(cx, obj, p, desc);
        }
        /* step 7 */
        return obj;
    }

    /**
     * 19.1.2.10.1 Runtime Semantics: GetOwnPropertyKeys ( O, Type ), with Type = String
     * 
     * @param cx
     *            the execution context
     * @param o
     *            the script object
     * @return the own string-valued property keys of <var>o</var>
     */
    public static ArrayObject GetOwnPropertyNames(ExecutionContext cx, Object o) {
        /* step 1 */
        ScriptObject obj = ToObject(cx, o);
        /* steps 2-4 */
        List<String> nameList = obj.ownPropertyNames(cx);
        /* step 5 */
        return CreateArrayFromList(cx, nameList);
    }

    /**
     * 19.1.2.10.1 Runtime Semantics: GetOwnPropertyKeys ( O, Type ), with Type = Symbol
     * 
     * @param cx
     *            the execution context
     * @param o
     *            the script object
     * @return the own symbol-valued property keys of <var>o</var>
     */
    public static ArrayObject GetOwnPropertySymbols(ExecutionContext cx, Object o) {
        /* step 1 */
        ScriptObject obj = ToObject(cx, o);
        /* steps 2-4 */
        List<Symbol> nameList = obj.ownPropertySymbols(cx);
        /* step 5 */
        return CreateArrayFromList(cx, nameList);
    }
}

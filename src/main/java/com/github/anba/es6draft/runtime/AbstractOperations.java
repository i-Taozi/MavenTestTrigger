/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime;

import static com.github.anba.es6draft.runtime.internal.Errors.newRangeError;
import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.language.Operators.InstanceofOperator;
import static com.github.anba.es6draft.runtime.objects.BooleanObject.BooleanCreate;
import static com.github.anba.es6draft.runtime.objects.SymbolObject.SymbolCreate;
import static com.github.anba.es6draft.runtime.objects.number.NumberObject.NumberCreate;
import static com.github.anba.es6draft.runtime.objects.simd.SIMDObject.SIMDCreate;
import static com.github.anba.es6draft.runtime.types.builtins.ArrayObject.DenseArrayCreate;
import static com.github.anba.es6draft.runtime.types.builtins.OrdinaryObject.ObjectCreate;
import static com.github.anba.es6draft.runtime.types.builtins.StringObject.StringCreate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.DToA;
import org.mozilla.javascript.v8dtoa.FastDtoa;

import com.github.anba.es6draft.parser.NumberParser;
import com.github.anba.es6draft.runtime.internal.CompatibilityOption;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.ScriptException;
import com.github.anba.es6draft.runtime.internal.ScriptIterator;
import com.github.anba.es6draft.runtime.internal.ScriptIterators;
import com.github.anba.es6draft.runtime.internal.Strings;
import com.github.anba.es6draft.runtime.objects.FunctionPrototype;
import com.github.anba.es6draft.runtime.objects.bigint.BigIntAbstractOperations;
import com.github.anba.es6draft.runtime.objects.bigint.BigIntObject;
import com.github.anba.es6draft.runtime.objects.bigint.BigIntType;
import com.github.anba.es6draft.runtime.objects.simd.SIMD;
import com.github.anba.es6draft.runtime.objects.simd.SIMDValue;
import com.github.anba.es6draft.runtime.objects.text.RegExpObject;
import com.github.anba.es6draft.runtime.types.*;
import com.github.anba.es6draft.runtime.types.builtins.ArgumentsObject;
import com.github.anba.es6draft.runtime.types.builtins.ArrayObject;
import com.github.anba.es6draft.runtime.types.builtins.BoundFunctionObject;
import com.github.anba.es6draft.runtime.types.builtins.OrdinaryObject;
import com.github.anba.es6draft.runtime.types.builtins.ProxyObject;
import com.google.doubleconversion.DoubleConversion;

/**
 * <h1>7 Abstract Operations</h1>
 * <ul>
 * <li>7.1 Type Conversion
 * <li>7.2 Testing and Comparison Operations
 * <li>7.3 Operations on Objects
 * <li>7.4 Operations on Iterator Objects
 * </ul>
 */
public final class AbstractOperations {
    private AbstractOperations() {
    }

    /**
     * Hint string for {@link AbstractOperations#ToPrimitive(ExecutionContext, Object, ToPrimitiveHint)}
     */
    public enum ToPrimitiveHint {
        Default, String, Number;

        @Override
        public String toString() {
            switch (this) {
            case String:
                return "string";
            case Number:
                return "number";
            case Default:
                return "default";
            default:
                throw new AssertionError();
            }
        }
    }

    /**
     * 7.1.1 ToPrimitive ( input [, PreferredType] )
     * 
     * @param cx
     *            the execution context
     * @param argument
     *            the argument value
     * @return the primitive value
     */
    public static Object ToPrimitive(ExecutionContext cx, Object argument) {
        if (!Type.isObject(argument)) {
            return argument;
        }
        return ToPrimitive(cx, Type.objectValue(argument), ToPrimitiveHint.Default);
    }

    /**
     * 7.1.1 ToPrimitive ( input [, PreferredType] )
     * 
     * @param cx
     *            the execution context
     * @param argument
     *            the argument value
     * @return the primitive value
     */
    public static Object ToPrimitive(ExecutionContext cx, ScriptObject argument) {
        return ToPrimitive(cx, argument, ToPrimitiveHint.Default);
    }

    /**
     * 7.1.1 ToPrimitive ( input [, PreferredType] )
     * 
     * @param cx
     *            the execution context
     * @param argument
     *            the argument value
     * @param preferredType
     *            the preferred primitive type
     * @return the primitive value
     */
    public static Object ToPrimitive(ExecutionContext cx, Object argument, ToPrimitiveHint preferredType) {
        if (!Type.isObject(argument)) {
            return argument;
        }
        return ToPrimitive(cx, Type.objectValue(argument), preferredType);
    }

    /**
     * 7.1.1 ToPrimitive ( input [, PreferredType] )
     * <p>
     * ToPrimitive for the Object type
     * 
     * @param cx
     *            the execution context
     * @param argument
     *            the argument value
     * @param preferredType
     *            the preferred primitive type
     * @return the primitive value
     */
    public static Object ToPrimitive(ExecutionContext cx, ScriptObject argument, ToPrimitiveHint preferredType) {
        /* steps 1-3 */
        String hint = preferredType.toString();
        /* step 4 */
        Callable exoticToPrim = GetMethod(cx, argument, BuiltinSymbol.toPrimitive.get());
        /* step 5 */
        if (exoticToPrim != null) {
            Object result = exoticToPrim.call(cx, argument, hint);
            if (!Type.isObject(result)) {
                return result;
            }
            throw newTypeError(cx, Messages.Key.NotPrimitiveType);
        }
        /* step 6 */
        if (preferredType == ToPrimitiveHint.Default) {
            preferredType = ToPrimitiveHint.Number;
        }
        /* step 7 */
        return OrdinaryToPrimitive(cx, argument, preferredType);
    }

    /**
     * 7.1.1 ToPrimitive ( input [, PreferredType] )
     * <p>
     * OrdinaryToPrimitive
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the argument object
     * @param hint
     *            the preferred primitive type
     * @return the primitive value
     */
    public static Object OrdinaryToPrimitive(ExecutionContext cx, ScriptObject object, ToPrimitiveHint hint) {
        /* steps 1-2 */
        assert hint == ToPrimitiveHint.String || hint == ToPrimitiveHint.Number;
        /* steps 3-4 */
        String tryFirst, trySecond;
        if (hint == ToPrimitiveHint.String) {
            tryFirst = "toString";
            trySecond = "valueOf";
        } else {
            tryFirst = "valueOf";
            trySecond = "toString";
        }
        /* step 5 (first try) */
        Object first = Get(cx, object, tryFirst);
        if (IsCallable(first)) {
            Object result = ((Callable) first).call(cx, object);
            if (!Type.isObject(result)) {
                return result;
            }
        }
        /* step 5 (second try) */
        Object second = Get(cx, object, trySecond);
        if (IsCallable(second)) {
            Object result = ((Callable) second).call(cx, object);
            if (!Type.isObject(result)) {
                return result;
            }
        }
        /* step 6 */
        throw newTypeError(cx, Messages.Key.NoPrimitiveRepresentation);
    }

    /**
     * 7.1.2 ToBoolean ( argument )
     * 
     * @param value
     *            the argument value
     * @return the boolean result
     */
    public static boolean ToBoolean(Object value) {
        switch (Type.of(value)) {
        case Undefined:
            return false;
        case Null:
            return false;
        case Boolean:
            return Type.booleanValue(value);
        case Number:
            double d = Type.numberValue(value);
            return !(d == 0 || Double.isNaN(d));
        case String:
            return Type.stringValue(value).length() != 0;
        case Symbol:
            return true;
        case SIMD:
            return true;
        case BigInt:
            return Type.bigIntValue(value).signum() != 0;
        case Object:
            return !(value instanceof HTMLDDAObject);
        default:
            throw new AssertionError();
        }
    }

    /**
     * 7.1.2 ToBoolean ( argument )
     * 
     * @param value
     *            the argument value
     * @return the boolean result
     */
    public static boolean ToBoolean(int value) {
        return value != 0;
    }

    /**
     * 7.1.2 ToBoolean ( argument )
     * 
     * @param value
     *            the argument value
     * @return the boolean result
     */
    public static boolean ToBoolean(long value) {
        return value != 0;
    }

    /**
     * 7.1.2 ToBoolean ( argument )
     * 
     * @param value
     *            the argument value
     * @return the boolean result
     */
    public static boolean ToBoolean(double value) {
        return !(value == 0 || Double.isNaN(value));
    }

    /**
     * 7.1.2 ToBoolean ( argument )
     * 
     * @param value
     *            the argument value
     * @return the boolean result
     */
    public static boolean ToBoolean(BigInteger value) {
        return value.signum() != 0;
    }

    /**
     * 7.1.3 ToNumber ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the number result
     */
    public static double ToNumber(ExecutionContext cx, Object value) {
        // Inlined: `if (Type.isNumber(value)) return Type.numberValue(value);`
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return ToNumberSlow(cx, value);
    }

    /**
     * 7.1.3 ToNumber ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the number result
     */
    private static double ToNumberSlow(ExecutionContext cx, Object value) {
        switch (Type.of(value)) {
        case Undefined:
            return Double.NaN;
        case Null:
            return +0;
        case Boolean:
            return Type.booleanValue(value) ? 1 : +0;
        case String:
            return ToNumber(Type.stringValue(value));
        case Symbol:
            throw newTypeError(cx, Messages.Key.SymbolNumber);
        case SIMD:
            throw newTypeError(cx, Messages.Key.SIMDNumber);
        case BigInt:
            throw newTypeError(cx, Messages.Key.BigIntNumber);
        case Object:
            Object primValue = ToPrimitive(cx, Type.objectValue(value), ToPrimitiveHint.Number);
            return ToNumber(cx, primValue);
        case Number:
        default:
            throw new AssertionError();
        }
    }

    /**
     * 7.1.3.1 ToNumber Applied to the String Type
     * 
     * @param string
     *            the argument value
     * @return the number result
     */
    public static double ToNumber(CharSequence string) {
        return ToNumberParser.parse(string.toString());
    }

    /**
     * 7.1.4 ToInteger ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static double ToInteger(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* step 2 */
        if (Double.isNaN(number))
            return +0d;
        /* step 3 */
        if (number == 0d || Double.isInfinite(number))
            return number;
        /* step 4 */
        // return Math.signum(number) * Math.floor(Math.abs(number));
        if (number < 0) {
            return Math.ceil(number);
        }
        return Math.floor(number);
    }

    /**
     * 7.1.4 ToInteger ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static double ToInteger(double number) {
        /* step 1 (not applicable) */
        /* step 2 */
        if (Double.isNaN(number))
            return +0d;
        /* step 3 */
        if (number == 0d || Double.isInfinite(number))
            return number;
        /* step 4 */
        // return Math.signum(number) * Math.floor(Math.abs(number));
        if (number < 0) {
            return Math.ceil(number);
        }
        return Math.floor(number);
    }

    /**
     * 7.1.5 ToInt32 ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static int ToInt32(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* steps 2-5 */
        return DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.5 ToInt32 ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static int ToInt32(double number) {
        /* step 1 (not applicable) */
        /* steps 2-5 */
        return DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.6 ToUint32 ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static long ToUint32(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* steps 2-5 */
        return DoubleConversion.doubleToInt32(number) & 0xffffffffL;
    }

    /**
     * 7.1.6 ToUint32 ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static long ToUint32(double number) {
        /* step 1 (not applicable) */
        /* steps 2-5 */
        return DoubleConversion.doubleToInt32(number) & 0xffffffffL;
    }

    /**
     * 7.1.7 ToInt16 ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static short ToInt16(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* steps 2-5 */
        return (short) DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.7 ToInt16 ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static short ToInt16(double number) {
        /* step 1 (not applicable) */
        /* steps 2-5 */
        return (short) DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.8 ToUint16 ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static char ToUint16(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* steps 2-5 */
        return (char) DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.8 ToUint16 ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static char ToUint16(double number) {
        /* step 1 (not applicable) */
        /* steps 2-5 */
        return (char) DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.9 ToInt8 ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static byte ToInt8(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* steps 2-5 */
        return (byte) DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.9 ToInt8 ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static byte ToInt8(double number) {
        /* step 1 (not applicable) */
        /* steps 2-5 */
        return (byte) DoubleConversion.doubleToInt32(number);
    }

    /**
     * 7.1.10 ToUint8 ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static int ToUint8(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* steps 2-5 */
        return DoubleConversion.doubleToInt32(number) & 0xFF;
    }

    /**
     * 7.1.10 ToUint8 ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static int ToUint8(double number) {
        /* step 1 (not applicable) */
        /* steps 2-5 */
        return DoubleConversion.doubleToInt32(number) & 0xFF;
    }

    /**
     * 7.1.11 ToUint8Clamp ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the integer result
     */
    public static int ToUint8Clamp(ExecutionContext cx, Object value) {
        /* step 1 */
        double number = ToNumber(cx, value);
        /* step 3 */
        if (number <= 0) {
            return 0;
        }
        /* step 4 */
        if (number >= 255) {
            return 255;
        }
        /* steps 2, 5-9 */
        return (int) Math.rint(number);
    }

    /**
     * 7.1.11 ToUint8Clamp ( argument )
     * 
     * @param number
     *            the argument value
     * @return the integer result
     */
    public static int ToUint8Clamp(double number) {
        /* step 1 (not applicable) */
        /* step 3 */
        if (number <= 0) {
            return 0;
        }
        /* step 4 */
        if (number >= 255) {
            return 255;
        }
        /* steps 2, 5-9 */
        return (int) Math.rint(number);
    }

    /**
     * 7.1.12 ToString ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the string result
     */
    public static String ToFlatString(ExecutionContext cx, Object value) {
        // Inlined: `if (Type.isString(value)) return Type.stringValue(value);`
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof ConsString) {
            return ((ConsString) value).toString();
        }
        return ToStringSlow(cx, value).toString();
    }

    /**
     * 7.1.12 ToString ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the string result
     */
    public static CharSequence ToString(ExecutionContext cx, Object value) {
        // Inlined: `if (Type.isString(value)) return Type.stringValue(value);`
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof ConsString) {
            return (ConsString) value;
        }
        return ToStringSlow(cx, value);
    }

    /**
     * 7.1.12 ToString ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the string result
     */
    private static CharSequence ToStringSlow(ExecutionContext cx, Object value) {
        switch (Type.of(value)) {
        case Undefined:
            return "undefined";
        case Null:
            return "null";
        case Boolean:
            return Type.booleanValue(value) ? "true" : "false";
        case Number:
            return ToString(Type.numberValue(value));
        case Symbol:
            throw newTypeError(cx, Messages.Key.SymbolString);
        case SIMD:
            return SIMD.ToString(Type.simdValue(value));
        case BigInt:
            return Type.bigIntValue(value).toString();
        case Object:
            Object primValue = ToPrimitive(cx, Type.objectValue(value), ToPrimitiveHint.String);
            return ToString(cx, primValue);
        case String:
        default:
            throw new AssertionError();
        }
    }

    private static final String[] cachedIntegerStrings = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

    /**
     * 7.1.12.1 ToString Applied to the Number Type
     * 
     * @param value
     *            the argument value
     * @return the string result
     */
    public static String ToString(int value) {
        if (0 <= value && value <= 9) {
            return cachedIntegerStrings[value];
        }
        return Integer.toString(value);
    }

    /**
     * 7.1.12.1 ToString Applied to the Number Type
     * 
     * @param value
     *            the argument value
     * @return the string result
     */
    public static String ToString(long value) {
        int intValue = (int) value;
        if (intValue == value) {
            return ToString(intValue);
        }
        if (-0x1F_FFFF_FFFF_FFFFL <= value && value <= 0x1F_FFFF_FFFF_FFFFL) {
            return Long.toString(value);
        }
        return ToStringSlow((double) value);
    }

    /**
     * 7.1.12.1 ToString Applied to the Number Type
     * 
     * @param value
     *            the argument value
     * @return the string result
     */
    public static String ToString(double value) {
        /* steps 1-4 (+ shortcut for integer values) */
        int intValue = (int) value;
        if (intValue == value) {
            return ToString(intValue);
        }
        if (value != value) {
            return "NaN";
        }
        if (value == Double.POSITIVE_INFINITY) {
            return "Infinity";
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return "-Infinity";
        }
        return ToStringSlow(value);
    }

    /**
     * 7.1.12.1 ToString Applied to the Number Type
     * 
     * @param value
     *            the argument value
     * @return the string result
     */
    public static String ToString(BigInteger value) {
        return value.toString();
    }

    /**
     * 7.1.12.1 ToString Applied to the Number Type
     * 
     * @param value
     *            the argument value
     * @return the string result
     */
    public static String ToString(Number value) {
        if (Type.isNumber(value)) {
            return ToString(value.doubleValue());
        }
        assert Type.isBigInt(value);
        return ToString(Type.bigIntValue(value));
    }

    private static String ToStringSlow(double value) {
        // call DToA for general number-to-string
        String result = FastDtoa.numberToString(value);
        if (result != null) {
            return result;
        }
        StringBuilder buffer = new StringBuilder();
        DToA.JS_dtostr(buffer, DToA.DTOSTR_STANDARD, 0, value);
        return buffer.toString();
    }

    /**
     * 7.1.13 ToObject ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the object result
     */
    public static ScriptObject ToObject(ExecutionContext cx, Object value) {
        // Inlined: `if (Type.isObject(value)) return Type.objectValue(value);`
        if (value instanceof ScriptObject) {
            return (ScriptObject) value;
        }
        return ToObjectSlow(cx, value);
    }

    /**
     * 7.1.13 ToObject ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the object result
     */
    private static ScriptObject ToObjectSlow(ExecutionContext cx, Object value) {
        switch (Type.of(value)) {
        case Undefined:
        case Null:
            throw newTypeError(cx, Messages.Key.UndefinedOrNull);
        case Boolean:
            return BooleanCreate(cx, Type.booleanValue(value));
        case Number:
            return NumberCreate(cx, Type.numberValue(value));
        case String:
            return StringCreate(cx, Type.stringValue(value));
        case Symbol:
            return SymbolCreate(cx, Type.symbolValue(value));
        case SIMD:
            // FIXME: spec bug - unclear/invalid description.
            return SIMDCreate(cx, Type.simdValue(value));
        case BigInt:
            return BigIntObject.BigIntCreate(cx, Type.bigIntValue(value));
        case Object:
        default:
            throw new AssertionError();
        }
    }

    /**
     * 7.1.14 ToPropertyKey ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the property key
     */
    public static Object ToPropertyKey(ExecutionContext cx, Object value) {
        /* step 1 */
        Object key = ToPrimitive(cx, value, ToPrimitiveHint.String);
        /* step 2 */
        if (key instanceof Symbol) {
            return key;
        }
        /* step 3 */
        return ToFlatString(cx, key);
    }

    /**
     * 7.1.15 ToLength ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the length value
     */
    public static long ToLength(ExecutionContext cx, Object value) {
        /* step 1 */
        long len = (long) ToNumber(cx, value); // ToInteger
        /* step 2 */
        if (len <= 0) {
            return 0;
        }
        /* step 3 */
        return Math.min(len, 0x1F_FFFF_FFFF_FFFFL);
    }

    /**
     * 7.1.15 ToLength ( argument )
     * 
     * @param value
     *            the argument value
     * @return the length value
     */
    public static long ToLength(double value) {
        /* step 1 (not applicable) */
        /* step 2 */
        if (value <= 0) {
            return 0;
        }
        /* step 3 */
        return Math.min((long) value, 0x1F_FFFF_FFFF_FFFFL);
    }

    /**
     * 7.1.16 CanonicalNumericIndexString ( argument )
     * 
     * @param value
     *            the argument value
     * @return the canonical number or -1 if not canonical
     */
    public static long CanonicalNumericIndexString(String value) {
        // Shortcut if value does not start with a valid canonical numeric index character
        if (value.isEmpty() || !isCanonicalNumericIndexStringPrefix(value.charAt(0))) {
            return -1;
        }
        /* step 1 (not applicable) */
        /* step 2 */
        if ("-0".equals(value)) {
            return Long.MAX_VALUE;
        }
        /* step 3 */
        double n = ToNumberParser.readDecimalLiteral(value);
        /* step 4 */
        if (!value.equals(ToString(n))) {
            return -1;
        }
        // Directly perform IsInteger() check and encode negative and non-integer indices as OOB.
        if (n < 0 || !IsInteger(n)) {
            return Long.MAX_VALUE;
        }
        /* step 5 */
        return (long) n;
    }

    private static boolean isCanonicalNumericIndexStringPrefix(char c) {
        return ('0' <= c && c <= '9') || c == '-' || c == 'I' || c == 'N';
    }

    /**
     * 7.1.17 ToIndex ( value )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the index value
     */
    public static long ToIndex(ExecutionContext cx, Object value) {
        /* steps 1, 3 */
        // FIXME: spec issue - extra branching for `value == undefined` not necessary.
        if (Type.isUndefined(value)) {
            return 0;
        }
        /* step 2.a */
        long integerIndex = (long) ToNumber(cx, value); // ToInteger
        /* step 2.b */
        if (integerIndex < 0) {
            throw newRangeError(cx, Messages.Key.NegativeArrayIndex);
        }
        /* steps 2.c-d */
        if (integerIndex > 0x1F_FFFF_FFFF_FFFFL) { // ToLength, SameValueZero
            throw newRangeError(cx, Messages.Key.InvalidArrayIndex);
        }
        /* step 3 */
        return integerIndex;
    }

    /**
     * ToNumeric ( value )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the numeric value
     */
    public static Number ToNumeric(ExecutionContext cx, Object value) {
        /* step 1 */
        Object primValue = ToPrimitive(cx, value, ToPrimitiveHint.Number);
        /* step 2 */
        if (Type.isBigInt(primValue)) {
            return Type.bigIntValue(primValue);
        }
        /* step 3 */
        return ToNumber(cx, primValue);
    }

    /**
     * ToNumeric ( value )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the numeric value
     */
    public static Number ToNumericInt32(ExecutionContext cx, Object value) {
        /* step 1 */
        Object primValue = ToPrimitive(cx, value, ToPrimitiveHint.Number);
        /* step 2 */
        if (Type.isBigInt(primValue)) {
            return Type.bigIntValue(primValue);
        }
        /* step 3 */
        return ToInt32(cx, primValue);
    }

    /**
     * 7.2.1 RequireObjectCoercible ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return the input argument unless it is either <code>undefined</code> or <code>null</code>
     */
    public static Object RequireObjectCoercible(ExecutionContext cx, Object value) {
        if (Type.isUndefinedOrNull(value)) {
            throw newTypeError(cx, Messages.Key.UndefinedOrNull);
        }
        return value;
    }

    /**
     * 7.2.2 IsArray ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return {@code true} if the argument is an Array object
     */
    public static boolean IsArray(ExecutionContext cx, Object value) {
        /* step 1 (implicit) */
        /* step 2 */
        if (value instanceof ArrayObject) {
            return true;
        }
        /* step 3 */
        if (value instanceof ProxyObject) {
            return ((ProxyObject) value).unwrap(cx) instanceof ArrayObject;
        }
        /* step 4 */
        return false;
    }

    /**
     * 7.2.3 IsCallable ( argument )
     * 
     * @param value
     *            the argument value
     * @return {@code true} if the value is a callable object
     */
    public static boolean IsCallable(Object value) {
        /* steps 1-3 */
        return value instanceof Callable;
    }

    /**
     * 7.2.4 IsConstructor ( argument )
     * 
     * @param value
     *            the argument value
     * @return {@code true} if the value is a constructor object
     */
    public static boolean IsConstructor(Object value) {
        /* steps 1-3 */
        return value instanceof Constructor;
    }

    /**
     * 7.2.5 IsExtensible (O)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @return {@code true} if the object is extensible
     */
    public static boolean IsExtensible(ExecutionContext cx, ScriptObject object) {
        /* steps 1-2 */
        return object.isExtensible(cx);
    }

    /**
     * 7.2.6 IsInteger ( argument )
     * 
     * @param value
     *            the argument value
     * @return {@code true} if the value is a finite integer
     */
    public static boolean IsInteger(Object value) {
        /* step 1 */
        if (!Type.isNumber(value)) {
            return false;
        }
        double d = Type.numberValue(value);
        /* step 2 */
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return false;
        }
        /* step 3 */
        if (Math.floor(Math.abs(d)) != Math.abs(d)) {
            return false;
        }
        /* step 4 */
        return true;
    }

    /**
     * 7.2.6 IsInteger ( argument )
     * 
     * @param value
     *            the argument value
     * @return {@code true} if the value is a finite integer
     */
    public static boolean IsInteger(double value) {
        /* step 1 (not applicable) */
        double d = value;
        /* step 2 */
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return false;
        }
        /* step 3 */
        if (Math.floor(Math.abs(d)) != Math.abs(d)) {
            return false;
        }
        /* step 4 */
        return true;
    }

    /**
     * 7.2.7 IsPropertyKey ( argument )
     * 
     * @param value
     *            the argument value
     * @return {@code true} if the value is a property key
     */
    public static boolean IsPropertyKey(Object value) {
        /* steps 1-3 */
        return value instanceof String || value instanceof Symbol;
    }

    /**
     * 7.2.8 IsRegExp ( argument )
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @return {@code true} if the value is a regular expression object
     */
    public static boolean IsRegExp(ExecutionContext cx, Object value) {
        /* step 1 */
        if (!Type.isObject(value)) {
            return false;
        }
        ScriptObject object = Type.objectValue(value);
        /* step 2 */
        Object isRegExp = Get(cx, object, BuiltinSymbol.match.get());
        /* step 3 */
        if (!Type.isUndefined(isRegExp)) {
            return ToBoolean(isRegExp);
        }
        /* step 4 */
        if (object instanceof RegExpObject) {
            return true;
        }
        /* step 5 */
        return false;
    }

    /**
     * 7.2.9 SameValue(x, y)<br>
     * 7.2.11 SameValueNonNumber (x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return {@code true} if both operands have the same value
     */
    public static boolean SameValue(Object x, Object y) {
        // Fast path for same reference.
        if (x == y) {
            return true;
        }
        Type tx = Type.of(x);
        Type ty = Type.of(y);
        /* step 1 */
        if (tx != ty) {
            return false;
        }
        /* step 2 */
        if (tx == Type.Number) {
            double dx = Type.numberValue(x);
            double dy = Type.numberValue(y);
            return Double.compare(dx, dy) == 0;
        }
        /* SameValueNonNumber, step 3 (Type(x) == Undefined; not applicable, handled by fast path) */
        /* SameValueNonNumber, step 4 (Type(x) == Null; not applicable, handled by fast path) */
        /* SameValueNonNumber, step 5 */
        if (tx == Type.String) {
            CharSequence sx = Type.stringValue(x);
            CharSequence sy = Type.stringValue(y);
            return sx.length() == sy.length() && sx.toString().equals(sy.toString());
        }
        /* SameValueNonNumber, step 6 */
        if (tx == Type.Boolean) {
            return Type.booleanValue(x) == Type.booleanValue(y);
        }
        /* Extension: SIMD */
        if (tx == Type.SIMD) {
            return SIMD.SameValue(Type.simdValue(x), Type.simdValue(y));
        }
        /* Extension: BigInt */
        if (tx == Type.BigInt) {
            return BigIntType.sameValue(Type.bigIntValue(x), Type.bigIntValue(y));
        }
        /* SameValueNonNumber, steps 7-8 (not applicable) */
        return false;
    }

    /**
     * 7.2.9 SameValue(x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return {@code true} if both operands have the same value
     */
    public static boolean SameValue(double x, double y) {
        /* steps 1, 3 (not applicable) */
        /* step 2 */
        return Double.compare(x, y) == 0;
    }

    /**
     * 7.2.10 SameValueZero(x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return {@code true} if both operands have the same value
     */
    public static boolean SameValue(Number x, Number y) {
        if (Type.isNumber(x)) {
            return Type.isNumber(y) && SameValue(x.doubleValue(), y.doubleValue());
        }
        assert Type.isBigInt(x);
        return Type.isBigInt(y) && BigIntType.sameValue(Type.bigIntValue(x), Type.bigIntValue(y));
    }

    /**
     * 7.2.10 SameValueZero(x, y)<br>
     * 7.2.11 SameValueNonNumber (x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return {@code true} if both operands have the same value
     */
    public static boolean SameValueZero(Object x, Object y) {
        // Fast path for same reference.
        if (x == y) {
            return true;
        }
        Type tx = Type.of(x);
        Type ty = Type.of(y);
        /* step 1 */
        if (tx != ty) {
            return false;
        }
        /* step 2 */
        if (tx == Type.Number) {
            double dx = Type.numberValue(x);
            double dy = Type.numberValue(y);
            return dx == dy || (Double.isNaN(dx) && Double.isNaN(dy));
        }
        /* SameValueNonNumber, step 3 (Type(x) == Undefined; not applicable, handled by fast path) */
        /* SameValueNonNumber, step 4 (Type(x) == Null; not applicable, handled by fast path) */
        /* SameValueNonNumber, step 5 */
        if (tx == Type.String) {
            CharSequence sx = Type.stringValue(x);
            CharSequence sy = Type.stringValue(y);
            return sx.length() == sy.length() && sx.toString().equals(sy.toString());
        }
        /* SameValueNonNumber, step 6 */
        if (tx == Type.Boolean) {
            return Type.booleanValue(x) == Type.booleanValue(y);
        }
        /* Extension: SIMD */
        if (tx == Type.SIMD) {
            return SIMD.SameValueZero(Type.simdValue(x), Type.simdValue(y));
        }
        /* Extension: BigInt */
        if (tx == Type.BigInt) {
            return BigIntType.sameValueZero(Type.bigIntValue(x), Type.bigIntValue(y));
        }
        /* SameValueNonNumber, steps 7-8 (not applicable) */
        return false;
    }

    /**
     * 7.2.10 SameValueZero(x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return {@code true} if both operands have the same value
     */
    public static boolean SameValueZero(double x, double y) {
        /* steps 1, 3 (not applicable) */
        /* step 2 */
        return x == y || (Double.isNaN(x) && Double.isNaN(y));
    }

    /**
     * 7.2.10 SameValueZero(x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return {@code true} if both operands have the same value
     */
    public static boolean SameValueZero(Number x, Number y) {
        if (Type.isNumber(x)) {
            return Type.isNumber(y) && SameValueZero(x.doubleValue(), y.doubleValue());
        }
        assert Type.isBigInt(x);
        return Type.isBigInt(y) && BigIntType.sameValueZero(Type.bigIntValue(x), Type.bigIntValue(y));
    }

    /**
     * 7.2.12 Abstract Relational Comparison
     * 
     * @param cx
     *            the execution context
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @param leftFirst
     *            the operation order flag
     * @return the comparison result
     */
    public static int RelationalComparison(ExecutionContext cx, Object x, Object y, boolean leftFirst) {
        // true -> 1
        // false -> 0
        // undefined -> -1
        /* steps 1-2 */
        Object px, py;
        if (leftFirst) {
            px = ToPrimitive(cx, x, ToPrimitiveHint.Number);
            py = ToPrimitive(cx, y, ToPrimitiveHint.Number);
        } else {
            py = ToPrimitive(cx, y, ToPrimitiveHint.Number);
            px = ToPrimitive(cx, x, ToPrimitiveHint.Number);
        }
        /* step 3 */
        if (Type.isString(px) && Type.isString(py)) {
            int c = Type.stringValue(px).toString().compareTo(Type.stringValue(py).toString());
            return c < 0 ? 1 : 0;
        }

        // Extension: BigInt
        if (Type.isBigInt(px)) {
            if (Type.isBigInt(py)) {
                return BigIntType.lessThan(Type.bigIntValue(x), Type.bigIntValue(py)) ? 1 : 0;
            }
            return RelationalComparison(Type.bigIntValue(x), ToNumber(cx, py));
        }
        if (Type.isBigInt(py)) {
            return RelationalComparison(ToNumber(cx, px), Type.bigIntValue(y));
        }

        /* step 4 */
        double nx = ToNumber(cx, px);
        double ny = ToNumber(cx, py);
        if (Double.isNaN(nx) || Double.isNaN(ny)) {
            return -1;
        }
        if (nx == ny) {
            return 0;
        }
        if (nx == Double.POSITIVE_INFINITY) {
            return 0;
        }
        if (ny == Double.POSITIVE_INFINITY) {
            return 1;
        }
        if (ny == Double.NEGATIVE_INFINITY) {
            return 0;
        }
        if (nx == Double.NEGATIVE_INFINITY) {
            return 1;
        }
        return nx < ny ? 1 : 0;
    }

    private static int RelationalComparison(BigInteger x, double ny) {
        if (Double.isNaN(ny)) {
            return -1;
        }
        if (ny == Double.POSITIVE_INFINITY) {
            return 1;
        }
        if (ny == Double.NEGATIVE_INFINITY) {
            return 0;
        }
        if (x.signum() == 0) {
            return ny > 0 ? 1 : 0;
        }
        if (x.signum() > 0) {
            if (ny <= 0) {
                return 0;
            }
            double y = Math.floor(ny);
            int r = x.compareTo(fromInteger(y));
            return r < 0 || (r == 0 && ny != y) ? 1 : 0;
        }
        assert x.signum() < 0;
        if (ny >= 0) {
            return 1;
        }
        double y = Math.ceil(ny);
        int r = x.compareTo(fromInteger(y));
        return r < 0 ? 1 : 0;
    }

    private static int RelationalComparison(double nx, BigInteger y) {
        if (Double.isNaN(nx)) {
            return -1;
        }
        if (nx == Double.NEGATIVE_INFINITY) {
            return 1;
        }
        if (nx == Double.POSITIVE_INFINITY) {
            return 0;
        }
        if (y.signum() == 0) {
            return nx < 0 ? 1 : 0;
        }
        if (y.signum() > 0) {
            if (nx <= 0) {
                return 1;
            }
            double x = Math.floor(nx);
            int r = fromInteger(x).compareTo(y);
            return r < 0 ? 1 : 0;
        }
        assert y.signum() < 0;
        if (nx >= 0) {
            return 0;
        }
        double x = Math.ceil(nx);
        int r = fromInteger(x).compareTo(y);
        return r < 0 || (r == 0 && nx != x) ? 1 : 0;
    }

    /**
     * 7.2.13 Abstract Equality Comparison
     * 
     * @param cx
     *            the execution context
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return the comparison result
     */
    public static boolean EqualityComparison(ExecutionContext cx, Object x, Object y) {
        // Fast path for same reference.
        if (x == y) {
            if (x instanceof Double) {
                return !((Double) x).isNaN();
            }
            if (!(x instanceof SIMDValue)) {
                return true;
            }
        }
        Type tx = Type.of(x);
        Type ty = Type.of(y);
        /* step 1 */
        if (tx == ty) {
            return StrictEqualityComparison(x, y);
        }
        /* step 2 */
        if (tx == Type.Null && ty == Type.Undefined) {
            return true;
        }
        /* step 3 */
        if (tx == Type.Undefined && ty == Type.Null) {
            return true;
        }

        if (cx.getRuntimeContext().isEnabled(CompatibilityOption.IsHTMLDDAObjects)) {
            if (tx == Type.Object && (ty == Type.Null || ty == Type.Undefined)) {
                return x instanceof HTMLDDAObject;
            }
            if ((tx == Type.Null || tx == Type.Undefined) && ty == Type.Object) {
                return y instanceof HTMLDDAObject;
            }
        }

        /* step 4 */
        if (tx == Type.Number && ty == Type.String) {
            // return EqualityComparison(cx, x, ToNumber(cx, y));
            return Type.numberValue(x) == ToNumber(Type.stringValue(y));
        }
        /* step 5 */
        if (tx == Type.String && ty == Type.Number) {
            // return EqualityComparison(cx, ToNumber(cx, x), y);
            return ToNumber(Type.stringValue(x)) == Type.numberValue(y);
        }

        // Extension: BigInt
        if (tx == Type.BigInt && ty == Type.String) {
            BigInteger n = BigIntAbstractOperations.StringToBigInt(Type.stringValue(y));
            if (n == null) {
                return false;
            }
            return BigIntType.equal(Type.bigIntValue(x), n);
        }
        if (tx == Type.String && ty == Type.BigInt) {
            BigInteger n = BigIntAbstractOperations.StringToBigInt(Type.stringValue(x));
            if (n == null) {
                return false;
            }
            return BigIntType.equal(n, Type.bigIntValue(y));
        }

        /* step 6 */
        if (tx == Type.Boolean) {
            return EqualityComparison(cx, Type.booleanValue(x) ? 1 : 0, y);
        }
        /* step 7 */
        if (ty == Type.Boolean) {
            return EqualityComparison(cx, x, Type.booleanValue(y) ? 1 : 0);
        }
        /* step 8 */
        if ((tx == Type.String || tx == Type.Number || tx == Type.BigInt || tx == Type.Symbol || tx == Type.SIMD)
                && ty == Type.Object) {
            return EqualityComparison(cx, x, ToPrimitive(cx, Type.objectValue(y)));
        }
        /* step 9 */
        if (tx == Type.Object && (ty == Type.String || ty == Type.Number || ty == Type.BigInt || ty == Type.Symbol
                || ty == Type.SIMD)) {
            return EqualityComparison(cx, ToPrimitive(cx, Type.objectValue(x)), y);
        }

        // Extension: BigInt
        if (tx == Type.BigInt && ty == Type.Number) {
            return EqualityComparison(Type.bigIntValue(x), Type.numberValue(y));
        }
        if (tx == Type.Number && ty == Type.BigInt) {
            return EqualityComparison(Type.bigIntValue(y), Type.numberValue(x));
        }

        /* step 10 */
        return false;
    }

    private static boolean EqualityComparison(BigInteger x, double y) {
        if (!Double.isFinite(y) || Math.rint(y) != y) {
            return false;
        }
        return Type.bigIntValue(x).equals(fromInteger(y));
    }

    private static BigInteger fromInteger(double d) {
        assert Double.isFinite(d) && Math.rint(d) == d;

        long longValue = (long) d;
        if (longValue == d) {
            return BigInteger.valueOf(longValue);
        }

        boolean negative = d < 0;
        if (negative) {
            d = -d;
        }
        long bits = Double.doubleToLongBits(d);
        int exponent = (int) (bits >> 52) & 0x7ff;
        long mantissa = bits & 0xf_ffff_ffff_ffffL;
        if (exponent == 0) {
            mantissa <<= 1;
        } else {
            mantissa |= 0x10_0000_0000_0000L;
        }
        if (negative) {
            mantissa = -mantissa;
        }
        return BigInteger.valueOf(mantissa).shiftLeft(exponent - 1075);
    }

    /**
     * 7.2.14 Strict Equality Comparison<br>
     * 7.2.11 SameValueNonNumber (x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return the comparison result
     */
    public static boolean StrictEqualityComparison(Object x, Object y) {
        // Fast path for same reference.
        if (x == y) {
            if (x instanceof Double) {
                return !((Double) x).isNaN();
            }
            if (!(x instanceof SIMDValue)) {
                return true;
            }
        }
        Type tx = Type.of(x);
        Type ty = Type.of(y);
        /* step 1 */
        if (tx != ty) {
            return false;
        }
        /* step 2 */
        if (tx == Type.Number) {
            return Type.numberValue(x) == Type.numberValue(y);
        }
        /* SameValueNonNumber, step 3 (Type(x) == Undefined; not applicable, handled by fast path) */
        /* SameValueNonNumber, step 4 (Type(x) == Null; not applicable, handled by fast path) */
        /* SameValueNonNumber, step 5 */
        if (tx == Type.String) {
            CharSequence sx = Type.stringValue(x);
            CharSequence sy = Type.stringValue(y);
            return sx.length() == sy.length() && sx.toString().equals(sy.toString());
        }
        /* SameValueNonNumber, step 6 */
        if (tx == Type.Boolean) {
            return Type.booleanValue(x) == Type.booleanValue(y);
        }
        /* Extension: SIMD */
        if (tx == Type.SIMD) {
            return SIMD.StrictEquality(Type.simdValue(x), Type.simdValue(y));
        }
        /* Extension: BigInt */
        if (tx == Type.BigInt) {
            return BigIntType.equal(Type.bigIntValue(x), Type.bigIntValue(y));
        }
        /* SameValueNonNumber, steps 7-8 (not applicable) */
        return false;
    }

    /**
     * 7.2.10 SameValueZero(x, y)
     * 
     * @param x
     *            the first operand
     * @param y
     *            the second operand
     * @return {@code true} if both operands have the same value
     */
    public static boolean StrictEqualityComparison(Number x, Number y) {
        if (Type.isNumber(x)) {
            return Type.isNumber(y) && (x.doubleValue() == y.doubleValue());
        }
        assert Type.isBigInt(x);
        return Type.isBigInt(y) && BigIntType.equal(Type.bigIntValue(x), Type.bigIntValue(y));
    }

    /**
     * 7.3.1 Get (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object Get(ExecutionContext cx, ScriptObject object, Object propertyKey) {
        /* steps 1-3 */
        return object.get(cx, propertyKey, object);
    }

    /**
     * 7.3.1 Get (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object Get(ExecutionContext cx, ScriptObject object, long propertyKey) {
        /* steps 1-3 */
        return object.get(cx, propertyKey, object);
    }

    /**
     * 7.3.1 Get (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object Get(ExecutionContext cx, ScriptObject object, String propertyKey) {
        /* steps 1-3 */
        return object.get(cx, propertyKey, object);
    }

    /**
     * 7.3.1 Get (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object Get(ExecutionContext cx, ScriptObject object, Symbol propertyKey) {
        /* steps 1-3 */
        return object.get(cx, propertyKey, object);
    }

    /**
     * 7.3.2 GetV (V, P)
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object GetV(ExecutionContext cx, Object value, Object propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        ScriptObject obj = ToObject(cx, value);
        /* step 3 */
        return obj.get(cx, propertyKey, value);
    }

    /**
     * 7.3.2 GetV (V, P)
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object GetV(ExecutionContext cx, Object value, long propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        ScriptObject obj = ToObject(cx, value);
        /* step 3 */
        return obj.get(cx, propertyKey, value);
    }

    /**
     * 7.3.2 GetV (V, P)
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object GetV(ExecutionContext cx, Object value, String propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        ScriptObject obj = ToObject(cx, value);
        /* step 3 */
        return obj.get(cx, propertyKey, value);
    }

    /**
     * 7.3.2 GetV (V, P)
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the argument value
     * @param propertyKey
     *            the property key
     * @return the property value
     */
    public static Object GetV(ExecutionContext cx, Object value, Symbol propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        ScriptObject obj = ToObject(cx, value);
        /* step 3 */
        return obj.get(cx, propertyKey, value);
    }

    /**
     * 7.3.3 Set (O, P, V, Throw)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @param _throw
     *            the throw flag
     */
    public static void Set(ExecutionContext cx, ScriptObject object, Object propertyKey, Object value, boolean _throw) {
        /* steps 1-3 (not applicable) */
        /* step 4 */
        boolean success = object.set(cx, propertyKey, value, object);
        /* step 5 */
        if (!success && _throw) {
            throw newTypeError(cx, Messages.Key.PropertyNotModifiable, propertyKey.toString());
        }
        /* step 6 (not applicable) */
    }

    /**
     * 7.3.3 Set (O, P, V, Throw)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @param _throw
     *            the throw flag
     */
    public static void Set(ExecutionContext cx, ScriptObject object, long propertyKey, Object value, boolean _throw) {
        /* steps 1-3 (not applicable) */
        /* step 4 */
        boolean success = object.set(cx, propertyKey, value, object);
        /* step 5 */
        if (!success && _throw) {
            throw newTypeError(cx, Messages.Key.PropertyNotModifiable, ToString(propertyKey));
        }
        /* step 6 (not applicable) */
    }

    /**
     * 7.3.3 Set (O, P, V, Throw)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @param _throw
     *            the throw flag
     */
    public static void Set(ExecutionContext cx, ScriptObject object, String propertyKey, Object value, boolean _throw) {
        /* steps 1-3 (not applicable) */
        /* step 4 */
        boolean success = object.set(cx, propertyKey, value, object);
        /* step 5 */
        if (!success && _throw) {
            throw newTypeError(cx, Messages.Key.PropertyNotModifiable, propertyKey);
        }
        /* step 6 (not applicable) */
    }

    /**
     * 7.3.3 Set (O, P, V, Throw)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @param _throw
     *            the throw flag
     */
    public static void Set(ExecutionContext cx, ScriptObject object, Symbol propertyKey, Object value, boolean _throw) {
        /* steps 1-3 (not applicable) */
        /* step 4 */
        boolean success = object.set(cx, propertyKey, value, object);
        /* step 5 */
        if (!success && _throw) {
            throw newTypeError(cx, Messages.Key.PropertyNotModifiable, propertyKey.toString());
        }
        /* step 6 (not applicable) */
    }

    /**
     * 7.3.4 CreateDataProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateDataProperty(ExecutionContext cx, ScriptObject object, Object propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, true, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.4 CreateDataProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateDataProperty(ExecutionContext cx, ScriptObject object, long propertyKey, Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, true, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.4 CreateDataProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateDataProperty(ExecutionContext cx, ScriptObject object, String propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, true, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.4 CreateDataProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateDataProperty(ExecutionContext cx, ScriptObject object, Symbol propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, true, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.5 CreateMethodProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateMethodProperty(ExecutionContext cx, ScriptObject object, Object propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, false, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.5 CreateMethodProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateMethodProperty(ExecutionContext cx, ScriptObject object, long propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, false, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.5 CreateMethodProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateMethodProperty(ExecutionContext cx, ScriptObject object, String propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, false, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.5 CreateMethodProperty (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     * @return {@code true} on success
     */
    public static boolean CreateMethodProperty(ExecutionContext cx, ScriptObject object, Symbol propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        PropertyDescriptor newDesc = new PropertyDescriptor(value, true, false, true);
        /* step 4 */
        return object.defineOwnProperty(cx, propertyKey, newDesc);
    }

    /**
     * 7.3.6 CreateDataPropertyOrThrow (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     */
    public static void CreateDataPropertyOrThrow(ExecutionContext cx, ScriptObject object, Object propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = CreateDataProperty(cx, object, propertyKey, value);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, propertyKey.toString());
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.6 CreateDataPropertyOrThrow (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     */
    public static void CreateDataPropertyOrThrow(ExecutionContext cx, ScriptObject object, long propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = CreateDataProperty(cx, object, propertyKey, value);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, ToString(propertyKey));
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.6 CreateDataPropertyOrThrow (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     */
    public static void CreateDataPropertyOrThrow(ExecutionContext cx, ScriptObject object, String propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = CreateDataProperty(cx, object, propertyKey, value);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, propertyKey);
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.6 CreateDataPropertyOrThrow (O, P, V)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param value
     *            the new property value
     */
    public static void CreateDataPropertyOrThrow(ExecutionContext cx, ScriptObject object, Symbol propertyKey,
            Object value) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = CreateDataProperty(cx, object, propertyKey, value);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, propertyKey.toString());
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.7 DefinePropertyOrThrow (O, P, desc)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param desc
     *            the property descriptor
     */
    public static void DefinePropertyOrThrow(ExecutionContext cx, ScriptObject object, Object propertyKey,
            PropertyDescriptor desc) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.defineOwnProperty(cx, propertyKey, desc);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, propertyKey.toString());
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.7 DefinePropertyOrThrow (O, P, desc)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param desc
     *            the property descriptor
     */
    public static void DefinePropertyOrThrow(ExecutionContext cx, ScriptObject object, long propertyKey,
            PropertyDescriptor desc) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.defineOwnProperty(cx, propertyKey, desc);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, ToString(propertyKey));
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.7 DefinePropertyOrThrow (O, P, desc)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param desc
     *            the property descriptor
     */
    public static void DefinePropertyOrThrow(ExecutionContext cx, ScriptObject object, String propertyKey,
            PropertyDescriptor desc) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.defineOwnProperty(cx, propertyKey, desc);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, propertyKey);
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.7 DefinePropertyOrThrow (O, P, desc)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param desc
     *            the property descriptor
     */
    public static void DefinePropertyOrThrow(ExecutionContext cx, ScriptObject object, Symbol propertyKey,
            PropertyDescriptor desc) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.defineOwnProperty(cx, propertyKey, desc);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotCreatable, propertyKey.toString());
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.8 DeletePropertyOrThrow (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     */
    public static void DeletePropertyOrThrow(ExecutionContext cx, ScriptObject object, Object propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.delete(cx, propertyKey);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotDeletable, propertyKey.toString());
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.8 DeletePropertyOrThrow (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     */
    public static void DeletePropertyOrThrow(ExecutionContext cx, ScriptObject object, long propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.delete(cx, propertyKey);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotDeletable, ToString(propertyKey));
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.8 DeletePropertyOrThrow (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     */
    public static void DeletePropertyOrThrow(ExecutionContext cx, ScriptObject object, String propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.delete(cx, propertyKey);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotDeletable, propertyKey);
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.8 DeletePropertyOrThrow (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     */
    public static void DeletePropertyOrThrow(ExecutionContext cx, ScriptObject object, Symbol propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        boolean success = object.delete(cx, propertyKey);
        /* step 4 */
        if (!success) {
            throw newTypeError(cx, Messages.Key.PropertyNotDeletable, propertyKey.toString());
        }
        /* step 5 (not applicable) */
    }

    /**
     * 7.3.9 GetMethod (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the method or {@code null} if not present
     */
    public static Callable GetMethod(ExecutionContext cx, Object object, Object propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Object func = GetV(cx, object, propertyKey);
        /* step 3 */
        if (Type.isUndefinedOrNull(func)) {
            return null;
        }
        /* step 4 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey.toString());
        }
        /* step 5 */
        return (Callable) func;
    }

    /**
     * 7.3.9 GetMethod (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the method or {@code null} if not present
     */
    public static Callable GetMethod(ExecutionContext cx, Object object, String propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Object func = GetV(cx, object, propertyKey);
        /* step 3 */
        if (Type.isUndefinedOrNull(func)) {
            return null;
        }
        /* step 4 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey);
        }
        /* step 5 */
        return (Callable) func;
    }

    /**
     * 7.3.9 GetMethod (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the method or {@code null} if not present
     */
    public static Callable GetMethod(ExecutionContext cx, Object object, Symbol propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Object func = GetV(cx, object, propertyKey);
        /* step 3 */
        if (Type.isUndefinedOrNull(func)) {
            return null;
        }
        /* step 4 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey.toString());
        }
        /* step 5 */
        return (Callable) func;
    }

    /**
     * 7.3.9 GetMethod (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the method or {@code null} if not present
     */
    public static Callable GetMethod(ExecutionContext cx, ScriptObject object, Object propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Object func = object.get(cx, propertyKey, object);
        /* step 3 */
        if (Type.isUndefinedOrNull(func)) {
            return null;
        }
        /* step 4 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey.toString());
        }
        /* step 5 */
        return (Callable) func;
    }

    /**
     * 7.3.9 GetMethod (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the method or {@code null} if not present
     */
    public static Callable GetMethod(ExecutionContext cx, ScriptObject object, String propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Object func = object.get(cx, propertyKey, object);
        /* step 3 */
        if (Type.isUndefinedOrNull(func)) {
            return null;
        }
        /* step 4 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey);
        }
        /* step 5 */
        return (Callable) func;
    }

    /**
     * 7.3.9 GetMethod (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return the method or {@code null} if not present
     */
    public static Callable GetMethod(ExecutionContext cx, ScriptObject object, Symbol propertyKey) {
        /* step 1 (not applicable) */
        /* step 2 */
        Object func = object.get(cx, propertyKey, object);
        /* step 3 */
        if (Type.isUndefinedOrNull(func)) {
            return null;
        }
        /* step 4 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey.toString());
        }
        /* step 5 */
        return (Callable) func;
    }

    /**
     * 7.3.10 HasProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasProperty(ExecutionContext cx, ScriptObject object, Object propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        return object.hasProperty(cx, propertyKey);
    }

    /**
     * 7.3.10 HasProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasProperty(ExecutionContext cx, ScriptObject object, long propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        return object.hasProperty(cx, propertyKey);
    }

    /**
     * 7.3.10 HasProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasProperty(ExecutionContext cx, ScriptObject object, String propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        return object.hasProperty(cx, propertyKey);
    }

    /**
     * 7.3.10 HasProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasProperty(ExecutionContext cx, ScriptObject object, Symbol propertyKey) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        return object.hasProperty(cx, propertyKey);
    }

    /**
     * 7.3.11 HasOwnProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasOwnProperty(ExecutionContext cx, ScriptObject object, Object propertyKey) {
        /* steps 1-2 (not applicable) */
        /* steps 3-5 */
        return object.hasOwnProperty(cx, propertyKey);
    }

    /**
     * 7.3.11 HasOwnProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasOwnProperty(ExecutionContext cx, ScriptObject object, long propertyKey) {
        /* steps 1-2 (not applicable) */
        /* steps 3-5 */
        return object.hasOwnProperty(cx, propertyKey);
    }

    /**
     * 7.3.11 HasOwnProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasOwnProperty(ExecutionContext cx, ScriptObject object, String propertyKey) {
        /* steps 1-2 (not applicable) */
        /* steps 3-5 */
        return object.hasOwnProperty(cx, propertyKey);
    }

    /**
     * 7.3.11 HasOwnProperty (O, P)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @return {@code true} if the property is present
     */
    public static boolean HasOwnProperty(ExecutionContext cx, ScriptObject object, Symbol propertyKey) {
        /* steps 1-2 (not applicable) */
        /* steps 3-5 */
        return object.hasOwnProperty(cx, propertyKey);
    }

    /**
     * 7.3.12 Call(F, V, [argumentsList])
     * 
     * @param cx
     *            the execution context
     * @param function
     *            the function object
     * @param thisValue
     *            the this value
     * @param argumentsList
     *            the function arguments
     * @return the function call return value
     */
    public static Object Call(ExecutionContext cx, Object function, Object thisValue, Object... argumentsList) {
        /* step 1 (not applicable) */
        /* step 2 */
        if (!IsCallable(function)) {
            throw newTypeError(cx, Messages.Key.NotCallable);
        }
        /* step 3 */
        return ((Callable) function).call(cx, thisValue, argumentsList);
    }

    /**
     * 7.3.12 Call(F, V, [argumentsList])
     * 
     * @param cx
     *            the execution context
     * @param function
     *            the function object
     * @param thisValue
     *            the this value
     * @param argumentsList
     *            the function arguments
     * @return the function call return value
     */
    public static Object Call(ExecutionContext cx, Callable function, Object thisValue, Object... argumentsList) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        return function.call(cx, thisValue, argumentsList);
    }

    /**
     * 7.3.13 Construct (F, [argumentsList], [newTarget])
     * 
     * @param cx
     *            the execution context
     * @param f
     *            the constructor function
     * @param argumentsList
     *            the constructor function arguments
     * @return the new object
     */
    public static ScriptObject Construct(ExecutionContext cx, Constructor f, Object... argumentsList) {
        /* steps 1-4 (not applicable) */
        /* step 5 */
        return f.construct(cx, argumentsList);
    }

    /**
     * 7.3.13 Construct (F, [argumentsList], [newTarget])
     * 
     * @param cx
     *            the execution context
     * @param f
     *            the constructor function
     * @param newTarget
     *            the newTarget constructor object
     * @param argumentsList
     *            the constructor function arguments
     * @return the new object
     */
    public static ScriptObject Construct(ExecutionContext cx, Constructor f, Constructor newTarget,
            Object... argumentsList) {
        /* steps 1-4 (not applicable) */
        /* step 5 */
        return f.construct(cx, newTarget, argumentsList);
    }

    /**
     * 7.3.14 SetIntegrityLevel (O, level)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param level
     *            the new integrity level
     * @return {@code true} on success
     */
    public static boolean SetIntegrityLevel(ExecutionContext cx, ScriptObject object, IntegrityLevel level) {
        /* steps 1-2 */
        assert level == IntegrityLevel.Sealed || level == IntegrityLevel.Frozen;
        /* steps 3-4 */
        if (!object.preventExtensions(cx)) {
            return false;
        }
        /* step 5 */
        List<?> keys = object.ownPropertyKeys(cx);
        /* steps 6-7 */
        if (level == IntegrityLevel.Sealed) {
            /* step 6 */
            PropertyDescriptor nonConfigurable = new PropertyDescriptor();
            nonConfigurable.setConfigurable(false);
            for (Object key : keys) {
                DefinePropertyOrThrow(cx, object, key, nonConfigurable);
            }
        } else {
            /* step 7 */
            PropertyDescriptor nonConfigurable = new PropertyDescriptor();
            nonConfigurable.setConfigurable(false);
            PropertyDescriptor nonConfigurableWritable = new PropertyDescriptor();
            nonConfigurableWritable.setConfigurable(false);
            nonConfigurableWritable.setWritable(false);
            for (Object key : keys) {
                Property currentDesc = object.getOwnProperty(cx, key);
                if (currentDesc != null) {
                    PropertyDescriptor desc;
                    if (currentDesc.isAccessorDescriptor()) {
                        desc = nonConfigurable;
                    } else {
                        desc = nonConfigurableWritable;
                    }
                    DefinePropertyOrThrow(cx, object, key, desc);
                }
            }
        }
        /* step 8 */
        return true;
    }

    /**
     * 7.3.15 TestIntegrityLevel (O, level)
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param level
     *            the integrity level to test
     * @return {@code true} if the object conforms to the integrity level
     */
    public static boolean TestIntegrityLevel(ExecutionContext cx, ScriptObject object, IntegrityLevel level) {
        /* steps 1-2 */
        assert level == IntegrityLevel.Sealed || level == IntegrityLevel.Frozen;
        boolean isFrozen = level == IntegrityLevel.Frozen;
        /* steps 3-4 */
        if (IsExtensible(cx, object)) {
            return false;
        }
        /* step 5 (note) */
        /* step 6 */
        List<?> keys = object.ownPropertyKeys(cx);
        /* step 7 */
        for (Object key : keys) {
            /* step 7.a */
            Property currentDesc = object.getOwnProperty(cx, key);
            /* step 7.b */
            if (currentDesc != null) {
                if (currentDesc.isConfigurable()) {
                    return false;
                }
                if (isFrozen && currentDesc.isDataDescriptor() && currentDesc.isWritable()) {
                    return false;
                }
            }
        }
        /* step 8 */
        return true;
    }

    /**
     * 7.3.16 CreateArrayFromList (elements)
     * 
     * @param cx
     *            the execution context
     * @param elements
     *            the array elements
     * @return the array object
     */
    public static ArrayObject CreateArrayFromList(ExecutionContext cx, Collection<?> elements) {
        /* steps 1-5 */
        return DenseArrayCreate(cx, elements);
    }

    /**
     * 7.3.16 CreateArrayFromList (elements)
     * 
     * @param cx
     *            the execution context
     * @param elements
     *            the array elements
     * @return the array object
     */
    public static ArrayObject CreateArrayFromList(ExecutionContext cx, List<?> elements) {
        /* steps 1-5 */
        return DenseArrayCreate(cx, elements);
    }

    /**
     * 7.3.16 CreateArrayFromList (elements)
     * 
     * @param cx
     *            the execution context
     * @param elements
     *            the array elements
     * @return the array object
     */
    public static ArrayObject CreateArrayFromList(ExecutionContext cx, Stream<?> elements) {
        /* steps 1-5 */
        return DenseArrayCreate(cx, elements);
    }

    /**
     * 7.3.16 CreateArrayFromList (elements)
     * 
     * @param cx
     *            the execution context
     * @param elements
     *            the array elements
     * @return the array object
     */
    public static ArrayObject CreateArrayFromList(ExecutionContext cx, Object... elements) {
        /* steps 1-5 */
        return DenseArrayCreate(cx, elements);
    }

    /**
     * 7.3.17 CreateListFromArrayLike (obj [, elementTypes] )
     * 
     * @param cx
     *            the execution context
     * @param obj
     *            the array-like object
     * @return the array elements
     */
    public static Object[] CreateListFromArrayLike(ExecutionContext cx, Object obj) {
        if (obj instanceof ArrayObject || obj instanceof ArgumentsObject) {
            // Fast-path for dense arrays/arguments
            OrdinaryObject array = (OrdinaryObject) obj;
            long len = array.getLength();
            if (array.isDenseArray(len)) {
                // CreateListFromArrayLike() is (currently) only used for argument arrays
                if (len > FunctionPrototype.getMaxArguments()) {
                    throw newRangeError(cx, Messages.Key.FunctionTooManyArguments);
                }
                return array.toArray(len);
            }
        }
        /* step 1 (not applicable) */
        /* step 2 */
        if (!Type.isObject(obj)) {
            throw newTypeError(cx, Messages.Key.NotObjectType);
        }
        ScriptObject object = Type.objectValue(obj);
        /* step 3 */
        long n = ToLength(cx, Get(cx, object, "length"));
        // CreateListFromArrayLike() is (currently) only used for argument arrays
        if (n > FunctionPrototype.getMaxArguments()) {
            throw newRangeError(cx, Messages.Key.FunctionTooManyArguments);
        }
        int length = (int) n;
        /* step 4 */
        Object[] list = new Object[length];
        /* steps 5-6 */
        for (int index = 0; index < length; ++index) {
            int indexName = index;
            Object next = Get(cx, object, indexName);
            list[index] = next;
        }
        /* step 7 */
        return list;
    }

    /**
     * 7.3.17 CreateListFromArrayLike (obj [, elementTypes] )
     * 
     * @param cx
     *            the execution context
     * @param obj
     *            the array-like object
     * @param elementTypes
     *            the set of allowed element types
     * @return the array elements
     */
    public static List<Object> CreateListFromArrayLike(ExecutionContext cx, Object obj, EnumSet<Type> elementTypes) {
        assert elementTypes.size() == 2 && elementTypes.contains(Type.String)
                && elementTypes.contains(Type.Symbol) : elementTypes;
        if (obj instanceof ArrayObject) {
            // Fast-path for dense arrays
            ArrayObject array = (ArrayObject) obj;
            if (array.isDenseArray()) {
                long len = array.getLength();
                if (len == 0) {
                    return Collections.emptyList();
                }
                Object[] list = array.toArray();
                for (int index = 0, length = list.length; index < length; ++index) {
                    Object next = list[index];
                    if (next instanceof String || next instanceof Symbol) {
                        // list[index] = next;
                    } else if (next instanceof ConsString) {
                        // enforce flat string
                        list[index] = ((ConsString) next).toString();
                    } else {
                        throw newTypeError(cx, Messages.Key.ProxyPropertyKey, Type.of(next).toString());
                    }
                }
                return Arrays.asList(list);
            }
        }
        /* step 1 (not applicable) */
        /* step 2 */
        if (!Type.isObject(obj)) {
            throw newTypeError(cx, Messages.Key.ProxyNotObject);
        }
        ScriptObject object = Type.objectValue(obj);
        /* step 3 */
        long n = ToLength(cx, Get(cx, object, "length"));
        int limit = (int) Math.min(n, Integer.MAX_VALUE);
        int initialLength = (int) Math.min(n, 0xffff);
        /* step 4 */
        ArrayList<Object> list = new ArrayList<>(initialLength);
        /* steps 5-6 */
        for (int index = 0; index < limit; ++index) {
            int indexName = index;
            Object next = Get(cx, object, indexName);
            if (next instanceof String || next instanceof Symbol) {
                list.add(next);
            } else if (next instanceof ConsString) {
                // enforce flat string
                list.add(((ConsString) next).toString());
            } else {
                throw newTypeError(cx, Messages.Key.ProxyPropertyKey, Type.of(next).toString());
            }
        }
        /* step 7 */
        return list;
    }

    /**
     * 7.3.18 Invoke(O, P, [argumentsList])
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param argumentsList
     *            the method call arguments
     * @return the method return value
     */
    public static Object Invoke(ExecutionContext cx, Object object, Object propertyKey, Object... argumentsList) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        Object func = GetV(cx, object, propertyKey);
        /* step 4 (inlined Call) */
        /* Call - steps 1-2 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey.toString());
        }
        /* Call - step 3 */
        return ((Callable) func).call(cx, object, argumentsList);
    }

    /**
     * 7.3.18 Invoke(O, P, [argumentsList])
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param argumentsList
     *            the method call arguments
     * @return the method return value
     */
    public static Object Invoke(ExecutionContext cx, Object object, String propertyKey, Object... argumentsList) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        Object func = GetV(cx, object, propertyKey);
        /* step 4 (inlined Call) */
        /* Call - steps 1-2 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey);
        }
        /* Call - step 3 */
        return ((Callable) func).call(cx, object, argumentsList);
    }

    /**
     * 7.3.18 Invoke(O, P, [argumentsList])
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param argumentsList
     *            the method call arguments
     * @return the method return value
     */
    public static Object Invoke(ExecutionContext cx, ScriptObject object, String propertyKey, Object... argumentsList) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        Object func = object.get(cx, propertyKey, object);
        /* step 4 (inlined Call) */
        /* Call - steps 1-2 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey);
        }
        /* Call - step 3 */
        return ((Callable) func).call(cx, object, argumentsList);
    }

    /**
     * 7.3.18 Invoke(O, P, [argumentsList])
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param argumentsList
     *            the method call arguments
     * @return the method return value
     */
    public static Object Invoke(ExecutionContext cx, Object object, Symbol propertyKey, Object... argumentsList) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        Object func = GetV(cx, object, propertyKey);
        /* step 4 (inlined Call) */
        /* Call - steps 1-2 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey.toString());
        }
        /* Call - step 3 */
        return ((Callable) func).call(cx, object, argumentsList);
    }

    /**
     * 7.3.18 Invoke(O, P, [argumentsList])
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param propertyKey
     *            the property key
     * @param argumentsList
     *            the method call arguments
     * @return the method return value
     */
    public static Object Invoke(ExecutionContext cx, ScriptObject object, Symbol propertyKey, Object... argumentsList) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        Object func = object.get(cx, propertyKey, object);
        /* step 4 (inlined Call) */
        /* Call - steps 1-2 */
        if (!IsCallable(func)) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, propertyKey.toString());
        }
        /* Call - step 3 */
        return ((Callable) func).call(cx, object, argumentsList);
    }

    /**
     * 7.3.19 OrdinaryHasInstance (C, O)
     * 
     * @param cx
     *            the execution context
     * @param c
     *            the constructor object
     * @param o
     *            the instance object
     * @return {@code true} on success
     */
    public static boolean OrdinaryHasInstance(ExecutionContext cx, Object c, Object o) {
        /* step 1 */
        if (!IsCallable(c)) {
            return false;
        }
        /* step 2 */
        if (c instanceof BoundFunctionObject) {
            Callable bc = ((BoundFunctionObject) c).getBoundTargetFunction();
            return InstanceofOperator(o, bc, cx);
        }
        /* step 3 */
        if (!Type.isObject(o)) {
            return false;
        }
        /* step 4 */
        Object p = Get(cx, (ScriptObject) c, "prototype");
        /* step 5 */
        if (!Type.isObject(p)) {
            throw newTypeError(cx, Messages.Key.PropertyNotObject, "prototype");
        }
        /* step 6 */
        for (ScriptObject obj = Type.objectValue(o), proto = Type.objectValue(p);;) {
            obj = obj.getPrototypeOf(cx);
            if (obj == null) {
                return false;
            }
            if (proto == obj) {
                return true;
            }
        }
    }

    /**
     * 7.3.20 SpeciesConstructor ( O, defaultConstructor )
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param defaultConstructor
     *            the default constructor
     * @return the constructor object
     */
    public static Constructor SpeciesConstructor(ExecutionContext cx, ScriptObject object,
            Intrinsics defaultConstructor) {
        /* step 1 (not applicable) */
        /* step 2 */
        Object constructor = Get(cx, object, "constructor");
        /* step 3 */
        if (Type.isUndefined(constructor)) {
            return (Constructor) cx.getIntrinsic(defaultConstructor);
        }
        /* step 4 */
        if (!Type.isObject(constructor)) {
            throw newTypeError(cx, Messages.Key.PropertyNotObject, "constructor");
        }
        /* step 5 */
        Object species = Get(cx, Type.objectValue(constructor), BuiltinSymbol.species.get());
        /* step 6 */
        if (Type.isUndefinedOrNull(species)) {
            return (Constructor) cx.getIntrinsic(defaultConstructor);
        }
        /* step 7 */
        if (IsConstructor(species)) {
            return (Constructor) species;
        }
        /* step 8 */
        throw newTypeError(cx, Messages.Key.PropertyNotConstructor, BuiltinSymbol.species.toString());
    }

    /**
     * The property kind enumeration for
     * {@link AbstractOperations#EnumerableOwnProperties(ExecutionContext, ScriptObject, PropertyKind)
     * EnumerableOwnProperties}.
     */
    public enum PropertyKind {
        Key, Value, KeyValue
    }

    /**
     * 7.3.21 EnumerableOwnProperties ( O, kind )
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @param kind
     *            the property kind
     * @return <var>object</var>'s own enumerable properties
     */
    public static List<Object> EnumerableOwnProperties(ExecutionContext cx, ScriptObject object, PropertyKind kind) {
        /* step 1 (not applicable) */
        /* step 2 */
        List<String> ownKeys = object.ownPropertyNames(cx);
        /* step 3 */
        int initialSize = Math.min(16, ownKeys.size());
        ArrayList<Object> properties = new ArrayList<>(initialSize);
        /* step 4 */
        for (String key : ownKeys) {
            Property desc = object.getOwnProperty(cx, (Object) key);
            if (desc != null && desc.isEnumerable()) {
                if (kind == PropertyKind.Key) {
                    properties.add(key);
                } else {
                    Object value = Get(cx, object, (Object) key);
                    if (kind == PropertyKind.Value) {
                        properties.add(value);
                    } else {
                        ArrayObject entry = CreateArrayFromList(cx, key, value);
                        properties.add(entry);
                    }
                }
            }
        }
        /* step 5 (sort keys - not applicable) */
        /* step 6 */
        return properties;
    }

    /**
     * 7.3.21 EnumerableOwnProperties ( O, kind )
     * 
     * @param cx
     *            the execution context
     * @param object
     *            the script object
     * @return <var>object</var>'s own enumerable string-valued property keys
     * @see #EnumerableOwnProperties(ExecutionContext, ScriptObject, PropertyKind)
     */
    public static List<String> EnumerableOwnNames(ExecutionContext cx, ScriptObject object) {
        /* step 1 (not applicable) */
        /* step 2 */
        List<String> ownKeys = object.ownPropertyNames(cx);
        /* step 3 */
        int initialSize = Math.min(16, ownKeys.size());
        ArrayList<String> names = new ArrayList<>(initialSize);
        /* step 4 */
        for (String key : ownKeys) {
            Property desc = object.getOwnProperty(cx, (Object) key);
            if (desc != null && desc.isEnumerable()) {
                names.add(key);
            }
        }
        /* step 5 (sort keys - not applicable) */
        /* step 6 */
        return names;
    }

    /**
     * 7.3.22 GetFunctionRealm ( obj )
     * 
     * @param cx
     *            the execution context
     * @param function
     *            the callable object
     * @return the function's realm
     */
    public static Realm GetFunctionRealm(ExecutionContext cx, Callable function) {
        /* steps 1-5 */
        return function.getRealm(cx);
    }

    /**
     * 7.4.1 GetIterator ( obj )
     * 
     * @param cx
     *            the execution context
     * @param obj
     *            the script object
     * @return the script iterator object
     */
    public static ScriptIterator<?> GetIterator(ExecutionContext cx, Object obj) {
        ScriptIterator<?> scriptIterator = ScriptIterators.GetScriptIterator(cx, obj);
        if (scriptIterator != null) {
            return scriptIterator;
        }
        /* step 1 */
        Callable method = GetMethod(cx, obj, BuiltinSymbol.iterator.get());
        /* steps 2-4 */
        return GetIterator(cx, obj, method);
    }

    /**
     * 7.4.1 GetIterator ( obj )
     * 
     * @param cx
     *            the execution context
     * @param obj
     *            the script object
     * @param method
     *            the iterator method
     * @return the script iterator object
     */
    public static ScriptIterator<?> GetIterator(ExecutionContext cx, Object obj, Callable method) {
        ScriptIterator<?> scriptIterator = ScriptIterators.GetScriptIterator(cx, obj, method);
        if (scriptIterator != null) {
            return scriptIterator;
        }
        /* step 1 (not applicable) */
        /* steps 3-4 (inlined Call operation) */
        if (method == null) {
            throw newTypeError(cx, Messages.Key.PropertyNotCallable, BuiltinSymbol.iterator.toString());
        }
        Object iterator = method.call(cx, obj);
        /* step 3 */
        if (!Type.isObject(iterator)) {
            throw newTypeError(cx, Messages.Key.NotObjectTypeReturned, BuiltinSymbol.iterator.toString());
        }
        /* step 4 */
        // FIXME: spec issue - Use Get instead of GetV.
        Object nextMethod = Get(cx, Type.objectValue(iterator), "next");
        /* steps 5-6 */
        return ScriptIterators.ToScriptIterator(cx, Type.objectValue(iterator), nextMethod);
    }

    /**
     * 7.4.2 IteratorNext ( iterator, value )
     * 
     * @param cx
     *            the execution context
     * @param iterator
     *            the script iterator object
     * @return the next value from the iterator
     */
    public static ScriptObject IteratorNext(ExecutionContext cx, ScriptIterator<?> iterator) {
        /* steps 1-2 */
        Object result = iterator.nextIterResult();
        /* step 3 */
        if (!Type.isObject(result)) {
            throw newTypeError(cx, Messages.Key.NotObjectTypeReturned, "next");
        }
        /* step 4 */
        return Type.objectValue(result);
    }

    /**
     * 7.4.2 IteratorNext ( iterator, value )
     * 
     * @param cx
     *            the execution context
     * @param iterator
     *            the script iterator object
     * @param value
     *            the value to pass to the next() function
     * @return the next value from the iterator
     */
    public static ScriptObject IteratorNext(ExecutionContext cx, ScriptIterator<?> iterator, Object value) {
        /* steps 1-2 */
        Object result = iterator.nextIterResult(value);
        /* step 3 */
        if (!Type.isObject(result)) {
            throw newTypeError(cx, Messages.Key.NotObjectTypeReturned, "next");
        }
        /* step 4 */
        return Type.objectValue(result);
    }

    /**
     * 7.4.3 IteratorComplete (iterResult)
     * 
     * @param cx
     *            the execution context
     * @param iterResult
     *            the iterator result object
     * @return {@code true} if the iterator is completed
     */
    public static boolean IteratorComplete(ExecutionContext cx, ScriptObject iterResult) {
        /* step 1 (not applicable) */
        /* step 2 */
        return ToBoolean(Get(cx, iterResult, "done"));
    }

    /**
     * 7.4.4 IteratorValue (iterResult)
     * 
     * @param cx
     *            the execution context
     * @param iterResult
     *            the iterator result object
     * @return the iterator result value
     */
    public static Object IteratorValue(ExecutionContext cx, ScriptObject iterResult) {
        /* step 1 (not applicable) */
        /* step 2 */
        return Get(cx, iterResult, "value");
    }

    /**
     * 7.4.5 IteratorStep ( iterator )
     * 
     * @param cx
     *            the execution context
     * @param iterator
     *            the script iterator object
     * @return the next value from the iterator or {@code null}
     */
    public static ScriptObject IteratorStep(ExecutionContext cx, ScriptIterator<?> iterator) {
        /* steps 1-2 */
        ScriptObject result = IteratorNext(cx, iterator);
        /* steps 3-4 */
        boolean done = IteratorComplete(cx, result);
        /* step 5 */
        if (done) {
            return null;
        }
        /* step 6 */
        return result;
    }

    /**
     * 7.4.6 IteratorClose( iterator, completion )
     * 
     * @param cx
     *            the execution context
     * @param iterator
     *            the script iterator object
     */
    public static void IteratorClose(ExecutionContext cx, ScriptIterator<?> iterator) {
        IteratorClose(cx, iterator.getScriptObject());
    }

    /**
     * 7.4.6 IteratorClose( iterator, completion )
     * 
     * @param cx
     *            the execution context
     * @param iterator
     *            the script iterator object
     */
    public static void IteratorClose(ExecutionContext cx, ScriptObject iterator) {
        /* steps 1-2 (not applicable) */
        /* step 3 */
        Callable returnMethod = GetMethod(cx, iterator, "return");
        /* step 4 */
        if (returnMethod != null) {
            /* steps 5, 7 */
            Object innerResult = returnMethod.call(cx, iterator);
            /* step 6 (not applicable) */
            /* step 8 */
            if (!Type.isObject(innerResult)) {
                throw newTypeError(cx, Messages.Key.NotObjectTypeReturned, "return");
            }
        }
        /* step 9 (not applicable) */
    }

    /**
     * 7.4.6 IteratorClose( iterator, completion )
     * 
     * @param cx
     *            the execution context
     * @param iterator
     *            the script iterator object
     * @param cause
     *            the exception cause
     */
    public static void IteratorClose(ExecutionContext cx, ScriptIterator<?> iterator, Throwable cause) {
        /* steps 1-2 (not applicable) */
        ScriptObject iteratorObject = iterator.getScriptObject();
        /* step 3 */
        Callable returnMethod = GetMethod(cx, iteratorObject, "return");
        /* step 4 */
        if (returnMethod != null) {
            /* steps 5-6 */
            try {
                returnMethod.call(cx, iteratorObject);
            } catch (ScriptException e) {
                // Ignore exceptions from "return" method.
                if (cause != e) {
                    cause.addSuppressed(e);
                }
            }
        }
        /* steps 7-9 (not applicable) */
    }

    /**
     * 7.4.7 CreateIterResultObject (value, done)
     * 
     * @param cx
     *            the execution context
     * @param value
     *            the iterator result value
     * @param done
     *            the iterator result state
     * @return the new iterator result object
     */
    public static OrdinaryObject CreateIterResultObject(ExecutionContext cx, Object value, boolean done) {
        /* step 1 (not applicable) */
        /* step 2 */
        OrdinaryObject obj = ObjectCreate(cx, Intrinsics.ObjectPrototype);
        /* step 3 */
        CreateDataProperty(cx, obj, "value", value);
        /* step 4 */
        CreateDataProperty(cx, obj, "done", done);
        /* step 5 */
        return obj;
    }

    /**
     * CopyDataProperties (target, source, excluded)
     * 
     * @param <TARGET>
     *            the target type
     * @param cx
     *            the execution context
     * @param target
     *            the target script object
     * @param source
     *            the source object
     * @param excluded
     *            the excluded property names
     * @return the <var>target</var> script object
     */
    public static <TARGET extends ScriptObject> TARGET CopyDataProperties(ExecutionContext cx, TARGET target,
            Object source, Set<?> excluded) {
        /* step 1 (not applicable) */
        /* steps 2, 5 */
        if (Type.isUndefinedOrNull(source)) {
            return target;
        }
        /* step 3.a */
        ScriptObject from = ToObject(cx, source);
        /* steps 3.b-c */
        List<?> keys = from.ownPropertyKeys(cx);
        /* step 4 */
        for (Object nextKey : keys) {
            if (!excluded.contains(nextKey)) {
                /* steps 4.i.a-b */
                Property desc = from.getOwnProperty(cx, nextKey);
                /* step 4.i.c */
                if (desc != null && desc.isEnumerable()) {
                    Object propValue = Get(cx, from, nextKey);
                    CreateDataProperty(cx, target, nextKey, propValue);
                }
            }
        }
        /* step 5 */
        return target;
    }

    /**
     * 7.1.3.1 ToNumber Applied to the String Type
     */
    private static final class ToNumberParser {
        private ToNumberParser() {
        }

        static double parse(String input) {
            String s = Strings.trim(input);
            int len = s.length();
            if (len == 0) {
                return 0;
            }
            if (s.charAt(0) == '0' && len > 2) {
                char c = s.charAt(1);
                if (c == 'x' || c == 'X') {
                    return readHexIntegerLiteral(s);
                }
                if (c == 'b' || c == 'B') {
                    return readBinaryIntegerLiteral(s);
                }
                if (c == 'o' || c == 'O') {
                    return readOctalIntegerLiteral(s);
                }
            }
            return readDecimalLiteral(s);
        }

        private static double readHexIntegerLiteral(String s) {
            assert s.length() > 2;
            final int start = 2; // "0x" prefix
            for (int index = start, end = s.length(); index < end; ++index) {
                char c = s.charAt(index);
                if (!(('0' <= c && c <= '9') || ('A' <= c && c <= 'F') || ('a' <= c && c <= 'f'))) {
                    return Double.NaN;
                }
            }
            return NumberParser.parseHex(s);
        }

        private static double readBinaryIntegerLiteral(String s) {
            assert s.length() > 2;
            final int start = 2; // "0b" prefix
            for (int index = start, end = s.length(); index < end; ++index) {
                char c = s.charAt(index);
                if (!(c == '0' || c == '1')) {
                    return Double.NaN;
                }
            }
            return NumberParser.parseBinary(s);
        }

        private static double readOctalIntegerLiteral(String s) {
            assert s.length() > 2;
            final int start = 2; // "0o" prefix
            for (int index = start, end = s.length(); index < end; ++index) {
                char c = s.charAt(index);
                if (!('0' <= c && c <= '7')) {
                    return Double.NaN;
                }
            }
            return NumberParser.parseOctal(s);
        }

        static double readDecimalLiteral(String s) {
            assert !s.isEmpty();

            outOfBounds: invalidChar: {
                final int end = s.length();
                int index = 0;
                int c = s.charAt(index++);
                boolean isPos = true;
                if (c == '+' || c == '-') {
                    if (index >= end)
                        break outOfBounds;
                    isPos = (c == '+');
                    c = s.charAt(index++);
                }
                if (c == 'I') {
                    // Infinity
                    final int Infinity_length = "Infinity".length();
                    if (index - 1 + Infinity_length == end
                            && s.regionMatches(index - 1, "Infinity", 0, Infinity_length)) {
                        return isPos ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                    }
                    break invalidChar;
                }
                boolean hasDot = (c == '.'), hasExp = false;
                if (hasDot) {
                    if (index >= end)
                        break outOfBounds;
                    c = s.charAt(index);
                }
                if (!('0' <= c && c <= '9')) {
                    break invalidChar;
                }
                if (!hasDot) {
                    while (index < end) {
                        c = s.charAt(index++);
                        if ('0' <= c && c <= '9') {
                            continue;
                        }
                        if (c == '.') {
                            hasDot = true;
                            break;
                        }
                        if (c == 'e' || c == 'E') {
                            hasExp = true;
                            break;
                        }
                        break invalidChar;
                    }
                }
                if (hasDot) {
                    while (index < end) {
                        c = s.charAt(index++);
                        if ('0' <= c && c <= '9') {
                            continue;
                        }
                        if (c == 'e' || c == 'E') {
                            hasExp = true;
                            break;
                        }
                        break invalidChar;
                    }
                }
                if (hasExp) {
                    if (index >= end)
                        break outOfBounds;
                    c = s.charAt(index++);
                    if (c == '+' || c == '-') {
                        if (index >= end)
                            break outOfBounds;
                        c = s.charAt(index++);
                    }
                    if (!('0' <= c && c <= '9')) {
                        break invalidChar;
                    }
                    while (index < end) {
                        c = s.charAt(index++);
                        if ('0' <= c && c <= '9') {
                            continue;
                        }
                        break invalidChar;
                    }
                }
                if (index != end) {
                    break invalidChar;
                }
                if (!(hasDot || hasExp)) {
                    return NumberParser.parseInteger(s);
                }
                return NumberParser.parseDecimal(s);
            }
            return Double.NaN;
        }
    }
}

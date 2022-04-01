/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects.number;

import static com.github.anba.es6draft.runtime.AbstractOperations.ToNumber;
import static com.github.anba.es6draft.runtime.AbstractOperations.ToString;
import static com.github.anba.es6draft.runtime.internal.Errors.newRangeError;
import static com.github.anba.es6draft.runtime.internal.Errors.newTypeError;
import static com.github.anba.es6draft.runtime.internal.Properties.createProperties;
import static com.github.anba.es6draft.runtime.objects.intl.NumberFormatConstructor.FormatNumber;
import static com.github.anba.es6draft.runtime.types.Undefined.UNDEFINED;

import org.mozilla.javascript.DToA;

import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Initializable;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.internal.Properties.Function;
import com.github.anba.es6draft.runtime.internal.Properties.Prototype;
import com.github.anba.es6draft.runtime.internal.Properties.Value;
import com.github.anba.es6draft.runtime.objects.intl.NumberFormatConstructor;
import com.github.anba.es6draft.runtime.objects.intl.NumberFormatObject;
import com.github.anba.es6draft.runtime.types.Intrinsics;
import com.github.anba.es6draft.runtime.types.Type;

/**
 * <h1>20 Numbers and Dates</h1><br>
 * <h2>20.1 Number Objects</h2>
 * <ul>
 * <li>20.1.3 Properties of the Number Prototype Object
 * </ul>
 */
public final class NumberPrototype extends NumberObject implements Initializable {
    /**
     * Constructs a new Number prototype object.
     * 
     * @param realm
     *            the realm object
     */
    public NumberPrototype(Realm realm) {
        super(realm, 0);
    }

    @Override
    public void initialize(Realm realm) {
        createProperties(realm, this, Properties.class);
    }

    /**
     * 20.1.3 Properties of the Number Prototype Object
     */
    public enum Properties {
        ;

        /**
         * Abstract operation thisNumberValue(value)
         * 
         * @param cx
         *            the execution context
         * @param value
         *            the value
         * @param method
         *            the method name
         * @return the number value
         */
        private static double thisNumberValue(ExecutionContext cx, Object value, String method) {
            if (Type.isNumber(value)) {
                return Type.numberValue(value);
            }
            if (value instanceof NumberObject) {
                return ((NumberObject) value).getNumberData();
            }
            throw newTypeError(cx, Messages.Key.IncompatibleThis, method, Type.of(value).toString());
        }

        @Prototype
        public static final Intrinsics __proto__ = Intrinsics.ObjectPrototype;

        /**
         * 20.1.3.1 Number.prototype.constructor
         */
        @Value(name = "constructor")
        public static final Intrinsics constructor = Intrinsics.Number;

        /**
         * 20.1.3.6 Number.prototype.toString ( [ radix ] )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param radix
         *            the optional radix value
         * @return the string representation for this number
         */
        @Function(name = "toString", arity = 1)
        public static Object toString(ExecutionContext cx, Object thisValue, Object radix) {
            /* steps 1-2 */
            double x = thisNumberValue(cx, thisValue, "Number.prototype.toString");
            /* steps 3-6 */
            int radixNumber = 10;
            if (!Type.isUndefined(radix)) {
                radixNumber = (int) ToNumber(cx, radix); // ToInteger
            }
            /* step 7 */
            if (radixNumber < 2 || radixNumber > 36) {
                throw newRangeError(cx, Messages.Key.InvalidRadix);
            }
            /* step 8 */
            if (radixNumber == 10) {
                return ToString(x);
            }
            /* step 9 */
            return DToA.JS_dtobasestr(radixNumber, x);
        }

        /**
         * 20.1.3.4 Number.prototype.toLocaleString( [ reserved1 [ ., reserved2 ] ])<br>
         * 13.2.1 Number.prototype.toLocaleString ([locales [, options ]])
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param locales
         *            the optional locales array
         * @param options
         *            the optional options object
         * @return the locale string representation for this number
         */
        @Function(name = "toLocaleString", arity = 0)
        public static Object toLocaleString(ExecutionContext cx, Object thisValue, Object locales, Object options) {
            // N.B. permissible but not encouraged:
            // return ToString(thisNumberValue(cx, thisValue));

            // ECMA-402
            /* steps 1-2 */
            double x = thisNumberValue(cx, thisValue, "Number.prototype.toLocaleString");
            /* steps 3-4 */
            NumberFormatConstructor ctor = (NumberFormatConstructor) cx.getIntrinsic(Intrinsics.Intl_NumberFormat);
            NumberFormatObject numberFormat = ctor.construct(cx, ctor, locales, options);
            /* step 5 */
            return FormatNumber(numberFormat, x);
        }

        /**
         * 20.1.3.7 Number.prototype.valueOf ( )
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @return the number value of this number object
         */
        @Function(name = "valueOf", arity = 0)
        public static Object valueOf(ExecutionContext cx, Object thisValue) {
            /* steps 1-2 */
            return thisNumberValue(cx, thisValue, "Number.prototype.valueOf");
        }

        /**
         * 20.1.3.3 Number.prototype.toFixed (fractionDigits)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param fractionDigits
         *            the number of fraction digits
         * @return the decimal fixed-point notation of this number
         */
        @Function(name = "toFixed", arity = 1)
        public static Object toFixed(ExecutionContext cx, Object thisValue, Object fractionDigits) {
            /* steps 1-2 */
            double x = thisNumberValue(cx, thisValue, "Number.prototype.toFixed");
            /* steps 3-4 */
            int f = (int) ToNumber(cx, fractionDigits); // ToInteger
            /* step 5 */
            if (f < 0 || f > 100) {
                throw newRangeError(cx, Messages.Key.InvalidPrecision);
            }
            /* step 6 */
            if (x != x) {
                return "NaN";
            }
            /* steps 7-11 */
            StringBuilder sb = new StringBuilder();
            DToA.JS_dtostr(sb, DToA.DTOSTR_FIXED, f, x);
            return sb.toString();
        }

        /**
         * 20.1.3.2 Number.prototype.toExponential (fractionDigits)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param fractionDigits
         *            the number of fraction digits
         * @return the decimal exponential notation of this number
         */
        @Function(name = "toExponential", arity = 1)
        public static Object toExponential(ExecutionContext cx, Object thisValue, Object fractionDigits) {
            /* steps 1-2 */
            double x = thisNumberValue(cx, thisValue, "Number.prototype.toExponential");
            /* steps 3-5 */
            int f = (int) ToNumber(cx, fractionDigits); // ToInteger
            assert fractionDigits != UNDEFINED || f == 0;
            /* steps 6-9 */
            if (x != x) {
                return "NaN";
            } else if (x == Double.POSITIVE_INFINITY) {
                return "Infinity";
            } else if (x == Double.NEGATIVE_INFINITY) {
                return "-Infinity";
            }
            /* step 10 */
            if (f < 0 || f > 100) {
                throw newRangeError(cx, Messages.Key.InvalidPrecision);
            }
            /* steps 11-17 */
            StringBuilder sb = new StringBuilder();
            if (fractionDigits == UNDEFINED) {
                DToA.JS_dtostr(sb, DToA.DTOSTR_STANDARD_EXPONENTIAL, 1 + f, x);
            } else {
                DToA.JS_dtostr(sb, DToA.DTOSTR_EXPONENTIAL, 1 + f, x);
            }
            return sb.toString();
        }

        /**
         * 20.1.3.5 Number.prototype.toPrecision (precision)
         * 
         * @param cx
         *            the execution context
         * @param thisValue
         *            the function this-value
         * @param precision
         *            the precision argument
         * @return the decimal exponential notation of this number
         */
        @Function(name = "toPrecision", arity = 1)
        public static Object toPrecision(ExecutionContext cx, Object thisValue, Object precision) {
            /* steps 1-2 */
            double x = thisNumberValue(cx, thisValue, "Number.prototype.toPrecision");
            /* step 3 */
            if (precision == UNDEFINED) {
                return ToString(x);
            }
            /* steps 4-5 */
            int p = (int) ToNumber(cx, precision); // ToInteger
            /* steps 6-9 */
            if (x != x) {
                return "NaN";
            } else if (x == Double.POSITIVE_INFINITY) {
                return "Infinity";
            } else if (x == Double.NEGATIVE_INFINITY) {
                return "-Infinity";
            }
            /* step 10 */
            if (p < 1 || p > 100) {
                throw newRangeError(cx, Messages.Key.InvalidPrecision);
            }
            /* steps 11-16 */
            StringBuilder sb = new StringBuilder();
            DToA.JS_dtostr(sb, DToA.DTOSTR_PRECISION, p, x);
            return sb.toString();
        }
    }
}

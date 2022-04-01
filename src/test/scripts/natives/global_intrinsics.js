/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const globalProperties = %GlobalProperties();
const globalObject = %GlobalObject();
const globalThis = %GlobalThis();

assertEq(globalThis, this);
assertEq(globalThis, globalObject);
if (globalThis === globalProperties) {
  assertEq(globalObject, "<fail>");
}

// 18.3  Constructor Properties of the Global Object

assertEq(globalObject.Array, %Intrinsic("Array"));
assertEq(globalObject.ArrayBuffer, %Intrinsic("ArrayBuffer"));
assertEq(globalObject.Boolean, %Intrinsic("Boolean"));
assertEq(globalObject.DataView, %Intrinsic("DataView"));
assertEq(globalObject.Date, %Intrinsic("Date"));
assertEq(globalObject.Error, %Intrinsic("Error"));
assertEq(globalObject.EvalError, %Intrinsic("EvalError"));
assertEq(globalObject.Float32Array, %Intrinsic("Float32Array"));
assertEq(globalObject.Float64Array, %Intrinsic("Float64Array"));
assertEq(globalObject.Function, %Intrinsic("Function"));
assertEq(globalObject.Int8Array, %Intrinsic("Int8Array"));
assertEq(globalObject.Int16Array, %Intrinsic("Int16Array"));
assertEq(globalObject.Int32Array, %Intrinsic("Int32Array"));
assertEq(globalObject.Map, %Intrinsic("Map"));
assertEq(globalObject.Number, %Intrinsic("Number"));
assertEq(globalObject.Object, %Intrinsic("Object"));
assertEq(globalObject.RangeError, %Intrinsic("RangeError"));
assertEq(globalObject.ReferenceError, %Intrinsic("ReferenceError"));
assertEq(globalObject.RegExp, %Intrinsic("RegExp"));
assertEq(globalObject.Set, %Intrinsic("Set"));
assertEq(globalObject.String, %Intrinsic("String"));
assertEq(globalObject.Symbol, %Intrinsic("Symbol"));
assertEq(globalObject.SyntaxError, %Intrinsic("SyntaxError"));
assertEq(globalObject.TypeError, %Intrinsic("TypeError"));
assertEq(globalObject.Uint8Array, %Intrinsic("Uint8Array"));
assertEq(globalObject.Uint8ClampedArray, %Intrinsic("Uint8ClampedArray"));
assertEq(globalObject.Uint16Array, %Intrinsic("Uint16Array"));
assertEq(globalObject.Uint32Array, %Intrinsic("Uint32Array"));
assertEq(globalObject.URIError, %Intrinsic("URIError"));
assertEq(globalObject.WeakMap, %Intrinsic("WeakMap"));
assertEq(globalObject.WeakSet, %Intrinsic("WeakSet"));

// 18.4  Other Properties of the Global Object
assertEq(globalObject.JSON, %Intrinsic("JSON"));
assertEq(globalObject.Math, %Intrinsic("Math"));
assertEq(globalObject.Proxy, %Intrinsic("Proxy"));
assertEq(globalObject.Reflect, %Intrinsic("Reflect"));

// ECMA - 402
assertEq(globalObject.Intl, %Intrinsic("Intl"));

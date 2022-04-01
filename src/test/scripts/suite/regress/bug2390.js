/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertDataProperty, assertTrue
} = Assert;

// 19.1.3.4: Object.prototype.propertyIsEnumerable() should accept symbols
// https://bugs.ecmascript.org/show_bug.cgi?id=2390

let sym = Symbol("my-symbol");
let obj = {
  [sym]: 0,
};

assertDataProperty(obj, sym, {value: 0, writable: true, enumerable: true, configurable: true});
assertTrue(obj.propertyIsEnumerable(sym));

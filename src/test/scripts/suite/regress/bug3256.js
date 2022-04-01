/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertTrue
} = Assert;

// New Array.prototype behavior of returning an instance of `this.constructor` breaks Zepto.
// https://bugs.ecmascript.org/show_bug.cgi?id=3256

var obj = Object.setPrototypeOf([], {slice: Array.prototype.slice});
var copy = obj.slice(0);
assertTrue(Array.isArray(copy));

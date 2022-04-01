/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertSame
} = Assert;

// Indexed access on object when prototype is typed array

var p = {0: "A", 1: "B"};
var ta = Object.setPrototypeOf(new Int8Array(1), p);
var o = Object.setPrototypeOf({}, ta);

assertSame(0, o[0]);
assertSame(void 0, o[1]);

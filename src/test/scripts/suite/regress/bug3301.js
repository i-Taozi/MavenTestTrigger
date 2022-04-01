/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertSame, assertTrue, assertThrows
} = Assert;

// Globals not treated as var bindings
// https://bugs.ecmascript.org/show_bug.cgi?id=3301

evalScript("function f(x) { return x === undefined }")
assertThrows(SyntaxError, () => evalScript("let undefined = 666;"));

assertSame(void 0, undefined);
assertTrue(f());

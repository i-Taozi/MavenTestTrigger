/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertSame
} = Assert;

// 9.2.14 Function Declaration Instantiation: "arguments" as function declaration not handled
// https://bugs.ecmascript.org/show_bug.cgi?id=2774

// No crash
function f() {
  function arguments() {}
}
f();

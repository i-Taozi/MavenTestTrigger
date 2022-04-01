/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertSame, assertThrows
} = Assert;

// 19.2.1.1, 25.2.1.1: Don't set [[Strict]] for wrong function kind
// https://bugs.ecmascript.org/show_bug.cgi?id=2855

if (false) {
  let GeneratorFunction = (function*(){}).constructor;
  let fn = new class extends Function { constructor() { /* no super */ } };

  function argumentsCaller(c) {
    // See bug 2718
    Object.defineProperty(arguments, "caller", {value: c});
    return arguments.caller;
  }

  assertSame(fn, argumentsCaller(fn));

  assertThrows(TypeError, () => GeneratorFunction.call(fn, "'not strict'"));
  assertSame(fn, argumentsCaller(fn));

  assertThrows(TypeError, () => GeneratorFunction.call(fn, "'use strict'"));
  assertSame(fn, argumentsCaller(fn));

  assertThrows(TypeError, () => GeneratorFunction.call(fn, "'not strict'"));
  assertSame(fn, argumentsCaller(fn));
}
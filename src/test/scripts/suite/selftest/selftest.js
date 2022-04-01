/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const knownExports = [
  "AssertionError", "fail",
  "assertSame", "assertNotSame",
  "assertThrows", "assertSyntaxError",
  "assertTrue", "assertFalse",
  "assertUndefined", "assertNotUndefined",
  "assertNull", "assertNotNull",
  "assertInstanceOf",
  "assertCallable", "assertNotCallable",
  "assertConstructor", "assertNotConstructor",
  "assertDataProperty", "assertAccessorProperty",
  "assertBuiltinFunction", "assertBuiltinConstructor", "assertBuiltinPrototype",
  "assertNativeFunction",
  "assertEquals",
];

if (typeof Assert === "undefined") {
  throw "Assert not defined";
}

for (let name of knownExports) {
  if (Assert[name] === void 0) {
    throw `Assert.${name} not exported`;
  }
}

for (let name in Assert) {
  if (knownExports.indexOf(name) === -1) {
    throw `Unknown export: Assert.${name}`;
  }
}

const {
  AssertionError, fail,
  assertSame, assertNotSame,
  assertThrows, assertSyntaxError,
  assertTrue, assertFalse,
  assertUndefined, assertNotUndefined,
  assertNull, assertNotNull,
  assertInstanceOf,
  assertCallable, assertNotCallable,
  assertConstructor, assertNotConstructor,
  assertDataProperty, assertAccessorProperty,
  assertBuiltinFunction, assertBuiltinConstructor, assertBuiltinPrototype,
  assertNativeFunction,
  assertEquals,
} = Assert;

try {
  fail();
  throw 0;
} catch (e) {
  if (!(e instanceof AssertionError)) {
    throw "fail() throws AssertionError";
  }
}

try {
  fail("string-message");
  throw 0;
} catch (e) {
  if (!(e instanceof AssertionError)) {
    throw "fail(string) throws AssertionError";
  }
  if (e.message !== "string-message") {
    throw "fail(string), unexpected .message: " + e.message;
  }
}

try {
  fail `template-${1 + 2}-message`;
  throw 0;
} catch (e) {
  if (!(e instanceof AssertionError)) {
    throw "fail(template) throws AssertionError";
  }
  if (e.message !== "template-3-message") {
    throw "fail(template), unexpected .message: " + e.message;
  }
}

const primitives = [void 0, null, true, false, +0, -0, +1, -1, +Math.PI, -Math.PI, +Infinity, -Infinity, NaN, "", "str", Symbol(), Symbol("desc")];
const objects = [[], {}, [0], {a: 0}, new Boolean, new Number, new String, Object(Symbol())];
const functions = [()=>{}, ({fn(){}}).fn, Date.now];
const constructors = [function(){}, Boolean, Number, String, Object, Function, Array, Date, Error];
const values = [...primitives, ...objects, ...functions, ...constructors];

for (let v of values) {
  assertSame(v, v);
  try {
    assertNotSame(v, v);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertNotSame() failure");
}

for (let a of values) {
  for (let b of values) {
    if (!Object.is(a, b)) {
      assertNotSame(a, b);
      try {
        assertSame(a, b);
      } catch (e) {
        if (e instanceof AssertionError) continue;
      }
      fail("assertSame() failure");
    }
  }
}

assertThrows(Error, () => {throw new Error});
assertThrows(TypeError, () => {throw new TypeError});
assertThrows(Date, () => {throw new Date});
assertThrows(TypeError, () => {Object.create(1)});
L1: {
  try {
    assertThrows(Error, () => {});
  } catch(e) {
    if (e instanceof AssertionError) break L1;
  }
  fail("assertThrows() failure");
}

assertSyntaxError("% garbage");
L1: {
  try {
    assertSyntaxError("not % garbage");
  } catch(e) {
    if (e instanceof AssertionError) break L1;
  }
  fail("assertSyntaxError() failure");
}

assertTrue(true);
for (let v of values) {
  if (v !== true) {
    try {
      assertTrue(v);
    } catch (e) {
      if (e instanceof AssertionError) continue;
    }
    fail("assertTrue() failure");
  }
}

assertFalse(false);
for (let v of values) {
  if (v !== false) {
    try {
      assertFalse(v);
    } catch (e) {
      if (e instanceof AssertionError) continue;
    }
    fail("assertFalse() failure");
  }
}

assertUndefined(void 0);
L1: {
  try {
    assertNotUndefined(void 0);
  } catch (e) {
    if (e instanceof AssertionError) break L1;
  }
  fail("assertNotUndefined() failure");
}
for (let v of values) {
  if (v !== void 0) {
    assertNotUndefined(v);
    try {
      assertUndefined(v);
    } catch (e) {
      if (e instanceof AssertionError) continue;
    }
    fail("assertUndefined() failure");
  }
}

assertNull(null);
L1: {
  try {
    assertNotNull(null);
  } catch (e) {
    if (e instanceof AssertionError) break L1;
  }
  fail("assertNotNull() failure");
}
for (let v of values) {
  if (v !== null) {
    assertNotNull(v);
    try {
      assertNull(v);
    } catch (e) {
      if (e instanceof AssertionError) continue;
    }
    fail("assertNull() failure");
  }
}

for (let c of constructors) {
  if (c !== Symbol) {
    assertInstanceOf(c, new c);
  }
}
assertInstanceOf(Symbol, Object(Symbol()));

for (let c of constructors) {
  for (let v of primitives) {
    try {
      assertInstanceOf(c, v);
    } catch (e) {
      if (e instanceof AssertionError) continue;
    }
    fail("assertInstanceOf() failure");
  }
}

for (let v of [...functions, ...constructors]) {
  assertCallable(v);
  try {
    assertNotCallable(v);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertNotCallable() failure");
}

for (let v of [...primitives, ...objects]) {
  assertNotCallable(v);
  try {
    assertCallable(v);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertCallable() failure");
}

for (let v of [...constructors]) {
  assertConstructor(v);
  try {
    assertNotConstructor(v);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertNotConstructor() failure");
}

for (let v of [...primitives, ...objects, ...functions]) {
  assertNotConstructor(v);
  try {
    assertConstructor(v);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertConstructor() failure");
}

{
  let seed = {
    value: [void 0, null, 1, "str", {}],
    writable: [true, false],
    enumerable: [true, false],
    configurable: [true, false]
  };
  let ds = [];
  for (let v of seed.value) {
    for (let w of seed.writable) {
      for (let e of seed.enumerable) {
        for (let c of seed.configurable) {
          ds.push({value: v, writable: w, enumerable: e, configurable: c});
        }
      }
    }
  }
  let o = {}, i = 0;
  for (let d of ds) {
    let pk = `p${i++}`;
    Object.defineProperty(o, pk, d);
    assertDataProperty(o, pk, d);
    try {
      assertAccessorProperty(o, pk, d);
    } catch (e) {
      if (e instanceof AssertionError) continue;
    }
    fail("assertAccessorProperty() failure");
  }
}

{
  let seed = {
    get: [void 0, function(){}],
    set: [void 0, function(){}],
    enumerable: [true, false],
    configurable: [true, false]
  };
  let ds = [];
  for (let g of seed.get) {
    for (let s of seed.set) {
      for (let e of seed.enumerable) {
        for (let c of seed.configurable) {
          ds.push({get: g, set: s, enumerable: e, configurable: c});
        }
      }
    }
  }
  let o = {}, i = 0;
  for (let d of ds) {
    let pk = `p${i++}`;
    Object.defineProperty(o, pk, d);
    assertAccessorProperty(o, pk, d);
    try {
      assertDataProperty(o, pk, d);
    } catch (e) {
      if (e instanceof AssertionError) continue;
    }
    fail("assertDataProperty() failure");
  }
}

for (let [fn, name, arity] of [[eval, "eval", 1], [Object.create, "create", 2]]) {
  assertBuiltinFunction(fn, name, arity);
}

for (let [fn, name, arity] of [[eval, "eval", 2], [Object, "Object", 1]]) {
  try {
    assertBuiltinFunction(fn, name, arity);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertBuiltinFunction() failure");
}

for (let [fn, name, arity] of [[Object, "Object", 1]]) {
  assertBuiltinConstructor(fn, name, arity);
}

for (let [fn, name, arity] of [[eval, "eval", 1], [Object.create, "create", 2]]) {
  try {
    assertBuiltinConstructor(fn, name, arity);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertBuiltinConstructor() failure");
}

for (let [o, p] of [[Object.prototype, null], [Function.prototype, Object.prototype], [Function.prototype]]) {
  assertBuiltinPrototype(o, p);
}

for (let [o, p] of [[Object.prototype], [Object.prototype, Function.prototype], [String.prototype, Number.prototype]]) {
  try {
    assertBuiltinPrototype(o, p);
  } catch (e) {
    if (e instanceof AssertionError) continue;
  }
  fail("assertBuiltinPrototype() failure");
}

for (let v of values) {
  assertEquals(v, v);
}

for (let a of values) {
  for (let b of values) {
    if (!Object.is(a, b)) {
      try {
        assertEquals(a, b);
      } catch (e) {
        if (e instanceof AssertionError) continue;
      }
      fail("assertEquals() failure");
    }
  }
}

assertEquals({}, {});
assertEquals({a: 1, b: 2, c: 3}, {a: 1, b: 2, c: 3});
assertEquals({a: 1, b: {}, c: []}, {a: 1, b: {}, c: []});
assertEquals([], []);
assertEquals([1, 2, 3], [1, 2, 3]);
assertEquals([1, {}, []], [1, {}, []]);

assertNativeFunction(eval, "eval", 1);
{
  let result = {};
  new Promise((resolve, reject) => { Object.assign(result, {resolve, reject}) });
  assertNativeFunction(result.resolve, undefined, 1);
  assertNativeFunction(result.reject, undefined, 1);
}
L1: {
  try {
    assertNativeFunction(() => {}, undefined, 0);
  } catch (e) {
    if (e instanceof AssertionError) break L1;
  }
  fail("assertNativeFunction() failure");
}

/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
// Indirect Eval, Not Strict Mode, VariableEnvironment=GlobalEnv, LexicalEnvironment=GlobalEnv
// - Testing [[Scope]] for nested eval() calls

const {
  assertTrue,
  assertFalse,
  assertSame,
  assertUndefined,
  assertNotUndefined,
} = Assert;

const global = this;

// Nested eval in indirect eval calls are script-code with global scope
// (1,eval)(`eval("...")`)

(1,eval)(`eval("function v1(){}")`);
assertSame("function", typeof v1);
assertTrue("v1" in global);
assertNotUndefined(global["v1"]);

(1,eval)(`eval("function* v2(){}")`);
assertSame("function", typeof v2);
assertTrue("v2" in global);
assertNotUndefined(global["v2"]);

(1,eval)(`eval("class v3{}")`);
assertSame("undefined", typeof v3);
assertFalse("v3" in global);
assertUndefined(global["v3"]);

(1,eval)(`eval("var v4")`);
assertSame("undefined", typeof v4);
assertTrue("v4" in global);
assertUndefined(global["v4"]);

(1,eval)(`eval("var v5 = 0")`);
assertSame("number", typeof v5);
assertTrue("v5" in global);
assertNotUndefined(global["v5"]);

(1,eval)(`eval("let v6")`);
assertSame("undefined", typeof v6);
assertFalse("v6" in global);
assertUndefined(global["v6"]);

(1,eval)(`eval("let v7 = 0")`);
assertSame("undefined", typeof v7);
assertFalse("v7" in global);
assertUndefined(global["v7"]);

(1,eval)(`eval("const v8 = 0")`);
assertSame("undefined", typeof v8);
assertFalse("v8" in global);
assertUndefined(global["v8"]);

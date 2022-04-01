/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
// Direct Eval, Not Strict Mode, VariableEnvironment=GlobalEnv, LexicalEnvironment=not GlobalEnv

const {
  assertTrue,
  assertFalse,
  assertSame,
  assertUndefined,
  assertNotUndefined,
} = Assert;

const global = this;

{
eval("function v1(){}");
assertSame("function", typeof v1);
assertTrue("v1" in global);
assertNotUndefined(global["v1"]);
}
{
eval("function* v2(){}");
assertSame("function", typeof v2);
assertTrue("v2" in global);
assertNotUndefined(global["v2"]);
}
{
eval("class v3{}");
assertSame("undefined", typeof v3);
assertFalse("v3" in global);
assertUndefined(global["v3"]);
}
{
eval("var v4");
assertSame("undefined", typeof v4);
assertTrue("v4" in global);
assertUndefined(global["v4"]);
}
{
eval("var v5 = 0");
assertSame("number", typeof v5);
assertTrue("v5" in global);
assertNotUndefined(global["v5"]);
}
{
eval("let v6");
assertSame("undefined", typeof v6);
assertFalse("v6" in global);
assertUndefined(global["v6"]);
}
{
eval("let v7 = 0");
assertSame("undefined", typeof v7);
assertFalse("v7" in global);
assertUndefined(global["v7"]);
}
{
eval("const v8 = 0");
assertSame("undefined", typeof v8);
assertFalse("v8" in global);
assertUndefined(global["v8"]);
}

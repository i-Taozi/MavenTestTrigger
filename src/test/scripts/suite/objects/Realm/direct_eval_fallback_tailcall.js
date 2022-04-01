/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertSame
} = Assert;

// Invalid direct eval call and tail calls:
// - direct eval fallback and 'wrong' eval function have both tail calls enabled
// - chaining them should preserve the tail call property

let realm = new class extends Reflect.Realm {
  nonEval(callee, thisArgument, ...args) {
    // "use strict";
    return callee(...args);
  }
};
realm.eval(`
  function returnCaller() {
    return returnCaller.caller;
  }

  function tailCall() {
    "use strict";
    return returnCaller();
  }

  function testFunction() {
    return eval("123");
  }

  eval = tailCall;
`);

assertSame(realm.global.testFunction, realm.eval(`testFunction()`));

/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertBuiltinFunction,
  fail,
  assertSame, assertEquals,
  assertFalse, assertTrue,
} = Assert;

System.load("lib/promises.jsm");
const {
  reportFailure
} = System.get("lib/promises.jsm");

// 25.4.4.1.1 Promise.all: Change [[AlreadyCalled]] to record type
// https://bugs.ecmascript.org/show_bug.cgi?id=2636

function tamper(p) {
  return Object.assign(p, {
    then(onFulfilled, onRejected) {
      onFulfilled();
      return Promise.prototype.then.call(this, onFulfilled, onRejected);
    }
  });
}

// Manual dispatch of Promise.all Countdown Functions (1)
{
  function* g() {
    yield tamper(Promise.resolve(0));
  }

  // Cannot prevent countdownHolder.[[Countdown]] from ever reaching zero
  Promise
    .all(g())
    .then(v => { assertEquals([void 0], v) })
    .catch(reportFailure);
}

// Manual dispatch of Promise.all Countdown Functions (2)
{
  function* g() {
    yield Promise.resolve(0);
    yield tamper(Promise.resolve(1));
    yield Promise.resolve(2)
      .then(() => {
        assertFalse(fulfillCalled);
      })
      .then(() => {
        assertFalse(fulfillCalled);
      })
      .catch(reportFailure);
  }

  // Promise from Promise.all never resolved before arguments
  let fulfillCalled = false;
  Promise
    .all(g())
    .then(() => {
      assertFalse(fulfillCalled);
      fulfillCalled = true;
    })
    .catch(reportFailure);
}

// Manual dispatch of Promise.all Countdown Functions (3)
{
  function* g() {
    yield Promise.resolve(0);
    yield tamper(Promise.resolve(1));
    yield Promise.reject(2);
  }

  // Promise from Promise.all never resolved if rejected promise in arguments
  Promise
    .all(g())
    .then(v => {
       fail `fulfilled with ${v}`
    }, v => {
      assertSame(2, v)
    })
    .catch(reportFailure);
}

// Manual dispatch of Promise.all Countdown Functions (4)
{
  let hijack = true;
  class P extends Promise {
    constructor(resolver) {
      if (hijack) {
        hijack = false;
        super((resolve, reject) => {
          return resolver(values => {
            actualArguments.push(values.slice());
            return resolve(values);
          }, reject);
        });
      } else {
        super(resolver);
      }
    }

    static resolve(p) {
      return p;
    }
  }
  function* g() {
    yield Promise.resolve(0);
    yield tamper(Promise.resolve(1));
    yield Promise.resolve(2);
  }
  let actualArguments = [];
  let expectedArguments = [[0, void 0, 2]];

  // Promise.all never calls resolver multiple times
  P.all(g()).catch(reportFailure);
  Promise
    .resolve()
    .then(() => assertEquals(expectedArguments, actualArguments))
    .catch(reportFailure);
}

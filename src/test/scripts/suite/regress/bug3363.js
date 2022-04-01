/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertTrue
} = Assert;

// 14.3.1 Early Errors: Constructor methods not handled (and typo)
// https://bugs.ecmascript.org/show_bug.cgi?id=3363

class Base { }
class Derived extends Base {
  constructor() {
    super(); // Initialize this
    assertTrue((new super) instanceof Base);
  }
}
new Derived;

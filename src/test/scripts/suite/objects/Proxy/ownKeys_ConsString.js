/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertThrows, assertEquals
} = Assert;

// Ensure ConsString is flattened
function testConsString(a, b) {
  Object.freeze(new Proxy({[a + b]: null}, {ownKeys: () => [a + b]}));
}
testConsString("a".repeat(100), "b".repeat(100))

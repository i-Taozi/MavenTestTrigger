/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertBuiltinFunction,
} = Assert;


/* Promise.race ( iterable ) */

assertBuiltinFunction(Promise.race, "race", 1);


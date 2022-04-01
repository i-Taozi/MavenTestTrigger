/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertSame, assertNotUndefined
} = Assert;

import {
  object as simpleObject,
  aliasedObject as simpleAliasedObject,
} from "./resources/export_alias.jsm";

import {
  object as fromObject,
  otherObject as fromOtherObject,
  aliasedObject as fromAliasedObject,
  otherAliasedObject as fromOtherAliasedObject,
} from "./resources/export_from_alias.jsm";

assertNotUndefined(simpleObject);
assertSame(simpleObject, simpleAliasedObject);
assertSame(simpleObject, fromObject);
assertSame(simpleObject, fromOtherObject);
assertSame(simpleObject, fromAliasedObject);
assertSame(simpleObject, fromOtherAliasedObject);
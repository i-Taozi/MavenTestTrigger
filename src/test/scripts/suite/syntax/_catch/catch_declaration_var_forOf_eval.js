/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
const {
  assertThrows
} = Assert;

// Catch variable is BindingIdentifier
assertThrows(SyntaxError, () => { try { throw []; } catch (e) { eval(`for(var e of []) ;`); } });
assertThrows(SyntaxError, () => { try { throw []; } catch (e) { eval(`for(var [e] of []) ;`); } });
assertThrows(SyntaxError, () => { try { throw []; } catch (e) { eval(`for(var {e} of []) ;`); } });


// Catch variable is ArrayBindingPattern
assertThrows(SyntaxError, () => { try { throw []; } catch ([e]) { eval(`for(var e of []) ;`); } });
assertThrows(SyntaxError, () => { try { throw []; } catch ([e]) { eval(`for(var [e] of []) ;`); } });
assertThrows(SyntaxError, () => { try { throw []; } catch ([e]) { eval(`for(var {e} of []) ;`); } });


// Catch variable is ObjectBindingPattern
assertThrows(SyntaxError, () => { try { throw []; } catch ({e}) { eval(`for(var e of []) ;`); } });
assertThrows(SyntaxError, () => { try { throw []; } catch ({e}) { eval(`for(var [e] of []) ;`); } });
assertThrows(SyntaxError, () => { try { throw []; } catch ({e}) { eval(`for(var {e} of []) ;`); } });

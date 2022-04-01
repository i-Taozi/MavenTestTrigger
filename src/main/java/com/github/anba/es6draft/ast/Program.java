/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast;

import java.util.EnumSet;

import com.github.anba.es6draft.parser.Parser;
import com.github.anba.es6draft.runtime.internal.Source;

/**
 * <h1>15 ECMAScript Language: Scripts and Modules</h1>
 * <ul>
 * <li>15.1 Scripts
 * <li>15.2 Modules
 * </ul>
 */
public abstract class Program extends AstNode {
    private final Source source;
    private final EnumSet<Parser.Option> parserOptions;

    public Program(long beginPosition, long endPosition, Source source, EnumSet<Parser.Option> parserOptions) {
        super(beginPosition, endPosition);
        this.source = source;
        this.parserOptions = parserOptions;
    }

    public final Source getSource() {
        return source;
    }

    public final EnumSet<Parser.Option> getParserOptions() {
        return parserOptions;
    }
}

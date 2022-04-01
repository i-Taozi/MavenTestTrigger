/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.modules.loader;

import com.github.anba.es6draft.runtime.internal.Source;
import com.github.anba.es6draft.runtime.modules.ModuleSource;
import com.github.anba.es6draft.runtime.modules.SourceIdentifier;

/**
 * 
 */
public final class StringModuleSource implements ModuleSource {
    private final SourceIdentifier sourceId;
    private final String sourceName;
    private final String sourceCode;

    public StringModuleSource(SourceIdentifier sourceId, String sourceName, String sourceCode) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.sourceCode = sourceCode;
    }

    @Override
    public String sourceCode() {
        return sourceCode;
    }

    @Override
    public Source toSource() {
        return new Source(sourceId, sourceName, 1);
    }
}

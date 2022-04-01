/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.ast.scope;

import java.util.List;
import java.util.Set;

import com.github.anba.es6draft.ast.Declaration;
import com.github.anba.es6draft.ast.FunctionNode;
import com.github.anba.es6draft.ast.Script;
import com.github.anba.es6draft.ast.StatementListItem;
import com.github.anba.es6draft.ast.TopLevelNode;

/**
 * Scope class for {@link FunctionNode} and {@link Script} objects.
 */
public interface TopLevelScope extends Scope {
    /**
     * Always returns <code>null</code> for top-level scopes.
     */
    @Override
    Scope getParent();

    /**
     * Returns the scope which encloses this scope.
     * 
     * @return the enclosing scope
     */
    Scope getEnclosingScope();

    @Override
    TopLevelNode<?> getNode();

    /**
     * Returns the set of lexically declared names.
     * 
     * @return the lexically declared names
     */
    Set<Name> lexicallyDeclaredNames();

    /**
     * Returns the list of lexically scoped declarations.
     * 
     * @return the lexically scoped declarations
     */
    List<Declaration> lexicallyScopedDeclarations();

    /**
     * Returns the set of variable declared names.
     * 
     * @return the variable declared names
     */
    Set<Name> varDeclaredNames();

    /**
     * Returns the list of variable scoped declarations.
     * 
     * @return the variable scoped declarations
     */
    List<StatementListItem> varScopedDeclarations();
}

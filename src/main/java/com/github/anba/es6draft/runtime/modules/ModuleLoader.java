/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.modules;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.anba.es6draft.compiler.CompilationException;
import com.github.anba.es6draft.parser.ParserException;
import com.github.anba.es6draft.runtime.Realm;

/**
 * 
 */
public interface ModuleLoader {
    /**
     * Returns the normalized module identifier.
     * 
     * @param unnormalizedName
     *            the unnormalized module name
     * @param referrerId
     *            the identifier of the including module or {@code null}
     * @return the normalized module identifier
     * @throws MalformedNameException
     *             if the name cannot be normalized
     */
    SourceIdentifier normalizeName(String unnormalizedName, SourceIdentifier referrerId) throws MalformedNameException;

    /**
     * Retrieves the requested module record.
     * <p>
     * If the module record is not linked, it will be linked against <var>realm</var>.
     * 
     * @param identifier
     *            the module source identifier
     * @param realm
     *            the realm instance
     * @return the module record or {@code null} if not found
     */
    ModuleRecord get(SourceIdentifier identifier, Realm realm);

    /**
     * Defines a new module record in this module loader.
     * <p>
     * If the module record is not linked, it will be linked against <var>realm</var>.
     * 
     * @param identifier
     *            the module source identifier
     * @param source
     *            the module source code
     * @param realm
     *            the realm record
     * @return the new module record
     * @throws IOException
     *             if there was any I/O error
     * @throws IllegalArgumentException
     *             if the source identifier cannot be processed by this loader
     * @throws ParserException
     *             if the module source contains any syntax errors
     * @throws CompilationException
     *             if the parsed module source cannot be compiled
     */
    ModuleRecord define(SourceIdentifier identifier, ModuleSource source, Realm realm)
            throws IOException, IllegalArgumentException, ParserException, CompilationException;

    /**
     * Defines a new module record in this module loader.
     * <p>
     * If the module record is not linked, it will be linked against <var>realm</var>.
     * 
     * @param identifier
     *            the module source identifier
     * @param source
     *            the module source code
     * @param realm
     *            the realm record
     * @return the new module record
     */
    default CompletionStage<ModuleRecord> defineAsync(SourceIdentifier identifier, ModuleSource source, Realm realm) {
        try {
            return CompletableFuture.completedFuture(define(identifier, source, realm));
        } catch (IOException | IllegalArgumentException | ParserException | CompilationException e) {
            CompletableFuture<ModuleRecord> c = new CompletableFuture<>();
            c.completeExceptionally(e);
            return c;
        }
    }

    /**
     * Resolves and links the requested module record.
     * <p>
     * If the module record is not linked, it will be linked against <var>realm</var>.
     * 
     * @param identifier
     *            the module source identifier
     * @param realm
     *            the realm instance
     * @return the module record
     * @throws IOException
     *             if there was any I/O error
     * @throws IllegalArgumentException
     *             if the source identifier cannot be processed by this loader
     * @throws ParserException
     *             if the module source contains any syntax errors
     * @throws CompilationException
     *             if the parsed module source cannot be compiled
     */
    ModuleRecord resolve(SourceIdentifier identifier, Realm realm)
            throws IOException, IllegalArgumentException, ParserException, CompilationException;

    /**
     * Resolves and links the requested module record.
     * <p>
     * If the module record is not linked, it will be linked against <var>realm</var>.
     * 
     * @param identifier
     *            the module source identifier
     * @param realm
     *            the realm instance
     * @return the module record
     */
    default CompletionStage<ModuleRecord> resolveAsync(SourceIdentifier identifier, Realm realm) {
        try {
            return CompletableFuture.completedFuture(resolve(identifier, realm));
        } catch (IOException | IllegalArgumentException | ParserException | CompilationException e) {
            CompletableFuture<ModuleRecord> c = new CompletableFuture<>();
            c.completeExceptionally(e);
            return c;
        }
    }

    /**
     * Loads a module and all of its dependencies.
     * 
     * @param identifier
     *            the module source identifier
     * @return the loaded module record
     * @throws IOException
     *             if there was any I/O error
     * @throws IllegalArgumentException
     *             if the source identifier cannot be processed by this loader
     * @throws MalformedNameException
     *             if the module specifier cannot be normalized
     */
    ModuleRecord load(SourceIdentifier identifier) throws IOException, IllegalArgumentException, MalformedNameException;

    /**
     * Loads a module and all of its dependencies.
     * 
     * @param identifier
     *            the module source identifier
     * @return the loaded module record
     */
    default CompletionStage<ModuleRecord> loadAsync(SourceIdentifier identifier) {
        try {
            return CompletableFuture.completedFuture(load(identifier));
        } catch (IOException | IllegalArgumentException | MalformedNameException e) {
            CompletableFuture<ModuleRecord> c = new CompletableFuture<>();
            c.completeExceptionally(e);
            return c;
        }
    }
}

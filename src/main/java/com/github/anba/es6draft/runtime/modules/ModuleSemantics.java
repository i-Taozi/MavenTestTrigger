/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.modules;

import static com.github.anba.es6draft.runtime.types.builtins.ModuleNamespaceObject.ModuleNamespaceCreate;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.github.anba.es6draft.compiler.CompilationException;
import com.github.anba.es6draft.parser.ParserException;
import com.github.anba.es6draft.runtime.ExecutionContext;
import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Messages;
import com.github.anba.es6draft.runtime.types.ScriptObject;

/**
 * <ul>
 * <li>15.2.1.17 Runtime Semantics: HostResolveImportedModule
 * <li>15.2.1.18 Runtime Semantics: GetModuleNamespace
 * <li>15.2.1.19 Runtime Semantics: TopLevelModuleEvaluationJob
 * </ul>
 */
public final class ModuleSemantics {
    private ModuleSemantics() {
    }

    /**
     * 15.2.1.17 Runtime Semantics: HostResolveImportedModule (referencingModule, specifier )
     * 
     * @param referencingModule
     *            the referencing module
     * @param specifier
     *            the module specifier string
     * @return the resolved module record
     * @throws IOException
     *             if there was any I/O error
     * @throws MalformedNameException
     *             if the module specifier cannot be normalized
     * @throws ResolutionException
     *             if the module cannot be resolved
     * @throws ParserException
     *             if the module source contains any syntax errors
     * @throws CompilationException
     *             if the parsed module source cannot be compiled
     */
    public static ModuleRecord HostResolveImportedModule(ModuleRecord referencingModule, String specifier)
            throws IOException, MalformedNameException, ResolutionException, ParserException, CompilationException {
        Realm realm = referencingModule.getRealm();
        assert realm != null : "module is not linked";
        ModuleLoader moduleLoader = realm.getModuleLoader();
        SourceIdentifier moduleId = moduleLoader.normalizeName(specifier, referencingModule.getSourceCodeId());
        try {
            return moduleLoader.resolve(moduleId, realm);
        } catch (NoSuchFileException e) {
            throw new ResolutionException(Messages.Key.ModulesUnresolvedModule, moduleId.toString(),
                    referencingModule.getSourceCodeId().toString());
        }
    }

    /**
     * 15.2.1.18 Runtime Semantics: GetModuleNamespace( module )
     * 
     * @param cx
     *            the execution context
     * @param module
     *            the module record
     * @return the module namespace object
     * @throws IOException
     *             if there was any I/O error
     * @throws MalformedNameException
     *             if any imported module request cannot be normalized
     * @throws ResolutionException
     *             if any export binding cannot be resolved
     */
    public static ScriptObject GetModuleNamespace(ExecutionContext cx, ModuleRecord module)
            throws IOException, MalformedNameException, ResolutionException {
        /* step 1 (not applicable) */
        /* step 2 */
        // FIXME: spec bug - [[Status]] only defined for Source Text Module Records.
        assert !(module instanceof SourceTextModuleRecord)
                || ((SourceTextModuleRecord) module).getStatus() != SourceTextModuleRecord.Status.Uninstantiated;
        /* step 3 */
        // FIXME: spec bug - [[Status]] only defined for Source Text Module Records.
        assert !(module instanceof SourceTextModuleRecord)
                || (((SourceTextModuleRecord) module).getStatus() != SourceTextModuleRecord.Status.Evaluated
                        || ((SourceTextModuleRecord) module).getEvaluationError() == null);
        /* step 4 */
        ScriptObject namespace = module.getNamespace();
        /* step 5 */
        if (namespace == null) {
            /* step 5.a */
            Set<String> exportedNames = module.getExportedNames(new HashSet<>());
            /* step 5.b */
            Set<String> unambiguousNames = new HashSet<>();
            /* step 5.c */
            for (String name : exportedNames) {
                ResolvedBinding resolution = module.resolveExport(name, new HashMap<>());
                if (resolution != null && !resolution.isAmbiguous()) {
                    unambiguousNames.add(name);
                }
            }
            /* step 5.d */
            namespace = ModuleNamespaceCreate(cx, module, unambiguousNames);
        }
        /* step 6 */
        return namespace;
    }

    /**
     * 15.2.1.19 Runtime Semantics: TopLevelModuleEvaluationJob ( sourceText)
     * 
     * @param realm
     *            the realm instance
     * @param sourceCodeId
     *            the module source code identifier
     * @param source
     *            the module source code
     * @throws IOException
     *             if there was any I/O error
     * @throws MalformedNameException
     *             if any imported module request cannot be normalized
     * @throws ParserException
     *             if the module source contains any syntax errors
     * @throws CompilationException
     *             if the parsed module source cannot be compiled
     * @throws ResolutionException
     *             if any export binding cannot be resolved
     */
    public static void ModuleEvaluationJob(Realm realm, SourceIdentifier sourceCodeId, ModuleSource source)
            throws IOException, MalformedNameException, ParserException, CompilationException, ResolutionException {
        ModuleLoader moduleLoader = realm.getModuleLoader();
        /* steps 1-2 (not applicable) */
        /* steps 3-5 */
        ModuleRecord m = moduleLoader.define(sourceCodeId, source, realm);
        /* step 6 */
        m.instantiate();
        /* steps 7-8 */
        m.evaluate();
    }
}

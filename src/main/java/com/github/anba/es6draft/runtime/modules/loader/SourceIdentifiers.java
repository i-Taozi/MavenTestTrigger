/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.modules.loader;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.anba.es6draft.runtime.modules.MalformedNameException;

/**
 *
 */
final class SourceIdentifiers {
    private SourceIdentifiers() {
    }

    /**
     * Normalizes the source identifier.
     * 
     * @param unnormalizedName
     *            the unnormalized module name
     * @param referrerId
     *            the referrer uri to resolve relative modules
     * @param base
     *            the base uri to resolve non-relative modules
     * @return the normalized source identifier {@link URI}
     * @throws MalformedNameException
     *             if the name cannot be normalized
     */
    static URI normalize(String unnormalizedName, URI referrerId, URI base) throws MalformedNameException {
        URI moduleName = parse(unnormalizedName);
        if (referrerId != null && isRelative(moduleName)) {
            moduleName = referrerId.resolve(moduleName);
        } else {
            moduleName = base.resolve(moduleName);
        }
        return moduleName.normalize();
    }

    private static URI parse(String unnormalizedName) throws MalformedNameException {
        URI moduleName;
        try {
            moduleName = new URI(unnormalizedName);
        } catch (URISyntaxException e) {
            throw new MalformedNameException(unnormalizedName);
        }
        if (hasIllegalComponents(moduleName) || hasEmptyPath(moduleName)) {
            throw new MalformedNameException(unnormalizedName);
        }
        if (isPath(moduleName)) {
            // TODO: Treat as ?
            throw new MalformedNameException(unnormalizedName);
        }
        if (isAbsolute(moduleName)) {
            // TODO: Treat as ?
            throw new MalformedNameException(unnormalizedName);
        }
        return moduleName;
    }

    private static boolean isAbsolute(URI moduleName) {
        return moduleName.getRawPath().startsWith("/");
    }

    private static boolean isPath(URI moduleName) {
        return moduleName.getRawPath().endsWith("/");
    }

    private static boolean isRelative(URI moduleName) {
        return moduleName.getRawPath().startsWith("./") || moduleName.getRawPath().startsWith("../");
    }

    private static boolean hasEmptyPath(URI moduleName) {
        return moduleName.getRawPath() == null || moduleName.getRawPath().isEmpty();
    }

    private static boolean hasIllegalComponents(URI moduleName) {
        // All components except for 'path' must be empty.
        if (moduleName.getScheme() != null) {
            return true;
        }
        if (moduleName.getRawAuthority() != null) {
            return true;
        }
        if (moduleName.getRawUserInfo() != null) {
            return true;
        }
        if (moduleName.getHost() != null) {
            return true;
        }
        if (moduleName.getPort() != -1) {
            return true;
        }
        if (moduleName.getRawQuery() != null) {
            return true;
        }
        if (moduleName.getRawFragment() != null) {
            return true;
        }
        return false;
    }
}

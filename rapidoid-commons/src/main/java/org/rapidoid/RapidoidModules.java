/*-
 * #%L
 * rapidoid-commons
 * %%
 * Copyright (C) 2014 - 2020 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.rapidoid;

import org.essentials4j.Do;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.cls.Cls;
import org.rapidoid.commons.CommonsModule;
import org.rapidoid.u.U;

import java.lang.reflect.Modifier;
import java.util.*;

@Authors("Nikolche Mihajlovski")
@Since("5.3.0")
public class RapidoidModules extends RapidoidThing {

    private static final Comparator<RapidoidModule> MODULE_COMPARATOR = (mod1, mod2) -> mod1.order() - mod2.order();

    public static RapidoidModule get(String name) {
        return Do.findIn(getAllAvailable())
                .first(module -> module.name().equals(name))
                .orElseThrow(() -> U.rte("Can't find Rapidoid module with name: ''!", name));
    }

    public static List<RapidoidModule> getAll() {
        return all(false);
    }

    public static List<RapidoidModule> getAllAvailable() {
        return all(true);
    }

    private static List<RapidoidModule> all(boolean availableOnly) {
        List<RapidoidModule> modules = U.list();

        addServiceLoaderModules(availableOnly, modules);

        addBuiltInModules(modules);

        validate(modules);

        modules.sort(MODULE_COMPARATOR);

        return modules;
    }

    private static void addBuiltInModules(List<RapidoidModule> modules) {
        modules.add(new CommonsModule());
        addModuleIfExists(modules, "org.rapidoid.http.HttpModule");
    }

    private static void addModuleIfExists(List<RapidoidModule> modules, String clsName) {
        Class<?> cls = Cls.getClassIfExists(clsName);

        boolean isModule = cls != null
                && !cls.isInterface()
                && RapidoidModule.class.isAssignableFrom(cls)
                && !Modifier.isAbstract(cls.getModifiers());

        if (isModule) {
            modules.add((RapidoidModule) Cls.newInstance(cls));
        }
    }

    private static void addServiceLoaderModules(boolean availableOnly, List<RapidoidModule> modules) {
        Iterator<RapidoidModule> it = ServiceLoader.load(RapidoidModule.class).iterator();

        while (it.hasNext()) {
            RapidoidModule mod;

            if (availableOnly) {
                try {
                    mod = it.next();
                } catch (ServiceConfigurationError e) {
                    mod = null;
                    // ignore it
                }
            } else {
                mod = it.next();
            }

            if (mod != null) {
                modules.add(mod);
            }
        }
    }

    private static void validate(List<RapidoidModule> modules) {
        for (RapidoidModule module : modules) {
            U.notNull(module.name(), "the name of module %s", module.getClass().getSimpleName());
        }
    }

}

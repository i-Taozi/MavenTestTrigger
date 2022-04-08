/*
 * Copyright (C) 2012-2021 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.archinnov.achilles.internals.strategy.naming;

import org.apache.commons.lang3.StringUtils;

public class CaseSensitiveNaming implements InternalNamingStrategy {
    @Override
    public String apply(String name) {
        if (StringUtils.isBlank(name)) return "";
        return toCaseSensitive(name);
    }

    @Override
    public String FQCN() {
        return this.getClass().getCanonicalName();
    }
}

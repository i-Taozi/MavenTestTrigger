/*-
 * #%L
 * rapidoid-http-server
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

package org.rapidoid.http.handler;

import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.commons.Err;
import org.rapidoid.http.DataSchema;
import org.rapidoid.http.impl.OpenAPIDataSchema;

import java.util.Map;

@Authors("Nikolche Mihajlovski")
@Since("6.0.0")
public class DataSchemas extends RapidoidThing {

    public static DataSchema openAPI(String id, Map<String, Object> schema) {
        return new OpenAPIDataSchema(id, schema);
    }

    public static DataSchema fromType(Class<?> type) {
        throw Err.notReady(); // FIXME construct data schema from Java types (primitive, collections or JavaBeans)
    }

}

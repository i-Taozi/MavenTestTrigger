/*-
 * #%L
 * rapidoid-rest
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

package org.rapidoid.docs.httpgeneric;

import org.rapidoid.setup.App;
import org.rapidoid.u.U;

public class Main {

    public static void main(String[] args) {
        App app = new App(args);

        /* Generic handlers match any request (in the declaration order) */

        app.req(req -> req.data().isEmpty() ? "Simple: " + req.uri() : null);

        /* The next handler is executed if the previous returns [NOT FOUND] */

        app.req(req -> U.list(req.verb(), req.uri(), req.data()));

        app.start();
    }

}

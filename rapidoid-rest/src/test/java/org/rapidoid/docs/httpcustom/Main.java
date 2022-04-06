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

package org.rapidoid.docs.httpcustom;

import org.rapidoid.config.Conf;
import org.rapidoid.setup.App;

public class Main {

    public static void main(String[] args) {
        App app = new App(args);

        // first thing to do - initializing Rapidoid, without bootstrapping anything at the moment

        // customizing the server address and port - before the server is bootstrapped
        app.address("0.0.0.0").port(9998);

        // fine-tuning the HTTP server
        Conf.HTTP.set("maxPipeline", 32);
        Conf.NET.set("bufSizeKB", 16);

        // continue with normal setup
        app.get("/x").json("x");

        app.start();
    }

}

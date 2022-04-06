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

package org.rapidoid.http;

import org.junit.jupiter.api.Test;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.setup.App;

@Authors("Nikolche Mihajlovski")
@Since("5.1.0")
public class HttpHandlerTypesTest extends IsolatedIntegrationTest {

    @Test
    public void testHandlerTypes() {
        App app = new App().start();

        app.get("/a").html(req -> "a");

        app.get("/b").html((req, resp) -> "b");

        app.get("/c").html((ReqHandler) req -> "c");

        app.get("/d").html((ReqRespHandler) (req, resp) -> "d");

        app.get("/e").html((Req req) -> "e");

        app.get("/f").html((Req req, Integer x) -> "f");

        app.get("/g").html((Req req, Resp resp) -> "g");

        app.get("/h").html((Resp yy, Integer tt, Resp xx, Req rrr, Boolean b) -> "h");

        onlyGet("/a");
        onlyGet("/b");
        onlyGet("/c");
        onlyGet("/d");
        onlyGet("/e");
        onlyGet("/f");
        onlyGet("/g");
        onlyGet("/h");
    }

}

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
import org.rapidoid.u.U;

import java.util.Map;

@Authors("Nikolche Mihajlovski")
@Since("5.4.3")
public class HttpUnmanagedTest extends IsolatedIntegrationTest {

    private static final Map<String, Integer> DATA = U.map("x", 123);

    private static final byte[] PRE_RENDERED = "{\"xyz\": 12345}".getBytes();

    @Test
    public void testUnmanagedHandlersWithPrerenderedJSON1() {
        App app = new App().start();

        app.post("/json").managed(false).json((Req req, Resp resp) -> resp.body(PRE_RENDERED));

        onlyPost("/json");
    }

    @Test
    public void testUnmanagedHandlersWithPrerenderedJSON2() {
        App app = new App().start();

        app.get("/json").managed(false).serve((Req req, Resp resp) -> resp.contentType(MediaType.JSON).body(PRE_RENDERED));

        onlyGet("/json");
    }

    @Test
    public void testUnmanagedHandlersWithPrerenderedJSON3() {
        App app = new App().start();

        app.post("/json").managed(false).serve((Req req, Resp resp) -> {

            resp.header("hdr1", "val1")
                    .cookie("cook1", "the-cookie");

            return resp.contentType(MediaType.JSON).code(500).body(PRE_RENDERED);
        });

        onlyPost("/json");
    }

    @Test
    public void testUnmanagedHandlersWithPrerenderedJSON4() {
        App app = new App().start();

        app.get("/json/{num}").managed(false).json((Integer num, Req req, Resp resp) -> {

            resp.header("hdr1", "val1")
                    .cookie("cook1", "the-cookie");

            return num < 0 ? resp.code(201).json(U.map("neg", num)) : U.map("pos", num);
        });

        onlyGet("/json/102030");
        onlyGet("/json/-7");
    }

    @Test
    public void testUnmanagedHandlersWithNormalJSON1() {
        App app = new App().start();

        app.get("/json").managed(false).json((Req req, Resp resp) -> DATA);

        onlyGet("/json");
    }

    @Test
    public void testUnmanagedHandlersWithNormalJSON2() {
        App app = new App().start();

        app.post("/json").managed(false).serve((Req req, Resp resp) -> {
            resp.contentType(MediaType.JSON)
                    .code(500)
                    .header("hdr1", "val1")
                    .cookie("cook1", "the-cookie");

            return DATA;
        });

        onlyPost("/json");
    }

    @Test
    public void testUnmanagedHandlersWithHtml() {
        App app = new App().start();

        app.post("/").managed(false).serve((Req req, Resp resp) -> resp.html("denied!").code(403));

        onlyPost("/");
    }

    @Test
    public void testUnmanagedHandlersWithPlainText() {
        App app = new App().start();

        app.post("/").managed(false).json((Req req, Resp resp) -> {
            resp.code(404)
                    .contentType(MediaType.PLAIN_TEXT_UTF_8)
                    .header("hdr1", "val1")
                    .cookie("cook1", "the-cookie");

            return "NOT found!";
        });

        onlyPost("/");
    }

    @Test
    public void testUnmanagedHandlersWithPlainTextSimple() {
        App app = new App().start();

        app.get("/").managed(false).plain(() -> "hi!");

        onlyGet("/");
    }

    @Test
    public void testUnmanagedHandlersWithErrors() {
        App app = new App().start();

        app.get("/").managed(false).json(() -> {
            throw U.rte("INTENTIONAL ERROR!");
        });

        onlyGet("/");
    }

}

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

package org.rapidoid.restapi;

import org.junit.jupiter.api.Test;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.data.JSON;
import org.rapidoid.http.HttpResp;
import org.rapidoid.http.IsolatedIntegrationTest;
import org.rapidoid.http.ReqHandler;
import org.rapidoid.http.Self;
import org.rapidoid.setup.App;
import org.rapidoid.test.ExpectErrors;
import org.rapidoid.u.U;

@Authors("Nikolche Mihajlovski")
@Since("5.2.9")
public class HttpRestAPITest extends IsolatedIntegrationTest {

    private App initAPI() {
        App app = new App().start();

        app.get("/inc/{x}").json((ReqHandler) req -> U.num(req.param("x")) + 1);

        return app;
    }

    @Test
    public void testRestAPI() {
        initAPI();

        Self.get("/inc/99").expect("100");
    }

    @Test
    public void testNotFound() {
        initAPI();

        HttpResp resp = Self.get("/foo/baz").execute();

        eq(resp.code(), 404);
        eq(resp.body(), JSON.stringify(U.map(
                "error", "The requested resource could not be found!",
                "code", 404,
                "status", "Not Found"
        )));
    }

    @Test
    @ExpectErrors
    public void testRuntimeError() {
        initAPI();

        HttpResp resp = Self.get("/inc/d9g").execute();

        eq(resp.code(), 500);
        eq(resp.body(), JSON.stringify(U.map(
                "error", "For input string: \"d9g\"",
                "code", 500,
                "status", "Internal Server Error"
        )));
    }

}

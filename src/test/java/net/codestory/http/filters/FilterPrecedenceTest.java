/**
 * Copyright (C) 2013-2015 all@code-story.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.codestory.http.filters;

import net.codestory.http.payload.Payload;
import net.codestory.http.testhelpers.AbstractProdWebServerTest;

import org.junit.Test;

public class FilterPrecedenceTest extends AbstractProdWebServerTest {
  @Test
  public void first_filter_first() {
    configure(routes -> routes
        .filter((uri, context, next) -> new Payload("first/" + next.get().rawContent()))
        .filter((uri, context, next) -> new Payload("second/" + next.get().rawContent()))
        .get("/", "last")
    );

    get("/").should().contain("first/second/last");
  }
}

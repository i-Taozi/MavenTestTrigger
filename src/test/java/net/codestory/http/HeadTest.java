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
package net.codestory.http;

import net.codestory.http.annotations.*;
import net.codestory.http.testhelpers.*;

import org.junit.*;

public class HeadTest extends AbstractProdWebServerTest {
  @Test
  public void implicit_head() {
    configure(routes -> routes
        .get("/", "Hello")
    );

    head("/").should().respond(200).haveType("").contain("");
  }

  @Test
  public void explicit_head() {
    configure(routes -> routes
        .head("/", "Hello")
    );

    head("/").should().respond(200).haveType("").contain("");
  }

  @Test
  public void implicit_head_resource() {
    configure(routes -> routes
        .add(Resource.class)
    );

    head("/implicit").should().respond(200).haveType("").contain("");
    head("/explicit").should().respond(200).haveType("").contain("");
  }

  public static class Resource {
    @Get("/implicit")
    public String implicit() {
      return "Hello";
    }

    @Head("/explicit")
    public String explicit() {
      return "Hello";
    }
  }
}

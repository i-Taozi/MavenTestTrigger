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
package net.codestory.http.errors;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpExceptionTest {
  @Test
  public void not_found() {
    HttpException error = new NotFoundException();

    assertThat(error.code()).isEqualTo(404);
  }

  @Test
  public void forbidden() {
    HttpException error = new ForbiddenException();

    assertThat(error.code()).isEqualTo(403);
  }

  @Test
  public void unauthorized() {
    HttpException error = new UnauthorizedException();

    assertThat(error.code()).isEqualTo(401);
  }

  @Test
  public void bad_request() {
    HttpException error = new BadRequestException();

    assertThat(error.code()).isEqualTo(400);
  }
}

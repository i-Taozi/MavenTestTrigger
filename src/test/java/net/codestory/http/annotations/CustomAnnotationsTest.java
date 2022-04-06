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
package net.codestory.http.annotations;

import net.codestory.http.*;
import net.codestory.http.filters.basic.BasicAuthFilter;
import net.codestory.http.payload.Payload;
import net.codestory.http.security.UsersList;
import net.codestory.http.testhelpers.AbstractProdWebServerTest;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class CustomAnnotationsTest extends AbstractProdWebServerTest {
  @Test
  public void around_annotation() {
    UsersList users = new UsersList.Builder()
      .addUser("user", "pwd")
      .addUser("dummy", "pwd")
      .build();

    configure(routes -> routes
        .filter(new BasicAuthFilter("/", "realm", users))
        .registerAroundAnnotation(DummyShallNotPass.class, (annotation, context, payloadSupplier) -> "dummy".equals(context.currentUser().name()) ? Payload.forbidden() : payloadSupplier.apply(context))
        .add(new MyResource())
    );

    get("/").withAuthentication("user", "pwd").should().contain("Hello");
    get("/").withAuthentication("dummy", "pwd").should().respond(403);
  }

  @Test
  public void after_annotation() {
    configure(routes -> routes
        .registerAfterAnnotation(Header.class, (annotation, context, payload) -> payload.withHeader(annotation.key(), annotation.value()))
        .add(new MyResource())
    );

    get("/").should().contain("Hello").haveHeader("theHeader", "theValue");
  }

  @Test
  public void around_annotation_class() {
    UsersList users = new UsersList.Builder()
      .addUser("user", "pwd")
      .addUser("dummy", "pwd")
      .build();

    configure(routes -> routes
        .filter(new BasicAuthFilter("/", "realm", users))
        .registerAroundAnnotation(DummyShallNotPass.class, ShallNotPass.class)
        .add(new MyResource())
    );

    get("/").withAuthentication("user", "pwd").should().contain("Hello");
    get("/").withAuthentication("dummy", "pwd").should().respond(403);
  }

  @Test
  public void after_annotation_class() {
    configure(routes -> routes
        .registerAfterAnnotation(Header.class, AddHeader.class)
        .add(new MyResource())
    );

    get("/").should().contain("Hello").haveHeader("theHeader", "theValue");
  }

  public static class ShallNotPass implements ApplyAroundAnnotation<DummyShallNotPass> {
    @Override
    public Payload apply(DummyShallNotPass annotation, Context context, Function<Context, Payload> payloadSupplier) {
      return "dummy".equals(context.currentUser().name()) ? Payload.forbidden() : payloadSupplier.apply(context);
    }
  }

  public static class AddHeader implements ApplyAfterAnnotation<Header> {
    @Override
    public Payload apply(Header annotation, Context context, Payload payload) {
      return payload.withHeader(annotation.key(), annotation.value());
    }
  }

  @Target(TYPE)
  @Retention(RUNTIME)
  @interface DummyShallNotPass {
  }

  @Target(METHOD)
  @Retention(RUNTIME)
  @interface Header {
    String key();

    String value();
  }

  @DummyShallNotPass
  static class MyResource {
    @Header(key = "theHeader", value = "theValue")
    @Get("/")
    public String hello() {
      return "Hello";
    }
  }
}

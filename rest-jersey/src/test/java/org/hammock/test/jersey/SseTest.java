/*
 * Copyright 2017 Hammock and its contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hammock.test.jersey;

import org.hammock.test.jersey.sse.SseEndpoint;
import org.hammock.test.jersey.sse.SseModel;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import ws.ament.hammock.johnzon.JohnzonExtension;
import ws.ament.hammock.test.support.EnableRandomWebServerPort;
import ws.ament.hammock.test.support.HammockArchive;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;

@RunWith(Arquillian.class)
@EnableRandomWebServerPort
public class SseTest {

    @Deployment
    public static JavaArchive createArchive() {
        return new HammockArchive().classes(SseEndpoint.class, JohnzonExtension.class).jar();
    }

    @ArquillianResource
    private URI uri;

    @Test
    public void shouldBeAbleToRetrieveRestEndpoint() throws Exception {
        WebTarget target = ClientBuilder.newClient().register(JohnzonExtension.class).target(uri+"/sse/{uuid}").resolveTemplate("uuid", UUID.randomUUID().toString());
        List<SseModel> receivedModels = new ArrayList<>();
        try (SseEventSource eventSource = SseEventSource.target(target).build()) {
            eventSource.register(event -> {
                SseModel body = event.readData(SseModel.class, MediaType.APPLICATION_JSON_TYPE);
                System.out.println("Received "+body.getName());
                receivedModels.add(body);
            }, System.out::println);
            eventSource.open();
            // Give the SSE stream some time to collect all events
            Thread.sleep(1000);
        }
        assertFalse(receivedModels.isEmpty());
    }

}

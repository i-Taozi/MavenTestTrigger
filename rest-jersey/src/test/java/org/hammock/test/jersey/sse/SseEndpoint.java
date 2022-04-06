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

package org.hammock.test.jersey.sse;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import static javax.ws.rs.core.MediaType.SERVER_SENT_EVENTS;

@RequestScoped
@Path("/sse/")
public class SseEndpoint {
    @GET
    @Path("/{uuid}")
    @Produces(SERVER_SENT_EVENTS)
    public void doSseCall(@PathParam("uuid") String uuid, @Context SseEventSink sink, @Context Sse sse) {
        final OutboundSseEvent.Builder builder = sse.newEventBuilder();
        OutboundSseEvent event = builder.id(uuid)
                .data(SseModel.class, new SseModel("some model "+uuid))
                .build();
        sink.send(event);
        sink.close();
    }
}

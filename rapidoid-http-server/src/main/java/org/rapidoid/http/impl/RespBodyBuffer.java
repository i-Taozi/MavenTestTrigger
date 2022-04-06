/*-
 * #%L
 * rapidoid-http-server
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

package org.rapidoid.http.impl;

import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.http.RespBody;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.u.U;

import java.nio.ByteBuffer;

@Authors("Nikolche Mihajlovski")
@Since("5.5.1")
public class RespBodyBuffer extends RapidoidThing implements RespBody {

    private final ByteBuffer buffer;

    public RespBodyBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int length() {
        return buffer.remaining();
    }

    @Override
    public void writeTo(Channel channel) {
        channel.write(buffer);
    }

    @Override
    public String toString() {
        return U.frmt("RespBodyBuffer(%s bytes)", length());
    }
}

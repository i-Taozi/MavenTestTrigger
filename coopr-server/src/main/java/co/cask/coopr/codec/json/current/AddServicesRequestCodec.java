/*
 * Copyright © 2012-2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.cask.coopr.codec.json.current;

import co.cask.coopr.http.request.AddServicesRequest;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Codec for deserializing a {@link co.cask.coopr.http.request.AddServicesRequest},
 * used so some validation is done on required fields.
 */
public class AddServicesRequestCodec implements JsonDeserializer<AddServicesRequest> {

  @Override
  public AddServicesRequest deserialize(JsonElement json, Type type, JsonDeserializationContext context)
    throws JsonParseException {
    JsonObject jsonObj = json.getAsJsonObject();
    Map<String, Object> providerFields = context.deserialize(jsonObj.get("providerFields"),
                                                             new TypeToken<Map<String, String>>() { }.getType());
    Set<String> services = context.deserialize(jsonObj.get("services"), new TypeToken<Set<String>>() { }.getType());

    return new AddServicesRequest(providerFields, services);
  }
}

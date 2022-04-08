/*
 * Copyright 2019 SPF4J.
 *
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
 */
package org.spf4j.avro.official;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderAdapter;
import org.apache.avro.io.EncoderFactory;
import org.spf4j.avro.Adapter;
import org.spf4j.avro.SchemaResolver;

/**
 * Adapter for the official library.
 * @author Zoltan Farkas
 */
public final class OfficialAvroAdapter implements Adapter {

  public static final JsonFactory FACTORY = new JsonFactory();

  static {
    FACTORY.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
  }

  private final EncoderFactory encFactory = EncoderFactory.get();

  private final DecoderFactory decFactory = DecoderFactory.get();

  @Override
  public Encoder getJsonEncoder(final Schema writerSchema, final OutputStream os) throws IOException {
    return encFactory.jsonEncoder(writerSchema, os);
  }

  @Override
  public Encoder getJsonEncoder(final Schema writerSchema, final Appendable os) throws IOException {
    return EncoderAdapter.jsonEncoder(writerSchema, FACTORY.createGenerator(CharStreams.asWriter(os)));
  }


  @Override
  public Schema.Field createField(final String name, final Schema schema, final String doc,
          final Object defaultVal,
          final boolean validateDefault, final boolean validateName, final Schema.Field.Order order) {
    return new Schema.Field(name, schema, doc, defaultVal);
  }

  @Override
  public Schema createRecordSchema(final String name, final String doc, final String namespace,
          final boolean isError, final List<Schema.Field> fields, final boolean validateName) {
    return Schema.createRecord(name, doc, namespace, isError, fields);
  }

  @Override
  public Schema createRecordSchema(final String name,
          final String doc, final String namespace, final boolean isError, final boolean validateName) {
    return Schema.createRecord(name, doc, namespace, isError);
  }

  @Override
  public Decoder getJsonDecoder(final Schema writerSchema, final InputStream is) throws IOException {
    return decFactory.jsonDecoder(writerSchema, is);
  }

  @Override
  public Schema parseSchema(final Reader reader, final boolean allowUndefinedLogicalTypes,
          final SchemaResolver resolver)
          throws IOException {
    Logger.getLogger(OfficialAvroAdapter.class.getName())
            .warning("Official avro lib schema references not supported yet");
    return new Schema.Parser().parse(CharStreams.toString(reader));
  }

  @Override
  public Schema parseSchema(final Reader reader) throws IOException {
    return new Schema.Parser().parse(CharStreams.toString(reader));
  }

  @Override
  public Decoder getJsonDecoder(final Schema writerSchema, final Reader reader) throws IOException {
    return decFactory.jsonDecoder(writerSchema,
            new ByteArrayInputStream(CharStreams.toString(reader).getBytes(StandardCharsets.UTF_8)));
  }

  @Override
  public Decoder getJsonDecoder(final Schema writerSchema, final JsonParser parser) throws IOException {
      StringWriter buff = new StringWriter();
      TokenBuffer.asCopyOfValue(parser).serialize(FACTORY.createGenerator(buff));
        return decFactory.jsonDecoder(writerSchema,
            new ByteArrayInputStream(buff.toString().getBytes(StandardCharsets.UTF_8)));

  }


  @Override
  public String toString() {
    return "OfficialAvroAdapter{" + "encFactory=" + encFactory + ", decFactory=" + decFactory + '}';
  }

  @Override
  public Decoder getYamlDecoder(final Schema schema, final Reader reader) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCompatible() {
    try {
      Schema.Field.class.getConstructor(String.class, Schema.class,
              String.class,  Object.class,
              boolean.class, boolean.class, Schema.Field.Order.class);
      return false;
    } catch (NoSuchMethodException | SecurityException ex) {
      return true;
    }
  }

}

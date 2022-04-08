/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
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
package org.spf4j.avro.schema;

import com.google.common.io.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.demo.avro.DemoRecordInfo;
import org.spf4j.test.TestRecord;
import org.spf4j.test.TestRecord2;

/**
 *
 * @author zoly
 */
public class SchemasTest {

  private static final Logger LOG = LoggerFactory.getLogger(SchemasTest.class);

  private static final String SCHEMA = "{\"type\":\"record\",\"name\":\"SampleNode\",\"doc\":\"caca\","
          + "\"namespace\":\"org.spf4j.ssdump2.avro\",\n"
          + " \"fields\":[\n"
          + "    {\"name\":\"count\",\"type\":\"int\",\"default\":0,\"doc\":\"caca\"},\n"
          + "    {\"name\":\"subNodes\",\"type\":\n"
          + "       {\"type\":\"array\",\"items\":{\n"
          + "           \"type\":\"record\",\"name\":\"SamplePair\",\n"
          + "           \"fields\":[\n"
          + "              {\"name\":\"method\",\"type\":\n"
          + "                  {\"type\":\"record\",\"name\":\"Method\",\n"
          + "                  \"fields\":[\n"
          + "                     {\"name\":\"declaringClass\","
          + "\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\n"
          + "                     {\"name\":\"methodName\","
          + "\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}\n"
          + "                  ]}},\n"
          + "              {\"name\":\"node\",\"type\":\"SampleNode\"}]}}}]}";

  @Test
  public void testVisit() throws IOException {
    Schema recSchema = new Schema.Parser().parse(SCHEMA);
    Schemas.visit(recSchema, new PrintingVisitor());

    String schemaStr = Resources.toString(Resources.getResource("SchemaBuilder.avsc"), StandardCharsets.US_ASCII);
    Schema schema = new Schema.Parser().parse(schemaStr);

    Map<String, Schema> schemas = Schemas.visit(schema, new SchemasWithClasses());
    Assert.assertThat(schemas, Matchers.hasValue(schema));

    Schema trimmed = Schemas.visit(recSchema, new CloningVisitor(recSchema));
    Assert.assertNull(trimmed.getDoc());
    Assert.assertNotNull(recSchema.getDoc());

    SchemaCompatibility.SchemaCompatibilityType compat
            = SchemaCompatibility.checkReaderWriterCompatibility(trimmed, recSchema).getType();
    Assert.assertEquals(SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE, compat);
    compat = SchemaCompatibility.checkReaderWriterCompatibility(recSchema, trimmed).getType();
    Assert.assertEquals(SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE, compat);

    Schema unmodifyable = Schemas.visit(recSchema, new ImmutableCloningVisitor(recSchema, false));
    Assert.assertNotNull(unmodifyable.getDoc());
    compat
            = SchemaCompatibility.checkReaderWriterCompatibility(unmodifyable, recSchema).getType();
    Assert.assertEquals(SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE, compat);
    compat = SchemaCompatibility.checkReaderWriterCompatibility(recSchema, unmodifyable).getType();
    Assert.assertEquals(SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE, compat);

    Schema schema1 = unmodifyable.getField("subNodes").schema().getElementType().getField("node").schema();
    try {
      schema1.addAlias("yahooo");
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
    }

    try {
      schema1.setFields(Collections.EMPTY_LIST);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
    }

  }

  private static class PrintingVisitor implements SchemaVisitor {

    @Override
    public SchemaVisitorAction visitTerminal(final Schema terminal) {
      LOG.debug("Terminal: {}", terminal.getFullName());
      return SchemaVisitorAction.CONTINUE;
    }

    @Override
    public SchemaVisitorAction visitNonTerminal(final Schema terminal) {
      LOG.debug("NONTerminal start: {}", terminal.getFullName());
      return SchemaVisitorAction.CONTINUE;
    }

    @Override
    public SchemaVisitorAction afterVisitNonTerminal(final Schema terminal) {
      LOG.debug("NONTerminal end: {}", terminal.getFullName());
      return SchemaVisitorAction.CONTINUE;
    }
  }

  @Test
  public void testSchemaPath() {
    Schema subSchema = Schemas.getSubSchema(DemoRecordInfo.SCHEMA$, "demoRecord.id");
    Assert.assertEquals(Schema.Type.STRING, subSchema.getType());
  }

  @Test
  public void testSchemaPath2() {
    Schema subSchema = Schemas.getSubSchema(Schema.createArray(DemoRecordInfo.SCHEMA$), "[].demoRecord.id");
    Assert.assertEquals(Schema.Type.STRING, subSchema.getType());
  }

  @Test
  public void testSchemaPath3() {
    Schema subSchema = Schemas.getSubSchema(TestRecord.getClassSchema(), "children.[]");
    Assert.assertEquals(Schema.Type.RECORD, subSchema.getType());
  }

  @Test
  public void testSchemaPath4() {
    Schema subSchema = Schemas.getSubSchema(TestRecord2.getClassSchema(), "children.[].hash");
    Assert.assertEquals(Schema.Type.FIXED, subSchema.getType());
  }

  @Test
  public void testSchemaPath5() {
    Schema subSchema = Schemas.getSubSchema(TestRecord.getClassSchema(), "children.[].hash");
    Assert.assertNull(subSchema);
  }


  @Test
  public void testProjections() {
    Schema project = Schemas.project(DemoRecordInfo.SCHEMA$, "demoRecord.id", "metaData");
    Schema.Field drF = project.getField("demoRecord");
    Assert.assertEquals(0, drF.pos());
    List<Schema.Field> drf = drF.schema().getFields();
    Assert.assertEquals(1, drf.size());
    Assert.assertEquals("id", drf.get(0).name());
    Assert.assertEquals(DemoRecordInfo.SCHEMA$.getField("metaData").schema(), project.getField("metaData").schema());
  }

  @Test
  public void testProjections2() {
    Schema project = Schemas.project(DemoRecordInfo.SCHEMA$, "demoRecord.id", "bubu");
    Assert.assertNull(project);
  }

  @Test
  public void testProjectionsOrder() {
    Schema project = Schemas.project(DemoRecordInfo.SCHEMA$, "metaData", "demoRecord.id");
    Schema.Field drF = project.getField("demoRecord");
    Assert.assertEquals(1, drF.pos());
    List<Schema.Field> drf = drF.schema().getFields();
    Assert.assertEquals(1, drf.size());
    Schema.Field field = drf.get(0);
    Assert.assertEquals("id", field.name());
    Assert.assertEquals(DemoRecordInfo.SCHEMA$.getField("metaData").schema(), project.getField("metaData").schema());
  }

  @Test
  public void testProjectionSame() {
    Schema tSchema = TestRecord.getClassSchema();
    Schema subSchema = Schemas.project(tSchema, "");
    Assert.assertEquals(tSchema, subSchema);
  }

  @Test
  @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT") // there is one
  public void testDiff1() {
    Schema tSchema = TestRecord.getClassSchema();
    Schemas.diff(tSchema, tSchema, (diff) -> Assert.fail(diff.toString()));
  }

  @Test
  public void testDiff2() {
    Schema tSchema = TestRecord.getClassSchema();
    List<SchemaDiff> diffs = new ArrayList<>();
    Schemas.diff(tSchema, TestRecord2.getClassSchema(),
            (diff) -> {
              LOG.debug("Diff: {}", diff);
              Schema atSchema = Schemas.getSubSchema(tSchema, diff.getPath());
              Assert.assertNotNull("Path does not resolve: " + diff.getPath(), atSchema);
              LOG.debug("At Schema {}", atSchema.getFullName());
              Assert.assertNotNull(Schemas.project(tSchema, diff.getPath()).getFullName());
              diffs.add(diff);
            });
    Assert.assertThat(diffs, Matchers.not(Matchers.empty()));
  }



}

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
package org.spf4j.ssdump2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.base.Methods;
import org.spf4j.base.avro.Converters;
import org.spf4j.base.avro.StackSampleElement;
import org.spf4j.stackmonitor.SampleNode;

/**
 *
 * @author zoly
 */
public class ConverterTest {

  private static final Logger LOG = LoggerFactory.getLogger(ConverterTest.class);

    private SampleNode testSample() {
         StackTraceElement[] st1 = new StackTraceElement[3];
        st1[0] = new StackTraceElement("C1", "m1", "C1.java", 10);
        st1[1] = new StackTraceElement("C1", "m2", "C1.java", 11);
        st1[2] = new StackTraceElement("C1", "m3", "C1.java", 12);
        SampleNode node = SampleNode.createSampleNode(st1);

        StackTraceElement[] st2 = new StackTraceElement[1];
        st2[0] = new StackTraceElement("C1", "m1", "C1.java", 10);
        SampleNode.addToSampleNode(node, st2);

        StackTraceElement[] st3 = new StackTraceElement[3];
        st3[0] = new StackTraceElement("C2", "m1", "C2.java", 10);
        st3[1] = new StackTraceElement("C2", "m2", "C2.java", 11);
        st3[2] = new StackTraceElement("C2", "m3", "C2.java", 12);
        SampleNode.addToSampleNode(node, st3);

        StackTraceElement[] st4 = new StackTraceElement[3];
        st4[0] = new StackTraceElement("C1", "m1", "C1.java", 10);
        st4[1] = new StackTraceElement("C1", "m2", "C1.java", 11);
        st4[2] = new StackTraceElement("C1", "m4", "C1.java", 14);
        SampleNode.addToSampleNode(node, st4);
        return node;
    }


    @Test
    public void testSaveLoad() throws IOException {
      File test = File.createTempFile("test", ".ssdumnp3");
      SampleNode testSample = testSample();
      Map<String, SampleNode> dumps = new HashMap<>(4);
      dumps.put("zero", null);
      dumps.put("something", testSample);
      Converter.saveLabeledDumps(test, dumps);
      Map<String, SampleNode> loadLabeledDumps = Converter.loadLabeledDumps(test);
      Assert.assertEquals(testSample, loadLabeledDumps.get("something"));
    }

     @Test
    public void test() {
        SampleNode testSample = testSample();
        final List<StackSampleElement> samples = new ArrayList<>();
        Converters.convert(Methods.ROOT, testSample, -1, 0,  samples::add);
        SampleNode back = Converter.convert(samples.iterator());
        Assert.assertEquals(testSample, back);
    }


    @Test
    public void testLoad() throws IOException {
      Map<String, SampleNode> labeledDumps
              = Converter.loadLabeledDumps(new File("./src/test/resources/test.ssdump3"));
      Assert.assertNotNull(labeledDumps);
      LOG.debug("dumps", labeledDumps);
      Assert.assertThat(labeledDumps.toString(),
              Matchers.containsString("getUnchecked@org.spf4j.concurrent.UnboundedLoadingCache"));

    }

    @Test
    public void testNameUtils() {
      String fileName = Converter.createLabeledSsdump2FileName("base", "bla_");
      Assert.assertEquals("bla_", Converter.getLabelFromSsdump2FileName(fileName));
    }


}

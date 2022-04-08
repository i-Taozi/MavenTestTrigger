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
package org.spf4j.perf;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.spf4j.perf.impl.RecorderFactory;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.base.avro.AvroCloseableIterable;
import org.spf4j.perf.impl.ProcessMeasurementStore;
import org.spf4j.tsdb2.avro.Observation;

/**
 *
 * @author zoly
 */
@SuppressWarnings("SleepWhileInLoop")
@SuppressFBWarnings("MDM_THREAD_YIELD")
public final class RecorderFactoryTest {

  private static final Logger LOG = LoggerFactory.getLogger(RecorderFactoryTest.class);

  /**
   * Test of createScalableQuantizedRecorder method, of class RecorderFactory.
   */
  @Test
  public void testCreateScalableQuantizedRecorder() throws  IOException, InterruptedException {
    String forWhat = "test1";
    String unitOfMeasurement = "ms";
    int sampleTime = 1000;
    int factor = 10;
    int lowerMagnitude = 0;
    int higherMagnitude = 3;
    int quantasPerMagnitude = 10;
    CloseableMeasurementRecorder result = RecorderFactory.createScalableQuantizedRecorder2(
            forWhat, unitOfMeasurement, sampleTime, factor, lowerMagnitude, higherMagnitude, quantasPerMagnitude);
    for (int i = 0; i < 500; i++) {
      result.record(i);
      Thread.sleep(20);
    }
    result.close();
    assertData(forWhat, 124750);
    MeasurementStore store = ProcessMeasurementStore.getMeasurementStore();
    MeasurementStoreQuery query = store.query();
    Collection<Schema> measurements = query.getMeasurements((x) -> forWhat.equals(x));
    Schema m = measurements.iterator().next();
    try (AvroCloseableIterable<Observation> observations = query.getObservations(m, Instant.EPOCH, Instant.now())) {
      for (Observation o : observations) {
        LOG.debug("RAW Obeservation", o);
      }
    }
    try (AvroCloseableIterable<Observation> observations = query.getAggregatedObservations(m,
            Instant.EPOCH, Instant.now(), 2, TimeUnit.SECONDS)) {
      for (Observation o : observations) {
        LOG.debug("AGG Obeservation", o);
      }
    }
  }

  /**
   * Test of createScalableQuantizedRecorderSource method, of class RecorderFactory.
   */
  @Test
  public void testCreateScalableQuantizedRecorderSource() throws IOException, InterruptedException {
    Object forWhat = "bla";
    String unitOfMeasurement = "ms";
    int sampleTime = 1000;
    int factor = 10;
    int lowerMagnitude = 0;
    int higherMagnitude = 3;
    int quantasPerMagnitude = 10;
    CloseableMeasurementRecorderSource result = RecorderFactory.createScalableQuantizedRecorderSource2(
            forWhat, unitOfMeasurement, sampleTime, factor, lowerMagnitude, higherMagnitude, quantasPerMagnitude);
    for (int i = 0; i < 5000; i++) {
      result.getRecorder("X" + i % 2).record(1);
      Thread.sleep(1);
    }
    result.close();
    assertData("bla_X0", 2500);
  }

  @Test
  public void testOutofQuantizedZoneValues() throws IOException, InterruptedException {
    String forWhat = "largeVals";
    String unitOfMeasurement = "ms";
    int sampleTime = 1000;
    int factor = 10;
    int lowerMagnitude = 0;
    int higherMagnitude = 3;
    int quantasPerMagnitude = 10;
    CloseableMeasurementRecorder result = RecorderFactory.createScalableQuantizedRecorder2(
            forWhat, unitOfMeasurement, sampleTime, factor, lowerMagnitude, higherMagnitude, quantasPerMagnitude);
    for (int i = 0; i < 500; i++) {
      result.record(10000);
      Thread.sleep(20);
    }
    result.close();
    assertData(forWhat, 5000000);

  }

  /**
   * Test of createScalableQuantizedRecorderSource method, of class RecorderFactory.
   */
  @Test
  public void testCreateScalableCountingRecorderSource() throws IOException, InterruptedException {
    String forWhat = "counters";
    String unitOfMeasurement = "counts";
    int sampleTime = 1000;
    CloseableMeasurementRecorderSource result = RecorderFactory.createScalableCountingRecorderSource2(
            forWhat, unitOfMeasurement, sampleTime);
    for (int i = 0; i < 5000; i++) {
      result.getRecorder("X" + i % 2).record(1);
      Thread.sleep(1);
    }
    result.close();
    assertData(forWhat + "_X1", 2500);
  }

  @SuppressFBWarnings("CLI_CONSTANT_LIST_INDEX")
  public static void assertData(final String forWhat, final long expectedValue) throws IOException {
    MeasurementStore store = ProcessMeasurementStore.getMeasurementStore();
    store.flush();
    MeasurementStoreQuery query = store.query();
    Collection<Schema> schemas = query.getMeasurements((x) -> forWhat.equals(x));
    Schema schema = schemas.iterator().next();
    try (AvroCloseableIterable<Observation> observations = query.getObservations(schema, Instant.EPOCH,
            Instant.ofEpochMilli(Long.MAX_VALUE))) {
      long sum = 0;
      for (Observation o : observations) {
        sum += o.getData().get(0);
      }
      Assert.assertEquals(expectedValue, sum);
    }
  }

  public static void main(final String[] args) throws IOException, InterruptedException {
    new RecorderFactoryTest().testCreateScalableQuantizedRecorder();
  }

}

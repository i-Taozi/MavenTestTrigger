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
package co.cask.coopr.management;

import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @param <T> Type of stat to count.
 */
public abstract class StatCounter<T> {
  private final ConcurrentMap<T, AtomicLong> stats;

  protected StatCounter() {
    this.stats = Maps.newConcurrentMap();
  }

  public void incrementStat(T action) {
    AtomicLong val = stats.get(action);
    if (val == null) {
      val = new AtomicLong(0);
      AtomicLong oldVal = stats.putIfAbsent(action, val);
      val = oldVal == null ? val : oldVal;
    }
    val.incrementAndGet();
  }

  protected long getValue(T action) {
    AtomicLong val = stats.get(action);
    return val == null ? 0 : val.get();
  }
}

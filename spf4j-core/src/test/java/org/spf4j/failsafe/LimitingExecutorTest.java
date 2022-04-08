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
package org.spf4j.failsafe;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.base.TimeSource;
import org.spf4j.log.Level;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.matchers.LogMatchers;
import org.spf4j.test.log.TestLoggers;

/**
 *
 * @author Zoltan Farkas
 */
public class LimitingExecutorTest {

  private static final Logger LOG = LoggerFactory.getLogger(LimitingExecutorTest.class);

  @Test(expected = RejectedExecutionException.class)
  public void testRateLimit() throws Exception {
    try (RateLimiter limiter = new RateLimiter(10, Duration.ofSeconds(1), 10)) {
      LimitingExecutor<?, Callable<?>> executor = new LimitingExecutor<>(limiter);
      Assert.assertEquals(10, limiter.getPermitsPerReplenishInterval());
      Assert.assertEquals(1000000000, limiter.getPermitReplenishIntervalNanos());
      for (int i = 0; i < 10; i++) {
        final int val = i;
        executor.execute(() -> {
          LOG.debug("executed nr {}", val);
          return null;
        });
      }
      Assert.fail();
    }
  }

  @Test
  public void testRateLimit2() throws Exception {
    LogAssert expect = TestLoggers.sys().expect(LimitingExecutorTest.class.getName(), Level.DEBUG, 10,
            LogMatchers.hasFormat("executed nr {}"));
    try (RateLimiter limiter = new RateLimiter(10, Duration.ofSeconds(1), 10)) {
      long replenishInterval = limiter.getPermitReplenishIntervalNanos();
       LOG.debug("replenish interval = {} ", replenishInterval);
      LimitingExecutor.RejectedExecutionHandler rejectedExecutionHandler
              = new LimitingExecutor.RejectedExecutionHandler() {
        @Override
        @SuppressFBWarnings("MDM_THREAD_YIELD")
        public Object reject(final LimitingExecutor executor, final Callable callable)
                throws Exception {
          long nanoTime = TimeSource.nanoTime();
          long lastReplenishmentNanos = limiter.getLastReplenishmentNanos();
          long waitNs = replenishInterval - (nanoTime - lastReplenishmentNanos);
          if (waitNs >= 0) {
            TimeUnit.NANOSECONDS.sleep(waitNs);
          } else {
            LOG.debug("negative wait time {}, current = {}, lastUpdated = {}, permits = {} ", waitNs,
                    nanoTime, lastReplenishmentNanos, limiter.getNrPermits());
          }
          return executor.execute(callable);
        }
      };
      LimitingExecutor<?, Callable<?>> executor
              = new LimitingExecutor<>(rejectedExecutionHandler, limiter.toSemaphore());
      for (int i = 0; i < 10; i++) {
        final int val = i;
        executor.execute(() -> {
          LOG.debug("executed nr {}", val);
          return null;
        });
      }
    }
    expect.assertObservation();
  }

}

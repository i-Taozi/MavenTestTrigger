/*
 * Copyright 2018 SPF4J.
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
package org.spf4j.test.log.junit4;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayDeque;
import java.util.List;
import org.spf4j.base.ExecutionContext;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.log.LogCollection;
import org.spf4j.test.log.TestLogRecord;

/**
 * @author Zoltan Farkas
 */
public final class TestBaggage {
  private final ExecutionContext ctx;
  private final LogCollection<ArrayDeque<TestLogRecord>> logCollection;
  private final List<LogAssert> assertions;

  TestBaggage(final ExecutionContext ctx,
          final LogCollection<ArrayDeque<TestLogRecord>> logCollection, final List<LogAssert> assertions) {
    this.ctx = ctx;
    this.logCollection = logCollection;
    this.assertions = assertions;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public ExecutionContext getCtx() {
    return ctx;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public LogCollection<ArrayDeque<TestLogRecord>> getLogCollection() {
    return logCollection;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public List<LogAssert> getAssertions() {
    return assertions;
  }

  @Override
  public String toString() {
    return "TestBaggage{" + "ctx=" + ctx + ", logCollection=" + logCollection + ", assertions=" + assertions + '}';
  }


}

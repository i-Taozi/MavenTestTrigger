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
package org.spf4j.test.log;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Zoltan Farkas
 */
@SuppressFBWarnings("LO_SUSPECT_LOG_CLASS") // in this case we are testing.
public class TestGlobalErrorIgnore {

  private static final Logger LOG = LoggerFactory.getLogger(TestGlobalErrorIgnore.class);

  private static final Logger LOG2 = LoggerFactory.getLogger("a.b");

  static {
    java.util.logging.Logger.getLogger(TestGlobalErrorIgnore.class.getName())
            .severe("This will not fail due to surefirre setup to ignore 0");
     LOG.error("This will not fail due to surefire setup to ignore 1");
     LOG2.error("l2");
  }


  @Test
  @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
  // no need for assert, asserting that this test passes while logging an error.
  public void testSomeHandler() {
    LOG.error("This will not fail due to surefirre setup to ignore 2");
    java.util.logging.Logger.getLogger(TestGlobalErrorIgnore.class.getName())
            .severe("This will not fail due to surefirre setup to ignore 3");
    LOG2.error("l2");
  }
}

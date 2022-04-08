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
package org.spf4j.log;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.spf4j.base.ExecutionContext;
import org.spf4j.base.ExecutionContexts;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.log.annotations.ExpectLog;
import org.spf4j.test.matchers.LogMatchers;

/**
 * @author Zoltan Farkas
 */
@SuppressFBWarnings("LO_SUSPECT_LOG_CLASS")
public class ExecContextLoggerTest {


  @Test
  @ExpectLog(level = Level.TRACE, messageRegexp = "msg1")
  public void testTrace() {
    ExecContextLogger log = new ExecContextLogger(LoggerFactory.getLogger("test"));
    log.trace("msg1");
    List<Slf4jLogRecord> logs = new ArrayList<>(2);
    ExecutionContexts.current().streamLogs(logs::add);
    Assert.assertEquals("msg1", logs.get(0).getMessageFormat());
  }

  @Test
  public void testTrace2() {
    ExecContextLogger log = new ExecContextLogger(LoggerFactory.getLogger("test"));
    LogAssert expect = TestLoggers.sys().expect("test", Level.DEBUG,
            Matchers.allOf(LogMatchers.hasExtraArgument(LogAttribute.origLevel(Level.TRACE)),
            LogMatchers.hasMessage("msg1")));
    ExecutionContext current = ExecutionContexts.current();
    current.setBackendMinLogLevel("test", Level.TRACE);
    log.trace("msg1");
    expect.assertObservation();
    List<Slf4jLogRecord> logs = new ArrayList<>(2);
    current.streamLogs(logs::add);
    Assert.assertEquals("msg1", logs.get(0).getMessageFormat());
  }

}

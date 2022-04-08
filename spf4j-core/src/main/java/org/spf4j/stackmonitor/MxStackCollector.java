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
package org.spf4j.stackmonitor;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author zoly
 */
public final class MxStackCollector implements ISampler {

    private static final ThreadMXBean THREAD_MX = ManagementFactory.getThreadMXBean();

    private final Thread ignore;

    private final StackCollector collector;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public MxStackCollector(final Thread ignore) {
      this.ignore = ignore;
      this.collector = new StackCollectorImpl();
    }

    @Override
    public void sample() {
        ThreadInfo[] stackDump = THREAD_MX.dumpAllThreads(false, false);
        recordStackDump(stackDump);
    }

    private void recordStackDump(final ThreadInfo[] stackDump) {
        final long id = ignore.getId();
        for (ThreadInfo entry : stackDump) {
            StackTraceElement[] stackTrace = entry.getStackTrace();
            if (stackTrace.length > 0 && (entry.getThreadId() != id)) {
                collector.collect(stackTrace);
            }
        }
    }

  @Override
  public Map<String, SampleNode> getCollectionsAndReset() {
    SampleNode nodes = collector.getAndReset();
    return nodes == null ? Collections.EMPTY_MAP : ImmutableMap.of("ALL", nodes);
  }

  @Override
  public Map<String, SampleNode> getCollections() {
    SampleNode nodes = collector.get();
    return nodes == null ? Collections.EMPTY_MAP : ImmutableMap.of("ALL", nodes);
  }

  @Override
  public String toString() {
    return "MxStackCollector{" + "ignore=" + ignore + ", collector=" + collector + '}';
  }


}

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
package org.spf4j.os;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spf4j.base.Strings;

/**
 * @author Zoltan Farkas
 */
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
public final class StdOutToStringProcessHandler implements ProcessHandler<String, String> {

  private Logger log;

  public StdOutToStringProcessHandler() {
    this.log = Logger.getLogger(StdOutToStringProcessHandler.class.getName());
  }

  public void started(final Process p) {
    int pid = ProcessUtil.getPid(p);
    log.log(Level.FINE, "Started {0} with pid={1} ", new Object[]{p, pid});
    this.log = Logger.getLogger(log.getName() + '.' + pid);
  }

  @Override
  public String handleStdOut(final InputStream stdout) throws IOException {
    StringBuilder result = new StringBuilder(128);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, Charset.defaultCharset()));
    String line;
    while ((line = reader.readLine()) != null) {
      log.fine(line);
      result.append(line).append(Strings.EOL);
    }
    log.fine("done with stdout");
    return result.toString();
  }

  @Override
  public String handleStdErr(final InputStream stderr) throws IOException {
    StringBuilder result = new StringBuilder(128);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stderr, Charset.defaultCharset()));
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("INFO")) {
        log.info(line);
      } else if (line.startsWith("WARN")) {
        log.warning(line);
      } else {
        log.severe(line);
      }
      result.append(line).append(Strings.EOL);
    }
    log.fine("done with stderr");
    return result.toString();
  }

  @Override
  public String toString() {
    return "StdOutToStringProcessHandler{" + "log=" + log + '}';
  }

}

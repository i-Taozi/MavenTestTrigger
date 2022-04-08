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
package org.spf4j.test.log;

import com.google.common.annotations.Beta;
import com.google.common.base.Ascii;
import java.util.List;
import org.spf4j.base.Env;

/**
 * @author Zoltan Farkas
 */
public final class TestUtils {
  private TestUtils() { }

  public static boolean isExecutedDuringMvnRelease() {
    return Env.getValue("MAVEN_CMD_LINE_ARGS", "").contains("release:");
  }

  public static boolean isExecutedInCI() {
    return isExecutedInTravis() || isExecutedInGithubAction();
  }

  public static boolean isExecutedInTravis() {
    return Ascii.equalsIgnoreCase("true", Env.getValue("TRAVIS", "false"));
  }

  public static boolean isExecutedInGithubAction() {
    return Ascii.equalsIgnoreCase("true", Env.getValue("GITHUB_ACTION", "false"));
  }

  public static boolean isExecutedFromNetbeans() {
    String mvnNetbeansCP = System.getProperty("maven.ext.class.path");
    return mvnNetbeansCP != null && mvnNetbeansCP.contains("netbeans");
  }

  @Beta
  public static boolean isExecutedFromIntelij() {
    for (StackTraceElement[] st : Thread.getAllStackTraces().values()) {
      if (st != null) {
        for (StackTraceElement ste : st) {
          if (ste.getClassName().startsWith("com.intellij.rt.execution")) {
            return true;
          }
        }
      }
    }
    return false;
  }


  @Beta
  public static boolean isExecutedFromEclipse() {
    for (StackTraceElement[] st : Thread.getAllStackTraces().values()) {
      if (st != null) {
        for (StackTraceElement ste : st) {
          if (ste.getClassName().startsWith("org.eclipse.jdt.internal")) {
            return true;
          }
        }
      }
    }
    return false;
  }



  /**
   * Supporting netbeans only at this time.
   * For other IDEs you need to configure them to pass the spf4j.execFromIDE property.
   * @return
   */
  public static boolean isExecutedFromIDE() {
    return isExecutedFromNetbeans() || System.getProperty("spf4j.execFromIDE") != null
            || isExecutedFromEclipse() || isExecutedFromIntelij();
  }

  public static boolean isExecutedFromIDEInvididually() {
    return isExecutedFromIDE() && System.getProperty("test") != null;
  }

  public static boolean isExecutedWithDebuggerAgent() {
    List<String> inputArguments = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments();
    return inputArguments.stream()
            .filter((arg) -> "-agentlib:jdwp".equals(arg) || "-Xdebug".equals(arg) || arg.startsWith("-Xrunjdwp"))
            .count() > 0;

  }

}

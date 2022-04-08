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
package org.spf4j.base;

import org.spf4j.os.OperatingSystem;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import org.spf4j.base.avro.ApplicationInfo;
import org.spf4j.base.avro.Organization;
import org.spf4j.concurrent.UIDGenerator;
import org.spf4j.io.ByteArrayBuilder;
import org.spf4j.jmx.JmxExport;
import org.spf4j.jmx.Registry;
import org.spf4j.os.ProcessHandler;
import org.spf4j.os.ProcessResponse;
import org.spf4j.os.ProcessUtil;
import org.spf4j.recyclable.impl.ArraySuppliers;
import org.spf4j.unix.CLibrary;
import org.spf4j.unix.JVMArguments;
import org.spf4j.unix.Lsof;
import org.spf4j.unix.UnixRuntime;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings("PATH_TRAVERSAL_IN")
public final class Runtime {

  public static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));
  public static final String TMP_FOLDER = System.getProperty("java.io.tmpdir");
  public static final Path TMP_FOLDER_PATH = Paths.get(TMP_FOLDER);
  public static final String JAVA_VERSION = System.getProperty("java.version");
  public static final String USER_NAME = System.getProperty("user.name");
  public static final String USER_DIR = System.getProperty("user.dir");
  public static final String USER_HOME = System.getProperty("user.home");
  public static final String JAVA_HOME = System.getProperty("java.home");

  /**
   * Unix PID identifying your process in the OC image it is running.
   */
  public static final int PID = ProcessUtil.getPid();
  public static final String PROCESS_NAME;
  public static final String OS_NAME = OperatingSystem.getOsName();

  /**
   * a unique ID for this JVM process.
   * PID@HOSTNAME:HEXNR
   */
  public static final String PROCESS_ID;
  public static final int NR_PROCESSORS;
  public static final Version JAVA_PLATFORM;

  private static final java.lang.Runtime JAVA_RUNTIME = java.lang.Runtime.getRuntime();


  static {
    // priming certain functionality to make sure it works when we need it (classes are already loaded).
    try (PrintStream stream = new PrintStream(new ByteArrayBuilder(), false, "UTF-8")) {
      RuntimeException rex = new RuntimeException("priming");
      Throwables.writeTo(rex, stream, Throwables.PackageDetail.NONE);
      Throwables.containsNonRecoverable(rex);
    } catch (UnsupportedEncodingException ex) {
      throw new ExceptionInInitializerError(ex);
    }
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    final int availableProcessors = JAVA_RUNTIME.availableProcessors();
    if (availableProcessors <= 0) {
      ErrLog.error("Invalid number of processors " + availableProcessors
              + " defaulting to 1");
      NR_PROCESSORS = 1;
    } else {
      NR_PROCESSORS = availableProcessors;
    }
    String mxBeanName = runtimeMxBean.getName();
    PROCESS_NAME = System.getProperty("spf4j.processName", mxBeanName);
    boolean useUIDGeneratorForJvmId = Boolean.getBoolean("spf4j.useUIDForProcessId");
    PROCESS_ID = useUIDGeneratorForJvmId ? UIDGenerator.generateIdBase("J", '-').toString()
            : mxBeanName + ':' + Long.toHexString((System.currentTimeMillis() - 1509741164184L) / 1000);
    JAVA_PLATFORM = Version.fromSpecVersion(JAVA_VERSION);
    if (Boolean.getBoolean("spf4j.runtime.jmx")) {
      Registry.export(Jmx.class);
    }
  }

  public enum Version {

    V1_0, V1_1, V1_2, V1_3, V1_4, V1_5, V1_6, V1_7, V1_8, V1_9, V_10, V_11, V_12;

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS") // not really redundant
    public static Version fromSpecVersion(final String specVersion) {
      String[] cmpnts = specVersion.split("\\.");
      if  (cmpnts.length > 1) {
        return Version.values()[Integer.parseInt(cmpnts[1])];
      } else if (cmpnts.length == 1) {
        return Version.values()[Integer.parseInt(cmpnts[0])];
      } else {
        throw new IllegalArgumentException("Unsupported specVersion: " + specVersion);
      }
    }

  }

  private Runtime() {
  }

  public static org.spf4j.base.Version getAppVersion() {
    return new org.spf4j.base.Version(getAppVersionString());
  }

  public static String getAppVersionString() {
    Class<?> mainClass = getMainClass();
    String version = mainClass.getPackage().getImplementationVersion();
    if (version == null) {
      return "N/A";
    } else  {
      return version;
    }
  }

  /**
   * Returns application information.
   * Information is retrieved from the app jar manifest.
   * Manifest can be generated with maven like:
   *
   * <pre>
   * {@code
   *   <plugin>
   *     <groupId>org.apache.maven.plugins</groupId>
   *     <artifactId>maven-jar-plugin</artifactId>
   *     <version>3.1.1</version>
   *     <configuration>
   *       <archive>
   *         <index>true</index>
   *         <manifest>
   *           <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
   *           <addClasspath>true</addClasspath>
   *           <classpathPrefix>lib/</classpathPrefix>
   *           <mainClass>org.spf4j.demo.Main</mainClass>
   *         </manifest>
   *         <manifestEntries>
   *           <Implementation-Vendor>${project.groupId}</Implementation-Vendor>
   *           <Implementation-Vendor-Id>${project.groupId}</Implementation-Vendor-Id>
   *           <Implementation-Title>${project.artifactId}</Implementation-Title>
   *           <Implementation-Version>${project.version}</Implementation-Version>
   *           <Implementation-Description>${project.description}</Implementation-Description>
   *           <Implementation-Url>${project.url}</Implementation-Url>
   *           <Implementation-Org>${project.organization.name}</Implementation-Org>
   *           <Implementation-Org-Url>${project.organization.url}</Implementation-Org-Url>
   *           <Implementation-Build>${buildNumber}</Implementation-Build>
   *           <Build-Time>${maven.build.timestamp}</Build-Time>
   *         </manifestEntries>
   *       </archive>
   *     </configuration>
   *   </plugin>
   *
   *
   * }
   * </pre>
   * @return
   */
  public static ApplicationInfo getApplicationInfo() {
    Class<?> mainClass = getMainClass();
    if (mainClass == null) {
      try {
        return new ApplicationInfo("N/A", "N/A main class", new URL("file://manifest/Implementation-Url"), null);
      } catch (MalformedURLException ex) {
        throw new RuntimeException(ex);
      }
    }
    Package p = mainClass.getPackage();
    if (p == null) {
      try {
        return new ApplicationInfo("N/A", "N/A package Info", new URL("file://manifest/Implementation-Url"), null);
      } catch (MalformedURLException ex) {
        throw new RuntimeException(ex);
      }
    }
    URL jarSourceUrl = PackageInfo.getJarSourceUrl(mainClass);
    String implementationTitle = p.getImplementationTitle();
    if (implementationTitle == null) {
      implementationTitle = "N/A manifest:Implementation-Title";
    }
    if (jarSourceUrl == null) {
      try {
        return new ApplicationInfo(implementationTitle, "N/A jar",
                new URL("file://manifest/Implementation-Url"), null);
      } catch (MalformedURLException ex) {
        throw new RuntimeException(ex);
      }
    }
    try {
      Manifest manifest = Reflections.getManifest(jarSourceUrl);
      if (manifest == null) {
        try {
          return new ApplicationInfo(implementationTitle, "N/A jar manifest",
                  new URL("file://manifest/Implementation-Url"), null);
        } catch (MalformedURLException ex) {
          throw new RuntimeException(ex);
        }
      }
      Attributes mainAttributes = manifest.getMainAttributes();
      String appDescription = mainAttributes.getValue("Implementation-Description");
      String appUrl = mainAttributes.getValue("Implementation-Url");
      String org = mainAttributes.getValue("Implementation-Org");
      String orgUrl = mainAttributes.getValue("Implementation-Org-Url");
      return new ApplicationInfo(implementationTitle, appDescription == null ? "" : appDescription,
              (appUrl == null || appUrl.trim().isEmpty())
                      ? new URL("file://manifest/Implementation-Url") : new URL(appUrl),
              (org != null && !org.trim().isEmpty()) ? new Organization(org,
                        new URL((orgUrl == null || orgUrl.trim().isEmpty())
                                ? "file://manifest/Implementation-Org-Url" : orgUrl)) : null);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static boolean isShuttingDown() {
      try {
        java.lang.Runtime runtime = java.lang.Runtime.getRuntime();
        Thread dummy = new Thread(AbstractRunnable.NOP);
        runtime.addShutdownHook(dummy);
        runtime.removeShutdownHook(dummy);
      } catch (IllegalStateException e) {
          return true;
      }
      return false;
  }

  /**
   * @deprecated use equivalent from ErrLog
   */
  @Deprecated
  public static void error(final String message) {
    ErrLog.error(message);
  }

  /**
   * @deprecated use equivalent from ErrLog
   */
  @Deprecated
  public static void error(final String message, final Throwable t) {
    ErrLog.error(message, t);
  }

  /**
   * @deprecated use equivalent from ErrLog
   */
  @Deprecated
  public static void errorNoPackageDetail(final String message, final Throwable t) {
    ErrLog.errorNoPackageDetail(message, t);
  }

  public static void goDownWithError(final SysExits exitCode) {
    goDownWithError(null, exitCode.exitCode());
  }

  public static void goDownWithError(@Nullable final Throwable t, final SysExits exitCode) {
    goDownWithError(t, exitCode.exitCode());
  }

  // Calling Halt is the only sensible thing to do when the JVM is hosed.
  @SuppressFBWarnings("MDM_RUNTIME_EXIT_OR_HALT")
  public static void goDownWithError(@Nullable final Throwable t, final int exitCode) {
    try {
      if (t != null) {
        Throwables.writeTo(t, System.err, Throwables.PackageDetail.NONE); //High probability attempt to log first
        ErrLog.error("Error, going down with exit code " + exitCode, t);
        //Now we are pushing it...
        Logger logger = Logger.getLogger(Runtime.class.getName());
        logger.log(Level.SEVERE, "Error, going down with exit code {0}", exitCode);
        logger.log(Level.SEVERE, "Exception detail", t);
      } else {
        ErrLog.error("Error, going down with exit code " + exitCode);
        Logger.getLogger(Runtime.class.getName())
                .log(Level.SEVERE, "Error, going down with exit code {0}", exitCode);
      }
    } finally {
      JAVA_RUNTIME.halt(exitCode);
    }
  }

  /**
   * @return true on macosx.
   * @deprecated use OperatingSystem
   */
  @Deprecated
  public static boolean isMacOsx() {
    return OperatingSystem.isMacOsx();
  }

  /**
   *
   * @return true on windows.
   * @deprecated use OperatingSystem
   */
  @Deprecated
  public static boolean isWindows() {
    return OperatingSystem.isWindows();
  }

  public static boolean isTestFramework() {
    StackTraceElement[][] stackTraces = Threads.getStackTraces(Threads.getThreads());
    for (StackTraceElement[] sts : stackTraces) {
      if (sts != null) {
        for (StackTraceElement ste : sts) {
          String className = ste.getClassName();
          if (className.startsWith("org.junit") || className.startsWith("org.openjdk.jmh")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * @return true if jna platform is present.
   * @deprecated use JNA instead
   */
  @Deprecated
  public static boolean haveJnaPlatform() {
    return JNA.haveJnaPlatform();
  }

  /**
   * @return true if jna platform clib is present.
   * @deprecated use JNA instead.
   */
  @Deprecated
  public static boolean haveJnaPlatformClib() {
    return JNA.haveJnaPlatformClib();
  }

  /**
   * get the number of open files by current java process.
   *
   * @return -1 if cannot get nr of open files
   * @deprecated use OperatingSystem.getOpenFileDescriptorCount() instead
   */
  @CheckReturnValue
  @Deprecated
  public static int getNrOpenFiles() {
    return (int) OperatingSystem.getOpenFileDescriptorCount();
  }

  /**
   * @deprecated use Lsof.getLsofOutput instead.
   */
  @Nullable
  @CheckReturnValue
  @Deprecated
  public static CharSequence getLsofOutput() {
    return Lsof.getLsofOutput();
  }

  /**
   * @deprecated use Processhandler
   */
  @Deprecated
  public interface ProcOutputHandler {

    void handleStdOut(byte[] bytes, int length);

    void stdOutDone();

    void handleStdErr(byte[] bytes, int length);

    void stdErrDone();
  }

  /**
   * @deprecated use OperatingSystem.forkExec.
   */
  @Deprecated
  public static CharSequence run(final String[] command,
          final long timeoutMillis) throws IOException, InterruptedException, ExecutionException, TimeoutException {
    return OperatingSystem.forkExec(command, timeoutMillis);
  }

  /**
   * @deprecated use OperatingSystem.killProcess.
   */
  @Deprecated
  public static int killProcess(final Process proc, final long terminateTimeoutMillis,
          final long forceTerminateTimeoutMillis)
          throws InterruptedException, TimeoutException {
    return OperatingSystem.killProcess(proc, terminateTimeoutMillis, forceTerminateTimeoutMillis);
  }

  /**
   * @deprecated use OperatingSystem.forkExec instead.
   */
  public static int run(final String[] command, final ProcOutputHandler handler,
          final long timeoutMillis)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    return run(command, handler, timeoutMillis, 60000);
  }

  /**
   * @deprecated use OperatingSystem.forkExec instead.
   */
  @SuppressFBWarnings("COMMAND_INJECTION")
  @Deprecated
  public static int run(final String[] command, final ProcOutputHandler handler,
          final long timeoutMillis, final long terminationTimeoutMillis)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    ProcessResponse<Void, Void> resp = OperatingSystem.forkExec(command,
            new ProcessHandler<Void, Void>() {
      @Override
      public Void handleStdOut(final InputStream is) throws IOException {
        int cos;
        byte[] buffer = ArraySuppliers.Bytes.TL_SUPPLIER.get(8192);
        try {
          while ((cos = is.read(buffer)) >= 0) {
            handler.handleStdOut(buffer, cos);
          }
        } finally {
          ArraySuppliers.Bytes.TL_SUPPLIER.recycle(buffer);
          handler.stdOutDone();
        }
        return null;
      }

      @Override
      public Void handleStdErr(final InputStream stderr) throws IOException {
        int cos;
        byte[] buffer = ArraySuppliers.Bytes.TL_SUPPLIER.get(8192);
        try {
          while ((cos = stderr.read(buffer)) >= 0) {
            handler.handleStdErr(buffer, cos);
          }
        } finally {
          ArraySuppliers.Bytes.TL_SUPPLIER.recycle(buffer);
          handler.stdErrDone();
        }
        return null;
      }
    }, timeoutMillis, terminationTimeoutMillis);
    return resp.getResponseCode();
  }

  /**
   * @deprecated use ShutdownRunnable directly
   */
  @Deprecated
  public static void queueHookAtBeginning(final Runnable runnable) {
      if (!ShutdownThread.get().queueHook(Integer.MIN_VALUE, runnable)) {
        throw new RejectedExecutionException("Rejected " + runnable);
      }
  }

  /**
   * @deprecated use ShutdownRunnable directly
   */
  @Deprecated
  public static void queueHookAtEnd(final Runnable runnable) {
    if (!ShutdownThread.get().queueHook(Integer.MAX_VALUE, runnable)) {
      throw new RejectedExecutionException("Rejected " + runnable);
    }
  }

  /**
   * @deprecated use ShutdownRunnable directly
   */
  @Deprecated
  public static void queueHook(final int priority, final Runnable runnable) {
    if (!ShutdownThread.get().queueHook(priority, runnable)) {
       throw new RejectedExecutionException("Rejected " + runnable);
    }
  }

  @Deprecated
  public static boolean removeQueuedShutdownHook(final Runnable runnable) {
    return ShutdownThread.get().removeQueuedShutdownHook(runnable);
  }

  /**
   * @return returns the deadline as millis since epoch.
   * @deprecated see ExecutionContexts.
   */
  @Deprecated
  public static long getDeadline() {
    return Timing.getCurrentTiming().fromNanoTimeToEpochMillis(ExecutionContexts.getContextDeadlineNanos());
  }

  /**
   * @return milliseconds until deadline.
   * @deprecated see ExecutionContexts.
   */
  @Deprecated
  public static long millisToDeadline() throws TimeoutException {
    return ExecutionContexts.getTimeToDeadline(TimeUnit.MILLISECONDS);
  }

  /**
   * Attempts to run the GC in a verifiable way.
   *
   * @param timeoutMillis - timeout for GC attempt
   * @return true if GC executed for sure, false otherwise, gc might have been executed though, but we cannot be sure.
   */
  @SuppressFBWarnings
  public static boolean gc(final long timeoutMillis) {
    WeakReference<Object> ref = new WeakReference<>(new Object());
    long deadline = TimeSource.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    do {
      System.gc();
    } while (ref.get() != null && TimeSource.nanoTime() < deadline);
    return ref.get() == null;
  }
  public static CharSequence jrun(final Class<?> classWithMain,
          final long timeoutMillis, final String... arguments)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
    return jrun(classWithMain, classPath, timeoutMillis, arguments);
  }

  public static CharSequence jrun(final Class<?> classWithMain, final String classPath, final long timeoutMillis,
          final String... arguments) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    String[] arr = getJvmArgsNoJMXNoDebug();
    return jrun(classWithMain, classPath, timeoutMillis, arr, arguments);
  }

  private static String[] getJvmArgsNoJMXNoDebug() {
    List<String> jvmInputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
    String[] arr;
    if (jvmInputArgs.isEmpty()) {
      arr = Arrays.EMPTY_STRING_ARRAY;
    } else {
      JVMArguments inputArguments = new JVMArguments(jvmInputArgs);
      inputArguments.removeAllSystemPropertiesStartingWith("com.sun.management.jmxremote");
      inputArguments.removeVMArgumentStartingWith("-agentlib:jdwp");
      arr = inputArguments.toArray();
    }
    return arr;
  }

  public static synchronized void setCurrentDir(final String sourceAbsolutePath) {
    if (haveJnaPlatformClib()) {
      CLibrary.INSTANCE.chdir(sourceAbsolutePath);
    }
    System.setProperty("user.dir", sourceAbsolutePath);
  }

  public static synchronized String getCurrentDir() {
    return System.getProperty("user.dir");
  }

  public static CharSequence jrun(final Class<?> classWithMain, final String classPath, final long timeoutMillis,
          final String[] jvmArgs,
          final String... arguments) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    final String jvmPath = JAVA_HOME + File.separatorChar + "bin" + File.separatorChar + "java";
    String[] command = Arrays.concat(new String[]{jvmPath},
            jvmArgs,
            new String[]{"-cp", classPath, classWithMain.getName()},
            arguments);
    return OperatingSystem.forkExec(command, timeoutMillis);
  }

 public static void jrunAndLog(final Class<?> classWithMain,
          final long timeoutMillis, final String... arguments)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
    jrunAndLog(classWithMain, classPath, timeoutMillis, arguments);
  }

  public static void jrunAndLog(final Class<?> classWithMain, final String classPath, final long timeoutMillis,
          final String... arguments) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    String[] arr = getJvmArgsNoJMXNoDebug();
    jrun(classWithMain, classPath, timeoutMillis, arr, arguments);
  }

  public static void jrunAndLog(final Class<?> classWithMain, final String classPath, final long timeoutMillis,
          final String[] jvmArgs,
          final String... arguments) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    final String jvmPath = JAVA_HOME + File.separatorChar + "bin" + File.separatorChar + "java";
    String[] command = Arrays.concat(new String[]{jvmPath},
            jvmArgs,
            new String[]{"-cp", classPath, classWithMain.getName()},
            arguments);
    OperatingSystem.forkExecLog(command, timeoutMillis);
  }

  /**
   * get the main Thread.
   * @return null if there is no main thread (can happen when calling this is a shutdown hook)
   */
  @Nullable
  public static Thread getMainThread() {
    Thread[] threads = Threads.getThreads();
    for (Thread t : threads) {
      if (t.getId() == 1L) {
        return t;
      }
    }
    return null;
  }

  /**
   * Method will figure out the main class and cache the result for successive invocations.
   * You should call this method prior to the main thread's termination.
   * @return null if main class cannot be found.
   */
  @Nullable
  public static Class<?> getMainClass() {
    return LazyMain.MAIN_CLASS;
  }

  private static class LazyMain {

    private static final Class<?> MAIN_CLASS = getMainClass();

    @Nullable
    private static Class<?> getMainClass() {
      Thread mainThread = getMainThread();
      if (mainThread == null) {
        return null;
      }
      StackTraceElement[] stackTrace = mainThread.getStackTrace();
      if (stackTrace.length == 0) {
        return null;
      }
      String className = stackTrace[stackTrace.length - 1].getClassName();
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException ex) {
        NoClassDefFoundError tex = new NoClassDefFoundError("Cannot find " + className);
        tex.initCause(ex);
        throw tex;
      }
    }
  }


  public static final class Jmx {

    @JmxExport
    public static org.spf4j.base.avro.PackageInfo getPackageInfo(@JmxExport("className") final String className) {
      return PackageInfo.getPackageInfo(className);
    }

    @JmxExport
    public static void restart() throws IOException {
      UnixRuntime.restart();
    }

  }

}

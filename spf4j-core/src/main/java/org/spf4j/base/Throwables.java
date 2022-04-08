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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gnu.trove.set.hash.THashSet;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.spf4j.base.avro.AThrowables;
import org.spf4j.base.avro.RemoteException;
import org.spf4j.ds.IdentityHashSet;

/**
 * utility class for throwables.
 *
 * @author zoly
 */
@ParametersAreNonnullByDefault
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
// Circular dependency with AThrowables, both static utility classes... should be fine...
public final class Throwables {

  /**
   * Caption for labeling suppressed exception stack traces
   */
  public static final String SUPPRESSED_CAPTION = "Suppressed: ";
  /**
   * Caption for labeling causative exception stack traces
   */
  public static final String CAUSE_CAPTION = "Caused by: ";


  private static final int MAX_SUPPRESS_CHAIN
          = Integer.getInteger("spf4j.throwables.defaultMaxSuppressChain", 100);

  private static final PackageDetail DEFAULT_PACKAGE_DETAIL
          = PackageDetail.valueOf(System.getProperty("spf4j.throwables.defaultStackTracePackageDetail", "SHORT"));


  private static final boolean DEFAULT_TRACE_ELEMENT_ABBREVIATION
          = Boolean.parseBoolean(System.getProperty("spf4j.throwables.defaultStackTraceAbbreviation", "true"));


  private static volatile Predicate<Throwable> nonRecoverableClassificationPredicate = new Predicate<Throwable>() {
    @Override
    @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
    public boolean test(final Throwable t) {

      if (t instanceof Error && !(t instanceof StackOverflowError)
              && !(t instanceof AssertionError)
              && !(t.getClass().getName().endsWith("TokenMgrError"))) {
        return true;
      }
      if (t instanceof IOException) {
        String message = t.getMessage();
        if (message != null && message.contains("Too many open files")) {
          return true;
        }
      }
      return false;
    }
  };


  private static volatile Function<Throwable, Boolean> isRetryablePredicate =
          new Function<Throwable, Boolean>() {
    /**
     * A default predicate that will answer if a exception is retry-able or not.
     * @param t
     * @return true if a exception is retry-able, false if it is not, null if this is not known.
     */
    @Override
    @SuppressFBWarnings({"ITC_INHERITANCE_TYPE_CHECKING", "NP_BOOLEAN_RETURN_NULL" })
    @Nullable
    public Boolean apply(final Throwable t) {
      // non recoverables are not retryable.
      if (Throwables.containsNonRecoverable(t)) {
        return Boolean.FALSE;
      }
      // check causal chaing
      Throwable e = Throwables.firstCause(t,
              ex -> {
                String exClassName = ex.getClass().getName();
                return (ex instanceof SQLTransientException
              || ex instanceof SQLRecoverableException
              || (ex instanceof IOException && !exClassName.contains("Json"))
              || ex instanceof TimeoutException
              || ex instanceof UncheckedTimeoutException
              || (exClassName.contains("Transient")
                        && !exClassName.contains("NonTransient")));
                        });
      return e != null ? Boolean.TRUE : null;
    }
  };


  private Throwables() {
  }

  /**
   * figure out if a Exception is retry-able or not.
   * If while executing a operation a exception is returned, that exception is retryable if retrying the operation
   * can potentially succeed.
   * @param value
   * @return true/false is retry-able or not, null when this is not clear and can be context dependent.
   */
  @CheckReturnValue
  public static boolean isRetryable(final Throwable value) {
    Boolean result = isRetryablePredicate.apply(value);
    return result == null ? false : result;
  }

  @Nonnull
  @CheckReturnValue
  public static Function<Throwable, Boolean> getIsRetryablePredicate() {
    return isRetryablePredicate;
  }

  public static void setIsRetryablePredicate(final Function<Throwable, Boolean> isRetryablePredicate) {
    Throwables.isRetryablePredicate = isRetryablePredicate;
  }


  public static int getNrSuppressedExceptions(final Throwable t) {
    return UnsafeThrowable.getSuppressedNoCopy(t).size();
  }

  public static int getNrRecursiveSuppressedExceptions(final Throwable t) {
    final List<Throwable> suppressedExceptions = UnsafeThrowable.getSuppressedNoCopy(t);
    int count = 0;
    for (Throwable se : suppressedExceptions) {
      count += 1 + getNrRecursiveSuppressedExceptions(se);
    }
    return count;
  }

  @Nullable
  public static Throwable removeOldestSuppressedRecursive(final Throwable t) {
      final List<Throwable> suppressedExceptions = UnsafeThrowable.getSuppressedNoCopy(t);
      if (suppressedExceptions.isEmpty()) {
        return null;
      } else {
        Throwable ex = suppressedExceptions.get(0);
        if (getNrSuppressedExceptions(ex) > 0) {
          return removeOldestSuppressedRecursive(ex);
        } else {
          return suppressedExceptions.remove(0);
        }
      }
  }

  @Nullable
  public static Throwable removeOldestSuppressed(final Throwable t) {
    final List<Throwable> suppressedExceptions = UnsafeThrowable.getSuppressedNoCopy(t);
    if (suppressedExceptions.isEmpty()) {
      return null;
    } else {
      return suppressedExceptions.remove(0);
    }
  }


  public static final class TrimmedException extends Exception {

    public TrimmedException(final String message) {
      super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
      return this;
    }
  }


  /**
   * Functionality will call Throwable.addSuppressed, 2 extra things happen:
   *
   * 1) limit to nr of exceptions suppressed.
   * 2) if exception is already suppressed, will not add it.
   * 3) will return a clone of exception t.
   *
   * @param <T>
   * @param t
   * @param suppressed
   * @returna clone of exception t with suppressed exception suppressed;
   * @deprecated use suppressLimited instead.
   *
   */
  @CheckReturnValue
  @Deprecated
  public static <T extends Throwable> T suppress(@Nonnull final T t, @Nonnull final Throwable suppressed) {
    return suppress(t, suppressed, MAX_SUPPRESS_CHAIN);
  }

  /**
   * Functionality will call Throwable.addSuppressed, 2 extra things happen:
   *
   * 1) limit to nr of exceptions suppressed.
   * 2) if exception is already suppressed, will not add it.
   *
   * @param t
   * @param suppressed
   */
  public static void suppressLimited(@Nonnull final Throwable t, @Nonnull final Throwable suppressed) {
    suppressLimited(t, suppressed, MAX_SUPPRESS_CHAIN);
  }

  @SuppressFBWarnings("NOS_NON_OWNED_SYNCHRONIZATION")
  public static void suppressLimited(@Nonnull final Throwable t, @Nonnull final Throwable suppressed,
          final int maxSuppressed) {
    if (contains(t, suppressed)) { //protect against circular references.
      Logger.getLogger(Throwables.class.getName()).log(Level.INFO,
              "Circular suppression attempted", new RuntimeException(suppressed));
      return;
    }
    synchronized (t) {
      t.addSuppressed(suppressed);
      while (getNrRecursiveSuppressedExceptions(t) > maxSuppressed) {
        if (removeOldestSuppressedRecursive(t) == null) {
          throw new IllegalArgumentException("Impossible state for " + t);
        }
      }
    }
  }


  @CheckReturnValue
  public static <T extends Throwable> T suppress(@Nonnull final T t, @Nonnull final Throwable suppressed,
          final int maxSuppressed) {
    T clone;
    try {
      clone = Objects.clone(t);
    } catch (RuntimeException ex) {
      t.addSuppressed(ex);
      clone = t;
    }
    if (contains(t, suppressed)) {
      return clone;
    }
    clone.addSuppressed(suppressed);
    while (getNrRecursiveSuppressedExceptions(clone) > maxSuppressed) {
      if (removeOldestSuppressedRecursive(clone) == null) {
        throw new IllegalArgumentException("Impossible state for " + clone);
      }
    }
    return clone;
  }

  /**
   * Utility to get suppressed exceptions.
   *
   * In java 1.7 it will return t.getSuppressed()
   * + in case it is Iterable<Throwable> any other linked exceptions (see
   * SQLException)
   *
   * java 1.6 behavior is deprecated.
   *
   * @param t
   * @return
   */
  public static Throwable[] getSuppressed(final Throwable t) {
    if (t instanceof Iterable) {
      // see SQLException
      Throwable[] osuppressed = t.getSuppressed();
      List<Throwable> suppressed = new ArrayList<>(osuppressed.length);
      Set<Throwable> ignore = new IdentityHashSet<>();
      ignore.addAll(java.util.Arrays.asList(osuppressed));
      suppressed.addAll(ignore);
      ignore.addAll(com.google.common.base.Throwables.getCausalChain(t));
      Iterator it = ((Iterable) t).iterator();
      while (it.hasNext()) {
        Object next = it.next();
        if (next instanceof Throwable) {
          if (ignore.contains(next)) {
            continue;
          }
          suppressed.add((Throwable) next);
          ignore.addAll(com.google.common.base.Throwables.getCausalChain((Throwable) next));
        } else {
          break;
        }
      }
      return suppressed.toArray(new Throwable[suppressed.size()]);
    } else {
      return t.getSuppressed();
    }

  }

  public static void writeTo(final StackTraceElement element, @Nullable final StackTraceElement previous,
          final Appendable to, final PackageDetail detail,
          final boolean abbreviatedTraceElement)
          throws IOException {
    String currClassName = element.getClassName();
    String prevClassName = previous == null ? null : previous.getClassName();
    if (abbreviatedTraceElement) {
      if (currClassName.equals(prevClassName)) {
        to.append('^');
      } else {
        writeAbreviatedClassName(currClassName, to);
      }
    } else {
      to.append(currClassName);
    }
    to.append('.');
    to.append(element.getMethodName());
    String currFileName = element.getFileName();
    String fileName = currFileName;
    if (abbreviatedTraceElement && java.util.Objects.equals(currFileName,
            previous == null ? null : previous.getFileName())) {
      fileName = "^";
    }
    final int lineNumber = element.getLineNumber();
    if (element.isNativeMethod()) {
      to.append("(Native Method)");
    } else if (fileName == null) {
      to.append("(Unknown Source)");
    } else if (lineNumber >= 0) {
      to.append('(').append(fileName).append(':')
              .append(Integer.toString(lineNumber)).append(')');
    } else {
      to.append('(').append(fileName).append(')');
    }
    if (detail == PackageDetail.NONE) {
      return;
    }
    if (abbreviatedTraceElement && currClassName.equals(prevClassName)) {
      to.append("[^]");
      return;
    }
    org.spf4j.base.avro.PackageInfo pInfo = PackageInfo.getPackageInfo(currClassName);
    if (abbreviatedTraceElement && prevClassName != null && pInfo.equals(PackageInfo.getPackageInfo(prevClassName))) {
      to.append("[^]");
      return;
    }
    if (!pInfo.getUrl().isEmpty() || !pInfo.getVersion().isEmpty()) {
      String jarSourceUrl = pInfo.getUrl();
      String version = pInfo.getVersion();
      to.append('[');
      if (!jarSourceUrl.isEmpty()) {
        if (detail == PackageDetail.SHORT) {
          String url = jarSourceUrl;
          int lastIndexOf = url.lastIndexOf('/');
          if (lastIndexOf >= 0) {
            int lpos = url.length() - 1;
            if (lastIndexOf == lpos) {
              int prevSlPos = url.lastIndexOf('/', lpos - 1);
              if (prevSlPos < 0) {
                to.append(url);
              } else {
                to.append(url, prevSlPos + 1, url.length());
              }
            } else {
              to.append(url, lastIndexOf + 1, url.length());
            }
          } else {
            to.append(url);
          }
        } else {
          to.append(jarSourceUrl);
        }
      } else {
        to.append("na");
      }
      if (!version.isEmpty()) {
        to.append(':');
        to.append(version);
      }
      to.append(']');
    }
  }

  /**
   * enum describing the PackageDetail level to be logged in the stack trace.
   */
  public enum PackageDetail {
    /**
     * No jar info or version info.
     */
    NONE,
    /**
     * jar file name + manifest version.
     */
    SHORT,
    /**
     * complete jar path + manifest version.
     */
    LONG

  }

  public static String toString(final Throwable t) {
    return toString(t, DEFAULT_PACKAGE_DETAIL);
  }

  public static String toString(final Throwable t, final PackageDetail detail) {
    return toString(t, detail, DEFAULT_TRACE_ELEMENT_ABBREVIATION);
  }

  public static String toString(final Throwable t, final PackageDetail detail, final boolean abbreviatedTraceElement) {
    StringBuilder sb = toStringBuilder(t, detail, abbreviatedTraceElement);
    return sb.toString();
  }

  public static StringBuilder toStringBuilder(final Throwable t, final PackageDetail detail) {
    return toStringBuilder(t, detail, DEFAULT_TRACE_ELEMENT_ABBREVIATION);
  }

  public static StringBuilder toStringBuilder(final Throwable t, final PackageDetail detail,
          final boolean abbreviatedTraceElement) {
    StringBuilder sb = new StringBuilder(1024);
    try {
      writeTo(t, sb, detail, abbreviatedTraceElement);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return sb;
  }

  public static void writeTo(@Nonnull final Throwable t, @Nonnull final PrintStream to,
          @Nonnull final PackageDetail detail) {
    writeTo(t, to, detail, DEFAULT_TRACE_ELEMENT_ABBREVIATION);
  }

  @SuppressFBWarnings({"OCP_OVERLY_CONCRETE_PARAMETER"}) // on purpose :-)
  public static void writeTo(@Nonnull final Throwable t, @Nonnull final PrintStream to,
          @Nonnull final PackageDetail detail, final boolean abbreviatedTraceElement) {
    StringBuilder sb = new StringBuilder(1024);
    try {
      writeTo(t, sb, detail, abbreviatedTraceElement);
      to.append(sb);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void writeTo(final Throwable t, final Appendable to, final PackageDetail detail) throws IOException {
    writeTo(t, to, detail, DEFAULT_TRACE_ELEMENT_ABBREVIATION);
  }

  public static void writeTo(final Throwable t, final Appendable to, final PackageDetail detail,
          final String prefix) throws IOException {
    writeTo(t, to, detail, DEFAULT_TRACE_ELEMENT_ABBREVIATION, prefix);
  }

  public static void writeTo(final Throwable t, final Appendable to, final PackageDetail detail,
          final boolean abbreviatedTraceElement) throws IOException {
    writeTo(t, to, detail, abbreviatedTraceElement, "");
  }

  public static void writeTo(final Throwable t, final Appendable to, final PackageDetail detail,
          final boolean abbreviatedTraceElement, final String prefix) throws IOException {
    if (t instanceof RemoteException) {
      AThrowables.writeTo((RemoteException) t, to, detail, abbreviatedTraceElement, prefix);
      return;
    }
    to.append(prefix);
    writeMessageString(to, t);
    to.append('\n');
    writeThrowableDetail(t, to, detail, abbreviatedTraceElement, prefix);
  }

  public static void writeThrowableDetail(final Throwable t, final Appendable to, final PackageDetail detail,
          final boolean abbreviatedTraceElement, final String prefix) throws IOException {
    StackTraceElement[] trace = t.getStackTrace();
    writeTo(trace, to, detail, abbreviatedTraceElement, prefix);
    Throwable[] suppressed = getSuppressed(t);
    Throwable ourCause = t.getCause();
    if (ourCause == null && suppressed.length == 0) {
      return;
    }
    Set<Throwable> dejaVu = new IdentityHashSet<Throwable>();
    dejaVu.add(t);
    // Print suppressed exceptions, if any
    for (Throwable se : suppressed) {
      printEnclosedStackTrace(se, to, trace, SUPPRESSED_CAPTION, prefix + "\t",
              dejaVu, detail, abbreviatedTraceElement);
    }
    // Print cause, if any
    if (ourCause != null) {
      printEnclosedStackTrace(ourCause, to, trace, CAUSE_CAPTION, prefix, dejaVu, detail, abbreviatedTraceElement);
    }
  }

  public static void writeMessageString(final Appendable to, final Throwable t) throws IOException {
    to.append(t.getClass().getName());
    String message = t.getMessage();
    if (message != null) {
      to.append(':').append(message);
    }
  }

  public static void writeTo(final StackTraceElement[] trace, final Appendable to, final PackageDetail detail,
          final boolean abbreviatedTraceElement)
          throws IOException {
    writeTo(trace, to, detail, abbreviatedTraceElement, "");
  }

  public static void writeTo(final StackTraceElement[] trace, final Appendable to, final PackageDetail detail,
          final boolean abbreviatedTraceElement, final String prefix)
          throws IOException {
    StackTraceElement prevElem = null;
    for (StackTraceElement traceElement : trace) {
      to.append(prefix);
      to.append("\tat ");
      writeTo(traceElement, prevElem, to, detail, abbreviatedTraceElement);
      to.append('\n');
      prevElem = traceElement;
    }
  }

  public static int commonFrames(final StackTraceElement[] trace, final StackTraceElement[] enclosingTrace) {
    int from = trace.length - 1;
    int m = from;
    int n = enclosingTrace.length - 1;
    while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
      m--;
      n--;
    }
    return from - m;
  }

  private static void printEnclosedStackTrace(final Throwable t, final Appendable s,
          final StackTraceElement[] enclosingTrace,
          final String caption,
          final String prefix,
          final Set<Throwable> dejaVu,
          final PackageDetail detail,
          final boolean abbreviatedTraceElement) throws IOException {
    if (dejaVu.contains(t)) {
      s.append("\t[CIRCULAR REFERENCE:");
      writeMessageString(s, t);
      s.append("]\n");
    } else {
      dejaVu.add(t);
      // Compute number of frames in common between this and enclosing trace
      StackTraceElement[] trace = t.getStackTrace();
      int framesInCommon = commonFrames(trace, enclosingTrace);
      int m = trace.length - framesInCommon;
      // Print our stack trace
      s.append(prefix).append(caption);
      writeMessageString(s, t);
      s.append('\n');
      StackTraceElement prev = null;
      for (int i = 0; i < m; i++) {
        s.append(prefix).append("\tat ");
        StackTraceElement ste = trace[i];
        writeTo(ste, prev, s, detail, abbreviatedTraceElement);
        s.append('\n');
        prev = ste;
      }
      if (framesInCommon != 0) {
        s.append(prefix).append("\t... ").append(Integer.toString(framesInCommon)).append(" more");
        s.append('\n');
      }

      // Print suppressed exceptions, if any
      for (Throwable se : getSuppressed(t)) {
        printEnclosedStackTrace(se, s, trace, SUPPRESSED_CAPTION, prefix + '\t',
                dejaVu, detail, abbreviatedTraceElement);
      }

      // Print cause, if any
      Throwable ourCause = t.getCause();
      if (ourCause != null) {
        printEnclosedStackTrace(ourCause, s, trace, CAUSE_CAPTION, prefix,
                dejaVu, detail, abbreviatedTraceElement);
      }
    }
  }

  /**
   * Is this Throwable a JVM non-recoverable exception. (Oom, VMError, etc...)
   * @param t
   * @return
   */
  public static boolean isNonRecoverable(@Nonnull final Throwable t) {
    return nonRecoverableClassificationPredicate.test(t);
  }

  /**
   * Does this Throwable contain a JVM non-recoverable exception. (Oom, VMError, etc...)
   * @param t
   * @return
   */
  public static boolean containsNonRecoverable(@Nonnull final Throwable t) {
    return contains(t, nonRecoverableClassificationPredicate);
  }

  /**
   * checks in the throwable + children (both causal and suppressed) contain a throwable that
   * respects the Predicate.
   * @param t the throwable
   * @param predicate the predicate
   * @return true if a Throwable matching the predicate is found.
   */
  public static boolean contains(@Nonnull final Throwable t, final Predicate<Throwable> predicate) {
    return first(t, predicate) != null;
  }


  /**
   * checks in the throwable + children (both causal and suppressed) contain a throwable that
   * respects the Predicate.
   * @param t the throwable
   * @param predicate the predicate
   * @return true if a Throwable matching the predicate is found.
   */
  public static boolean contains(@Nonnull final Throwable t, @Nonnull final Throwable toLookFor) {
    return first(t, (x) -> x == toLookFor) != null;
  }

  /**
   * return first Exception in the causal chain Assignable to clasz.
   * @param <T>
   * @param t
   * @param clasz
   * @return
   */
  @Nullable
  @CheckReturnValue
  public static <T extends Throwable> T first(@Nonnull final Throwable t, final Class<T> clasz) {
    return (T) first(t, (Throwable th) -> clasz.isAssignableFrom(th.getClass()));
  }

  /**
   * Returns the first Throwable that matches the predicate in the causal and suppressed chain,
   * the suppressed chain includes the supression mechanism included in SQLException.
   * @param t the Throwable
   * @param predicate the Predicate
   * @return the Throwable the first matches the predicate or null is none matches.
   */
  @Nullable
  @CheckReturnValue
  public static Throwable first(@Nonnull final Throwable t, final Predicate<Throwable> predicate) {
    if (predicate.test(t)) { //shortcut
      return t;
    }
    ArrayDeque<Throwable> toScan =  new ArrayDeque<>();
    Throwable cause = t.getCause();
    if (cause != null) {
      toScan.addFirst(cause);
    }
    for (Throwable supp : getSuppressed(t)) {
      toScan.addLast(supp);
    }
    Throwable th;
    THashSet<Throwable> seen = new IdentityHashSet<>();
    while ((th = toScan.pollFirst()) != null) {
      if (seen.contains(th)) {
        continue;
      }
      if (predicate.test(th)) {
        return th;
      } else {
        cause = th.getCause();
        if (cause != null) {
          toScan.addFirst(cause);
        }
        for (Throwable supp : getSuppressed(th)) {
          toScan.addLast(supp);
        }
      }
      seen.add(th);
    }
    return null;
  }


  /**
   * Returns first Throwable in the causality chain that is matching the provided predicate.
   * @param throwable the Throwable to go through.
   * @param predicate the predicate to apply
   * @return the first Throwable from the chain that the predicate matches.
   */

  @Nullable
  @CheckReturnValue
  public static Throwable firstCause(@Nonnull final Throwable throwable, final Predicate<Throwable> predicate) {
    Throwable t = throwable;
    do {
      if (predicate.test(t)) {
        return t;
      }
      t = t.getCause();
    } while (t != null);
    return null;
  }


  public static Predicate<Throwable> getNonRecoverablePredicate() {
    return nonRecoverableClassificationPredicate;
  }

  /**
   * Overwrite the default non-recoverable predicate.
   * @param predicate
   */
  public static void setNonRecoverablePredicate(final Predicate<Throwable> predicate) {
    Throwables.nonRecoverableClassificationPredicate = predicate;
  }

  public static void writeAbreviatedClassName(final String className, final Appendable writeTo) throws IOException {
    int ldIdx = className.lastIndexOf('.');
    if (ldIdx < 0) {
      writeTo.append(className);
      return;
    }
    boolean isPreviousDot = true;
    for (int i = 0; i < ldIdx; i++) {
      char c = className.charAt(i);
      boolean isCurrentCharDot = c == '.';
      if (isPreviousDot || isCurrentCharDot) {
        writeTo.append(c);
      }
      isPreviousDot = isCurrentCharDot;
    }
    writeTo.append(className, ldIdx, className.length());
  }

  @SuppressFBWarnings("ITC_INHERITANCE_TYPE_CHECKING")
  public static void throwException(final Exception ex) throws IOException, InterruptedException,
          ExecutionException, TimeoutException {
    if (ex instanceof IOException) {
      throw (IOException) ex;
    } else if (ex instanceof InterruptedException) {
      throw (InterruptedException) ex;
    } else if (ex instanceof ExecutionException) {
      throw (ExecutionException) ex;
    } else if (ex instanceof TimeoutException) {
      throw (TimeoutException) ex;
    } else {
      throw new ExecutionException(ex);
    }
  }


}

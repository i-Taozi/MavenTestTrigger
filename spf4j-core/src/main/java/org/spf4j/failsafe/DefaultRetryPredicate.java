/*
 * Copyright 2017 SPF4J.
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
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.log.Level;
import org.spf4j.log.SLf4jXLogAdapter;
import org.spf4j.log.XLog;

/**
 * @author Zoltan Farkas
 */
@SuppressFBWarnings("AI_ANNOTATION_ISSUES_NEEDS_NULLABLE") // false positive...
@ThreadSafe
final class DefaultRetryPredicate<T> implements RetryPredicate<T, Callable<T>> {

  private static final Level RETRY_LOG_LEVEL =
          Level.valueOf(System.getProperty("spf4j.failsafe.retryLogLevel", "WARN"));

  private static final PartialResultRetryPredicate[] NO_RP = new PartialResultRetryPredicate[0];

  private static final PartialExceptionRetryPredicate[] NO_EP = new PartialExceptionRetryPredicate[0];

  private static final Logger LOG =  LoggerFactory.getLogger(DefaultRetryPredicate.class);

  private final Function<Object, RetryDelaySupplier> defaultBackoffSupplier;

  private final PartialResultRetryPredicate<T, Callable<T>>[] resultPredicates;

  private final PartialExceptionRetryPredicate<T, Callable<T>>[] exceptionPredicates;

  private final XLog log;

  @SuppressFBWarnings("LO_SUSPECT_LOG_PARAMETER") // not suspect if you want to customize logging behavior.
  DefaultRetryPredicate(@Nullable final Logger log, final long startNanos, final long deadlineNanos,
          final Supplier<Function<Object, RetryDelaySupplier>> defaultBackoffSupplierSupplier,
          final TimedSupplier<PartialResultRetryPredicate<T, Callable<T>>>[] resultPredicates,
          final TimedSupplier<PartialExceptionRetryPredicate<T, Callable<T>>>... exceptionPredicates) {
    this.log = new SLf4jXLogAdapter(log == null ? LOG : log);
    this.defaultBackoffSupplier = defaultBackoffSupplierSupplier.get();
    int rpl = resultPredicates.length;
    if (rpl > 0) {
      this.resultPredicates = new PartialResultRetryPredicate[rpl];
      for (int i = 0; i < rpl; i++) {
        this.resultPredicates[i] = resultPredicates[i].get(startNanos, deadlineNanos);
      }
    } else {
      this.resultPredicates = NO_RP;
    }
    int epl = exceptionPredicates.length;
    if (epl > 0) {
      this.exceptionPredicates = new PartialExceptionRetryPredicate[epl];
      for (int i = 0; i < epl; i++) {
        this.exceptionPredicates[i] = exceptionPredicates[i].get(startNanos, deadlineNanos);
      }
    } else {
      this.exceptionPredicates = NO_EP;
    }
  }

  @Override
  @Nonnull
  public RetryDecision<T, Callable<T>> getDecision(final T value, final Callable<T> what) {

    for (PartialResultRetryPredicate<T, Callable<T>> predicate : resultPredicates) {
      RetryDecision<T, Callable<T>> decision = predicate.getDecision(value, what);
      if (decision != null) {
        if (decision.getDecisionType() == RetryDecision.Type.Retry) {
          Callable<?> newCallable = decision.getNewCallable();
          if (decision.getDelayNanos() < 0) {
            RetryDelaySupplier backoff = defaultBackoffSupplier.apply(value);
            decision = (RetryDecision) RetryDecision.retry(backoff.nextDelay(), newCallable);
          }
          log.log(null, RETRY_LOG_LEVEL, "Result {}, retrying {} with {}", value,  newCallable, decision);
        }
        return decision;
      }
    }
    return RetryDecision.abortReturn(value);
  }

  @Override
  @Nonnull
  public RetryDecision<T, Callable<T>> getExceptionDecision(final Throwable value, final Callable<T> what) {
    for (PartialExceptionRetryPredicate<T, Callable<T>> predicate : exceptionPredicates) {
      RetryDecision<T, Callable<T>> decision = predicate.getExceptionDecision(value, what);
      if (decision != null) {
        if (decision.getDecisionType() == RetryDecision.Type.Retry) {
          Callable<?> newCallable = decision.getNewCallable();
          if (decision.getDelayNanos() < 0) {
            RetryDelaySupplier backoff = defaultBackoffSupplier.apply(value);
            decision = (RetryDecision) RetryDecision.retry(backoff.nextDelay(), newCallable);
          }
          log.log(null, RETRY_LOG_LEVEL,
                  "Result {}, retrying {} with {}", value.getClass().getName(), newCallable, decision, value);
        }
        return decision;
      }
    }
    return RetryDecision.abortThrow(value);

  }

  @Override
  public String toString() {
    return "DefaultRetryPredicate{" + "defaultBackoffSupplier=" + defaultBackoffSupplier
            + ", resultPredicates=" + Arrays.toString(resultPredicates)
            + ", exceptionPredicates=" + Arrays.toString(exceptionPredicates) + '}';
  }

}

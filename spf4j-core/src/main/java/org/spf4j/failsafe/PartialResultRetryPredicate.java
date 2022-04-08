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
package org.spf4j.failsafe;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Chainable Result predicate.
 * This predicate will not return a RetryDecision (null) when if want to deffer the retry decision.
 * @author Zoltan Farkas
 */
@FunctionalInterface
public interface PartialResultRetryPredicate<T, C extends Callable<? extends T>>
        extends  BiFunction<T, C, RetryDecision<T, C>>  {

  @Nullable
  RetryDecision<T, C> getDecision(@Nonnull T value, @Nonnull C what);

  @Override
  @Nullable
  default RetryDecision<T, C> apply(final T t, final C u) {
    return getDecision(t, u);
  }

}

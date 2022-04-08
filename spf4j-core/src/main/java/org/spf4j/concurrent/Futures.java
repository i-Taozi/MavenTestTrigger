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
package org.spf4j.concurrent;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.spf4j.base.Pair;
import org.spf4j.base.Throwables;
import org.spf4j.base.TimeSource;

/**
 *
 * @author zoly
 */
public final class Futures {

  private Futures() { }

  @Nullable
  @CheckReturnValue
  public static RuntimeException cancelAll(final boolean mayInterrupt, final Future<?>... futures) {
    return cancelAll(mayInterrupt, futures, 0);
  }

  @Nullable
  @CheckReturnValue
  public static RuntimeException cancelAll(final boolean mayInterrupt, final Future[] futures, final int from) {
    RuntimeException ex = null;
    for (int i = from; i < futures.length; i++) {
      Future future = futures[i];
      try {
        future.cancel(mayInterrupt);
      } catch (RuntimeException e) {
        if (ex == null) {
          ex = e;
        } else {
          Throwables.suppressLimited(ex, e);
        }
      }
    }
    return ex;
  }

  @Nullable
  public static RuntimeException cancelAll(final boolean mayInterrupt, final Iterator<Future<?>> iterator) {
    RuntimeException ex = null;
    while (iterator.hasNext()) {
      Future future = iterator.next();
      try {
        future.cancel(mayInterrupt);
      } catch (RuntimeException e) {
        if (ex == null) {
          ex = e;
        } else {
          Throwables.suppressLimited(ex, e);
        }
      }
    }
    return ex;
  }

  @CheckReturnValue
  @Nonnull
  public static Pair<Map<Future, Object>, Exception> getAll(final long timeoutMillis, final Future... futures)  {
    long deadlineNanos = TimeSource.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    return getAllWithDeadlineNanos(deadlineNanos, futures);
  }



  @CheckReturnValue
  @Nonnull
  public static  Pair<Map<Future, Object>, Exception> getAllWithDeadlineNanos(final long deadlineNanos,
          final Future... futures) {
    Map<Future, Object> res = Maps.newHashMapWithExpectedSize(futures.length);
    Exception ex = getAllWithDeadlineNanos(deadlineNanos, res::put, futures);
    return Pair.of(res, ex);
  }

  /**
   *
   * @param deadlineNanos
   * @param futures
   * @return
   */
  @CheckReturnValue
  @Nullable
  public static <T> Exception getAllWithDeadlineNanos(final long deadlineNanos,
          final BiConsumer<Future<T>, T> consumer,
          final Future<T>... futures) {
    Exception exception = null;
    for (int i = 0; i < futures.length; i++) {
      Future<T> future = futures[i];
      try {
        final long toNanos = deadlineNanos - TimeSource.nanoTime();
        T get = future.get(Math.max(0, toNanos), TimeUnit.NANOSECONDS);
        consumer.accept(future, get);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        if (exception == null) {
          exception = ex;
        } else {
          Throwables.suppressLimited(ex, exception);
          exception = ex;
        }
        RuntimeException cex = cancelAll(true, futures, i + 1);
        if (cex != null) {
          Throwables.suppressLimited(exception, cex);
        }
      }  catch (TimeoutException  ex) {
        try {
          future.cancel(true);
        } catch (RuntimeException ex2) {
          ex.addSuppressed(ex2);
        }
        if (exception == null) {
          exception = ex;
        } else {
          Throwables.suppressLimited(exception, ex);
        }
      } catch (ExecutionException | RuntimeException ex) {
        if (exception == null) {
          exception = ex;
        } else {
          Throwables.suppressLimited(exception, ex);
        }
      }
    }
    return exception;
  }

 /**
   * Gets all futures resuls for futures that return Void (no return).
   *
   * @param deadlineNanos
   * @param futures
   * @return
   */
  @CheckReturnValue
  @Nullable
  @SuppressWarnings("unchecked")
  public static Exception getAllWithDeadlineNanosRetVoid(final long deadlineNanos,
          final Future... futures) {
    return getAllWithDeadlineNanos(deadlineNanos, (a, b) -> { }, futures);
  }

  @CheckReturnValue
  @Nonnull
  public static Pair<Map<Future, Object>, Exception> getAll(final long timeoutMillis, final Iterable<Future> futures)  {
    long deadlineNanos = TimeSource.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    return getAllWithDeadlineNanos(deadlineNanos, futures);
  }


  @CheckReturnValue
  @Nonnull
  public static Pair<Map<Future, Object>, Exception> getAllWithDeadlineNanos(final long deadlineNanos,
          final Iterable<Future> futures) {
    Map<Future, Object> results;
    if (futures instanceof Collection) {
      results = Maps.newHashMapWithExpectedSize(((Collection) futures).size());
    } else {
      results = new HashMap<>();
    }
    @SuppressWarnings("unchecked")
    Exception ex = getAllWithDeadlineNanos(deadlineNanos, results::put, (Iterable) futures);
    return Pair.of(results, ex);
  }

  @CheckReturnValue
  @Nullable
  public static <T> Exception getAllWithDeadlineNanos(final long deadlineNanos,
          final BiConsumer<Future<T>, T> consumer,
          final Iterable<Future<T>> futures) {
    Exception exception = null;
    Iterator<Future<T>> iterator = futures.iterator();
    while (iterator.hasNext()) {
      Future<T> future = iterator.next();
      try {
        final long toNanos = deadlineNanos - TimeSource.nanoTime();
        T get = future.get(Math.max(0, toNanos), TimeUnit.NANOSECONDS);
        consumer.accept(future, get);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        if (exception == null) {
          exception = ex;
        } else {
          Throwables.suppressLimited(ex, exception);
          exception = ex;
        }
        RuntimeException cex = cancelAll(true, (Iterator) iterator);
        if (cex != null) {
          Throwables.suppressLimited(exception, cex);
        }
        break;
      } catch (TimeoutException ex) {
        try {
          future.cancel(true);
        } catch (RuntimeException ex2) {
          ex.addSuppressed(ex2);
        }
        if (exception == null) {
          exception = ex;
        } else {
          Throwables.suppressLimited(exception, ex);
        }
      } catch (ExecutionException | RuntimeException ex) {
        if (exception == null) {
          exception = ex;
        } else {
          Throwables.suppressLimited(exception, ex);
        }
      }
    }
    return exception;
  }

  public static <T> List<Future<T>> timedOutFutures(final int copies, final TimeoutException ex) {
    Future<T> fut = new Future<T>() {
      @Override
      public boolean cancel(final boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isDone() {
        return true;
      }

      @Override
      public T get() throws ExecutionException {
        throw new ExecutionException(ex);
      }

      @Override
      public T get(final long timeout, final TimeUnit unit) throws TimeoutException {
        throw ex;
      }
    };
    return Collections.nCopies(copies, fut);
  }


}

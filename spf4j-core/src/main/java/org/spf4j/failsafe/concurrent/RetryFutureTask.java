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
package org.spf4j.failsafe.concurrent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import org.spf4j.base.Either;
import org.spf4j.failsafe.RetriedResultException;
import org.spf4j.failsafe.RetryDecision;
import org.spf4j.failsafe.RetryPredicate;

/**
 *
 * @author Zoltan Farkas
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
class RetryFutureTask<T> extends FutureTask<T> {

  private static final int MAX_EX_CHAIN_DEFAULT = Integer.getInteger("spf4j.failsafe.defaultMaxExceptionChain", 5);

  private final RetryPredicate<T, Callable<? extends T>> retryPredicate;

  private final DelayQueue<DelayedTask<RetryFutureTask<?>>> delayedTasks;

  private Deque<Either<T, Throwable>> previousResults;

  private final Runnable onRetry;

  private volatile DelayedTask<RetryFutureTask<?>> exec;

  RetryFutureTask(final Callable<T> callable, final RetryPredicate<T, Callable<? extends T>> retryPredicate,
          final DelayQueue<DelayedTask<RetryFutureTask<?>>> delayedTasks, final Runnable onRetry) {
    super(callable);
    this.onRetry = onRetry;
    this.retryPredicate = retryPredicate;
    this.delayedTasks = delayedTasks;
    this.previousResults = null;
  }

  public final void setExec(final DelayedTask<RetryFutureTask<?>> exec) {
    this.exec = exec;
  }


  @Override
  public final boolean cancel(final boolean mayInterruptIfRunning) {
    DelayedTask<RetryFutureTask<?>> e = exec;
    if (e != null) {
      delayedTasks.remove(e);
    }
    return super.cancel(mayInterruptIfRunning);
  }


  @SuppressWarnings("unchecked")
  @SuppressFBWarnings("NOS_NON_OWNED_SYNCHRONIZATION")
  protected T report(final int s) throws ExecutionException {
    try {
      return super.report(s);
    } catch (CancellationException ex) {
      synchronized (this) {
        attachPrevious(ex);
        throw ex;
      }
    }
  }

  private void attachPrevious(final Throwable ex) {
    if (previousResults != null) {
      for (Either<T, Throwable> previousResult : previousResults) {
        if (previousResult.isRight()) {
          Throwable e = previousResult.getRight();
          if (e != ex) {
            ex.addSuppressed(e);
          }
        } else {
          ex.addSuppressed(new RetriedResultException(previousResult.getLeft()));
        }
      }
    }
  }


  @Override
  protected synchronized boolean setException(final Throwable t) {
    if (this.isCancelled()) {
      return false;
    }
    RetryDecision<T, Callable<? extends T>> decision = this.retryPredicate.getExceptionDecision(t, getCallable());
    final RetryDecision.Type decisionType = decision.getDecisionType();
    switch (decisionType) {
      case Retry:
        onRetry.run();
        final long delayNanos = decision.getDelayNanos();
        this.setCallable((Callable<T>) decision.getNewCallable());
        Throwable at = t;
        if (previousResults == null) {
         previousResults = new ArrayDeque<>();
        } else if (previousResults.size() >= MAX_EX_CHAIN_DEFAULT) {
          previousResults.removeFirst();
        }
        previousResults.addLast(Either.right(at));
        DelayedTask<RetryFutureTask<?>> delayedTask = new DelayedTask<>(this, delayNanos);
        this.exec = delayedTask;
        delayedTasks.add(delayedTask);
        return false;
      case Abort:
        this.exec = null;
        Either<Throwable, T> newRes = decision.getResult();
        if (newRes == null) {
          attachPrevious(t);
          super.setException(t);
        } else if (newRes.isLeft()) {
          Throwable tx = newRes.getLeft();
          attachPrevious(tx);
          super.setException(tx);
        } else {
          super.set(newRes.getRight());
        }
        return true;
      default:
        throw new IllegalStateException("Invalid decision type" + decisionType, t);
    }
  }

  @Override
  protected synchronized boolean set(final T v) {
    if (this.isCancelled()) {
      return false;
    }
    RetryDecision<T, Callable<? extends T>> decision = this.retryPredicate.getDecision(v, getCallable());
    final RetryDecision.Type decisionType = decision.getDecisionType();
    switch (decisionType) {
      case Retry:
        onRetry.run();
        final long delayNanos = decision.getDelayNanos();
        this.setCallable((Callable<T>) decision.getNewCallable());
        DelayedTask<RetryFutureTask<?>> delayedTask = new DelayedTask<>(this, delayNanos);
        this.exec = delayedTask;
        delayedTasks.add(delayedTask);
        if (previousResults == null) {
         previousResults = new ArrayDeque<>();
        } else if (previousResults.size() >= MAX_EX_CHAIN_DEFAULT) {
          previousResults.removeFirst();
        }
        previousResults.addLast(Either.left(v));
        return false;
      case Abort:
        this.exec = null;
        Either<Throwable, T> newRes = decision.getResult();
        if (newRes == null) {
          super.set(v);
        } else if (newRes.isLeft()) {
          super.setException(newRes.getLeft());
        } else {
          super.set(newRes.getRight());
        }
        return true;
      default:
        throw new IllegalStateException("Invalid decision type" + decisionType);
    }
  }




}

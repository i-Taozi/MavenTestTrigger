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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.spf4j.base.AbstractRunnable;
import org.spf4j.base.TimeSource;
import org.spf4j.base.Timing;
import org.spf4j.base.UncheckedExecutionException;
import static org.spf4j.concurrent.RejectedExecutionHandler.REJECT_EXCEPTION_EXEC_HANDLER;
import org.spf4j.ds.SimpleStack;
import org.spf4j.jmx.JmxExport;
import org.spf4j.jmx.Registry;

/**
 *
 * LIFO scheduled java thread pool, this behavior is identical  with the JDK's fork join pool, but different from the
 * older jdk ThreadPoolExecutor implementations.
 *
 * This implementation behaves differently compared with a java Thread pool in that it prefers to spawn a
 * thread if possible instead of queueing a task when nr of threads has room to grow.
 *
 * Performance of this pool implementation is similar to ThreadPoolExecutor however due to LIFO scheduling,
 * it will have lower resource usage in most use cases.
 *
 * The JDK Fork Join pool's performance is superior to this implementation, however unlike this implementation,
 * with the fork join pool you will not be able to cancel+interrupt running tasks.
 *
 * @author zoly
 */
@ParametersAreNonnullByDefault
@SuppressFBWarnings({"MDM_THREAD_PRIORITIES", "MDM_WAIT_WITHOUT_TIMEOUT"})
public final class LifoThreadPoolExecutorSQP extends AbstractExecutorService implements MutableLifoThreadPool {


  private static final Logger LOG = LoggerFactory.getLogger(LifoThreadPoolExecutorSQP.class);
  /**
   * when a thread survives due core size, this the minimum wait time that core threads will wait for. worker threads
   * have a maximum time they are idle, after which they are retired... in case a user configures a thread pool with
   * idle times less than min wait, the core threads will have to have a minimum wait time to avoid spinning and hogging
   * the CPU. this value is used only when the max idle time of the pool is smaller, and it interferes with thread
   * retirement in that case... I do not see that case as a useful pooling case to be worth trying to optimize it...
   */
  private static final long CORE_MINWAIT_NANOS = Long.getLong("spf4j.lifoTp.coreMaxWaitNanos", 1000000000);

  private static final int LL_THRESHOLD = Integer.getInteger("spf4j.lifoTp.llQueueSizeThreshold", 64000);

  private final ReentrantLock stateLock;

  private final Condition stateCondition;

  private final String poolName;

  private final RejectedExecutionHandler rejectionHandler;

  @GuardedBy("stateLock")
  private final Queue<Runnable> taskQueue;

  @GuardedBy("stateLock")
  private final SimpleStack<QueuedThread> threadQueue;

  @GuardedBy("stateLock")
  private int maxIdleTimeMillis;

  @GuardedBy("stateLock")
  private int maxThreadCount;

  @GuardedBy("stateLock")
  private final PoolState state;

  @GuardedBy("stateLock")
  private int queueSizeLimit;

  @GuardedBy("stateLock")
  private boolean daemonThreads;

  @GuardedBy("stateLock")
  private int threadPriority;

  @GuardedBy("stateLock")
  private int threadCreationCount;

  public LifoThreadPoolExecutorSQP(final int maxNrThreads, final String name) {
    this(name, 0, maxNrThreads, 5000, 0);
  }


  public LifoThreadPoolExecutorSQP(final String poolName, final int coreSize,
          final int maxSize, final int maxIdleTimeMillis,
          final int queueSize, final boolean daemonThreads) {
    this(poolName, coreSize, maxSize, maxIdleTimeMillis,
            queueSize, daemonThreads, REJECT_EXCEPTION_EXEC_HANDLER);
  }

  public LifoThreadPoolExecutorSQP(final String poolName, final int coreSize,
          final int maxSize, final int maxIdleTimeMillis,
          final int queueSizeLimit) {
    this(poolName, coreSize, maxSize, maxIdleTimeMillis,
            queueSizeLimit, false, REJECT_EXCEPTION_EXEC_HANDLER);
  }

  public LifoThreadPoolExecutorSQP(final String poolName, final int coreSize,
          final int maxSize, final int maxIdleTimeMillis,
          final int queueSizeLimit, final boolean daemonThreads,
          final RejectedExecutionHandler rejectionHandler) {
    this(poolName, coreSize, maxSize, maxIdleTimeMillis, queueSizeLimit, daemonThreads,
            rejectionHandler, Thread.NORM_PRIORITY);
  }

  public LifoThreadPoolExecutorSQP(final String poolName, final int coreSize,
          final int maxSize, final int maxIdleTimeMillis,
          final int queueSizeLimit, final boolean daemonThreads,
          final RejectedExecutionHandler rejectionHandler,
          final int threadPriority) {
    if (coreSize > maxSize) {
      throw new IllegalArgumentException("Core size must be smaller than max size " + coreSize
              + " < " + maxSize);
    }
    if (coreSize < 0 || maxSize < 0 || maxIdleTimeMillis < 0 || queueSizeLimit < 0) {
      throw new IllegalArgumentException("All numberic TP configs must be positive values: "
              + coreSize + ", " + maxSize + ", " + maxIdleTimeMillis
              + ", " + queueSizeLimit);
    }
    this.stateLock = new ReentrantLock();
    this.rejectionHandler = rejectionHandler;
    this.poolName = poolName;
    this.maxIdleTimeMillis = maxIdleTimeMillis;
    this.taskQueue = new ArrayDeque<>(Math.min(queueSizeLimit, LL_THRESHOLD));
    this.queueSizeLimit = queueSizeLimit;
    this.threadQueue = new SimpleStack<>(Math.min(1024, maxSize));
    this.threadPriority = threadPriority;
    state = new PoolState(coreSize, new THashSet<>(Math.min(maxSize, 2048)));
    this.stateCondition = stateLock.newCondition();
    this.daemonThreads = daemonThreads;
    for (int i = 0; i < coreSize; i++) {
      QueuedThread qt = new QueuedThread(poolName + '-' + (threadCreationCount++), threadQueue,
              taskQueue, maxIdleTimeMillis, null, state, stateLock, stateCondition);
      qt.setDaemon(daemonThreads);
      qt.setPriority(threadPriority);
      state.addThread(qt);
      qt.start();
    }
    maxThreadCount = maxSize;
  }

  @Override
  public void exportJmx() {
    Registry.export(LifoThreadPoolExecutorSQP.class.getName(), poolName, this);
  }

  @Override
  @SuppressFBWarnings(value = {"MDM_WAIT_WITHOUT_TIMEOUT", "MDM_LOCK_ISLOCKED", "UL_UNRELEASED_LOCK_EXCEPTION_PATH"},
          justification = "no blocking is done while holding the lock,"
          + " lock is released on all paths, findbugs just cannot figure it out...")
  public void execute(final Runnable command) {
    boolean reject = false;
    stateLock.lock();
    try {
      if (state.isShutdown()) {
        // if shutting down, reject
        stateLock.unlock();
        this.rejectionHandler.rejectedExecution(command, this);
        return;
      }
      QueuedThread nqt = threadQueue.pollLast();
      if (nqt != null) {
        nqt.runNext(command);
        stateLock.unlock();
        return;
      }
      int tc = state.getThreadCount();
      // was not able to submit to an existing available thread, will attempt to create a new thread.
      if (tc < maxThreadCount) {
        QueuedThread qt;
        try {
          qt = new QueuedThread(poolName  + '-' + (threadCreationCount++), threadQueue, taskQueue, maxIdleTimeMillis,
                  command, state, stateLock, stateCondition);
          qt.setDaemon(daemonThreads);
          qt.setPriority(threadPriority);
          state.addThread(qt);
        } finally {
          stateLock.unlock();
        }
        qt.start();
        return;
      }
      // was not able to submit to an existing available thread, reached the maxThread limit.
      // will attempt to queue the task, and reject if unable to
      reject = taskQueue.size() >= queueSizeLimit || !taskQueue.offer(command);
    } catch (Throwable t) {
      if (stateLock.isHeldByCurrentThread()) {
        stateLock.unlock();
      }
      throw t;
    }
    stateLock.unlock();
    if (reject) {
      rejectionHandler.rejectedExecution(command, this);
    }
  }

  @Override
  @SuppressFBWarnings("MDM_WAIT_WITHOUT_TIMEOUT")
  public void shutdown() {
    stateLock.lock();
    try {
      if (!state.isShutdown()) {
        state.setShutdown(true); // set the shutdown flag, to reject new submissions.
        QueuedThread th;
        while ((th = threadQueue.pollLast()) != null) {
          th.signal(); // signal all waiting threads, so thay can start going down.
        }
      }
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  public boolean awaitTermination(final long time, final TimeUnit unit) throws InterruptedException {
    long deadlinenanos = TimeSource.nanoTime() + unit.toNanos(time);
    int threadCount;
    stateLock.lock();
    try {
      if (!state.isShutdown()) {
        throw new IllegalStateException("Threadpool is not is shutdown mode " + this);
      }
      threadCount = state.getThreadCount();
      long timeoutNs = deadlinenanos - TimeSource.nanoTime();
      while (threadCount > 0) {
        if (timeoutNs > 0) {
          timeoutNs = stateCondition.awaitNanos(timeoutNs);
        } else {
          break;
        }
        threadCount = state.getThreadCount();
      }
    } finally {
      stateLock.unlock();
    }
    return threadCount == 0;
  }

  @Override
  public List<Runnable> shutdownNow() {
    shutdown(); // shutdown
    stateLock.lock();
    try {
      state.interruptAll(); // interrupt all running threads.
      return new ArrayList<>(taskQueue);
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  @JmxExport
  public boolean isShutdown() {
    stateLock.lock();
    try {
      return state.isShutdown();
    } finally {
      stateLock.unlock();
    }
  }

  @JmxExport
  @Override
  public boolean isDaemonThreads() {
    stateLock.lock();
    try {
      return daemonThreads;
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  @JmxExport
  public boolean isTerminated() {
    stateLock.lock();
    try {
      return state.isShutdown() && state.getThreadCount() == 0;
    } finally {
      stateLock.unlock();
    }
  }

  @JmxExport
  @Override
  public int getThreadCount() {
    stateLock.lock();
    try {
      return state.getThreadCount();
    } finally {
      stateLock.unlock();
    }
  }

  @JmxExport
  @Override
  public int getMaxThreadCount() {
    stateLock.lock();
    try {
      return maxThreadCount;
    } finally {
      stateLock.unlock();
    }
  }

  @JmxExport
  @Override
  public int getCoreThreadCount() {
    stateLock.lock();
    try {
      return state.getCoreThreads();
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  public ReentrantLock getStateLock() {
    return stateLock;
  }

  @JmxExport
  @SuppressFBWarnings(value = "MDM_WAIT_WITHOUT_TIMEOUT",
          justification = "Holders of this lock will not block")
  @Override
  public int getNrQueuedTasks() {
    stateLock.lock();
    try {
      return taskQueue.size();
    } finally {
      stateLock.unlock();
    }
  }

  @JmxExport
  @Override
  public int getQueueSizeLimit() {
    stateLock.lock();
    try {
      return queueSizeLimit;
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  public void unregisterJmx() {
    Registry.unregister(LifoThreadPoolExecutorSQP.class.getName(), poolName);
  }

  @Override
  @JmxExport
  public void setDaemonThreads(final boolean daemonThreads) {
    stateLock.lock();
    try {
      this.daemonThreads = daemonThreads;
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  @JmxExport
  public void setMaxIdleTimeMillis(final int maxIdleTimeMillis) {
    stateLock.lock();
    try {
      this.maxIdleTimeMillis = maxIdleTimeMillis;
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  @JmxExport
  public void setMaxThreadCount(final int maxThreadCount) {
    stateLock.lock();
    try {
      this.maxThreadCount = maxThreadCount;
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  @JmxExport
  public void setCoreThreadCount(final int coreThreadCount) {
    stateLock.lock();
    try {
      this.state.setCoreThreads(coreThreadCount);
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  @JmxExport
  public void setQueueSizeLimit(final int queueSizeLimit) {
    stateLock.lock();
    try {
      this.queueSizeLimit = queueSizeLimit;
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  @JmxExport
  public void setThreadPriority(final int threadPriority) {
    stateLock.lock();
    try {
      this.threadPriority = threadPriority;
    } finally {
      stateLock.unlock();
    }
  }

  @SuppressFBWarnings("NO_NOTIFY_NOT_NOTIFYALL")
  private static final class QueuedThread extends Thread {

    private final SimpleStack<QueuedThread> threadQueue;

    private final Queue<Runnable> taskQueue;

    private final int maxIdleTimeMillis;

    @GuardedBy("poolStateLock")
    private final PoolState state;

    private long lastRunNanos;

    private final ReentrantLock poolStateLock;

    private final Condition poolStateCondition;

    private final Condition submitCondition;

    @Nullable
    private Runnable toRun;

    QueuedThread(final String name, final SimpleStack<QueuedThread> threadQueue,
            final Queue<Runnable> taskQueue, final int maxIdleTimeMillis,
            @Nullable final Runnable runFirst, final PoolState state,
            final ReentrantLock submitMonitor, final Condition submitCondition) {
      super(name);
      this.threadQueue = threadQueue;
      this.taskQueue = taskQueue;
      this.maxIdleTimeMillis = maxIdleTimeMillis;
      this.state = state;
      this.lastRunNanos = TimeSource.nanoTime();
      this.poolStateLock = submitMonitor;
      this.submitCondition = submitMonitor.newCondition();
      this.poolStateCondition = submitCondition;
      this.toRun = runFirst;
    }

    /**
     * will return false when this thread is not running anymore...
     *
     * @param runnable
     * @return
     */
    @CheckReturnValue
    @SuppressFBWarnings("MDM_SIGNAL_NOT_SIGNALALL") // Only one thread will away on this condition
    private void runNext(final Runnable runnable) {
        toRun = runnable;
        submitCondition.signal();
    }

    @SuppressFBWarnings
    private void signal() {
      runNext(AbstractRunnable.NOP);
    }

    @Override
    public void run() {
      Runnable r = toRun;
      if (r != null) {
        try {
          execute(r);
        } finally {
          toRun = null;
        }
      }
      doRun(TimeUnit.MILLISECONDS.toNanos(maxIdleTimeMillis));
    }

    @SuppressFBWarnings({"MDM_LOCK_ISLOCKED", "UL_UNRELEASED_LOCK"})
    private void doRun(final long maxIdleNanos) {
      try {
        while (true) {
          poolStateLock.lock();
          Runnable poll = taskQueue.poll();
          if (poll != null) {
            poolStateLock.unlock();
            execute(poll);
          } else { // nothing in the queue, will put the thread to thread queue.
            if (state.isShutdown()) {
              removeThread();
              break;
            }
            long timeoutNanos = lastRunNanos + maxIdleNanos - TimeSource.nanoTime();
            if (timeoutNanos <= 0) { // Thread was idle more than it should
              final int tc = state.getThreadCount();
              if (tc > state.getCoreThreads()) { // can we terminate.
                removeThread();
                break;
              } else { // this is a core thread for now.
                timeoutNanos = CORE_MINWAIT_NANOS;
              }
            }
            int ptr = threadQueue.pushAndGetIdx(this);
            try {
              timeoutNanos = submitCondition.awaitNanos(timeoutNanos);
            } catch (InterruptedException ex) {
              if (state.isShutdown()) {
                removeThread();
                break;
              }
            }
            Runnable r = toRun;
            if (r != null) {
                poolStateLock.unlock();
                try {
                  execute(r);
                } finally {
                  toRun = null;
                }
            } else {
              QueuedThread qt = threadQueue.get(ptr);
              if (qt == this) {
                threadQueue.remove(ptr);
              } else {
                if (!threadQueue.remove(this))  {
                  throw new IllegalStateException("Thread "  + this + " not present in " +  threadQueue);
                }
              }
              if (timeoutNanos <= 0) {
                  final int tc = state.getThreadCount();
                  if (state.isShutdown() || tc > state.getCoreThreads()) {
                    removeThread();
                    break;
                  }
                }
                poolStateLock.unlock();
            }
          }
        }
      } catch (Throwable t) {
        LOG.error("Unexpected exception", t);
        if (poolStateLock.isHeldByCurrentThread()) {
          poolStateLock.unlock();
        }
        throw t;
      }

    }

    private void removeThread() {
      state.removeThread(this);
      poolStateCondition.signalAll();
      poolStateLock.unlock();
    }

    private void execute(final Runnable runnable) {
      try {
        runnable.run();
      }  catch (Throwable e) {
          // Will run the thread uncaught handlers
          // but will continue the thread running unless a uncaught handler throws an exception
          final Thread.UncaughtExceptionHandler uexh = this.getUncaughtExceptionHandler();
          try {
            uexh.uncaughtException(this, e);
          } catch (RuntimeException ex) {
            ex.addSuppressed(e);
            throw new UncheckedExecutionException("Uncaught exception handler blew up: " + uexh, ex);
          }
      } finally {
        lastRunNanos = TimeSource.nanoTime();
      }
    }

    @Override
    public String toString() {
      StackTraceElement[] stackTrace;
      try {
        stackTrace = this.getStackTrace();
      } catch (RuntimeException ex) {
        stackTrace = new StackTraceElement[0];
      }
      return "QueuedThread{name = " + getName() + ", lastRunNanos="
              + Timing.getCurrentTiming().fromNanoTimeToInstant(lastRunNanos)
              + ", stack =" + Arrays.toString(stackTrace)
              + ", toRun = " + toRun + '}';
    }

  }

  private static final class PoolState {

    private boolean shutdown;

    private int coreThreads;

    private final Set<QueuedThread> allThreads;

    PoolState(final int thnr, final Set<QueuedThread> allThreads) {
      this.shutdown = false;
      this.coreThreads = thnr;
      this.allThreads = allThreads;
    }

    public void addThread(final QueuedThread thread) {
      if (!allThreads.add(thread)) {
        throw new IllegalStateException("Attempting to add a thread twice: " + thread);
      }
      LOG.debug("Started thread {}", thread.getName());
    }

    public void removeThread(final QueuedThread thread) {
      if (!allThreads.remove(thread)) {
        throw new IllegalStateException("Removing thread failed: " + thread);
      }
      LOG.debug("Terminating thread {}", thread.getName());
    }

    public void interruptAll() {
      for (Thread thread : allThreads) {
        thread.interrupt();
      }
    }

    public int getCoreThreads() {
      return coreThreads;
    }

    public void setCoreThreads(final int setCoreThreads) {
       coreThreads = setCoreThreads;
    }

    public boolean isShutdown() {
      return shutdown;
    }

    public void setShutdown(final boolean shutdown) {
      this.shutdown = shutdown;
    }

    public int getThreadCount() {
      return allThreads.size();
    }

    @Override
    public String toString() {
      return "ExecState{" + "shutdown=" + shutdown + ", threadCount="
              + allThreads.size() + '}';
    }

  }

  @Override
  public String toString() {
    return "LifoThreadPoolExecutorSQP{" + "threadQueue=" + threadQueue + ", maxIdleTimeMillis="
            + maxIdleTimeMillis + ", maxThreadCount=" + maxThreadCount + ", state=" + state
            + ", submitMonitor=" + stateLock + ", queueCapacity=" + queueSizeLimit
            + ", poolName=" + poolName + '}';
  }

  @JmxExport
  @Override
  public int getMaxIdleTimeMillis() {
    stateLock.lock();
    try {
      return maxIdleTimeMillis;
    } finally {
      stateLock.unlock();
    }
  }

  @JmxExport
  @Override
  public String getPoolName() {
    return poolName;
  }

  @JmxExport
  @Override
  public int getThreadPriority() {
    stateLock.lock();
    try {
     return threadPriority;
    } finally {
      stateLock.unlock();
    }
  }

}

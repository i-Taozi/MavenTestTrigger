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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.spf4j.base.AbstractRunnable;
import org.spf4j.base.CharSequences;
import org.spf4j.base.SuppressForbiden;
import org.spf4j.base.TimeSource;
import org.spf4j.base.Timing;
import org.spf4j.concurrent.DefaultExecutor;
import org.spf4j.jmx.JmxExport;
import org.spf4j.jmx.Registry;

/**
 * Utility to sample stack traces. Stack traces can be persisted for later analysis.
 *
 * please read http://sape.inf.usi.ch/sites/default/files/publication/pldi10.pdf pure java stack sampling will probably
 * have safepoint bias.
 *
 * @author zoly
 */
@ThreadSafe
public final class Sampler {

  private static Sampler instance;

  public static final String DEFAULT_SS_DUMP_FOLDER = System.getProperty("spf4j.perf.ms.defaultSsdumpFolder",
          System.getProperty("java.io.tmpdir"));

  public static final String DEFAULT_SS_DUMP_FILE_NAME_PREFIX = CharSequences.validatedFileName(
          System.getProperty("spf4j.perf.ms.defaultSsdumpFilePrefix",
                  ManagementFactory.getRuntimeMXBean().getName()));

  @GuardedBy("sync")
  private boolean stopped;

  private volatile long sampleTimeNanos;
  private volatile long dumpTimeNanos;
  private final SamplerSupplier stackCollectorSupp;
  private volatile long lastDumpTimeNanos;

  private final Object sync = new Object();

  @GuardedBy("sync")
  private ISampler stackCollector;

  @GuardedBy("sync")
  private Future<?> samplerFuture;

  private volatile ProfilePersister persister;

  @Override
  public String toString() {
    return "Sampler{" + "stopped=" + stopped + ", sampleTimeNanos="
            + sampleTimeNanos + ", dumpTimeNanos=" + dumpTimeNanos + ", lastDumpTimeNanos="
            + lastDumpTimeNanos + ", dumpFolder=" + getDumpFolder() + ", filePrefix=" + getFilePrefix() + '}';
  }

  public Sampler() {
    this(10, 3600000, (t) -> new FastStackCollector(false, true, new Thread[]{t}));
  }

  public Sampler(final int sampleTimeMillis) {
    this(sampleTimeMillis, 3600000, (t) -> new FastStackCollector(false, true, new Thread[]{t}));
  }

  public Sampler(final int sampleTimeMillis, final SamplerSupplier collector) {
    this(sampleTimeMillis, 3600000, collector);
  }

  public Sampler(final SamplerSupplier collector) {
    this(10, 3600000, collector);
  }

  @SuppressFBWarnings("PATH_TRAVERSAL_IN")
  public Sampler(final int sampleTimeMillis, final int dumpTimeMillis) {
    this(sampleTimeMillis, dumpTimeMillis, (t) -> new FastStackCollector(false, true, new Thread[]{t}),
            DEFAULT_SS_DUMP_FOLDER, DEFAULT_SS_DUMP_FILE_NAME_PREFIX);
  }

  @SuppressFBWarnings("PATH_TRAVERSAL_IN")
  public Sampler(final int sampleTimeMillis, final int dumpTimeMillis, final SamplerSupplier collector) {
    this(sampleTimeMillis, dumpTimeMillis, collector,
            DEFAULT_SS_DUMP_FOLDER, DEFAULT_SS_DUMP_FILE_NAME_PREFIX);
  }

  @SuppressFBWarnings("PATH_TRAVERSAL_IN")
  public Sampler(final int sampleTimeMillis, final int dumpTimeMillis, final SamplerSupplier collector,
          final String dumpFolder, final String dumpFilePrefix) {
    this(sampleTimeMillis, dumpTimeMillis, collector, new File(dumpFolder), dumpFilePrefix);
  }

  public Sampler(final int sampleTimeMillis, final int dumpTimeMillis, final SamplerSupplier collector,
          final File dumpFolder, final String dumpFilePrefix) {
    this(sampleTimeMillis, dumpTimeMillis, collector, dumpFolder, dumpFilePrefix, true);
  }

  public Sampler(final int sampleTimeMillis, final int dumpTimeMillis, final SamplerSupplier collector,
          final File dumpFolder, final String dumpFilePrefix, final boolean compressDumps) {
    this(sampleTimeMillis, dumpTimeMillis, collector,
            new LegacyProfilePersister(dumpFolder.toPath(), dumpFilePrefix, compressDumps));
  }

  public Sampler(final int sampleTimeMillis, final int dumpTimeMillis, final SamplerSupplier collector,
          final ProfilePersister persister) {
    stopped = true;
    if (sampleTimeMillis < 1) {
      throw new IllegalArgumentException("Invalid sample time " + sampleTimeMillis);
    }
    this.sampleTimeNanos = TimeUnit.MILLISECONDS.toNanos(sampleTimeMillis);
    if (sampleTimeNanos < 0) {
      throw new IllegalArgumentException("Invalid sample time " + sampleTimeMillis);
    }
    this.dumpTimeNanos = TimeUnit.MILLISECONDS.toNanos(dumpTimeMillis);
    this.stackCollectorSupp = collector;
    this.persister = persister;
  }


  public static synchronized Sampler getSampler(final int sampleTimeMillis,
          final int dumpTimeMillis,
          final File dumpFolder, final String dumpFilePrefix) throws InterruptedException, IOException {
    return getSampler(sampleTimeMillis, dumpTimeMillis,
            (t) -> new FastStackCollector(false, true, new Thread[]{t}), dumpFolder, dumpFilePrefix);
  }

  /**
   * Gets a sampler.
   * This method will always creates a new sampler instance and will dispose of any sampler
   * instances previously created with this method.
   * There can only be one sampler instance running at a time.
   * @param sampleTimeMillis
   * @param dumpTimeMillis
   * @param collector
   * @param dumpFolder
   * @param dumpFilePrefix
   * @return
   * @throws InterruptedException
   */
  @SuppressFBWarnings("MS_EXPOSE_REP")
  public static synchronized Sampler getSampler(final int sampleTimeMillis,
          final int dumpTimeMillis, final SamplerSupplier collector,
          final File dumpFolder, final String dumpFilePrefix) throws InterruptedException, IOException {
    if (instance != null) {
      instance.dispose();
    }
    try {
      instance = new Sampler(sampleTimeMillis, dumpTimeMillis, collector,
              dumpFolder.getCanonicalFile(), dumpFilePrefix);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
    instance.registerJmx();
    return instance;
  }

  public void registerJmx() {
    Registry.export(this);
  }

  @JmxExport(description = "start stack sampling")
  public void start() {
    synchronized (sync) {
      if (stopped) {
        stopped = false;
        final long stNanos = sampleTimeNanos;
        samplerFuture = DefaultExecutor.INSTANCE.submit(new AbstractRunnable("SPF4J-Sampling-Thread") {

          @SuppressWarnings("SleepWhileInLoop")
          @SuppressFBWarnings({"MDM_THREAD_YIELD", "PREDICTABLE_RANDOM"})
          @Override
          public void doRun() {
            lastDumpTimeNanos = TimeSource.nanoTime();
            synchronized (sync) {
              stackCollector = stackCollectorSupp.get(Thread.currentThread());
            }
            final long lDumpTimeNanos = dumpTimeNanos;
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            long dumpCounterNanos = 0;
            long sleepTimeNanos = 0;
            long halfStNanos = stNanos / 2;
            if (halfStNanos == 0) {
              halfStNanos = 1;
            }
            long maxSleeepNanos = stNanos + halfStNanos;
            while (true) {
              try {
                synchronized (sync) {
                  stackCollector.sample();
                  if (stopped) {
                    break;
                  }
                }
                dumpCounterNanos += sleepTimeNanos;
                if (dumpCounterNanos >= lDumpTimeNanos) {
                  long nanosSinceLastDump = TimeSource.nanoTime() - lastDumpTimeNanos;
                  if (nanosSinceLastDump >= lDumpTimeNanos) {
                    dumpCounterNanos = 0;
                    File dumpFile = dumpToFile();
                    if (dumpFile != null) {
                      Logger.getLogger(Sampler.class.getName())
                              .log(Level.INFO, "Stack samples written to {0}", dumpFile);
                    }
                  } else {
                    dumpCounterNanos = nanosSinceLastDump;
                  }
                }
                sleepTimeNanos = random.nextLong(halfStNanos, maxSleeepNanos);
                TimeUnit.NANOSECONDS.sleep(sleepTimeNanos);
              } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
              } catch (IOException | RuntimeException ex) {
                Logger.getLogger(Sampler.class.getName()).log(Level.SEVERE,
                        "Exception encountered while samplig, will continue sampling", ex);
              }
            }
          }
        });
      } else {
        throw new IllegalStateException("Sampling can only be started once for " + this);
      }
    }
  }

  @JmxExport
  public boolean isCompressDumps() {
    return persister.isCompressing();
  }

  @JmxExport
  public void setCompressDumps(final boolean compressDumps) throws IOException {
    ProfilePersister p = this.persister;
    p.close();
    this.persister = p.witCompression(compressDumps);
  }


  private static class ProfileData {
    private final Instant from;
    private final Instant to;
    private final Map<String, SampleNode> samples;

    ProfileData(final Instant from, final Instant to, final Map<String, SampleNode> samples) {
      this.from = from;
      this.to = to;
      this.samples = samples;
    }

    public Instant getFrom() {
      return from;
    }

    public Instant getTo() {
      return to;
    }

    public Map<String, SampleNode> getSamples() {
      return samples;
    }
  }

  @Nullable
  private ProfileData getAndResetProfileSamples() {
    long fromNanos;
    Map<String, SampleNode> collections;
    long nowNanos;
    synchronized (sync) {
      if (stackCollector == null) {
        return null;
      }
      collections = stackCollector.getCollectionsAndReset();
      if (collections.isEmpty()) {
        return null;
      }
      fromNanos = lastDumpTimeNanos;
      nowNanos = TimeSource.nanoTime();
      lastDumpTimeNanos = nowNanos;
    }
    Timing currentTiming = Timing.getCurrentTiming();
    return new ProfileData(currentTiming.fromNanoTimeToInstant(fromNanos),
            currentTiming.fromNanoTimeToInstant(nowNanos), collections);

  }

  @JmxExport(description = "save stack samples to file")
  @Nullable
  public File dumpToFile() throws IOException {
    ProfileData data = getAndResetProfileSamples();
    if (data == null) {
      return null;
    }
    return persister.persist(data.getSamples(), null, data.getFrom(), data.getTo()).toFile();
  }

  /**
   * Dumps the sampled stacks to file. the collected samples are reset
   *
   * @param id - id will be added to file name returns the name of the file.
   * @return - the file name where the data was persisted or null if there was no data to persist. The file name
   * will be of format: [filePrefix](_[id])?_["yyyyMMdd'T'HHmmssSSSz]_["yyyyMMdd'T'HHmmssSSSz]_[label].ssdump2(.gz)?
   * or [filePrefix](_[id])?_["yyyyMMdd'T'HHmmssSSSz]_["yyyyMMdd'T'HHmmssSSSz].ssdump3(.gz)?
   * @throws IOException - io issues while persisting data.
   */
  @JmxExport(value = "dumpToSpecificFile", description = "save stack samples to file")
  @Nullable
  public File dumpToFile(
          @JmxExport(value = "fileID", description = "the ID that will be part of the file name")
          @Nullable final String id) throws IOException {
    ProfileData data = getAndResetProfileSamples();
    if (data == null) {
      return null;
    }
    return persister.persist(data.getSamples(), id, data.getFrom(), data.getTo()).toFile();
  }

  /**
   * Dump  collected samples to disk.
   * @param destinationFolder the destination folder.
   * @param pbaseFileName file name base, will be URLEncoded.
   * The appropriate extension will be added depending on situation.
   * either ssdump2 or ssdump3 will be used depending on the number of sample aggregate groups.
   * additionally .gz will be added if compression is enabled.
   * in case of ssdump2 the label will be appended to the file name like _[label].ssdump2
   *
   * @return the saved file, or null if no collected samples to save.
   * @throws IOException
   */
  @Nullable
  @SuppressFBWarnings("PATH_TRAVERSAL_IN") // not possible the provided ID is validated for path separators.
  public File dumpToFile(@Nonnull final File destinationFolder, final String pbaseFileName) throws IOException {
    ProfileData data = getAndResetProfileSamples();
    if (data == null) {
      return null;
    }
    return persister.withBaseFileName(destinationFolder.toPath(), pbaseFileName)
           .persist(data.getSamples(), null, data.getFrom(), data.getTo()).toFile();
  }

  @JmxExport(description = "stop stack sampling")
  public void stop() throws InterruptedException {
    Future<?> toCancel = null;
    synchronized (sync) {
      if (!stopped) {
        stopped = true;
        toCancel = samplerFuture;
      }
    }
    if (toCancel != null) {
      try {
        toCancel.get(dumpTimeNanos * 3, TimeUnit.NANOSECONDS);
      } catch (TimeoutException ex) {
        toCancel.cancel(true);
        throw new Spf4jProfilerException(ex);
      } catch (ExecutionException ex) {
        throw new Spf4jProfilerException(ex);
      }
    }
  }

  @JmxExport(description = "stack sample time in milliseconds")
  public int getSampleTimeMillis() {
    return (int) TimeUnit.NANOSECONDS.toMillis(sampleTimeNanos);
  }

  @JmxExport
  public void setSampleTimeMillis(final int sampleTimeMillis) {
    this.sampleTimeNanos = (int) TimeUnit.MILLISECONDS.toNanos(sampleTimeMillis);
  }

  @JmxExport(description = "is the stack sampling stopped")
  public boolean isStopped() {
    return stopped;
  }

  @JmxExport(description = "clear in memory collected stack samples")
  public void clear() {
    synchronized (sync) {
      stackCollector.getCollectionsAndReset();
    }
  }

  @JmxExport
  public void flushPersister() throws IOException {
    this.persister.flush();
  }

  public Map<String, SampleNode> getStackCollectionsAndReset() {
    synchronized (sync) {
      if (stackCollector == null) {
        return Collections.EMPTY_MAP;
      }
      return stackCollector.getCollectionsAndReset();
    }
  }

  public Map<String, SampleNode> getStackCollections() {
    synchronized (sync) {
      if (stackCollector == null) {
        return Collections.EMPTY_MAP;
      }
      return stackCollector.getCollections();
    }
  }

  @PreDestroy
  public void dispose() throws InterruptedException, IOException {
    try {
      stop();
    } finally {
      Registry.unregister(this);
      this.persister.close();
    }
  }

  @JmxExport(description = "interval in milliseconds to save stack stamples periodically")
  public int getDumpTimeMillis() {
    return (int) TimeUnit.NANOSECONDS.toMillis(dumpTimeNanos);
  }

  @JmxExport
  public void setDumpTimeMillis(final int dumpTimeMillis) {
    this.dumpTimeNanos = TimeUnit.MILLISECONDS.toNanos(dumpTimeMillis);
  }

  @JmxExport
  public String getFilePrefix() {
    return persister.getBaseFileName();
  }

  @JmxExport
  public String getDumpFolder() {
    return persister.getTargetPath().toString();
  }

  @JmxExport
  @SuppressForbiden // need to use an openType
  public Date getLastDumpTime() {
    return new Date(Timing.getCurrentTiming().fromNanoTimeToEpochMillis(lastDumpTimeNanos));
  }

  @JmxExport
  @SuppressForbiden // need to use an openType
  public Instant getLastDumpInstant() {
    return Timing.getCurrentTiming().fromNanoTimeToInstant(lastDumpTimeNanos);
  }

}

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
package org.spf4j.perf.impl.ms;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.spf4j.jmx.Registry;
import org.spf4j.perf.MeasurementStore;
import org.spf4j.perf.impl.NopMeasurementStore;
import org.spf4j.perf.impl.ms.graphite.GraphiteTcpStore;
import org.spf4j.perf.impl.ms.graphite.GraphiteUdpStore;
import org.spf4j.perf.impl.ms.tsdb.AvroMeasurementStore;
import org.spf4j.perf.impl.ms.tsdb.TSDBMeasurementStore;
import org.spf4j.perf.impl.ms.tsdb.TSDBTxtMeasurementStore;
import org.spf4j.recyclable.ObjectCreationException;

/**
 *
 * @author zoly
 */
public enum StoreType {
    TSDB(new StoreFactory() {
        @Override
        @SuppressFBWarnings("PATH_TRAVERSAL_IN") // not supplied by user
        public MeasurementStore create(final String pconfig) throws IOException {
            String config;
            if (!pconfig.endsWith("tsdb2"))  {
                config = pconfig + ".tsdb2";
            } else {
                config = pconfig;
            }
            return new TSDBMeasurementStore(new File(config));
        }
    }),
    TSDB_AVRO(new StoreFactory() {
        @Override
        @SuppressFBWarnings("PATH_TRAVERSAL_IN") // not supplied by user
        public MeasurementStore create(final String pconfig) throws IOException {
          Path path = Paths.get(pconfig);
          Path parent = path.getParent();
          if (parent == null) {
            throw new IllegalArgumentException("Invalid store config " + pconfig);
          }
          Path fileName = path.getFileName();
          if (fileName == null) {
            throw new IllegalArgumentException("Invalid store config " + pconfig);
          }
          return new AvroMeasurementStore(parent, fileName.toString());
        }
    }),
    TSDB_TXT(new StoreFactory() {
        @Override
        @SuppressFBWarnings("PATH_TRAVERSAL_IN") // not supplied by user
        public MeasurementStore create(final String config) throws IOException {
            return new TSDBTxtMeasurementStore(new File(config));
        }
    }),
    GRAPHITE_UDP(new StoreFactory() {
        @Override
        public MeasurementStore create(final String config) throws ObjectCreationException {
            try {
                return new GraphiteUdpStore(config);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Invalid configuration " + config, ex);
            }
        }
    }),
    GRAPHITE_TCP(new StoreFactory() {
        @Override
        public MeasurementStore create(final String config) throws ObjectCreationException {
            try {
                return new GraphiteTcpStore(config);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Invalid configuration " + config, ex);
            }
        }
    }),
    NOP_STORE(new StoreFactory() {

        @Override
        public MeasurementStore create(final String config) {
            return new NopMeasurementStore();
        }
    }),
    CUSTOM(new StoreFactory() {

        @Override
        public MeasurementStore create(final String config) {
          try {
            return (MeasurementStore) Class.forName(config).getConstructor().newInstance();
          } catch (ClassNotFoundException | NoSuchMethodException
                  | SecurityException | InstantiationException | IllegalAccessException
                  | InvocationTargetException ex) {
           throw new RuntimeException(ex);
          }
        }
    }),
    WRAPPER(new StoreFactory() {

        @Override
        public MeasurementStore create(final String config) throws IOException, ObjectCreationException {
          int fp = config.indexOf('(');
          int lp = config.lastIndexOf(')');
          if (fp < 0 || lp < 0 || lp < fp) {
            throw new IllegalArgumentException("Invalid wrapper config: " + config);
          }
          String className = config.substring(0, fp);
          MeasurementStore ms = fromString(config.substring(fp + 1, lp));
          try {
            return (MeasurementStore) Class.forName(className).getConstructor(MeasurementStore.class).newInstance(ms);
          } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                  | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
          }
        }
    });

    private final StoreFactory factory;

    StoreType(final StoreFactory factory) {
        this.factory = factory;
    }

    private MeasurementStore create(final String configuration) throws IOException, ObjectCreationException {
        MeasurementStore store =  factory.create(configuration);
        Registry.exportIfNeeded(store.getClass().getName(),
                    store.toString(), store);
        return store;
    }


  public static MeasurementStore fromString(final String string) throws IOException, ObjectCreationException {
    int atIdx = string.indexOf('@');
    final int length = string.length();
    if (atIdx < 0) {
      atIdx = length;
    }
    StoreType type = StoreType.valueOf(string.substring(0, atIdx));
    if (atIdx >= length) {
      return type.create("");
    } else {
      return type.create(string.substring(atIdx + 1));
    }
  }

}

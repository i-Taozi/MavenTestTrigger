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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.spf4j.log.Level;

/**
 * @author Zoltan Farkas
 */
@Immutable
@ParametersAreNonnullByDefault
final class LogConfigImpl implements LogConfig {

  static final Comparator<String> REV_STR_COMPARATOR = ((Comparator<String>) String::compareTo).reversed();

  private static final int DEFAULT_HANDLER_COUNT = Integer.getInteger("spf4j.test.log.defaultNrOfHandlers", 4);

  private final ImmutableList<LogHandler> rootHandler;

  private final SortedMap<String, List<LogHandler>> logHandlers;

  LogConfigImpl(final ImmutableList<LogHandler> rootHandler, final Map<String, List<LogHandler>> catHandlers) {
    this.rootHandler = rootHandler;
    logHandlers = new TreeMap<>(REV_STR_COMPARATOR);
    logHandlers.putAll(catHandlers);
  }

  @Override
  public LogConfigImpl add(final String category, final LogHandler handler,
          final ToIntFunction<List<LogHandler>> whereTo) {
    ImmutableList<LogHandler> rh;
    Map<String, List<LogHandler>> ch;
    if (category.isEmpty()) {
      List<LogHandler> existing = rootHandler;
      List<LogHandler> handlers = new ArrayList<>(existing.size() + 1);
      handlers.addAll(existing);
      handlers.add(whereTo.applyAsInt(handlers), handler);
      rh = ImmutableList.<LogHandler>builder().addAll(handlers).build();
      ch = logHandlers;
    } else {
      rh = rootHandler;
      ch = new HashMap<>(logHandlers);
      List<LogHandler> hndlrs = ch.get(category);
      if (hndlrs == null) {
        hndlrs = new ArrayList<>(2);
        ch.put(category, hndlrs);
        hndlrs.add(handler);
      } else {
        List<LogHandler> exHandlers = hndlrs;
        hndlrs = new ArrayList<>(exHandlers.size() + 1);
        hndlrs.addAll(exHandlers);
        hndlrs.add(whereTo.applyAsInt(hndlrs), handler);
        ch.put(category, hndlrs);
      }
    }
    return new LogConfigImpl(rh, ch);
  }


  @Override
  public LogConfigImpl remove(final String category, final LogHandler handler) {
    ImmutableList<LogHandler> rh;
    Map<String, List<LogHandler>> ch;
    if (category.isEmpty()) {
      rh = rootHandler.stream().filter((h) ->  h != handler).collect(ImmutableList.toImmutableList());
      ch = logHandlers;
    } else {
      rh = rootHandler;
      ch = new HashMap<>(logHandlers);
      List<LogHandler> hndlrs = ch.get(category);
      if (hndlrs != null) {
        hndlrs = new ArrayList<>(hndlrs);
        hndlrs.remove(handler);
        if (hndlrs.isEmpty()) {
          ch.remove(category);
        } else {
          ch.put(category, hndlrs);
        }
      }
    }
    return new LogConfigImpl(rh, ch);
  }

  @Override
  @Nullable
  public LogConsumer getLogConsumer(final String category, final Level level) {
    ArrayList<LogHandler> res = new ArrayList<>(DEFAULT_HANDLER_COUNT);
    if (category.isEmpty() || logHandlers.isEmpty()) {
      addAll(level, rootHandler, res);
      return CompositeLogHandler.from(res);
    }
    for (Map.Entry<String, List<LogHandler>> entry : logHandlers.tailMap(category).entrySet()) {
      String key = entry.getKey();
      if (category.startsWith(key)) {
        if (addAll(level, entry.getValue(), res)) {
          return CompositeLogHandler.from(res);
        }
      } else if (key.charAt(0) != category.charAt(0)) {
        break;
      }
    }
    addAll(level, rootHandler, res);
    return CompositeLogHandler.from(res);
  }

  private static boolean addAll(final Level level, final List<LogHandler> from, final List<LogHandler> to) {
    for (LogHandler handler : from) {
      LogHandler.Handling handling = handler.handles(level);
      switch (handling) {
        case HANDLE_CONSUME:
          if (!(handler instanceof ConsumeAllLogs)) {
            to.add(handler);
          }
          return true;
        case HANDLE_PASS:
          to.add(handler);
          break;
        case NONE:
          break;
        default:
          throw new UnsupportedOperationException("Not known " + handling);
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "LogConfigImpl{" + "rootHandler=" + rootHandler + ", logHandlers=" + logHandlers + '}';
  }

}

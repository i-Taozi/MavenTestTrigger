/**
 * Copyright (C) 2014 The SciGraph authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.scigraph.internal.reachability;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.ForwardingMap;

@ThreadSafe
final class InMemoryReachabilityIndex extends ForwardingMap<Long, InOutList> {

  ConcurrentSkipListMap<Long, InOutList> delegate = new ConcurrentSkipListMap<>();

  @Override
  protected Map<Long, InOutList> delegate() {
    return delegate;
  }

  @Override
  public InOutList get(Object key) {
    delegate.putIfAbsent((Long) key, new InOutList());
    return delegate.get(key);
  }

}
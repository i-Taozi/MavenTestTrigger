/*
 * Copyright (c) 2016 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.dns;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Test;

public class AggregatingChangeNotifierTest {
  @Test
  public void testEmptySet() {
    MyNotifier childNotifier = new MyNotifier();
    AggregatingChangeNotifier<String> notifier = new AggregatingChangeNotifier<>(Arrays.asList(childNotifier));

    ChangeNotifier.Listener listener = mock(ChangeNotifier.Listener.class);
    notifier.setListener(listener, false);

    verify(listener, never()).onChange(any(ChangeNotifier.ChangeNotification.class));

    childNotifier.set(Sets.newHashSet());
    verifyNoMoreInteractions(listener);

  }

  private static class MyNotifier extends AbstractChangeNotifier<String> {
    private volatile Set<String> records = ChangeNotifiers.initialEmptyDataInstance();

    @Override
    protected void closeImplementation() {
    }

    @Override
    public Set<String> current() {
      return records;
    }

    public void set(Set<String> records) {
      fireRecordsUpdated(newChangeNotification(records, current()));
      this.records = records;
    }
  }
}

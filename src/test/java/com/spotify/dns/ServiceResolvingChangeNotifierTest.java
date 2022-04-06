/*
 * Copyright (c) 2015 Spotify AB
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServiceResolvingChangeNotifierTest {

  private static final String FQDN = "example.com";

  @Mock
  public DnsSrvResolver resolver;

  @Mock
  ErrorHandler errorHandler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldCallListenerOnChange() {
    ChangeNotifierFactory.RunnableChangeNotifier<LookupResult> sut = createNotifier();
    ChangeNotifier.Listener<LookupResult> listener = mock(ChangeNotifier.Listener.class);
    sut.setListener(listener, false);

    LookupResult result1 = result("host", 1234);
    LookupResult result2 = result("host", 4321);
    when(resolver.resolve(FQDN)).thenReturn(Arrays.asList(result1), Arrays.asList(result1, result2));
    when(resolver.resolveAsync(FQDN))
        .thenReturn(CompletableFuture.completedFuture(Arrays.asList(result1)),
                CompletableFuture.completedFuture(Arrays.asList(result1, result2)));

    sut.run();
    sut.run();

    ArgumentCaptor<ChangeNotifier.ChangeNotification> captor =
        ArgumentCaptor.forClass(ChangeNotifier.ChangeNotification.class);
    verify(listener, times(2)).onChange(captor.capture());

    List<ChangeNotifier.ChangeNotification> notifications = captor.getAllValues();
    assertThat(notifications.size(), is(2));

    ChangeNotifier.ChangeNotification<LookupResult> change1 = notifications.get(0);
    assertThat(change1.previous().size(), is(0));
    assertThat(change1.current().size(), is(1));
    assertThat(change1.current(), containsInAnyOrder(result1));

    ChangeNotifier.ChangeNotification<LookupResult> change2 = notifications.get(1);
    assertThat(change2.previous().size(), is(1));
    assertThat(change2.previous(), containsInAnyOrder(result1));
    assertThat(change2.current().size(), is(2));
    assertThat(change2.current(), containsInAnyOrder(result1, result2));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldCallListenerOnSet() {
    ChangeNotifierFactory.RunnableChangeNotifier<LookupResult> sut = createNotifier();
    ChangeNotifier.Listener<LookupResult> listener = mock(ChangeNotifier.Listener.class);

    LookupResult result = result("host", 1234);
    when(resolver.resolve(FQDN))
            .thenReturn(Arrays.asList(result));
    when(resolver.resolveAsync(FQDN))
        .thenReturn(CompletableFuture.completedFuture(Arrays.asList(result)));

    sut.run();
    sut.setListener(listener, true);

    ArgumentCaptor<ChangeNotifier.ChangeNotification> captor =
        ArgumentCaptor.forClass(ChangeNotifier.ChangeNotification.class);
    verify(listener).onChange(captor.capture());

    ChangeNotifier.ChangeNotification<LookupResult> notification = captor.getValue();
    assertThat(notification.previous().size(), is(0));
    assertThat(notification.current().size(), is(1));
    assertThat(notification.current(), containsInAnyOrder(result));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldReturnImmutableSets() {
    ChangeNotifierFactory.RunnableChangeNotifier<LookupResult> sut = createNotifier();
    ChangeNotifier.Listener<LookupResult> listener = mock(ChangeNotifier.Listener.class);

    LookupResult result1 = result("host", 1234);
    LookupResult result2 = result("host", 4321);
    when(resolver.resolve(FQDN))
            .thenReturn(Arrays.asList(result1), Arrays.asList(result1, result2));
    when(resolver.resolveAsync(FQDN))
        .thenReturn(CompletableFuture.completedFuture(Arrays.asList(result1)),
                CompletableFuture.completedFuture(Arrays.asList(result1, result2)));

    sut.run();
    sut.setListener(listener, true);
    sut.run();

    ArgumentCaptor<ChangeNotifier.ChangeNotification> captor =
        ArgumentCaptor.forClass(ChangeNotifier.ChangeNotification.class);
    verify(listener, times(2)).onChange(captor.capture());

    for (ChangeNotifier.ChangeNotification<LookupResult> notification : captor.getAllValues()){
      try {
        notification.previous().clear();
        fail();
      } catch (UnsupportedOperationException ignore) {
      }
      try {
        notification.current().clear();
        fail();
      } catch (UnsupportedOperationException ignore) {
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldOnlyChangeIfTransformedValuesChange() {
    ChangeNotifierFactory.RunnableChangeNotifier<String> sut = createHostNotifier();
    ChangeNotifier.Listener<String> listener = mock(ChangeNotifier.Listener.class);
    sut.setListener(listener, false);

    LookupResult result1 = result("host", 1234);
    LookupResult result2 = result("host", 4321);
    when(resolver.resolve(FQDN))
            .thenReturn(Arrays.asList(result1), Arrays.asList(result1, result2));
    when(resolver.resolveAsync(FQDN))
        .thenReturn(CompletableFuture.completedFuture(Arrays.asList(result1)),
                CompletableFuture.completedFuture(Arrays.asList(result1, result2)));

    sut.run();
    sut.run();

    ArgumentCaptor<ChangeNotifier.ChangeNotification> captor =
        ArgumentCaptor.forClass(ChangeNotifier.ChangeNotification.class);
    verify(listener).onChange(captor.capture());

    ChangeNotifier.ChangeNotification<String> notification = captor.getValue();
    assertThat(notification.previous().size(), is(0));
    assertThat(notification.current().size(), is(1));
    assertThat(notification.current(), containsInAnyOrder("host"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldStopResolvingAfterClose() {
    ChangeNotifierFactory.RunnableChangeNotifier<LookupResult> sut = createNotifier();
    ChangeNotifier.Listener<LookupResult> listener = mock(ChangeNotifier.Listener.class);
    sut.setListener(listener, false);

    sut.close();
    sut.run();

    verify(resolver, never()).resolve(any(String.class));
    verify(listener, never()).onChange(any(ChangeNotifier.ChangeNotification.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldDoSomethingWithNulls() {
    Function<LookupResult, String> f = mock(Function.class);
    ChangeNotifierFactory.RunnableChangeNotifier<String> sut = createTransformingNotifier(f);
    ChangeNotifier.Listener<String> listener = mock(ChangeNotifier.Listener.class);

    when(resolver.resolve(FQDN))
            .thenReturn(Arrays.asList(
                    result("host1", 1234),
                    result("host2", 1234),
                    result("host3", 1234)));
    when(resolver.resolveAsync(FQDN))
        .thenReturn(CompletableFuture.completedFuture(Arrays.asList(
            result("host1", 1234),
            result("host2", 1234),
            result("host3", 1234))));

    when(f.apply(any(LookupResult.class)))
        .thenReturn("foo", null, "bar");

    sut.setListener(listener, false);
    sut.run();

    verify(listener, times(1)).onChange(any(ChangeNotifier.ChangeNotification.class));
    verifyNoMoreInteractions(listener);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldCallErrorHandlerOnResolveErrors() {
    Function<LookupResult, String> f = mock(Function.class);
    ChangeNotifierFactory.RunnableChangeNotifier<String> sut = createTransformingNotifier(f);
    ChangeNotifier.Listener<String> listener = mock(ChangeNotifier.Listener.class);

    DnsException exception = new DnsException("something wrong");
    when(resolver.resolve(FQDN))
            .thenThrow(exception);
    when(resolver.resolveAsync(FQDN))
        .thenReturn(DnsTestUtil.failedFuture(exception));

    sut.setListener(listener, false);
    sut.run();

    verify(errorHandler).handle(FQDN, exception);
    verifyNoMoreInteractions(f);
    verify(listener, times(1)).onChange(any(ChangeNotifier.ChangeNotification.class));
    verifyNoMoreInteractions(listener);
  }

  private ChangeNotifierFactory.RunnableChangeNotifier<LookupResult> createNotifier() {
    return createTransformingNotifier(Function.identity());
  }

  private ChangeNotifierFactory.RunnableChangeNotifier<String> createHostNotifier() {
    return createTransformingNotifier(input -> input != null ? input.host() : null);
  }

  private <T> ChangeNotifierFactory.RunnableChangeNotifier<T> createTransformingNotifier(
      Function<LookupResult, T> f) {
    return new ServiceResolvingChangeNotifier<T>(resolver, FQDN, f, errorHandler);
  }

  private static LookupResult result(String host, int port) {
    return LookupResult.create(host, port, 1, 5000, 300);
  }
}

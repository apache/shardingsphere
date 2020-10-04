/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.governance.repository.etcd;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EtcdRepositoryTest {
    
    private static final String CENTER_TYPE = "etcd";
    
    @Mock
    private Client client;
    
    @Mock
    private KV kv;
    
    @Mock
    private Watch watch;
    
    @Mock
    private Lease lease;
    
    @Mock
    private CompletableFuture getFuture;
    
    @Mock
    private CompletableFuture leaseFuture;
    
    @Mock
    private LeaseGrantResponse leaseGrantResponse;
    
    @Mock
    private GetResponse getResponse;
    
    @Mock
    private CompletableFuture putFuture;
    
    private final EtcdRepository repository = new EtcdRepository();
    
    @Before
    public void setUp() {
        setClient();
        setProperties();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setClient() {
        mockClient();
        FieldSetter.setField(repository, repository.getClass().getDeclaredField("client"), client);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProperties() {
        FieldSetter.setField(repository, repository.getClass().getDeclaredField("etcdProperties"), new EtcdProperties(new Properties()));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    private Client mockClient() {
        when(client.getKVClient()).thenReturn(kv);
        when(kv.get(any(ByteSequence.class))).thenReturn(getFuture);
        when(kv.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(getFuture);
        when(kv.put(any(ByteSequence.class), any(ByteSequence.class))).thenReturn(putFuture);
        when(kv.put(any(ByteSequence.class), any(ByteSequence.class), any(PutOption.class))).thenReturn(putFuture);
        when(getFuture.get()).thenReturn(getResponse);
        when(client.getLeaseClient()).thenReturn(lease);
        when(lease.grant(anyLong())).thenReturn(leaseFuture);
        when(leaseFuture.get()).thenReturn(leaseGrantResponse);
        when(leaseGrantResponse.getID()).thenReturn(123L);
        when(client.getWatchClient()).thenReturn(watch);
        return client;
    }
    
    @Test
    public void assertGetKey() {
        repository.get("key");
        verify(kv).get(ByteSequence.from("key", Charsets.UTF_8));
        verify(getResponse).getKvs();
    }
    
    @Test
    public void assertGetChildrenKeys() {
        io.etcd.jetcd.api.KeyValue keyValue1 = io.etcd.jetcd.api.KeyValue.newBuilder()
            .setKey(ByteString.copyFromUtf8("/key/key1/key1-1"))
            .setValue(ByteString.copyFromUtf8("value1")).build();
        io.etcd.jetcd.api.KeyValue keyValue2 = io.etcd.jetcd.api.KeyValue.newBuilder()
            .setKey(ByteString.copyFromUtf8("/key/key2"))
            .setValue(ByteString.copyFromUtf8("value3")).build();
        List<KeyValue> keyValues = Arrays.asList(new KeyValue(keyValue1, ByteSequence.EMPTY), new KeyValue(keyValue2, ByteSequence.EMPTY), 
                new KeyValue(keyValue1, ByteSequence.EMPTY));
        when(getResponse.getKvs()).thenReturn(keyValues);
        List<String> actual = repository.getChildrenKeys("/key");
        assertThat(actual.size(), is(2));
        Iterator<String> iterator = actual.iterator();
        assertThat(iterator.next(), is("key1"));
        assertThat(iterator.next(), is("key2"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertPersistEphemeral() {
        repository.persistEphemeral("key1", "value1");
        verify(lease).grant(anyLong());
        verify(lease).keepAlive(anyLong(), any(StreamObserver.class));
        verify(kv).put(any(ByteSequence.class), any(ByteSequence.class), any(PutOption.class));
    }
    
    @Test
    public void assertWatchUpdate() {
        doAnswer(invocationOnMock -> {
            Watch.Listener listener = (Watch.Listener) invocationOnMock.getArguments()[1];
            listener.onNext(buildWatchResponse(WatchEvent.EventType.PUT));
            return mock(Watch.Watcher.class);
        }).when(watch).watch(any(ByteSequence.class), any(Watch.Listener.class));
        repository.watch("key1", dataChangedEvent -> {
        });
        verify(watch).watch(any(ByteSequence.class), any(Watch.Listener.class));
    }
    
    @Test
    public void assertWatchDelete() {
        doAnswer(invocationOnMock -> {
            Watch.Listener listener = (Watch.Listener) invocationOnMock.getArguments()[1];
            listener.onNext(buildWatchResponse(WatchEvent.EventType.DELETE));
            return mock(Watch.Watcher.class);
        }).when(watch).watch(any(ByteSequence.class), any(Watch.Listener.class));
        repository.watch("key1", dataChangedEvent -> {
        });
        verify(watch).watch(any(ByteSequence.class), any(Watch.Listener.class));
    }
    
    @Test
    public void assertWatchIgnored() {
        doAnswer(invocationOnMock -> {
            Watch.Listener listener = (Watch.Listener) invocationOnMock.getArguments()[1];
            listener.onNext(buildWatchResponse(WatchEvent.EventType.UNRECOGNIZED));
            return mock(Watch.Watcher.class);
        }).when(watch).watch(any(ByteSequence.class), any(Watch.Listener.class));
        repository.watch("key1", dataChangedEvent -> {
        });
        verify(watch).watch(any(ByteSequence.class), any(Watch.Listener.class));
    }
    
    @Test
    public void assertDelete() {
        repository.delete("key");
        verify(kv).delete(ByteSequence.from("key", Charsets.UTF_8));
    }
    
    @Test
    public void assertPersist() {
        repository.persist("key1", "value1");
        verify(kv).put(any(ByteSequence.class), any(ByteSequence.class));
    }
    
    @Test
    public void assertClose() {
        repository.close();
        verify(client).close();
    }
    
    @Test
    public void assertGetType() {
        assertThat(repository.getType(), is(CENTER_TYPE));
    }
    
    @Test
    public void assertProperties() {
        Properties props = new Properties();
        repository.setProps(props);
        assertThat(repository.getProps(), is(props));
    }
    
    @Test
    public void assertGetKeyWhenThrowInterruptedException() throws ExecutionException, InterruptedException {
        doThrow(InterruptedException.class).when(getFuture).get();
        try {
            repository.get("key");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertThat(ex, instanceOf(InterruptedException.class));
        }
    }
    
    @Test
    public void assertGetKeyWhenThrowExecutionException() throws ExecutionException, InterruptedException {
        doThrow(ExecutionException.class).when(getFuture).get();
        try {
            repository.get("key");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertThat(ex, instanceOf(ExecutionException.class));
        }
    }
    
    @Test
    public void assertGetChildrenKeysWhenThrowInterruptedException() throws ExecutionException, InterruptedException {
        doThrow(InterruptedException.class).when(getFuture).get();
        try {
            repository.getChildrenKeys("/key/key1");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertThat(ex, instanceOf(InterruptedException.class));
        }
    }
    
    @Test
    public void assertGetChildrenKeysWhenThrowExecutionException() throws ExecutionException, InterruptedException {
        doThrow(ExecutionException.class).when(getFuture).get();
        try {
            repository.getChildrenKeys("/key/key1");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertThat(ex, instanceOf(ExecutionException.class));
        }
    }
    
    @SneakyThrows({NoSuchFieldException.class, SecurityException.class})
    private WatchResponse buildWatchResponse(final WatchEvent.EventType eventType) {
        WatchResponse result = new WatchResponse(mock(io.etcd.jetcd.api.WatchResponse.class), ByteSequence.EMPTY);
        List<WatchEvent> events = new LinkedList<>();
        io.etcd.jetcd.api.KeyValue keyValue1 = io.etcd.jetcd.api.KeyValue.newBuilder()
                .setKey(ByteString.copyFromUtf8("key1"))
                .setValue(ByteString.copyFromUtf8("value1")).build();
        KeyValue keyValue = new KeyValue(keyValue1, ByteSequence.EMPTY);
        events.add(new WatchEvent(keyValue, mock(KeyValue.class), eventType));
        FieldSetter.setField(result, result.getClass().getDeclaredField("events"), events);
        return result;
    }
}

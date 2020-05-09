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

package org.apache.shardingsphere.orchestration.center.instance;

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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EtcdCenterRepositoryTest {
    
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
    
    private final EtcdCenterRepository centerRepository = new EtcdCenterRepository();
    
    @Before
    public void setUp() {
        setClient();
        setProperties();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setClient() {
        mockClient();
        FieldSetter.setField(centerRepository, centerRepository.getClass().getDeclaredField("client"), client);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProperties() {
        FieldSetter.setField(centerRepository, centerRepository.getClass().getDeclaredField("etcdProperties"), new EtcdProperties(new Properties()));
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @SuppressWarnings("unchecked")
    private Client mockClient() {
        when(client.getKVClient()).thenReturn(kv);
        when(kv.get(any(ByteSequence.class))).thenReturn(getFuture);
        when(kv.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(getFuture);
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
        centerRepository.get("key");
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
        List<String> actual = centerRepository.getChildrenKeys("/key");
        assertThat(actual.size(), is(2));
        Iterator<String> iterator = actual.iterator();
        assertThat(iterator.next(), is("key1"));
        assertThat(iterator.next(), is("key2"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertPersistEphemeral() {
        centerRepository.persistEphemeral("key1", "value1");
        verify(lease).grant(anyLong());
        verify(lease).keepAlive(anyLong(), any(StreamObserver.class));
        verify(kv).put(any(ByteSequence.class), any(ByteSequence.class), any(PutOption.class));
    }
    
    @Test
    public void assertWatch() {
        centerRepository.watch("key1", dataChangedEvent -> {
        });
        verify(watch).watch(any(ByteSequence.class), any(Watch.Listener.class));
    }
    
    @Test
    public void assertDelete() {
        centerRepository.delete("key");
        verify(kv).delete(ByteSequence.from("key", Charsets.UTF_8));
    }
}

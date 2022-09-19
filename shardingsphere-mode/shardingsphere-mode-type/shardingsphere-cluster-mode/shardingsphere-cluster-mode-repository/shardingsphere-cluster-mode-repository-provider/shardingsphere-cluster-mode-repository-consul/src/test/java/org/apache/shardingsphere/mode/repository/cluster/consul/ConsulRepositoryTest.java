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

package org.apache.shardingsphere.mode.repository.cluster.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.google.protobuf.ByteString;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.plugins.MemberAccessor;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class ConsulRepositoryTest {
    
    private final ConsulRepository repository = new ConsulRepository();
    
    @Mock
    private ConsulClient client;

    @Mock
    private Response<GetValue> response;
    
    @Mock
    private Response<List<String>> responseList;

    @Mock
    private Response<List<GetValue>> responseGetValueList;

    @Mock
    private Response<Boolean> responseBoolean;
    
    @Mock
    private Response<String> sessionResponse;

    @Mock
    private GetValue getValue;

    @Mock
    private List<GetValue> getValueList;

    private long index = 123456L;
    
    @Before
    public void setUp() {
        setClient();
        setProperties();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setClient() {
        mockClient();
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(repository.getClass().getDeclaredField("consulClient"), repository, client);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProperties() {
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(repository.getClass().getDeclaredField("consulProperties"), repository, new ConsulProperties(new Properties()));
        accessor.set(repository.getClass().getDeclaredField("scheduledThreadPoolExecutor"), repository, new ScheduledThreadPoolExecutor(3));
        accessor.set(repository.getClass().getDeclaredField("watchKeyMap"), repository, new HashMap<>(4));
    }
    
    @SuppressWarnings("unchecked")
//    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    private ConsulClient mockClient() {
        when(client.getKVValue(any(String.class))).thenReturn(response);
        when(response.getValue()).thenReturn(getValue);
        when(client.getKVValues(any(String.class), any(QueryParams.class))).thenReturn(responseGetValueList);
        when(client.getKVKeysOnly(any(String.class))).thenReturn(responseList);
        when(client.sessionCreate(any(NewSession.class), any(QueryParams.class))).thenReturn(sessionResponse);
        when(sessionResponse.getValue()).thenReturn("12323ddsf3sss");
        when(responseGetValueList.getConsulIndex()).thenReturn(index++);
        when(responseGetValueList.getValue()).thenReturn(getValueList);
        when(client.setKVValue(any(String.class), any(String.class))).thenReturn(responseBoolean);
        return client;
    }
    
    @Test
    public void assertGetKey() {
        repository.get("key");
        verify(client).getKVValue("key");
        verify(response).getValue();
    }
    
    @Test
    public void assertGetChildrenKeys() {
        final String key = "/key";
        String k1 = "/key/key1/key1-1";
        String v1 = "value1";
        client.setKVValue(k1, v1);
        String k2 = "/key/key2";
        String v2 = "value2";
        client.setKVValue(k2, v2);
        List<String> getValues = Arrays.asList(k1, k2);
        when(responseList.getValue()).thenReturn(getValues);
        List<String> actual = repository.getChildrenKeys(key);
        assertThat(actual.size(), is(2));
        Iterator<String> iterator = actual.iterator();
        assertThat(iterator.next(), is("/key/key1/key1-1"));
        assertThat(iterator.next(), is("/key/key2"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertPersistEphemeral() {
        repository.persistEphemeral("key1", "value1");
        verify(client).sessionCreate(any(NewSession.class), any(QueryParams.class));
        verify(client).setKVValue(any(String.class), any(String.class), any(PutParams.class));
        //verify(client).renewSession(any(String.class), any(QueryParams.class));
    }
    
    @Test
    public void assertWatchUpdate() {
        String key = "sharding/key";
        String k1 = "sharding/key/key1";
        String v1 = "value1";
        client.setKVValue(k1,v1);
        GetValue getValue1 = new GetValue();
        getValue1.setKey(k1);
        getValue1.setValue(v1);
        List<GetValue> getValues = Arrays.asList(getValue1);
        when(responseGetValueList.getValue()).thenReturn(getValues);
        repository.watch(key, event -> {
        });
        verify(client.getKVValues(any(String.class), any(QueryParams.class)));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    
    @Test
    public void assertWatchDelete() {

    }
    
    @Test
    public void assertWatchIgnored() {
        // doAnswer(invocationOnMock -> {
        // Watch.Listener listener = (Watch.Listener) invocationOnMock.getArguments()[2];
        // listener.onNext(buildWatchResponse(WatchEvent.EventType.UNRECOGNIZED));
        // return mock(Watch.Watcher.class);
        // }).when(watch).watch(any(ByteSequence.class), any(WatchOption.class), any(Watch.Listener.class));
        // repository.watch("key1", event -> {
        // });
        // verify(watch).watch(any(ByteSequence.class), any(WatchOption.class), any(Watch.Listener.class));
    }

//    @SneakyThrows({NoSuchFieldException.class, SecurityException.class, IllegalAccessException.class})
//    private WatchResponse buildWatchResponse(final WatchEvent.EventType eventType) {
//        WatchResponse result = new WatchResponse(mock(io.etcd.jetcd.api.WatchResponse.class), ByteSequence.EMPTY);
//        List<WatchEvent> events = new LinkedList<>();
//        io.etcd.jetcd.api.KeyValue keyValue1 = io.etcd.jetcd.api.KeyValue.newBuilder()
//                .setKey(ByteString.copyFromUtf8("key1"))
//                .setValue(ByteString.copyFromUtf8("value1")).build();
//        KeyValue keyValue = new KeyValue(keyValue1, ByteSequence.EMPTY);
//        events.add(new WatchEvent(keyValue, mock(KeyValue.class), eventType));
//        MemberAccessor accessor = Plugins.getMemberAccessor();
//        accessor.set(result.getClass().getDeclaredField("events"), result, events);
//        return result;
//    }
    
    @Test
    public void assertDelete() {
        repository.delete("key");
        verify(client).deleteKVValue(any(String.class));
    }
    
    @Test
    public void assertPersist() {
        repository.persist("key1", "value1");
        verify(client).setKVValue(any(String.class), any(String.class));
    }
    
    @Test
    public void assertClose() {
        repository.close();
        // verify(client).close();
    }
}

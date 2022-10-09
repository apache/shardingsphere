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

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.repository.cluster.consul.lock.ConsulInternalLockProvider;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.plugins.MemberAccessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConsulRepositoryTest {
    
    private final ConsulRepository repository = new ConsulRepository();
    
    @Mock
    private ShardingSphereConsulClient client;
    
    @Mock
    private Response<GetValue> response;
    
    @Mock
    private Response<List<String>> responseList;
    
    @Mock
    private Response<List<GetValue>> responseGetValueList;
    
    @Mock
    private Response<Boolean> responseBoolean;
    
    // @Mock
    // private Response<String> sessionResponse;
    
    @Mock
    private GetValue getValue;
    
    // @Mock
    // private List<GetValue> getValueList;
    //
    // private long index = 123456L;
    
    @Before
    public void setUp() {
        setClient();
        setProperties();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setClient() {
        when(client.getKVValue(any(String.class))).thenReturn(response);
        when(response.getValue()).thenReturn(getValue);
        // when(client.getKVValues(any(String.class), any(QueryParams.class))).thenReturn(responseGetValueList);
        when(client.getKVKeysOnly(any(String.class))).thenReturn(responseList);
        // when(client.sessionCreate(any(NewSession.class), any(QueryParams.class))).thenReturn(sessionResponse);
        // when(sessionResponse.getValue()).thenReturn("12323ddsf3sss");
        // when(responseGetValueList.getConsulIndex()).thenReturn(index++);
        // when(responseGetValueList.getValue()).thenReturn(getValueList);
        when(client.setKVValue(any(String.class), any(String.class))).thenReturn(responseBoolean);
        Plugins.getMemberAccessor().set(repository.getClass().getDeclaredField("consulClient"), repository, client);
        Plugins.getMemberAccessor().set(repository.getClass().getDeclaredField("consulInternalLockProvider"), repository, mock(ConsulInternalLockProvider.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProperties() {
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(repository.getClass().getDeclaredField("consulProps"), repository, new ConsulProperties(new Properties()));
        accessor.set(repository.getClass().getDeclaredField("watchKeyMap"), repository, new HashMap<>(4));
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
    @Ignore
    public void assertPersistEphemeral() throws InterruptedException {
        repository.persistEphemeral("key1", "value1");
        verify(client).sessionCreate(any(NewSession.class), any(QueryParams.class));
        verify(client).setKVValue(any(String.class), any(String.class), any(PutParams.class));
        Thread.sleep(6000L);
        verify(client).renewSession(any(String.class), any(QueryParams.class));
    }
    
    @Test
    @Ignore
    public void assertWatchUpdate() throws InterruptedException {
        final String key = "sharding/key";
        final String k1 = "sharding/key/key1";
        final String v1 = "value1";
        client.setKVValue(k1, v1);
        GetValue getValue1 = new GetValue();
        getValue1.setKey(k1);
        getValue1.setValue(v1);
        when(responseGetValueList.getValue()).thenReturn(Collections.singletonList(getValue1));
        repository.watch(key, event -> { }, null);
        client.setKVValue(k1, "value1-1");
        verify(client, atLeastOnce()).getKVValues(any(String.class), any(QueryParams.class));
        Thread.sleep(10000L);
    }
    
    @Test
    @Ignore
    public void assertWatchDelete() throws InterruptedException {
        final String key = "sharding/key";
        final String k1 = "sharding/key/key1";
        final String v1 = "value1";
        final String k2 = "sharding/key/key2";
        final String v2 = "value1";
        client.setKVValue(k1, v1);
        client.setKVValue(k2, v2);
        GetValue getValue1 = new GetValue();
        getValue1.setKey(k1);
        getValue1.setValue(v1);
        when(responseGetValueList.getValue()).thenReturn(Collections.singletonList(getValue1));
        repository.watch(key, event -> { }, null);
        client.deleteKVValue(k2);
        verify(client, atLeastOnce()).getKVValues(any(String.class), any(QueryParams.class));
        Thread.sleep(10000L);
    }
    
    @Test
    public void assertWatchIgnored() {
        // TODO
    }
    
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
}

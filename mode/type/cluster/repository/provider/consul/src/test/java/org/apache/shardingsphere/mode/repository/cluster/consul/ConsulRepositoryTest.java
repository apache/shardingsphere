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

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.monitoring.ClientEventHandler;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.repository.cluster.lock.holder.DistributedLockHolder;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsulRepositoryTest {
    
    private final ConsulRepository repository = new ConsulRepository();
    
    @Mock
    private Consul client;
    
    @Mock
    private KeyValueClient keyValueClient;
    
    @Mock
    private ClientConfig clientConfig;
    
    @Mock
    private CacheConfig cacheConfig;
    
    @Mock
    private ClientEventHandler clientEventHandler;
    
    @Mock
    private Consul.NetworkTimeoutConfig networkTimeoutConfig;
    
    @BeforeEach
    void setUp() {
        setClient();
        setProperties();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setClient() {
        when(client.keyValueClient()).thenReturn(keyValueClient);
        when(keyValueClient.getValueAsString(any(String.class), any(Charset.class))).thenReturn(Optional.of("mockValue"));
        when(keyValueClient.getConfig()).thenReturn(clientConfig);
        when(keyValueClient.getEventHandler()).thenReturn(clientEventHandler);
        when(keyValueClient.getNetworkTimeoutConfig()).thenReturn(networkTimeoutConfig);
        when(clientConfig.getCacheConfig()).thenReturn(cacheConfig);
        when(networkTimeoutConfig.getClientReadTimeoutMillis()).thenReturn(60 * 1000);
        when(networkTimeoutConfig.getClientConnectTimeoutMillis()).thenReturn(10 * 1000);
        Plugins.getMemberAccessor().set(repository.getClass().getDeclaredField("consulClient"), repository, client);
        Plugins.getMemberAccessor().set(repository.getClass().getDeclaredField("distributedLockHolder"), repository, mock(DistributedLockHolder.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProperties() {
    }
    
    @Test
    void assertDirectlyKey() {
        repository.getDirectly("key");
        verify(keyValueClient).getValueAsString("key", StandardCharsets.UTF_8);
    }
    
    @Test
    void assertGetChildrenKeys() {
        final String key = "/key";
        String k1 = "/key/key1/key1-1";
        client.keyValueClient().putValue(k1, "value1");
        String k2 = "/key/key2";
        client.keyValueClient().putValue(k2, "value2");
        List<String> getValues = Arrays.asList(key, k1, k2);
        when(client.keyValueClient().getKeys(any(String.class))).thenReturn(getValues);
        List<String> actual = repository.getChildrenKeys(key);
        assertThat(actual.size(), is(2));
        Iterator<String> iterator = actual.iterator();
        assertThat(iterator.next(), is(k1));
        assertThat(iterator.next(), is(k2));
    }
    
    @Test
    void assertPersistEphemeral() {
        repository.persistEphemeral("key1", "value1");
        verify(keyValueClient).putValue(any(String.class), any(String.class), any(Charset.class));
    }
    
    // TODO lingh
    @Test
    @Disabled
    void assertWatchUpdate() {
        final String key = "sharding/key";
        final String k1 = "sharding/key/key1";
        final String v1 = "value1";
        client.keyValueClient().putValue(k1, v1);
        repository.watch(key, event -> {
        });
        client.keyValueClient().putValue(k1, "value1-1");
        while (true) {
            Awaitility.await().pollDelay(100L, TimeUnit.MILLISECONDS).until(() -> true);
            try {
                verify(keyValueClient, atLeastOnce()).getValues(any(String.class));
                break;
            } catch (final MockitoException ignored) {
            }
        }
    }
    
    // TODO
    @Test
    @Disabled
    void assertWatchDelete() {
        final String key = "sharding/key";
        final String k1 = "sharding/key/key1";
        final String k2 = "sharding/key/key2";
        client.keyValueClient().putValue(k1, "value1");
        client.keyValueClient().putValue(k2, "value1");
        repository.watch(key, event -> {
        });
        client.keyValueClient().deleteKey(k2);
        while (true) {
            Awaitility.await().pollDelay(100L, TimeUnit.MILLISECONDS).until(() -> true);
            try {
                verify(client.keyValueClient(), atLeastOnce()).getValues(any(String.class));
                break;
            } catch (final MockitoException ignored) {
            }
        }
    }
    
    @Test
    void assertDelete() {
        when(client.keyValueClient().getValuesAsString(any(String.class), any(Charset.class)))
                .thenReturn(Collections.singletonList("value"));
        repository.delete("key");
        verify(keyValueClient).deleteKeys(any(String.class));
    }
    
    @Test
    void assertPersist() {
        repository.persist("key1", "value1");
        verify(keyValueClient).putValue(any(String.class), any(String.class), any(Charset.class));
    }
}

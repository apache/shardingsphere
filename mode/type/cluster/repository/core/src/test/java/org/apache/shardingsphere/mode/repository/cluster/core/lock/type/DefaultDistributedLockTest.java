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

package org.apache.shardingsphere.mode.repository.cluster.core.lock.type;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.props.DefaultLockPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.props.DefaultLockTypedProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultDistributedLockTest {
    
    @Mock
    private ClusterPersistRepository client;
    
    private DefaultDistributedLock distributedLock;
    
    @BeforeEach
    void setUp() {
        distributedLock = new DefaultDistributedLock(
                "foo_key", client, new DefaultLockTypedProperties(PropertiesBuilder.build(new Property(DefaultLockPropertyKey.INSTANCE_ID.getKey(), "foo_instance_id"))));
    }
    
    @Test
    void assertTryLockFailed() {
        assertFalse(distributedLock.tryLock(10L));
        assertTrue(getThreadData().isEmpty());
    }
    
    @Test
    void assertTryLockNewKey() {
        when(client.persistExclusiveEphemeral("foo_key", "foo_instance_id")).thenReturn(true);
        assertTrue(distributedLock.tryLock(10L));
        assertFalse(getThreadData().isEmpty());
    }
    
    @Test
    void assertTryLockExistedKey() {
        when(client.persistExclusiveEphemeral("foo_key", "foo_instance_id")).thenReturn(true);
        assertTrue(distributedLock.tryLock(10L));
        assertTrue(distributedLock.tryLock(10L));
        assertFalse(getThreadData().isEmpty());
    }
    
    @Test
    void assertTryUnlockWithoutExistedKey() {
        assertThrows(IllegalMonitorStateException.class, () -> distributedLock.unlock());
    }
    
    @Test
    void assertTryUnlockWithExistedKeyOnce() {
        when(client.persistExclusiveEphemeral("foo_key", "foo_instance_id")).thenReturn(true);
        distributedLock.tryLock(10L);
        distributedLock.tryLock(10L);
        distributedLock.unlock();
        verify(client, never()).delete("foo_key");
        assertFalse(getThreadData().isEmpty());
    }
    
    @Test
    void assertTryUnlockWithExistedKeyCompletely() {
        when(client.persistExclusiveEphemeral("foo_key", "foo_instance_id")).thenReturn(true);
        distributedLock.tryLock(10L);
        distributedLock.unlock();
        verify(client).delete("foo_key");
        assertTrue(getThreadData().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Thread, ?> getThreadData() {
        return (Map<Thread, ?>) Plugins.getMemberAccessor().get(DefaultDistributedLock.class.getDeclaredField("threadData"), distributedLock);
    }
}

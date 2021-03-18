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

package org.apache.shardingsphere.governance.core.lock;

import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceLockTest {
    
    @Mock
    private RegistryCenter registryCenter;
    
    private ShardingSphereLock lock;
    
    @Before
    public void setUp() {
        lock = new GovernanceLock(registryCenter);
    }
    
    @Test
    public void assertTryLock() {
        when(registryCenter.tryLock(eq(50L))).thenReturn(Boolean.TRUE);
        lock.tryLock("sharding_db", "t_order", 50L);
        verify(registryCenter).tryLock(eq(50L));
        verify(registryCenter).addLockedResources(eq(Arrays.asList("sharding_db.t_order")));
    }
    
    @Test
    public void assertReleaseLock() {
        lock.releaseLock("sharding_db", "t_order");
        verify(registryCenter).releaseLock();
        verify(registryCenter).deleteLockedResources(eq(Arrays.asList("sharding_db.t_order")));
    }
}

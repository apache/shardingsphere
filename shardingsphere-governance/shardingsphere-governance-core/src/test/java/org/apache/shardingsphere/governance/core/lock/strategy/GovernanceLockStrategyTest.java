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

package org.apache.shardingsphere.governance.core.lock.strategy;

import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.util.FieldUtil;
import org.apache.shardingsphere.infra.lock.LockStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceLockStrategyTest {
    
    @Mock
    private RegistryCenter registryCenter;
    
    private LockStrategy lockStrategy;
    
    @Before
    public void setUp() {
        lockStrategy = new GovernanceLockStrategy();
        FieldUtil.setField(lockStrategy, "registryCenter", registryCenter);
    }
    
    @Test
    public void assertTryLock() {
        lockStrategy.tryLock(50L, TimeUnit.MILLISECONDS);
        verify(registryCenter).tryGlobalLock(eq(50L), eq(TimeUnit.MILLISECONDS));
    }
    
    @Test
    public void assertReleaseLock() {
        lockStrategy.releaseLock();
        verify(registryCenter).releaseGlobalLock();
    }
}

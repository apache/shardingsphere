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

package org.apache.shardingsphere.schedule.core.job.statistics.collect;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticsCollectContextManagerLifecycleListenerTest {
    
    @Test
    void assertOnInitializedWithNotProxy() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()).thenReturn(true);
        new StatisticsCollectContextManagerLifecycleListener().onInitialized(contextManager);
        verify(contextManager.getComputeNodeInstanceContext()).getModeConfiguration();
    }
    
    @Test
    void assertOnInitializedWithProxy() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()).thenReturn(true);
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.PROXY);
        new StatisticsCollectContextManagerLifecycleListener().onInitialized(contextManager);
        verify(contextManager.getComputeNodeInstanceContext(), times(2)).getModeConfiguration();
    }
    
    @Test
    void assertOnDestroyed() {
        assertDoesNotThrow(() -> new StatisticsCollectContextManagerLifecycleListener().onDestroyed(mock(ContextManager.class)));
    }
}

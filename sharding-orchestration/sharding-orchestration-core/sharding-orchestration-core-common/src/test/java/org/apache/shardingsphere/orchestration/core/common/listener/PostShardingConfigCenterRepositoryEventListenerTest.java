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

package org.apache.shardingsphere.orchestration.core.common.listener;

import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingOrchestrationEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PostShardingConfigCenterRepositoryEventListenerTest {
    
    @Mock
    private ConfigCenterRepository configCenterRepository;
    
    @Test
    public void assertWatch() {
        PostShardingConfigCenterEventListener postShardingConfigCenterEventListener = new PostShardingConfigCenterEventListener(configCenterRepository, Arrays.asList("test")) {
            
            @Override
            protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
                return mock(ShardingOrchestrationEvent.class);
            }
        };
        postShardingConfigCenterEventListener.watch();
        verify(configCenterRepository).watch(eq("test"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertWatchMultipleKey() {
        PostShardingConfigCenterEventListener postShardingConfigCenterEventListener = new PostShardingConfigCenterEventListener(configCenterRepository, Arrays.asList("test", "dev")) {
            
            @Override
            protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
                return mock(ShardingOrchestrationEvent.class);
            }
        };
        postShardingConfigCenterEventListener.watch();
        verify(configCenterRepository).watch(eq("test"), ArgumentMatchers.any());
        verify(configCenterRepository).watch(eq("dev"), ArgumentMatchers.any());
    }
}

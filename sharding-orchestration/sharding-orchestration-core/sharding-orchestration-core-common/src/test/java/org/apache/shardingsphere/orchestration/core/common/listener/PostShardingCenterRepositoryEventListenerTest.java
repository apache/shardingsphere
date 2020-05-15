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

import org.apache.shardingsphere.orchestration.center.CenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingOrchestrationEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PostShardingCenterRepositoryEventListenerTest {
    
    @Mock
    private CenterRepository centerRepository;
    
    @Test
    public void assertWatch() {
        PostShardingCenterRepositoryEventListener postShardingCenterRepositoryEventListener = new PostShardingCenterRepositoryEventListener(centerRepository, Collections.singletonList("test")) {
            
            @Override
            protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
                return mock(ShardingOrchestrationEvent.class);
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", DataChangedEvent.ChangedType.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(centerRepository).watch(anyString(), any(DataChangedEventListener.class));
        postShardingCenterRepositoryEventListener.watch(DataChangedEvent.ChangedType.UPDATED);
        verify(centerRepository).watch(eq("test"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertWatchMultipleKey() {
        PostShardingCenterRepositoryEventListener postShardingCenterRepositoryEventListener = new PostShardingCenterRepositoryEventListener(centerRepository, Arrays.asList("test", "dev")) {
            
            @Override
            protected ShardingOrchestrationEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
                return mock(ShardingOrchestrationEvent.class);
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", DataChangedEvent.ChangedType.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(centerRepository).watch(anyString(), any(DataChangedEventListener.class));
        postShardingCenterRepositoryEventListener.watch(DataChangedEvent.ChangedType.UPDATED, DataChangedEvent.ChangedType.DELETED);
        verify(centerRepository).watch(eq("test"), ArgumentMatchers.any());
        verify(centerRepository).watch(eq("dev"), ArgumentMatchers.any());
    }
}

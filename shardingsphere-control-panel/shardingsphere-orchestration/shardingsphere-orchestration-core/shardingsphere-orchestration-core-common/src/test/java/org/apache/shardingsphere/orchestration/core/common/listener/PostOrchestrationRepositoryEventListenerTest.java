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

import org.apache.shardingsphere.orchestration.core.common.event.OrchestrationEvent;
import org.apache.shardingsphere.orchestration.repository.api.OrchestrationRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEventListener;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public final class PostOrchestrationRepositoryEventListenerTest {
    
    @Mock
    private OrchestrationRepository orchestrationRepository;
    
    @Test
    public void assertWatch() {
        PostOrchestrationRepositoryEventListener postEventListener = new PostOrchestrationRepositoryEventListener(orchestrationRepository, Collections.singletonList("test")) {
            
            @Override
            protected OrchestrationEvent createOrchestrationEvent(final DataChangedEvent event) {
                return mock(OrchestrationEvent.class);
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", ChangedType.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(orchestrationRepository).watch(anyString(), any(DataChangedEventListener.class));
        postEventListener.watch(ChangedType.UPDATED);
        verify(orchestrationRepository).watch(eq("test"), any());
    }
    
    @Test
    public void assertWatchMultipleKey() {
        PostOrchestrationRepositoryEventListener postEventListener = new PostOrchestrationRepositoryEventListener(orchestrationRepository, Arrays.asList("test", "dev")) {
            
            @Override
            protected OrchestrationEvent createOrchestrationEvent(final DataChangedEvent event) {
                return mock(OrchestrationEvent.class);
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", ChangedType.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(orchestrationRepository).watch(anyString(), any(DataChangedEventListener.class));
        postEventListener.watch(ChangedType.UPDATED, ChangedType.DELETED);
        verify(orchestrationRepository).watch(eq("test"), any());
        verify(orchestrationRepository).watch(eq("dev"), any());
    }
}

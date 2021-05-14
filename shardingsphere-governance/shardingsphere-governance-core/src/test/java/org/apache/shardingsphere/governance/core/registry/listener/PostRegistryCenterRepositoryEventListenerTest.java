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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PostRegistryCenterRepositoryEventListenerTest {
    
    @Mock
    private RegistryCenterRepository repository;
    
    @Test
    public void assertWatch() {
        PostGovernanceRepositoryEventListener<GovernanceEvent> postEventListener = new PostGovernanceRepositoryEventListener<GovernanceEvent>(repository, Collections.singletonList("test")) {
            
            @Override
            protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
                return Optional.of(mock(GovernanceEvent.class));
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", Type.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(repository).watch(anyString(), any(DataChangedEventListener.class));
        postEventListener.watch(Type.UPDATED);
        verify(repository).watch(eq("test"), any());
    }
    
    @Test
    public void assertWatchMultipleKey() {
        PostGovernanceRepositoryEventListener<GovernanceEvent> postEventListener = new PostGovernanceRepositoryEventListener<GovernanceEvent>(repository, Arrays.asList("test", "dev")) {
            
            @Override
            protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
                return Optional.of(mock(GovernanceEvent.class));
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", Type.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(repository).watch(anyString(), any(DataChangedEventListener.class));
        postEventListener.watch(Type.UPDATED, Type.DELETED);
        verify(repository).watch(eq("test"), any());
        verify(repository).watch(eq("dev"), any());
    }
}

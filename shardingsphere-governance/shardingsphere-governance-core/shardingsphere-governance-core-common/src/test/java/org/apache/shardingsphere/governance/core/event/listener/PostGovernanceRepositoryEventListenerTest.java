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

package org.apache.shardingsphere.governance.core.event.listener;

import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.repository.api.GovernanceRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
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
public final class PostGovernanceRepositoryEventListenerTest {
    
    @Mock
    private GovernanceRepository governanceRepository;
    
    @Test
    public void assertWatch() {
        PostGovernanceRepositoryEventListener postEventListener = new PostGovernanceRepositoryEventListener(governanceRepository, Collections.singletonList("test")) {
            
            @Override
            protected Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
                return Optional.of(mock(GovernanceEvent.class));
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", ChangedType.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(governanceRepository).watch(anyString(), any(DataChangedEventListener.class));
        postEventListener.watch(ChangedType.UPDATED);
        verify(governanceRepository).watch(eq("test"), any());
    }
    
    @Test
    public void assertWatchMultipleKey() {
        PostGovernanceRepositoryEventListener postEventListener = new PostGovernanceRepositoryEventListener(governanceRepository, Arrays.asList("test", "dev")) {
            
            @Override
            protected Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
                return Optional.of(mock(GovernanceEvent.class));
            }
        };
        doAnswer(invocationOnMock -> {
            DataChangedEventListener listener = (DataChangedEventListener) invocationOnMock.getArguments()[1];
            listener.onChange(new DataChangedEvent("test", "value", ChangedType.UPDATED));
            return mock(DataChangedEventListener.class);
        }).when(governanceRepository).watch(anyString(), any(DataChangedEventListener.class));
        postEventListener.watch(ChangedType.UPDATED, ChangedType.DELETED);
        verify(governanceRepository).watch(eq("test"), any());
        verify(governanceRepository).watch(eq("dev"), any());
    }
}

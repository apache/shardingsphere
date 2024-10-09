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

package org.apache.shardingsphere.mode.manager.cluster.listener;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.cluster.event.builder.DispatchEventBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalMetaDataChangedListenerTest {
    
    private GlobalMetaDataChangedListener listener;
    
    @Mock
    private EventBusContext eventBusContext;
    
    @Mock
    private DispatchEventBuilder<Object> builder;
    
    @BeforeEach
    void setUp() {
        listener = new GlobalMetaDataChangedListener(eventBusContext, builder);
        when(builder.getSubscribedTypes()).thenReturn(Collections.singleton(Type.ADDED));
    }
    
    @Test
    void assertOnChangeWithUnsupportedType() {
        DataChangedEvent event = new DataChangedEvent("key", "value", Type.DELETED);
        listener.onChange(event);
        verify(builder, times(0)).build(event);
    }
    
    @Test
    void assertOnChangeWithSupportedType() {
        DataChangedEvent event = new DataChangedEvent("key", "value", Type.ADDED);
        Object builtEvent = mock(Object.class);
        when(builder.build(event)).thenReturn(Optional.of(builtEvent));
        listener.onChange(event);
        verify(eventBusContext).post(builtEvent);
    }
}

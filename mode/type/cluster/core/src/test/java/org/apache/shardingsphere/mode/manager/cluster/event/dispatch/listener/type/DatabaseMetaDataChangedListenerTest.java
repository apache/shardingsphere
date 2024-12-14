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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.listener.type;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DatabaseMetaDataChangedListenerTest {
    
    private DatabaseMetaDataChangedListener listener;
    
    @Mock
    private EventBusContext eventBusContext;
    
    @BeforeEach
    void setUp() {
        listener = new DatabaseMetaDataChangedListener(eventBusContext);
    }
    
    @Test
    void assertOnChangeWithoutDatabase() {
        listener.onChange(new DataChangedEvent("/metadata", "value", Type.IGNORED));
        verify(eventBusContext, times(0)).post(any());
    }
    
    @Test
    void assertOnChangeWithMetaDataChanged() {
        listener.onChange(new DataChangedEvent("/metadata/foo_db/schemas/foo_schema", "value", Type.ADDED));
        verify(eventBusContext).post(any());
    }
    
    @Test
    void assertOnChangeWithRuleConfigurationChanged() {
        listener.onChange(new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/rule/", "value", Type.ADDED));
        verify(eventBusContext, times(0)).post(any());
    }
}

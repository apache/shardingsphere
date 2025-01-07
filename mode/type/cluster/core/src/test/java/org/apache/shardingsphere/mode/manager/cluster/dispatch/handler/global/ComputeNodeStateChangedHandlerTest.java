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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.DataChangedEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ComputeNodeStateChangedHandlerTest {
    
    private DataChangedEventHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        handler = ShardingSphereServiceLoader.getServiceInstances(DataChangedEventHandler.class).stream()
                .filter(each -> each.getSubscribedKey().equals("/nodes/compute_nodes")).findFirst().orElse(null);
    }
    
    @Test
    void assertHandleWithEmptyInstanceId() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/status", "", Type.ADDED));
        verify(contextManager, times(0)).getComputeNodeInstanceContext();
    }
    
    @Test
    void assertHandleWithComputeNodeInstanceStateChangedEvent() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/status/foo_instance_id", "OK", Type.ADDED));
        verify(contextManager.getComputeNodeInstanceContext()).updateStatus("foo_instance_id", "OK");
    }
    
    @Test
    void assertHandleWithLabelsEvent() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/labels/foo_instance_id", "", Type.ADDED));
        verify(contextManager.getComputeNodeInstanceContext()).updateLabels("foo_instance_id", Collections.emptyList());
    }
    
    @Test
    void assertHandleWithWorkerIdEvent() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/worker_id/foo_instance_id", "1", Type.ADDED));
        verify(contextManager.getComputeNodeInstanceContext()).updateWorkerId("foo_instance_id", 1);
    }
}

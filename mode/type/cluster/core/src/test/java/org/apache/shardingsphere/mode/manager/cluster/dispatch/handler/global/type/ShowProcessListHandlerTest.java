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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.type;

import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings({ProcessRegistry.class, ProcessOperationLockRegistry.class})
class ShowProcessListHandlerTest {
    
    private GlobalDataChangedEventHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId()).thenReturn("foo_instance_id");
        handler = ShardingSphereServiceLoader.getServiceInstances(GlobalDataChangedEventHandler.class).stream()
                .filter(each -> each.getSubscribedKey().equals("/nodes/compute_nodes/show_process_list_trigger")).findFirst().orElse(null);
    }
    
    @Test
    void assertHandleWithInvalidShowProcessListTriggerEventKey() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id", "", Type.DELETED));
        verify(ProcessOperationLockRegistry.getInstance(), times(0)).notify(any());
    }
    
    @Test
    void assertHandleReportLocalProcessesWithNotCurrentInstance() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/show_process_list_trigger/bar_instance_id:foo_task_id", "", Type.ADDED));
        verify(contextManager.getPersistServiceFacade().getRepository(), times(0)).delete(any());
    }
    
    @Test
    void assertHandleReportLocalProcesses() {
        when(ProcessRegistry.getInstance().listAll()).thenReturn(Collections.emptyList());
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_task_id", "", Type.ADDED));
        verify(contextManager.getPersistServiceFacade().getRepository()).delete(any());
    }
    
    @Test
    void assertHandleCompleteToReportLocalProcesses() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_task_id", "", Type.DELETED));
        verify(ProcessOperationLockRegistry.getInstance()).notify("foo_task_id");
    }
}

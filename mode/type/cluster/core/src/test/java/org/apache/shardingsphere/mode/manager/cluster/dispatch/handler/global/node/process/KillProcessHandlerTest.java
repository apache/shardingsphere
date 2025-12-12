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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.process;

import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(ProcessOperationLockRegistry.class)
class KillProcessHandlerTest {
    
    private GlobalDataChangedEventHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId()).thenReturn("foo_instance_id");
        handler = ShardingSphereServiceLoader.getServiceInstances(GlobalDataChangedEventHandler.class).stream()
                .filter(each -> "/nodes/compute_nodes/kill_process_trigger".equals(NodePathGenerator.toPath(each.getSubscribedNodePath()))).findFirst().orElse(null);
    }
    
    @Test
    void assertHandleWithInvalidKillProcessListTriggerEventKey() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/foo_instance_id", "", Type.DELETED));
        verify(ProcessOperationLockRegistry.getInstance(), never()).notify(any());
    }
    
    @Test
    void assertHandleKillLocalProcessWithCurrentInstance() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid", "", Type.ADDED));
        verify(contextManager.getPersistServiceFacade().getRepository()).delete(any());
    }
    
    @Test
    void assertHandleKillLocalProcessWithNotCurrentInstance() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/bar_instance_id:foo_pid", "", Type.ADDED));
        verify(contextManager.getPersistServiceFacade().getRepository(), never()).delete(any());
    }
    
    @Test
    void assertHandleCompleteToKillLocalProcess() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid", "", Type.DELETED));
        verify(ProcessOperationLockRegistry.getInstance()).notify("foo_pid");
    }
}

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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ReportLocalProcessesCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ReportLocalProcessesEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings({ProcessRegistry.class, ProcessOperationLockRegistry.class})
class ProcessListChangedSubscriberTest {
    
    private ProcessListChangedSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getPersistServiceFacade().getRepository()).thenReturn(mock(ClusterPersistRepository.class));
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId()).thenReturn("foo_instance_id");
        subscriber = new ProcessListChangedSubscriber(contextManager);
    }
    
    @Test
    void assertReportLocalProcessesWithNotCurrentInstance() {
        subscriber.reportLocalProcesses(new ReportLocalProcessesEvent("bar_instance_id", "foo_task_id"));
        verify(contextManager.getPersistServiceFacade().getRepository(), times(0)).delete(any());
    }
    
    @Test
    void assertReportEmptyLocalProcesses() {
        when(ProcessRegistry.getInstance().listAll()).thenReturn(Collections.emptyList());
        subscriber.reportLocalProcesses(new ReportLocalProcessesEvent("foo_instance_id", "foo_task_id"));
        verify(contextManager.getPersistServiceFacade().getRepository(), times(0)).persist(any(), any());
        verify(contextManager.getPersistServiceFacade().getRepository()).delete("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_task_id");
    }
    
    @Test
    void assertReportNotEmptyLocalProcesses() {
        when(ProcessRegistry.getInstance().listAll()).thenReturn(Collections.singleton(mock(Process.class, RETURNS_DEEP_STUBS)));
        subscriber.reportLocalProcesses(new ReportLocalProcessesEvent("foo_instance_id", "foo_task_id"));
        verify(contextManager.getPersistServiceFacade().getRepository()).persist(eq("/execution_nodes/foo_task_id/foo_instance_id"), any());
        verify(contextManager.getPersistServiceFacade().getRepository()).delete("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_task_id");
    }
    
    @Test
    void assertCompleteToReportLocalProcesses() {
        subscriber.completeToReportLocalProcesses(new ReportLocalProcessesCompletedEvent("foo_task_id"));
        verify(ProcessOperationLockRegistry.getInstance()).notify("foo_task_id");
    }
    
    @Test
    void assertKillLocalProcessWithNotCurrentInstance() throws SQLException {
        subscriber.killLocalProcess(new KillLocalProcessEvent("bar_instance_id", "foo_pid"));
        verify(contextManager.getPersistServiceFacade().getRepository(), times(0)).delete(any());
    }
    
    @Test
    void assertKillLocalProcessWithoutExistedProcess() throws SQLException {
        when(ProcessRegistry.getInstance().get("foo_pid")).thenReturn(null);
        subscriber.killLocalProcess(new KillLocalProcessEvent("foo_instance_id", "foo_pid"));
        verify(contextManager.getPersistServiceFacade().getRepository()).delete("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid");
    }
    
    @Test
    void assertKillLocalProcessWithExistedProcess() throws SQLException {
        Process process = mock(Process.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        when(process.getProcessStatements()).thenReturn(Collections.singletonMap(1, statement));
        when(ProcessRegistry.getInstance().get("foo_pid")).thenReturn(process);
        subscriber.killLocalProcess(new KillLocalProcessEvent("foo_instance_id", "foo_pid"));
        verify(process).setInterrupted(true);
        verify(statement).cancel();
        verify(contextManager.getPersistServiceFacade().getRepository()).delete("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid");
    }
    
    @Test
    void assertCompleteToKillLocalProcess() {
        subscriber.completeToKillLocalProcess(new KillLocalProcessCompletedEvent("foo_pid"));
        verify(ProcessOperationLockRegistry.getInstance()).notify("foo_pid");
    }
}

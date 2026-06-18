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

package org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.process;

import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProcessRegistry.class)
class ClusterProcessPersistCoordinatorTest {
    
    @Mock
    private PersistRepository repository;
    
    private ClusterProcessPersistCoordinator processPersistCoordinator;
    
    @BeforeEach
    void setUp() {
        processPersistCoordinator = new ClusterProcessPersistCoordinator(repository);
    }
    
    @Test
    void assertReportEmptyLocalProcesses() {
        when(ProcessRegistry.getInstance().listAll()).thenReturn(Collections.emptyList());
        processPersistCoordinator.reportLocalProcesses("foo_instance_id", "foo_task_id");
        verify(repository, never()).persist(any(), any());
        verify(repository).delete("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_task_id");
    }
    
    @Test
    void assertReportNotEmptyLocalProcesses() {
        when(ProcessRegistry.getInstance().listAll()).thenReturn(Collections.singleton(mock(Process.class, RETURNS_DEEP_STUBS)));
        processPersistCoordinator.reportLocalProcesses("foo_instance_id", "foo_task_id");
        verify(repository).persist(eq("/execution_nodes/foo_task_id/foo_instance_id"), any());
        verify(repository).delete("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_task_id");
    }
    
    @Test
    void assertCleanProcess() {
        processPersistCoordinator.cleanProcess("foo_instance_id", "foo_pid");
        verify(repository).delete("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid");
    }
}

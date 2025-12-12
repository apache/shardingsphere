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

package org.apache.shardingsphere.mode.manager.cluster.persist.service;

import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcess;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessList;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProcessRegistry.class, ProcessOperationLockRegistry.class})
class ClusterProcessPersistServiceTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    private ClusterProcessPersistService processPersistService;
    
    @BeforeEach
    void setUp() {
        processPersistService = new ClusterProcessPersistService(repository);
    }
    
    @Test
    void assertGetCompletedProcessList() {
        when(ProcessOperationLockRegistry.getInstance().waitUntilReleaseReady(any(), anyInt(), any())).thenReturn(true);
        assertGetProcessList();
        verify(repository, never()).delete(contains("/nodes/compute_nodes/show_process_list_trigger/abc:"));
    }
    
    @Test
    void assertGetUncompletedProcessList() {
        assertGetProcessList();
        verify(repository).delete(contains("/nodes/compute_nodes/show_process_list_trigger/abc:"));
    }
    
    private void assertGetProcessList() {
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/jdbc")).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/proxy")).thenReturn(Collections.singletonList("abc"));
        when(repository.getChildrenKeys(contains("/execution_nodes/"))).thenReturn(Collections.singletonList("abc"));
        when(repository.query(contains("/execution_nodes/"))).thenReturn(YamlEngine.marshal(createYamlProcessList()));
        Collection<Process> actual = processPersistService.getProcessList();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getId(), is("foo_process_id"));
        verify(repository).persist(contains("/nodes/compute_nodes/show_process_list_trigger/abc:"), eq(""));
        verify(repository).delete(contains("/execution_nodes/"));
    }
    
    private static YamlProcessList createYamlProcessList() {
        YamlProcessList result = new YamlProcessList();
        YamlProcess yamlProcess = new YamlProcess();
        yamlProcess.setId("foo_process_id");
        yamlProcess.setStartMillis(100L);
        result.getProcesses().add(yamlProcess);
        return result;
    }
    
    @Test
    void assertKillCompletedProcess() {
        when(ProcessOperationLockRegistry.getInstance().waitUntilReleaseReady(any(), anyInt(), any())).thenReturn(true);
        assertKillProcess();
        verify(repository, never()).delete("/nodes/compute_nodes/kill_process_trigger/abc:foo_process_id");
    }
    
    @Test
    void assertKillUncompletedProcess() {
        assertKillProcess();
        verify(repository).delete("/nodes/compute_nodes/kill_process_trigger/abc:foo_process_id");
    }
    
    private void assertKillProcess() {
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/jdbc")).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/proxy")).thenReturn(Collections.singletonList("abc"));
        processPersistService.killProcess("foo_process_id");
        verify(repository).persist("/nodes/compute_nodes/kill_process_trigger/abc:foo_process_id", "");
    }
}

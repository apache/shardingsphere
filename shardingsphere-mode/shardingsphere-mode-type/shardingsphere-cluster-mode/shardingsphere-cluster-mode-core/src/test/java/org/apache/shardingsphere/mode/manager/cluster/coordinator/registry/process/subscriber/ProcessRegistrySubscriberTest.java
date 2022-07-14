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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber;

import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProcessRegistrySubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    private ProcessRegistrySubscriber processRegistrySubscriber;
    
    @Mock
    private ShowProcessListManager showProcessListManager;
    
    @Before
    public void setUp() {
        processRegistrySubscriber = new ProcessRegistrySubscriber(repository, eventBusContext);
    }
    
    @Test
    public void assertLoadShowProcessListData() {
        when(repository.getChildrenKeys(ComputeNode.getOnlineNodePath(InstanceType.JDBC))).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys(ComputeNode.getOnlineNodePath(InstanceType.PROXY))).thenReturn(Collections.singletonList("abc"));
        when(repository.get(any())).thenReturn(null);
        ShowProcessListRequestEvent showProcessListRequestEvent = mock(ShowProcessListRequestEvent.class);
        processRegistrySubscriber.loadShowProcessListData(showProcessListRequestEvent);
        verify(repository, times(1)).persist(any(), any());
    }
    
    @Test
    public void assertReportExecuteProcessSummary() {
        try (MockedStatic<ShowProcessListManager> mockedStatic = mockStatic(ShowProcessListManager.class)) {
            mockedStatic.when(ShowProcessListManager::getInstance).thenReturn(showProcessListManager);
            ExecuteProcessContext executeProcessContext = mock(ExecuteProcessContext.class);
            ExecuteProcessSummaryReportEvent event = mock(ExecuteProcessSummaryReportEvent.class);
            when(event.getExecuteProcessContext()).thenReturn(executeProcessContext);
            when(executeProcessContext.getExecutionID()).thenReturn("id");
            processRegistrySubscriber.reportExecuteProcessSummary(event);
            verify(showProcessListManager, times(1)).putProcessContext(any(), any());
        }
    }
    
    @Test
    public void assertReportExecuteProcessUnit() {
        try (MockedStatic<ShowProcessListManager> mockedStatic = mockStatic(ShowProcessListManager.class)) {
            mockedStatic.when(ShowProcessListManager::getInstance).thenReturn(showProcessListManager);
            ExecuteProcessUnitReportEvent event = mock(ExecuteProcessUnitReportEvent.class);
            when(event.getExecutionID()).thenReturn("id");
            YamlExecuteProcessContext context = mockYamlExecuteProcessContext();
            when(showProcessListManager.getProcessContext(event.getExecutionID())).thenReturn(context);
            ExecuteProcessUnit unit = mockExecuteProcessUnit();
            when(event.getExecuteProcessUnit()).thenReturn(unit);
            processRegistrySubscriber.reportExecuteProcessUnit(event);
            assertThat(context.getUnitStatuses().iterator().next().getStatus(), is(ExecuteProcessConstants.EXECUTE_STATUS_DONE));
        }
    }
    
    @Test
    public void assertReportExecuteProcess() {
        try (MockedStatic<ShowProcessListManager> mockedStatic = mockStatic(ShowProcessListManager.class)) {
            mockedStatic.when(ShowProcessListManager::getInstance).thenReturn(showProcessListManager);
            ExecuteProcessReportEvent event = mock(ExecuteProcessReportEvent.class);
            when(showProcessListManager.getProcessContext(any())).thenReturn(mock(YamlExecuteProcessContext.class));
            processRegistrySubscriber.reportExecuteProcess(event);
            verify(showProcessListManager, times(1)).removeProcessContext(any());
        }
    }
    
    private YamlExecuteProcessContext mockYamlExecuteProcessContext() {
        YamlExecuteProcessUnit yamlExecuteProcessUnit = new YamlExecuteProcessUnit();
        yamlExecuteProcessUnit.setUnitID("159917166");
        yamlExecuteProcessUnit.setStatus(ExecuteProcessConstants.EXECUTE_STATUS_START);
        Collection<YamlExecuteProcessUnit> unitStatuses = Collections.singletonList(yamlExecuteProcessUnit);
        YamlExecuteProcessContext result = new YamlExecuteProcessContext();
        result.setUnitStatuses(unitStatuses);
        return result;
    }
    
    private ExecuteProcessUnit mockExecuteProcessUnit() {
        ExecuteProcessUnit result = mock(ExecuteProcessUnit.class);
        when(result.getUnitID()).thenReturn("159917166");
        when(result.getStatus()).thenReturn(ExecuteProcessConstants.EXECUTE_STATUS_DONE);
        return result;
    }
}

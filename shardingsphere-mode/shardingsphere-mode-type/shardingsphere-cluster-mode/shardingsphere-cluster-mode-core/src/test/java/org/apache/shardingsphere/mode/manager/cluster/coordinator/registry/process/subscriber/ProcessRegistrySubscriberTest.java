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

import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessReportContext;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProcessRegistrySubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @InjectMocks
    private ProcessRegistrySubscriber processRegistrySubscriber;
    
    @Test
    public void assertLoadShowProcessListData() {
        ShowProcessListRequestEvent showProcessListRequestEvent = mock(ShowProcessListRequestEvent.class);
        when(repository.getChildrenKeys(any())).thenReturn(Collections.singletonList("abc"));
        when(repository.get(any())).thenReturn("abc");
        processRegistrySubscriber.loadShowProcessListData(showProcessListRequestEvent);
        verify(repository, times(1)).get(any());
    }
    
    @Test
    public void assertReportExecuteProcessSummary() {
        ExecuteProcessSummaryReportEvent event = mock(ExecuteProcessSummaryReportEvent.class);
        when(event.getExecutionID()).thenReturn("id");
        Map<String, Object> dataMap = mockDataMap();
        when(event.getDataMap()).thenReturn(dataMap);
        processRegistrySubscriber.reportExecuteProcessSummary(event);
        verify(event, times(1)).getDataMap();
    }
    
    // TODO FIX ME!
    @Ignore
    @Test
    public void assertReportExecuteProcessSummaryWithId() {
        ExecutionGroupContext executionGroupContext = mock(ExecutionGroupContext.class);
        when(executionGroupContext.getExecutionID()).thenReturn("id");
        ExecuteProcessReportContext executeProcessReportContext = new ExecuteProcessReportContext("id", 16);
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext("sql1", executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START);
        executeProcessReportContext.setYamlExecuteProcessContext(new YamlExecuteProcessContext(executeProcessContext));
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put(ExecuteProcessConstants.EXECUTE_ID.name(), executeProcessReportContext);
        ExecuteProcessSummaryReportEvent event = new ExecuteProcessSummaryReportEvent("id", dataMap);
        ProcessRegistrySubscriber subscriber = new ProcessRegistrySubscriber(repository);
        subscriber.reportExecuteProcessSummary(event);
        verify(repository).persist("/execution_nodes/id", YamlEngine.marshal(new YamlExecuteProcessContext(executeProcessContext)));
    }
    
    @Test
    public void assertReportExecuteProcessUnit() {
        ExecuteProcessUnitReportEvent event = mock(ExecuteProcessUnitReportEvent.class);
        Map<String, Object> dataMap = mockDataMap();
        when(event.getDataMap()).thenReturn(dataMap);
        when(event.getExecutionID()).thenReturn("id");
        when(event.getExecuteProcessUnit()).thenReturn(mockExecuteProcessUnit());
        processRegistrySubscriber.reportExecuteProcessUnit(event);
        verify(event, times(1)).getDataMap();
        verify(event, times(1)).getExecuteProcessUnit();
    }
    
    @Test
    public void assertReportExecuteProcess() {
        ExecuteProcessReportEvent event = mock(ExecuteProcessReportEvent.class);
        when(event.getExecutionID()).thenReturn("id");
        Map<String, Object> dataMap = mockDataMap();
        when(event.getDataMap()).thenReturn(dataMap);
        processRegistrySubscriber.reportExecuteProcess(event);
        verify(event, times(1)).getDataMap();
    }
    
    private ExecuteProcessUnit mockExecuteProcessUnit() {
        ExecutionUnit executionUnit = mock(ExecutionUnit.class);
        return new ExecuteProcessUnit(executionUnit, ExecuteProcessConstants.EXECUTE_STATUS_DONE);
    }
    
    private Map<String, Object> mockDataMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(ExecuteProcessConstants.EXECUTE_ID.name(), mockExecuteProcessReportContext());
        return result;
    }
    
    private ExecuteProcessReportContext mockExecuteProcessReportContext() {
        ExecuteProcessReportContext executeProcessReportContext = new ExecuteProcessReportContext("id", 16);
        YamlExecuteProcessContext yamlExecuteProcessContext = mock(YamlExecuteProcessContext.class);
        when(yamlExecuteProcessContext.getStartTimeMillis()).thenReturn(System.currentTimeMillis());
        executeProcessReportContext.setYamlExecuteProcessContext(yamlExecuteProcessContext);
        return executeProcessReportContext;
    }
}

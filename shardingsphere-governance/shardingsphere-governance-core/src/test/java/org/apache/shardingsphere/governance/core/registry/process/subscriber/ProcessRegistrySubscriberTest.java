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

package org.apache.shardingsphere.governance.core.registry.process.subscriber;

import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public final class ProcessRegistrySubscriberTest {
    
    @Mock
    private RegistryCenterRepository repository;
    
    @InjectMocks
    private ProcessRegistrySubscriber processRegistrySubscriber;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void assertLoadShowProcessListData() {
        ShowProcessListRequestEvent showProcessListRequestEvent = mock(ShowProcessListRequestEvent.class);
        Mockito.when(repository.getChildrenKeys(any())).thenReturn(Collections.singletonList("abc"));
        Mockito.when(repository.get(any())).thenReturn("abc");
        processRegistrySubscriber.loadShowProcessListData(showProcessListRequestEvent);
        Mockito.verify(repository, times(1)).get(any());
    }
    
    @Test
    public void assertReportExecuteProcessSummary() {
        ExecuteProcessContext executeProcessContext = mock(ExecuteProcessContext.class);
        ExecuteProcessSummaryReportEvent event = mock(ExecuteProcessSummaryReportEvent.class);
        Mockito.when(event.getExecuteProcessContext()).thenReturn(executeProcessContext);
        Mockito.when(executeProcessContext.getExecutionID()).thenReturn("id");
        processRegistrySubscriber.reportExecuteProcessSummary(event);
        Mockito.verify(event, times(1)).getExecuteProcessContext();
        Mockito.verify(repository, times(1)).persist(anyString(), any());
    }
    
    @Test
    public void assertReportExecuteProcessSummaryWithId() {
        ExecutionGroupContext executionGroupContext = mock(ExecutionGroupContext.class);
        Mockito.when(executionGroupContext.getExecutionID()).thenReturn("id");
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext("sql1", executionGroupContext, ExecuteProcessConstants.EXECUTE_STATUS_START);
        ExecuteProcessSummaryReportEvent event = new ExecuteProcessSummaryReportEvent(executeProcessContext);
        ProcessRegistrySubscriber subscriber = new ProcessRegistrySubscriber(repository);
        subscriber.reportExecuteProcessSummary(event);
        Mockito.verify(repository).persist("/executionnodes/id", YamlEngine.marshal(new YamlExecuteProcessContext(executeProcessContext)));
    }
    
    @Test
    public void assertReportExecuteProcessUnit() {
        ExecuteProcessUnitReportEvent event = mock(ExecuteProcessUnitReportEvent.class);
        Mockito.when(event.getExecutionID()).thenReturn("id");
        Mockito.when(repository.get(anyString())).thenReturn(mockYamlExecuteProcessContext());
        Mockito.when(event.getExecuteProcessUnit()).thenReturn(mockExecuteProcessUnit());
        processRegistrySubscriber.reportExecuteProcessUnit(event);
        Mockito.verify(repository, times(1)).persist(any(), any());
    }
    
    @Test
    public void assertReportExecuteProcess() {
        ExecuteProcessReportEvent event = mock(ExecuteProcessReportEvent.class);
        Mockito.when(event.getExecutionID()).thenReturn("id");
        Mockito.when(repository.get(anyString())).thenReturn(mockYamlExecuteProcessContext());
        processRegistrySubscriber.reportExecuteProcess(event);
        Mockito.verify(repository, times(1)).delete(any());
    }
    
    private String mockYamlExecuteProcessContext() {
        YamlExecuteProcessUnit yamlExecuteProcessUnit = new YamlExecuteProcessUnit();
        yamlExecuteProcessUnit.setUnitID("159917166");
        yamlExecuteProcessUnit.setStatus(ExecuteProcessConstants.EXECUTE_STATUS_DONE);
        Collection<YamlExecuteProcessUnit> unitStatuses = Collections.singleton(yamlExecuteProcessUnit);
        YamlExecuteProcessContext yamlExecuteProcessContext = new YamlExecuteProcessContext();
        yamlExecuteProcessContext.setUnitStatuses(unitStatuses);
        return YamlEngine.marshal(yamlExecuteProcessContext);
    }
    
    private ExecuteProcessUnit mockExecuteProcessUnit() {
        ExecutionUnit executionUnit = mock(ExecutionUnit.class);
        return new ExecuteProcessUnit(executionUnit, ExecuteProcessConstants.EXECUTE_STATUS_DONE);
    }
}

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

package org.apache.shardingsphere.mode.process;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class GovernanceExecuteProcessReporterTest {
    
    private MockedStatic<ShowProcessListManager> mockedStatic;
    
    private ShowProcessListManager showProcessListManager;
    
    private final GovernanceExecuteProcessReporter reporter = new GovernanceExecuteProcessReporter();
    
    @Before
    public void setUp() {
        mockedStatic = mockStatic(ShowProcessListManager.class);
        showProcessListManager = mock(ShowProcessListManager.class);
        mockedStatic.when(ShowProcessListManager::getInstance).thenReturn(showProcessListManager);
    }
    
    @Test
    public void assertReport() {
        QueryContext queryContext = new QueryContext(null, null, null);
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = mockExecutionGroupContext();
        reporter.report(queryContext, executionGroupContext, ExecuteProcessConstants.EXECUTE_ID, EventBusContextHolderFixture.EVENT_BUS_CONTEXT);
        verify(showProcessListManager, times(1)).putProcessContext(eq(executionGroupContext.getExecutionID()), any());
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionGroupContext<? extends SQLExecutionUnit> mockExecutionGroupContext() {
        ExecutionGroupContext<? extends SQLExecutionUnit> result = mock(ExecutionGroupContext.class);
        when(result.getExecutionID()).thenReturn(UUID.randomUUID().toString());
        return result;
    }
    
    @Test
    public void assertReportUnit() {
        SQLExecutionUnit sqlExecutionUnit = mock(SQLExecutionUnit.class);
        ExecutionUnit executionUnit = mock(ExecutionUnit.class);
        when(sqlExecutionUnit.getExecutionUnit()).thenReturn(executionUnit);
        YamlExecuteProcessContext yamlExecuteProcessContext = mock(YamlExecuteProcessContext.class);
        when(yamlExecuteProcessContext.getUnitStatuses()).thenReturn(Collections.emptyList());
        when(showProcessListManager.getProcessContext("foo_id")).thenReturn(yamlExecuteProcessContext);
        reporter.report("foo_id", sqlExecutionUnit, ExecuteProcessConstants.EXECUTE_ID, EventBusContextHolderFixture.EVENT_BUS_CONTEXT);
        verify(showProcessListManager, times(1)).getProcessContext(eq("foo_id"));
    }
    
    @Test
    public void assertReportComplete() {
        YamlExecuteProcessContext yamlExecuteProcessContext = mock(YamlExecuteProcessContext.class);
        when(yamlExecuteProcessContext.getUnitStatuses()).thenReturn(Collections.emptyList());
        when(showProcessListManager.getProcessContext("foo_id")).thenReturn(yamlExecuteProcessContext);
        reporter.report("foo_id", ExecuteProcessConstants.EXECUTE_STATUS_DONE, EventBusContextHolderFixture.EVENT_BUS_CONTEXT);
        verify(showProcessListManager, times(1)).getProcessContext(eq("foo_id"));
        verify(showProcessListManager, times(1)).removeProcessContext(eq("foo_id"));
    }
    
    @Test
    public void assertReportClean() {
        reporter.reportClean("foo_id");
        verify(showProcessListManager, times(1)).removeProcessContext(eq("foo_id"));
    }
    
    @After
    public void tearDown() {
        mockedStatic.close();
    }
}

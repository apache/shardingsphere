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

package org.apache.shardingsphere.infra.executor.sql.process;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessStatusEnum;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShowProcessListManager.class)
public final class ExecuteProcessReporterTest {
    
    @Mock
    private ShowProcessListManager showProcessListManager;
    
    @BeforeEach
    public void setUp() {
        when(ShowProcessListManager.getInstance()).thenReturn(showProcessListManager);
    }
    
    @Test
    public void assertReport() {
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = mockExecutionGroupContext();
        new ExecuteProcessReporter().report(new QueryContext(null, null, null), executionGroupContext, ExecuteProcessStatusEnum.START);
        verify(showProcessListManager).putProcessContext(eq(executionGroupContext.getReportContext().getExecutionID()), any());
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionGroupContext<? extends SQLExecutionUnit> mockExecutionGroupContext() {
        ExecutionGroupContext<? extends SQLExecutionUnit> result = mock(ExecutionGroupContext.class);
        ExecutionGroupReportContext reportContext = mock(ExecutionGroupReportContext.class);
        when(reportContext.getExecutionID()).thenReturn(UUID.randomUUID().toString());
        when(result.getReportContext()).thenReturn(reportContext);
        return result;
    }
    
    @Test
    public void assertReportUnit() {
        SQLExecutionUnit sqlExecutionUnit = mock(SQLExecutionUnit.class);
        when(sqlExecutionUnit.getExecutionUnit()).thenReturn(mock(ExecutionUnit.class));
        when(showProcessListManager.getProcessContext("foo_id")).thenReturn(mock(ExecuteProcessContext.class));
        new ExecuteProcessReporter().report("foo_id", sqlExecutionUnit, ExecuteProcessStatusEnum.DONE);
        verify(showProcessListManager).getProcessContext("foo_id");
    }
    
    @Test
    public void assertReportClean() {
        when(showProcessListManager.getProcessContext("foo_id")).thenReturn(mock(ExecuteProcessContext.class));
        new ExecuteProcessReporter().reportClean("foo_id");
        verify(showProcessListManager).removeProcessStatement("foo_id");
    }
}

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

package org.apache.shardingsphere.governance.context.process;

import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceExecuteProcessReporterTest {

    @Mock
    private ShardingSphereEventBus shardingSphereEventBus;

    @Test
    public void assertReport() {
        ExecuteProcessReporter executeProcessReporter = new GovernanceExecuteProcessReporter(shardingSphereEventBus);
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(ExecuteProcessConstants.EXECUTE_PROCESS_TYPE, "ds_0", "schema_0", "table_0", "sql_0");
        ExecuteProcessUnit executeProcessUnit = new ExecuteProcessUnit(ExecuteProcessConstants.EXECUTE_PROCESS_TYPE, "ds_0", "schema_0", "table_0", "sql_0");
        ExecuteProcessReportEvent executeProcessReportEvent = new ExecuteProcessReportEvent(executeProcessContext, executeProcessUnit, executionGroupContext);
        executeProcessReporter.report(executeProcessReportEvent);
        verify(shardingSphereEventBus).post(any(ExecuteProcessSummaryReportEvent.class));
        verify(shardingSphereEventBus).post(any(ExecuteProcessUnitReportEvent.class));
    }

    @Test
    public void assertReportWithLogicSQL() {
        ExecuteProcessReporter executeProcessReporter = new GovernanceExecuteProcessReporter(shardingSphereEventBus);
        ExecutionGroupContext executionGroupContext = new ExecutionGroupContext(ExecuteProcessConstants.EXECUTE_PROCESS_TYPE, "ds_0", "schema_0", "table_0", "sql_0");
        ExecuteProcessReportEvent executeProcessReportEvent = new ExecuteProcessReportEvent(executeProcessContext, executionGroupContext);
        executeProcessReporter.report(executeProcessReportEvent);
        verify(shardingSphereEventBus).post(any(ExecuteProcessSummaryReportEvent.class));
        verify(shardingSphereEventBus).post(any(ExecuteProcessUnitReportEvent.class));
        verify(shardingSphereEventBus).post(any(LogicSQL.class));
    }
    
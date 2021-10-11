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

package org.apache.shardingsphere.mode.manager.cluster.process;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.event.ExecuteProcessUnitReportEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceExecuteProcessReporterTest {

    private final GovernanceExecuteProcessReporter governanceExecuteProcessReporter = new GovernanceExecuteProcessReporter();

    private String str = "";

    private ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext;

    private SQLExecutionUnit executionUnit = new SQLExecutionUnit() {
        @Override
        public ExecutionUnit getExecutionUnit() {
            return new ExecutionUnit("", null);
        }

        @Override
        public ConnectionMode getConnectionMode() {
            return null;
        }
    };

    @Before
    public void setUp() {
        ShardingSphereEventBus.getInstance().register(this);
        executionGroupContext = createMockedExecutionGroups();
    }

    /**
     * onExecuteProcessSummaryReportEvent.
     * @param executeProcessSummaryReportEvent An event.
     */
    @Subscribe
    public void onExecuteProcessSummaryReportEvent(final ExecuteProcessSummaryReportEvent executeProcessSummaryReportEvent) {
        ExecuteProcessContext executeProcessContext = executeProcessSummaryReportEvent.getExecuteProcessContext();
        str = executeProcessContext.getExecutionID();
    }

    /**
     * onExecuteProcessUnitReportEvent.
     * @param executeProcessUnitReportEvent An event.
     */
    @Subscribe
    public void onExecuteProcessUnitReportEvent(final ExecuteProcessUnitReportEvent executeProcessUnitReportEvent) {
        str = executeProcessUnitReportEvent.getExecutionID();
    }

    /**
     * onExecuteProcessReportEvent.
     * @param executeProcessReportEvent An event.
     */
    @Subscribe
    public void onExecuteProcessReportEvent(final ExecuteProcessReportEvent executeProcessReportEvent) {
        str = executeProcessReportEvent.getExecutionID();
    }

    @Test
    public void assertReport() {
        ExecuteProcessConstants constants = ExecuteProcessConstants.EXECUTE_ID;
        LogicSQL logicSQL = new LogicSQL(null, null, null);
        ExecuteProcessContext executeProcessContext = new ExecuteProcessContext(logicSQL.getSql(), executionGroupContext, constants);

        governanceExecuteProcessReporter.report(logicSQL, executionGroupContext, constants);
        assertTrue(str.equals(executeProcessContext.getExecutionID()));
        governanceExecuteProcessReporter.report("TEST", executionUnit, constants);
        assertFalse(str.equals(executeProcessContext.getExecutionID()));
        assertTrue("TEST".equals(str));
        governanceExecuteProcessReporter.report("TEST1", constants);
        assertFalse(str.equals(executeProcessContext.getExecutionID()));
        assertFalse("TEST".equals(str));
        assertTrue("TEST1".equals(str));
    }

    private ExecutionGroupContext<? extends SQLExecutionUnit> createMockedExecutionGroups() {
        ExecutionGroupContext<? extends SQLExecutionUnit> result = mock(ExecutionGroupContext.class);
        when(result.getExecutionID()).thenReturn(UUID.randomUUID().toString());
        return result;
    }
}

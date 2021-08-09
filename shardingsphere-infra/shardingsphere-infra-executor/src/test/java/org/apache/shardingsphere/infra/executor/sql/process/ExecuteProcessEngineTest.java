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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.fixture.ExecuteProcessReporterFixture;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExecuteProcessEngineTest {

    private ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext;

    @Before
    public void setUp() {
        LogicSQL logicSQL = createLogicSQL();
        executionGroupContext = createMockedExecutionGroups();
        ConfigurationProperties actual = createConfigurationProperties();
        ExecuteProcessEngine.initialize(logicSQL, executionGroupContext, actual);
        assertThat(ExecutorDataMap.getValue().get("EXECUTE_ID"), is(executionGroupContext.getExecutionID()));
        assertThat(ExecuteProcessReporterFixture.ACTIONS.get(0), is("Report the summary of this task."));
    }

    @Test
    public void assertFinish() {
        ExecutionUnit executionUnit = new ExecutionUnit("actualName1", new SQLUnit("sql1", Collections.singletonList("parameter1")));
        RawSQLExecutionUnit rawExecutionUnit = new RawSQLExecutionUnit(executionUnit, ConnectionMode.MEMORY_STRICTLY);
        ExecuteProcessEngine.finish(executionGroupContext.getExecutionID(), rawExecutionUnit);
        assertThat(ExecuteProcessReporterFixture.ACTIONS.get(1), is("Report a unit of this task."));
        ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
        assertThat(ExecuteProcessReporterFixture.ACTIONS.get(2), is("Report this task on completion."));
    }

    @Test
    public void assertClean() {
        ExecuteProcessEngine.clean();
        assertThat(ExecutorDataMap.getValue().size(), is(0));
    }

    private LogicSQL createLogicSQL() {
        DDLStatement ddlStatement = mock(DDLStatement.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(ddlStatement);
        LogicSQL result = mock(LogicSQL.class);
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        return result;
    }

    private ConfigurationProperties createConfigurationProperties() {
        ConfigurationProperties result = mock(ConfigurationProperties.class);
        when(result.getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(Boolean.TRUE);
        when(result.getValue(ConfigurationPropertyKey.SHOW_PROCESS_LIST_ENABLED)).thenReturn(Boolean.TRUE);
        return result;
    }

    private ExecutionGroupContext<? extends SQLExecutionUnit> createMockedExecutionGroups() {
        ExecutionGroupContext<? extends SQLExecutionUnit> result = mock(ExecutionGroupContext.class);
        String uuid = UUID.randomUUID().toString();
        when(result.getExecutionID()).thenReturn(uuid);
        return result;
    }

}

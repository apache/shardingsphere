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
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.fixture.ExecuteProcessReporterFixture;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.junit.Before;
import org.junit.Test;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExecuteProcessEngineTest {
    
    private ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext;
    
    @Before
    public void setUp() {
        executionGroupContext = createMockedExecutionGroups();
        ExecuteProcessEngine.initialize(createLogicSQL(), executionGroupContext, createConfigurationProperties());
        assertThat(ExecutorDataMap.getValue().get("EXECUTE_ID"), is(executionGroupContext.getExecutionID()));
        assertThat(ExecuteProcessReporterFixture.ACTIONS.get(0), is("Report the summary of this task."));
    }
    
    @Test
    public void assertFinish() {
        ExecuteProcessEngine.finish(executionGroupContext.getExecutionID(), mock(RawSQLExecutionUnit.class));
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
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
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
        when(result.getExecutionID()).thenReturn(UUID.randomUUID().toString());
        return result;
    }
}

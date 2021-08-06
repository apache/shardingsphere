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
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.fixture.ExecuteProcessReporterFixture;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.LinkedList;

import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class ExecuteProcessEngineTest {

    private ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext;

    @Before
    public void setUp() {
        LogicSQL logicSQL = createLogicSQL();
        executionGroupContext = createMockedExecutionGroups(2, 2);
        ConfigurationProperties actual = createConfigurationProperties();
        ExecuteProcessEngine.initialize(logicSQL, executionGroupContext, actual);
        assertTrue(executionGroupContext.getExecutionID().equals(ExecutorDataMap.getValue().get("EXECUTE_ID")));
        assertTrue(ExecuteProcessReporterFixture.ACTIONS.get(0).equals("Report the summary of this task."));
    }

    @Test
    public void assertFinish() {
        ExecutionUnit executionUnit = new ExecutionUnit("actualName1", new SQLUnit("sql1", Collections.singletonList("parameter1")));
        RawSQLExecutionUnit rawExecutionUnit = new RawSQLExecutionUnit(executionUnit, ConnectionMode.MEMORY_STRICTLY);
        ExecuteProcessEngine.finish(executionGroupContext.getExecutionID(), rawExecutionUnit);
        assertTrue(ExecuteProcessReporterFixture.ACTIONS.get(1).equals("Report a unit of this task."));
        ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
        assertTrue(ExecuteProcessReporterFixture.ACTIONS.get(2).equals("Report this task on completion."));
    }

    @Test
    public void assertClean() {
        ExecuteProcessEngine.clean();
        assertTrue(ExecutorDataMap.getValue().size() == 0);
    }

    private LogicSQL createLogicSQL() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, null, null));
        selectStatement.setProjections(projectionsSegment);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(mockMetaDataMap(), Collections.emptyList(), selectStatement, DefaultSchema.LOGIC_NAME);
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, null, null);
        return logicSQL;
    }

    private ConfigurationProperties createConfigurationProperties() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        props.setProperty(ConfigurationPropertyKey.SHOW_PROCESS_LIST_ENABLED.getKey(), Boolean.TRUE.toString());
        ConfigurationProperties actual = new ConfigurationProperties(props);
        return actual;
    }

    private Map<String, ShardingSphereMetaData> mockMetaDataMap() {
        return Collections.singletonMap(DefaultSchema.LOGIC_NAME, mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
    }

    private ExecutionGroupContext<? extends SQLExecutionUnit> createMockedExecutionGroups(final int groupSize, final int unitSize) {
        Collection<ExecutionGroup<Object>> result = new LinkedList<>();
        for (int i = 0; i < groupSize; i++) {
            result.add(new ExecutionGroup<>(createMockedInputs(unitSize)));
        }
        return new ExecutionGroupContext(result);
    }

    private List<Object> createMockedInputs(final int size) {
        List<Object> result = new LinkedList<>();
        for (int j = 0; j < size; j++) {
            result.add(mock(Object.class));
        }
        return result;
    }
}

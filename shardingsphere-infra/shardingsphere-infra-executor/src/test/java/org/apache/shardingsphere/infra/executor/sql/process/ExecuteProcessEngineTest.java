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
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.executor.sql.process.fixture.ExecuteProcessReporterFixture;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class ExecuteProcessEngineTest {

    private static final String SQL = "SELECT * FROM t_user";
    private ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext;

    @Test
    public void assertExecuteIDCanBePutAndRemoved(){

        Map<String, Object> map = ExecutorDataMap.getValue();

        LogicSQL logicSQL = createLogicSQL();
        executionGroupContext = createMockedExecutionGroups(2, 2);
        ConfigurationProperties actual = createConfigurationProperties();

        ExecuteProcessEngine.initialize(logicSQL,executionGroupContext,actual);
        assertTrue(executionGroupContext.getExecutionID().equals(ExecutorDataMap.getValue().get("EXECUTE_ID")));
        assertTrue(ExecuteProcessReporterFixture.ACTIONS.get(0).equals("Report the summary of this task."));
        ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
        assertTrue(ExecuteProcessReporterFixture.ACTIONS.get(1).equals("Report this task on completion."));
        ExecuteProcessEngine.clean();
        assertTrue(ExecutorDataMap.getValue().size()==0);

        LogicSQL logicSQL_2 = createLogicSQL();
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext_2 = createMockedExecutionGroups(2,2);
        ConfigurationProperties actual_2 = createConfigurationProperties();

        ExecuteProcessEngine.initialize(logicSQL_2,executionGroupContext_2,actual_2);
        assertTrue(executionGroupContext_2.getExecutionID().equals(ExecutorDataMap.getValue().get("EXECUTE_ID")));
        assertTrue(ExecuteProcessReporterFixture.ACTIONS.get(2).equals("Report the summary of this task."));
        ExecuteProcessEngine.finish(executionGroupContext_2.getExecutionID());
        assertTrue(ExecuteProcessReporterFixture.ACTIONS.get(3).equals("Report this task on completion."));
        ExecuteProcessEngine.clean();
        assertTrue(ExecutorDataMap.getValue().size()==0);
    }

    private LogicSQL createLogicSQL(){
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, null, null));
        selectStatement.setProjections(projectionsSegment);

        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(mockMetaDataMap(), Collections.emptyList(), selectStatement, DefaultSchema.LOGIC_NAME);
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext,null,null);
        return logicSQL;
    }

    private ConfigurationProperties createConfigurationProperties(){
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        props.setProperty(ConfigurationPropertyKey.SHOW_PROCESS_LIST_ENABLED.getKey(),Boolean.TRUE.toString());
        ConfigurationProperties actual = new ConfigurationProperties(props);
        return actual;
    }

    private Map<String, ShardingSphereMetaData> mockMetaDataMap() {
        return Collections.singletonMap(DefaultSchema.LOGIC_NAME, mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
    }

    private Collection<ExecutionUnit> mockExecutionUnits(final Collection<String> dataSourceNames, final String sql) {
        return dataSourceNames.stream().map(each -> new ExecutionUnit(each, new SQLUnit(sql, new ArrayList<>()))).collect(Collectors.toList());
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

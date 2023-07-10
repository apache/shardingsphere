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

package org.apache.shardingsphere.driver.executor.batch;

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BatchPreparedStatementExecutorTest {
    
    private static final String SQL = "DELETE FROM table_x WHERE id=?";
    
    private final ExecutorEngine executorEngine = ExecutorEngine.createExecutorEngineWithCPU();
    
    private BatchPreparedStatementExecutor executor;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @BeforeEach
    void setUp() {
        SQLExecutorExceptionHandler.setExceptionThrown(true);
        ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager());
        executor = new BatchPreparedStatementExecutor(
                connection.getContextManager().getMetaDataContexts(), new JDBCExecutor(executorEngine, connection.getDatabaseConnectionManager().getConnectionContext()), "foo_db");
        when(sqlStatementContext.getTablesContext()).thenReturn(mock(TablesContext.class));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getDataSourceMap("foo_db")).thenReturn(mockDataSourceMap());
        return result;
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Arrays.asList(mockTransactionRule(), new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build())));
        when(result.getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageTypes())
                .thenReturn(Collections.singletonMap("ds_0", TypedSPILoader.getService(DatabaseType.class, "H2")));
        ShardingSphereRuleMetaData databaseRuleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mockShardingRule()));
        when(result.getMetaData().getDatabase("foo_db").getRuleMetaData()).thenReturn(databaseRuleMetaData);
        return result;
    }
    
    private TransactionRule mockTransactionRule() {
        return new TransactionRule(new TransactionRuleConfiguration(TransactionType.LOCAL.name(), "", new Properties()), Collections.emptyMap());
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.isNeedAccumulate(any())).thenReturn(true);
        return result;
    }
    
    private Map<String, DataSource> mockDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        result.put("ds_0", dataSource);
        result.put("ds_1", dataSource);
        return result;
    }
    
    @AfterEach
    void tearDown() {
        executorEngine.close();
    }
    
    @Test
    void assertNoPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.executeBatch()).thenReturn(new int[]{0, 0});
        setExecutionGroups(Collections.singletonList(preparedStatement));
        assertThat(executor.executeBatch(sqlStatementContext), is(new int[]{0, 0}));
    }
    
    @Test
    void assertExecuteBatchForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.executeBatch()).thenReturn(new int[]{10, 20});
        setExecutionGroups(Collections.singletonList(preparedStatement));
        assertThat(executor.executeBatch(sqlStatementContext), is(new int[]{10, 20}));
        verify(preparedStatement).executeBatch();
    }
    
    @Test
    void assertExecuteBatchForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.executeBatch()).thenReturn(new int[]{10, 20});
        when(preparedStatement2.executeBatch()).thenReturn(new int[]{20, 40});
        setExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2));
        assertThat(executor.executeBatch(sqlStatementContext), is(new int[]{30, 60}));
        verify(preparedStatement1).executeBatch();
        verify(preparedStatement2).executeBatch();
    }
    
    @Test
    void assertExecuteBatchForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement.executeBatch()).thenThrow(ex);
        setExecutionGroups(Collections.singletonList(preparedStatement));
        assertThrows(SQLException.class, () -> executor.executeBatch(sqlStatementContext));
        verify(preparedStatement).executeBatch();
    }
    
    @Test
    void assertExecuteBatchForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement1.executeBatch()).thenThrow(ex);
        setExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2));
        assertThrows(SQLException.class, () -> executor.executeBatch(sqlStatementContext));
    }
    
    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:h2:mem:primary_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        return result;
    }
    
    private void setExecutionGroups(final List<PreparedStatement> preparedStatements) {
        Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = new LinkedList<>();
        List<JDBCExecutionUnit> executionUnits = new LinkedList<>();
        executionGroups.add(new ExecutionGroup<>(executionUnits));
        Collection<BatchExecutionUnit> batchExecutionUnits = new LinkedList<>();
        for (PreparedStatement each : preparedStatements) {
            BatchExecutionUnit batchExecutionUnit = new BatchExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit(SQL, Collections.singletonList(1))));
            batchExecutionUnit.mapAddBatchCount(0);
            batchExecutionUnit.mapAddBatchCount(1);
            batchExecutionUnits.add(batchExecutionUnit);
            executionUnits.add(new JDBCExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit(SQL, Collections.singletonList(1))), ConnectionMode.MEMORY_STRICTLY, each));
        }
        setFields(executionGroups, batchExecutionUnits);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setFields(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, final Collection<BatchExecutionUnit> batchExecutionUnits) {
        Plugins.getMemberAccessor().set(BatchPreparedStatementExecutor.class.getDeclaredField("executionGroupContext"), executor, new ExecutionGroupContext<>(executionGroups,
                new ExecutionGroupReportContext("logic_db")));
        Plugins.getMemberAccessor().set(BatchPreparedStatementExecutor.class.getDeclaredField("batchExecutionUnits"), executor, batchExecutionUnits);
        Plugins.getMemberAccessor().set(BatchPreparedStatementExecutor.class.getDeclaredField("batchCount"), executor, 2);
    }
}

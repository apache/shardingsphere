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

package org.apache.shardingsphere.sqlfederation.engine;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.kernel.connection.SQLExecutionInterruptedException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationCompilerEngine;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.exception.SQLFederationUnsupportedSQLException;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.fixture.rule.SQLFederationDeciderRuleMatchFixture;
import org.apache.shardingsphere.sqlfederation.engine.fixture.rule.SQLFederationDeciderRuleNotMatchFixture;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessorFactory;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQLFederationEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Test
    void assertDecideWhenNotConfigSqlFederationEnabled() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(
                new SQLFederationRule(new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        assertFalse(engine.decide(mock(QueryContext.class), globalRuleMetaData));
        engine.close();
    }
    
    private SQLFederationEngine createSQLFederationEngine(final Collection<ShardingSphereRule> globalRules, final Collection<ShardingSphereRule> databaseRules) {
        when(metaData.getDatabase("foo_db").getRuleMetaData().getRules()).thenReturn(databaseRules);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(globalRules));
        return new SQLFederationEngine("foo_db", "foo_db", metaData, mock(ShardingSphereStatistics.class), mock(JDBCExecutor.class));
    }
    
    @Test
    void assertDecideWhenConfigAllQueryUseSQLFederation() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(
                new SQLFederationRule(new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        assertTrue(engine.decide(queryContext, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenExecuteNotSelectStatement() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(
                new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(CreateTableStatement.class));
        assertFalse(engine.decide(queryContext, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigSingleMatchedRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(
                new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Collections.singleton(new SQLFederationDeciderRuleMatchFixture());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new RuleMetaData(globalRules), Collections.emptyList());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getUsedDatabase()).thenReturn(database);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        assertTrue(engine.decide(queryContext, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigSingleNotMatchedRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(
                new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Collections.singleton(new SQLFederationDeciderRuleNotMatchFixture());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(), new RuleMetaData(databaseRules), Collections.emptyList());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getUsedDatabase()).thenReturn(database);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        assertFalse(engine.decide(queryContext, new RuleMetaData(globalRules)));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigMultiRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Arrays.asList(new SQLFederationDeciderRuleNotMatchFixture(),
                new SQLFederationDeciderRuleMatchFixture());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new RuleMetaData(databaseRules), Collections.emptyList());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(queryContext.getUsedDatabase()).thenReturn(database);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        assertTrue(engine.decide(queryContext, new RuleMetaData(globalRules)));
        engine.close();
    }
    
    @Test
    void assertDecideWithMultipleDatabases() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(new LinkedList<>(Arrays.asList("foo_db", "bar_db")));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        assertTrue(engine.decide(queryContext, new RuleMetaData(globalRules)));
        engine.close();
    }
    
    @Test
    void assertDecideWithExplainStatement() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(
                new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        ExplainStatementContext explainStatementContext = mock(ExplainStatementContext.class, RETURNS_DEEP_STUBS);
        ExplainStatement explainStatement = mock(ExplainStatement.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(explainStatement.getExplainableSQLStatement()).thenReturn(selectStatement);
        when(explainStatementContext.getSqlStatement()).thenReturn(explainStatement);
        when(explainStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(explainStatementContext);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        assertFalse(engine.decide(queryContext, new RuleMetaData(globalRules)));
        engine.close();
    }
    
    @Test
    void assertExecuteQueryWithLoggingAndRelease() throws SQLException {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1));
        ShardingSphereTable table = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData actualMetaData = createMetaData(config, Collections.emptyList(), Collections.singleton(table), TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), props);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        when(processor.getConvention()).thenReturn(mock(Convention.class));
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class);
        JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
        SQLFederationEngine engine = createEngineWithProcessor(processor, actualMetaData, statistics, jdbcExecutor);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement, selectStatement, selectStatement, selectStatement, mock(CreateTableStatement.class));
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        ExplainStatementContext explainStatementContext = mock(ExplainStatementContext.class, RETURNS_DEEP_STUBS);
        ExplainStatement explainStatement = mock(ExplainStatement.class, RETURNS_DEEP_STUBS);
        when(explainStatement.getExplainableSQLStatement()).thenReturn(selectStatement);
        when(explainStatementContext.getSqlStatement()).thenReturn(explainStatement);
        when(explainStatementContext.getExplainableSQLStatementContext()).thenReturn(selectStatementContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(explainStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM t_order");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost"));
        connectionContext.setCurrentDatabaseName("foo_db");
        when(queryContext.getConnectionContext()).thenReturn(connectionContext);
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_1");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(processor.executePlan(eq(prepareEngine), eq(callback), any(SQLFederationExecutionPlan.class), any(SQLFederationRelConverter.class), eq(federationContext), any())).thenReturn(resultSet);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        when(executionPlan.getPhysicalPlan()).thenReturn(mock(RelNode.class));
        try (
                MockedConstruction<SQLFederationRelConverter> converterMocked = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> compilerMocked = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            assertSame(resultSet, engine.executeQuery(prepareEngine, callback, federationContext));
            ArgumentCaptor<ExecutionPlanCacheKey> cacheKeyCaptor = ArgumentCaptor.forClass(ExecutionPlanCacheKey.class);
            verify(compilerMocked.constructed().get(0)).compile(cacheKeyCaptor.capture(), eq(false));
            Assertions.assertEquals(1, cacheKeyCaptor.getValue().getTableMetaDataVersions().size());
            assertSame(resultSet, engine.getResultSet());
            engine.close();
            verify(processor).release("foo_db", "foo_schema", queryContext, converterMocked.constructed().get(0).getSchemaPlus());
        }
    }
    
    @Test
    void assertExecuteQueryWithParametersAndOwner() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1));
        ShardingSphereTable table = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData actualMetaData = createMetaData(config, Collections.emptyList(), Collections.singleton(table), TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), props);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        when(processor.getConvention()).thenReturn(mock(Convention.class));
        final SQLFederationEngine engine = createEngineWithProcessor(processor, actualMetaData, mock(ShardingSphereStatistics.class), mock(JDBCExecutor.class));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(tableNameSegment);
        simpleTableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_schema")));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement, selectStatement, selectStatement, mock(CreateTableStatement.class));
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM t_order");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_2");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(processor.executePlan(eq(prepareEngine), eq(callback), any(SQLFederationExecutionPlan.class), any(SQLFederationRelConverter.class), eq(federationContext), any())).thenReturn(resultSet);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        when(executionPlan.getPhysicalPlan()).thenReturn(mock(RelNode.class));
        try (
                MockedConstruction<SQLFederationRelConverter> ignoredConverter = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            engine.executeQuery(prepareEngine, callback, federationContext);
            assertNull(engine.getResultSet());
        }
    }
    
    @Test
    void assertExecuteQueryWithMissingTableThrowsUnsupported() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE.toString());
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1));
        ShardingSphereMetaData actualMetaData = createMetaData(config, Collections.emptyList(), Collections.emptyList(), databaseType, props);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        when(processor.getConvention()).thenReturn(mock(Convention.class));
        final SQLFederationEngine engine = createEngineWithProcessor(processor, actualMetaData, mock(ShardingSphereStatistics.class), mock(JDBCExecutor.class));
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM t_order");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_3");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        assertThrows(SQLFederationUnsupportedSQLException.class, () -> {
            try (
                    MockedConstruction<SQLFederationRelConverter> ignored = mockConstruction(SQLFederationRelConverter.class,
                            (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)))) {
                engine.executeQuery(prepareEngine, callback, federationContext);
            }
        });
    }
    
    @Test
    void assertExecuteQueryWhenExecutionInterrupted() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE.toString());
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1));
        ShardingSphereTable table = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData actualMetaData = createMetaData(config, Collections.emptyList(), Collections.singleton(table), databaseType, props);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        when(processor.getConvention()).thenReturn(mock(Convention.class));
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM t_order");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_4");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        SQLFederationEngine engine = createEngineWithProcessor(processor, actualMetaData, mock(ShardingSphereStatistics.class), mock(JDBCExecutor.class));
        assertThrows(SQLExecutionInterruptedException.class, () -> executeWithInterruptedPlan(engine, processor, prepareEngine, callback, federationContext, executionPlan));
    }
    
    @Test
    void assertCloseCollectsSQLException() throws SQLException {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1L));
        ShardingSphereTable table = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData actualMetaData = createMetaData(config, Collections.emptyList(), Collections.singleton(table), TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), props);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        when(processor.getConvention()).thenReturn(mock(Convention.class));
        SQLFederationEngine engine = createEngineWithProcessor(processor, actualMetaData, mock(ShardingSphereStatistics.class), mock(JDBCExecutor.class));
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement, selectStatement);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM t_order");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_5");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        ResultSet resultSet = mock(ResultSet.class);
        doThrow(new SQLException("close error")).when(resultSet).close();
        when(processor.executePlan(eq(prepareEngine), eq(callback), any(SQLFederationExecutionPlan.class), any(SQLFederationRelConverter.class), eq(federationContext), any())).thenReturn(resultSet);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        when(executionPlan.getPhysicalPlan()).thenReturn(mock(RelNode.class));
        try (
                MockedConstruction<SQLFederationRelConverter> ignored = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            engine.executeQuery(prepareEngine, callback, federationContext);
            SQLException actual = assertThrows(SQLException.class, engine::close);
            Assertions.assertEquals("close error", actual.getNextException().getMessage());
            verify(processor).release(eq("foo_db"), eq("foo_schema"), eq(queryContext), any());
        }
    }
    
    private void executeWithInterruptedPlan(final SQLFederationEngine engine, final SQLFederationProcessor processor,
                                            final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                            final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationContext federationContext,
                                            final SQLFederationExecutionPlan executionPlan) {
        try (
                MockedConstruction<SQLFederationRelConverter> ignored = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan))) {
            when(processor.executePlan(eq(prepareEngine), eq(callback), eq(executionPlan), any(SQLFederationRelConverter.class), eq(federationContext), any()))
                    .thenThrow(new SQLExecutionInterruptedException());
            engine.executeQuery(prepareEngine, callback, federationContext);
        }
    }
    
    private ShardingSphereMetaData createMetaData(final SQLFederationRuleConfiguration config, final Collection<ShardingSphereRule> databaseRules,
                                                  final Collection<ShardingSphereTable> tables, final DatabaseType protocolType, final Properties props) {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", tables, Collections.emptyList());
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData databaseRuleMetaData = new RuleMetaData(databaseRules);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", protocolType, resourceMetaData, databaseRuleMetaData, Collections.singleton(schema));
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(config, Collections.singletonList(database)));
        return new ShardingSphereMetaData(Collections.singletonList(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(globalRules), new ConfigurationProperties(props));
    }
    
    private SQLFederationEngine createEngineWithProcessor(final SQLFederationProcessor processor, final ShardingSphereMetaData metaData,
                                                          final ShardingSphereStatistics statistics, final JDBCExecutor jdbcExecutor) {
        try (MockedStatic<SQLFederationProcessorFactory> factoryMock = mockStatic(SQLFederationProcessorFactory.class)) {
            SQLFederationProcessorFactory factory = mock(SQLFederationProcessorFactory.class);
            factoryMock.when(SQLFederationProcessorFactory::getInstance).thenReturn(factory);
            when(factory.newInstance(statistics, jdbcExecutor)).thenReturn(processor);
            return new SQLFederationEngine("foo_db", "foo_schema", metaData, statistics, jdbcExecutor);
        }
    }
}

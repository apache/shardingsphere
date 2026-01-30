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

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
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
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLFederationEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final SQLFederationCacheOption cacheOption = new SQLFederationCacheOption(1, 1L);
    
    @Test
    void assertDecideWhenSQLFederationDisabled() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration(false, false, cacheOption), Collections.emptyList()));
        try (SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList())) {
            assertFalse(engine.decide(mock(QueryContext.class), new RuleMetaData(globalRules)));
        }
    }
    
    @Test
    void assertDecideWhenEnableAllQueryUseSQLFederation() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration(true, true, cacheOption), Collections.emptyList()));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS));
        try (SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList())) {
            assertTrue(engine.decide(queryContext, new RuleMetaData(globalRules)));
        }
    }
    
    @Test
    void assertDecideWhenExecuteNotSelectStatement() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, cacheOption), Collections.emptyList()));
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(CreateTableStatement.class));
        try (SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList())) {
            assertFalse(engine.decide(queryContext, new RuleMetaData(globalRules)));
        }
    }
    
    @Test
    void assertDecideWithNotMatchedRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, cacheOption), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Collections.singleton(new SQLFederationDeciderRuleNotMatchFixture());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getUsedDatabase()).thenReturn(new ShardingSphereDatabase("foo_db", databaseType, mock(), new RuleMetaData(databaseRules), Collections.emptyList()));
        try (SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules)) {
            assertFalse(engine.decide(queryContext, new RuleMetaData(globalRules)));
        }
    }
    
    @Test
    void assertDecideWithMultipleRules() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, cacheOption), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Arrays.asList(new SQLFederationDeciderRuleNotMatchFixture(), new SQLFederationDeciderRuleMatchFixture());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getUsedDatabase()).thenReturn(new ShardingSphereDatabase("foo_db", databaseType, mock(), new RuleMetaData(databaseRules), Collections.emptyList()));
        try (SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules)) {
            assertTrue(engine.decide(queryContext, new RuleMetaData(globalRules)));
        }
    }
    
    @Test
    void assertDecideWithMultipleDatabases() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, cacheOption), Collections.emptyList()));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Arrays.asList("foo_db", "bar_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        try (SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList())) {
            assertTrue(engine.decide(queryContext, new RuleMetaData(globalRules)));
        }
    }
    
    @Test
    void assertDecideWithExplainStatement() throws SQLException {
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, cacheOption), Collections.emptyList()));
        ExplainStatementContext explainStatementContext = mock(ExplainStatementContext.class, RETURNS_DEEP_STUBS);
        ExplainStatement explainStatement = mock(ExplainStatement.class, RETURNS_DEEP_STUBS);
        when(explainStatementContext.getSqlStatement()).thenReturn(explainStatement);
        when(explainStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(explainStatementContext);
        try (SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList())) {
            assertFalse(engine.decide(queryContext, new RuleMetaData(globalRules)));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteQueryWithDefaultSchemaButWithoutOwner() throws SQLException {
        ShardingSphereMetaData actualMetaData = createMetaData(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE.toString())));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_schema_path");
        AtomicReference<List<String>> actualSchemaPath = new AtomicReference<>();
        try (
                SQLFederationEngine engine = createSQLFederationEngine(mock(), actualMetaData);
                MockedConstruction<DatabaseTypeRegistry> ignoredRegistry = mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
                    DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
                    when(schemaOption.getDefaultSchema()).thenReturn(Optional.of("public"));
                    DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
                    when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(schemaOption);
                    when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
                });
                MockedConstruction<SQLFederationRelConverter> ignoredConverter = mockConstruction(SQLFederationRelConverter.class, (mock, context) -> {
                    actualSchemaPath.set((List<String>) context.arguments().get(1));
                    when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class));
                });
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(mock(SQLFederationExecutionPlan.class)));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            engine.executeQuery(mock(), mock(), federationContext);
            assertThat(actualSchemaPath.get(), is(Arrays.asList("foo_db", "foo_schema")));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteQueryWithLoggingAndRelease() throws SQLException {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement, selectStatement, selectStatement, selectStatement, mock(CreateTableStatement.class));
        when(selectStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        ExplainStatementContext explainStatementContext = mock(ExplainStatementContext.class);
        ExplainStatement explainStatement = mock(ExplainStatement.class);
        when(explainStatement.getExplainableSQLStatement()).thenReturn(selectStatement);
        when(explainStatementContext.getSqlStatement()).thenReturn(explainStatement);
        when(explainStatementContext.getExplainableSQLStatementContext()).thenReturn(selectStatementContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(explainStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost"));
        connectionContext.setCurrentDatabaseName("foo_db");
        when(queryContext.getConnectionContext()).thenReturn(connectionContext);
        ShardingSphereMetaData actualMetaData = createMetaData(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString())));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_1");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        ResultSet resultSet = mock(ResultSet.class);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        when(processor.executePlan(eq(prepareEngine), eq(callback), any(SQLFederationExecutionPlan.class), any(SQLFederationRelConverter.class), eq(federationContext), any())).thenReturn(resultSet);
        SQLFederationEngine engine = createSQLFederationEngine(processor, actualMetaData);
        try (
                MockedConstruction<SQLFederationRelConverter> converterMocked = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> compilerMocked = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(mock(SQLFederationExecutionPlan.class, RETURNS_DEEP_STUBS)));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            assertThat(engine.executeQuery(prepareEngine, callback, federationContext), is(resultSet));
            ArgumentCaptor<ExecutionPlanCacheKey> cacheKeyCaptor = ArgumentCaptor.forClass(ExecutionPlanCacheKey.class);
            verify(compilerMocked.constructed().get(0)).compile(cacheKeyCaptor.capture(), eq(false));
            assertThat(cacheKeyCaptor.getValue().getTableMetaDataVersions().size(), is(1));
            assertThat(engine.getResultSet(), is(resultSet));
            engine.close();
            verify(processor).release("foo_db", "foo_schema", queryContext, converterMocked.constructed().get(0).getSchemaPlus());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteQueryWithParametersAndOwner() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(tableNameSegment);
        simpleTableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_schema")));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement, selectStatement, selectStatement, mock(CreateTableStatement.class));
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        ShardingSphereMetaData actualMetaData = createMetaData(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString())));
        SQLFederationEngine engine = createSQLFederationEngine(mock(), actualMetaData);
        try (
                MockedConstruction<SQLFederationRelConverter> ignoredConverter = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(mock(SQLFederationExecutionPlan.class, RETURNS_DEEP_STUBS)));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            engine.executeQuery(mock(), mock(), new SQLFederationContext(false, queryContext, actualMetaData, "process_2"));
            assertNull(engine.getResultSet());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetSchemaPathWithDefaultSchemaAndOwner() throws SQLException {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        simpleTableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_schema")));
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        ShardingSphereMetaData actualMetaData = createMetaData(new Properties());
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_schema_path_owner");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        AtomicReference<List<String>> actualSchemaPath = new AtomicReference<>();
        try (
                SQLFederationEngine engine = createSQLFederationEngine(mock(), actualMetaData);
                MockedConstruction<DatabaseTypeRegistry> ignoredRegistry = mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
                    DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
                    when(schemaOption.getDefaultSchema()).thenReturn(Optional.of("public"));
                    DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
                    when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(schemaOption);
                    when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
                });
                MockedConstruction<SQLFederationRelConverter> ignoredConverter = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> actualSchemaPath.set((List<String>) context.arguments().get(1)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            engine.executeQuery(prepareEngine, callback, federationContext);
        }
        assertThat(actualSchemaPath.get(), is(Collections.singletonList("foo_db")));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteQueryWithoutSQLShow() throws SQLException {
        ShardingSphereMetaData actualMetaData = createMetaData(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE.toString())));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_6");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.isClosed()).thenReturn(true);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        when(processor.executePlan(eq(prepareEngine), eq(callback), eq(executionPlan), any(SQLFederationRelConverter.class), eq(federationContext), any())).thenReturn(resultSet);
        try (
                SQLFederationEngine engine = createSQLFederationEngine(processor, actualMetaData);
                MockedConstruction<SQLFederationRelConverter> ignored = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            engine.executeQuery(prepareEngine, callback, federationContext);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertThrowIntegrityConstraintViolationDirectly() {
        ShardingSphereMetaData actualMetaData = createMetaData(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE.toString())));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "process_8");
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        SQLFederationEngine engine = createSQLFederationEngine(processor, actualMetaData);
        try (
                MockedConstruction<SQLFederationRelConverter> ignored = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            doAnswer(invocation -> {
                throw new SQLIntegrityConstraintViolationException();
            }).when(processor).executePlan(eq(prepareEngine), eq(callback), eq(executionPlan), any(SQLFederationRelConverter.class), eq(federationContext), any());
            assertThrows(SQLIntegrityConstraintViolationException.class, () -> engine.executeQuery(prepareEngine, callback, federationContext));
        }
    }
    
    @Test
    void assertGetResultSetForSelectStatement() throws ReflectiveOperationException {
        ShardingSphereMetaData actualMetaData = createMetaData(Collections.emptyList(), new Properties());
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        QueryContext selectQueryContext = mock(QueryContext.class);
        when(selectQueryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        ResultSet expectedResultSet = mock(ResultSet.class);
        SQLFederationEngine engine = createSQLFederationEngine(mock(), actualMetaData);
        Plugins.getMemberAccessor().set(SQLFederationEngine.class.getDeclaredField("queryContext"), engine, selectQueryContext);
        Plugins.getMemberAccessor().set(SQLFederationEngine.class.getDeclaredField("resultSet"), engine, expectedResultSet);
        assertThat(engine.getResultSet(), is(expectedResultSet));
    }
    
    @Test
    void assertCloseWithoutSchema() throws SQLException, ReflectiveOperationException {
        ShardingSphereMetaData actualMetaData = createMetaData(Collections.emptyList(), new Properties());
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        try (SQLFederationEngine engine = createSQLFederationEngine(processor, actualMetaData)) {
            Plugins.getMemberAccessor().set(SQLFederationEngine.class.getDeclaredField("queryContext"), engine, mock(QueryContext.class));
            verify(processor, never()).release(any(), any(), any(), any());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCloseWhenThrowsException() throws SQLException, ReflectiveOperationException {
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        ShardingSphereMetaData actualMetaData = createMetaData(new Properties());
        SQLFederationContext federationContext = new SQLFederationContext(false, queryContext, actualMetaData, "warning_process");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        SQLFederationEngine engine = createSQLFederationEngine(processor, actualMetaData);
        ResultSet closableResultSet = mock(ResultSet.class);
        doThrow(SQLException.class).when(closableResultSet).close();
        Plugins.getMemberAccessor().set(SQLFederationEngine.class.getDeclaredField("resultSet"), engine, closableResultSet);
        try (
                MockedConstruction<SQLFederationRelConverter> ignoredConverter = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(mock(SQLFederationExecutionPlan.class)));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            doThrow(RuntimeException.class).when(processor).prepare(eq(prepareEngine), eq(callback), anyString(), anyString(), eq(federationContext), any(), any(SchemaPlus.class));
            assertThrows(SQLFederationUnsupportedSQLException.class, () -> engine.executeQuery(prepareEngine, callback, federationContext));
        }
    }
    
    private SQLFederationEngine createSQLFederationEngine(final Collection<ShardingSphereRule> globalRules, final Collection<ShardingSphereRule> databaseRules) {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(globalRules));
        when(metaData.getDatabase("foo_db").getRuleMetaData().getRules()).thenReturn(databaseRules);
        return new SQLFederationEngine("foo_db", "foo_db", metaData, mock(), mock());
    }
    
    private SQLFederationEngine createSQLFederationEngine(final SQLFederationProcessor processor, final ShardingSphereMetaData metaData) {
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class);
        JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
        try (MockedStatic<SQLFederationProcessorFactory> factoryMock = mockStatic(SQLFederationProcessorFactory.class)) {
            SQLFederationProcessorFactory factory = mock(SQLFederationProcessorFactory.class);
            when(factory.newInstance(statistics, jdbcExecutor)).thenReturn(processor);
            factoryMock.when(SQLFederationProcessorFactory::getInstance).thenReturn(factory);
            return new SQLFederationEngine("foo_db", "foo_schema", metaData, statistics, jdbcExecutor);
        }
    }
    
    private ShardingSphereMetaData createMetaData(final Properties props) {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return createMetaData(Collections.singleton(table), props);
    }
    
    private ShardingSphereMetaData createMetaData(final Collection<ShardingSphereTable> tables, final Properties props) {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", databaseType, tables, Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(true, false, cacheOption);
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(ruleConfig, Collections.singleton(database)));
        return new ShardingSphereMetaData(Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(globalRules), new ConfigurationProperties(props));
    }
}

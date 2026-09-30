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

package org.apache.shardingsphere.sqlfederation.provider.calcite;

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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
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
import org.apache.shardingsphere.sqlfederation.provider.calcite.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.provider.calcite.engine.processor.SQLFederationProcessorFactory;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

class CalciteSQLFederationExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final SQLFederationCacheOption cacheOption = new SQLFederationCacheOption(1, 1L);
    
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
                CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(mock(), actualMetaData);
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
            executor.executeQuery(mock(), mock(), federationContext);
            assertThat(actualSchemaPath.get(), is(Arrays.asList("foo_db", "foo_schema")));
        }
    }
    
    private ShardingSphereMetaData createMetaData(final Properties props) {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return createMetaData(Collections.singleton(table), props);
    }
    
    private ShardingSphereMetaData createMetaData(final Collection<ShardingSphereTable> tables, final Properties props) {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", databaseType, tables, Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema),
                new ConfigurationProperties(new Properties()));
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(false, cacheOption, "CALCITE");
        Collection<ShardingSphereRule> globalRules = Collections.singleton(new SQLFederationRule(ruleConfig, Collections.singleton(database)));
        return new ShardingSphereMetaData(Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(globalRules), new ConfigurationProperties(props));
    }
    
    private CalciteSQLFederationExecutor createCalciteSQLFederationExecutor(final SQLFederationProcessor processor, final ShardingSphereMetaData metaData) {
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class);
        JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
        try (MockedStatic<SQLFederationProcessorFactory> factoryMock = mockStatic(SQLFederationProcessorFactory.class)) {
            SQLFederationProcessorFactory factory = mock(SQLFederationProcessorFactory.class);
            when(factory.newInstance(statistics, jdbcExecutor)).thenReturn(processor);
            factoryMock.when(SQLFederationProcessorFactory::getInstance).thenReturn(factory);
            CalciteSQLFederationProvider provider = (CalciteSQLFederationProvider) metaData.getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class).getProvider();
            return new CalciteSQLFederationExecutor("foo_db", "foo_schema", statistics, jdbcExecutor, new ProcessEngine(), provider);
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
        when(processor.executePlan(any(SQLFederationExecutionPlan.class), any(SQLFederationRelConverter.class), eq(federationContext), any())).thenReturn(resultSet);
        CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(processor, actualMetaData);
        try (
                MockedConstruction<SQLFederationRelConverter> converterMocked = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> compilerMocked = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(mock(SQLFederationExecutionPlan.class, RETURNS_DEEP_STUBS)));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            assertThat(executor.executeQuery(prepareEngine, callback, federationContext), is(resultSet));
            ArgumentCaptor<ExecutionPlanCacheKey> cacheKeyCaptor = ArgumentCaptor.forClass(ExecutionPlanCacheKey.class);
            verify(compilerMocked.constructed().get(0)).compile(cacheKeyCaptor.capture(), eq(false));
            assertThat(cacheKeyCaptor.getValue().getTableMetaDataVersions().size(), is(1));
            assertThat(executor.getResultSet(), is(resultSet));
            executor.close();
            verify(processor).release("foo_db", "foo_schema", queryContext, converterMocked.constructed().get(0).getSchemaPlus());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteQueryWithBoundTable() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(tableNameSegment);
        simpleTableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_schema")));
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        SQLStatementContext selectStatementContext = mock(SQLStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM foo_tbl");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList, new Grantee("root", "localhost")));
        ShardingSphereMetaData actualMetaData = createMetaData(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString())));
        CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(mock(), actualMetaData);
        try (
                MockedConstruction<SQLFederationRelConverter> ignoredConverter = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> compilerMocked = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(mock(SQLFederationExecutionPlan.class, RETURNS_DEEP_STUBS)));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            executor.executeQuery(mock(), mock(), new SQLFederationContext(false, queryContext, actualMetaData, "process_2"));
            ArgumentCaptor<ExecutionPlanCacheKey> cacheKeyCaptor = ArgumentCaptor.forClass(ExecutionPlanCacheKey.class);
            verify(compilerMocked.constructed().get(0)).compile(cacheKeyCaptor.capture(), eq(false));
            assertThat(cacheKeyCaptor.getValue().getTableMetaDataVersions().get("foo_db.foo_schema.foo_tbl"), is(0));
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
                CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(mock(), actualMetaData);
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
            executor.executeQuery(prepareEngine, callback, federationContext);
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
        when(processor.executePlan(eq(executionPlan), any(SQLFederationRelConverter.class), eq(federationContext), any())).thenReturn(resultSet);
        try (
                CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(processor, actualMetaData);
                MockedConstruction<SQLFederationRelConverter> ignored = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            executor.executeQuery(prepareEngine, callback, federationContext);
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
        CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(processor, actualMetaData);
        try (
                MockedConstruction<SQLFederationRelConverter> ignored = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(executionPlan));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            doAnswer(invocation -> {
                throw new SQLIntegrityConstraintViolationException();
            }).when(processor).executePlan(eq(executionPlan), any(SQLFederationRelConverter.class), eq(federationContext), any());
            assertThrows(SQLIntegrityConstraintViolationException.class, () -> executor.executeQuery(prepareEngine, callback, federationContext));
        }
    }
    
    @Test
    void assertCloseWithoutSchema() throws SQLException, ReflectiveOperationException {
        ShardingSphereMetaData actualMetaData = createMetaData(Collections.emptyList(), new Properties());
        SQLFederationProcessor processor = mock(SQLFederationProcessor.class);
        try (CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(processor, actualMetaData)) {
            Plugins.getMemberAccessor().set(CalciteSQLFederationExecutor.class.getDeclaredField("queryContext"), executor, mock(QueryContext.class));
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
        CalciteSQLFederationExecutor executor = createCalciteSQLFederationExecutor(processor, actualMetaData);
        ResultSet closableResultSet = mock(ResultSet.class);
        doThrow(SQLException.class).when(closableResultSet).close();
        Plugins.getMemberAccessor().set(CalciteSQLFederationExecutor.class.getDeclaredField("resultSet"), executor, closableResultSet);
        try (
                MockedConstruction<SQLFederationRelConverter> ignoredConverter = mockConstruction(SQLFederationRelConverter.class,
                        (mock, context) -> when(mock.getSchemaPlus()).thenReturn(mock(SchemaPlus.class)));
                MockedConstruction<SQLFederationCompilerEngine> ignoredCompiler = mockConstruction(SQLFederationCompilerEngine.class,
                        (mock, context) -> when(mock.compile(any(ExecutionPlanCacheKey.class), eq(false))).thenReturn(mock(SQLFederationExecutionPlan.class)));
                MockedStatic<RelOptUtil> relOptUtil = mockStatic(RelOptUtil.class)) {
            relOptUtil.when(() -> RelOptUtil.toString(any(RelNode.class), eq(SqlExplainLevel.ALL_ATTRIBUTES))).thenReturn("plan");
            doThrow(RuntimeException.class).when(processor).prepare(eq(prepareEngine), eq(callback), anyString(), anyString(), eq(federationContext), any(), any(SchemaPlus.class));
            assertThrows(SQLFederationUnsupportedSQLException.class, () -> executor.executeQuery(prepareEngine, callback, federationContext));
        }
    }
    
}

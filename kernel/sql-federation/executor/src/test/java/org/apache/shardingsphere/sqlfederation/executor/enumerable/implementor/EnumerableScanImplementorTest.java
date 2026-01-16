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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.implementor;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.kernel.connection.SQLExecutionInterruptedException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementorContext;
import org.apache.shardingsphere.sqlfederation.executor.context.ExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.memory.MemoryTableStatisticsBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnumerableScanImplementorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertImplementWithSystemTable() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singleton("pg_catalog"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(queryContext.getMetaData()).thenReturn(metaData);
        when(queryContext.getConnectionContext()).thenReturn(mock(ConnectionContext.class));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("pg_database");
        when(table.getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("datname", Types.VARCHAR, true, false, "varchar", false, false, true, false)));
        TableStatistics tableStatistics = mock(TableStatistics.class);
        when(tableStatistics.getRows()).thenReturn(Collections.singletonList(new RowStatistics(Collections.singletonList("foo_db"))));
        DialectDriverQuerySystemCatalogOption driverOption = mock(DialectDriverQuerySystemCatalogOption.class);
        when(driverOption.isSystemTable("pg_database")).thenReturn(true);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getDriverQuerySystemCatalogOption()).thenReturn(Optional.of(driverOption));
        DialectDataTypeOption dataTypeOption = mock(DialectDataTypeOption.class);
        when(dataTypeOption.findExtraSQLTypeClass(anyInt(), anyBoolean())).thenReturn(Optional.empty());
        when(dialectDatabaseMetaData.getDataTypeOption()).thenReturn(dataTypeOption);
        try (
                MockedConstruction<SystemDatabase> mockedSystemDatabase = mockConstruction(SystemDatabase.class,
                        (constructed, context) -> when(constructed.getSystemSchemas()).thenReturn(Collections.singletonList("pg_catalog")));
                MockedConstruction<DatabaseTypeRegistry> mockedTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (constructed, context) -> when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData));
                MockedStatic<MemoryTableStatisticsBuilder> memoryBuilderMockedStatic = mockStatic(MemoryTableStatisticsBuilder.class)) {
            memoryBuilderMockedStatic.when(() -> MemoryTableStatisticsBuilder.buildTableStatistics(table, metaData, driverOption)).thenReturn(tableStatistics);
            Enumerable<Object> enumerable = new EnumerableScanImplementor(queryContext, mock(), mock()).implement(table, new ScanImplementorContext(mock(), "SELECT datname FROM pg_database", null));
            try (Enumerator<Object> actual = enumerable.enumerator()) {
                assertTrue(actual.moveNext());
                assertThat(((Object[]) actual.current())[0], is("foo_db"));
            }
            assertFalse(mockedSystemDatabase.constructed().isEmpty());
            assertFalse(mockedTypeRegistry.constructed().isEmpty());
        }
    }
    
    @Test
    void assertImplementWithDriverOptionNotSystemTable() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singleton("pg_catalog"));
        when(sqlStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.of("pg_catalog"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getConnectionContext()).thenReturn(mock(ConnectionContext.class));
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("custom_stats");
        when(table.getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.INTEGER, true, false, "int", false, false, true, false)));
        ExecutorContext executorContext = mock(ExecutorContext.class);
        when(executorContext.getCurrentDatabaseName()).thenReturn("foo_db");
        when(executorContext.getCurrentSchemaName()).thenReturn("pg_catalog");
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class, RETURNS_DEEP_STUBS);
        TableStatistics tableStatistics = mock(TableStatistics.class);
        when(tableStatistics.getRows()).thenReturn(Collections.singletonList(new RowStatistics(Collections.singletonList(2))));
        when(statistics.getDatabaseStatistics("foo_db").getSchemaStatistics("pg_catalog").getTableStatistics("custom_stats")).thenReturn(tableStatistics);
        when(executorContext.getStatistics()).thenReturn(statistics);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getDriverQuerySystemCatalogOption()).thenReturn(Optional.of(mock(DialectDriverQuerySystemCatalogOption.class)));
        DialectDataTypeOption dataTypeOption = mock(DialectDataTypeOption.class);
        when(dataTypeOption.findExtraSQLTypeClass(anyInt(), anyBoolean())).thenReturn(Optional.empty());
        when(dialectDatabaseMetaData.getDataTypeOption()).thenReturn(dataTypeOption);
        try (
                MockedConstruction<SystemDatabase> mockedSystemDatabase = mockConstruction(SystemDatabase.class,
                        (constructed, context) -> when(constructed.getSystemSchemas()).thenReturn(Collections.singletonList("pg_catalog")));
                MockedConstruction<DatabaseTypeRegistry> mockedTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                        (constructed, context) -> when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData))) {
            Enumerable<Object> enumerable = new EnumerableScanImplementor(queryContext, mock(), executorContext)
                    .implement(table, new ScanImplementorContext(mock(), "SELECT id FROM custom_stats", null));
            try (Enumerator<Object> actual = enumerable.enumerator()) {
                assertTrue(actual.moveNext());
                assertThat(((Object[]) actual.current())[0], is(2));
            }
            assertFalse(mockedSystemDatabase.constructed().isEmpty());
            assertFalse(mockedTypeRegistry.constructed().isEmpty());
        }
    }
    
    @Test
    void assertImplementWithDriverOptionButNonSystemTable() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singleton("pg_catalog"));
        when(sqlStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.of("pg_catalog"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("non_system");
        when(table.getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.INTEGER, true, false, "int", false, false, true, false)));
        ExecutorContext executorContext = mock(ExecutorContext.class);
        when(executorContext.getCurrentDatabaseName()).thenReturn("foo_db");
        when(executorContext.getCurrentSchemaName()).thenReturn("pg_catalog");
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class, RETURNS_DEEP_STUBS);
        TableStatistics tableStatistics = mock(TableStatistics.class);
        when(tableStatistics.getRows()).thenReturn(Collections.singletonList(new RowStatistics(Collections.singletonList(1))));
        when(statistics.getDatabaseStatistics("foo_db").getSchemaStatistics("pg_catalog").getTableStatistics("non_system")).thenReturn(tableStatistics);
        when(executorContext.getStatistics()).thenReturn(statistics);
        try (
                MockedConstruction<SystemDatabase> ignored = mockConstruction(SystemDatabase.class,
                        (constructed, context) -> when(constructed.getSystemSchemas()).thenReturn(Collections.singletonList("pg_catalog")))) {
            Enumerable<Object> enumerable = new EnumerableScanImplementor(queryContext, mock(), executorContext).implement(table, new ScanImplementorContext(mock(), "SELECT 1", null));
            try (Enumerator<Object> actual = enumerable.enumerator()) {
                assertTrue(actual.moveNext());
            }
        }
    }
    
    @Test
    void assertImplementWithNonSystemSchema() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singletonList("custom_schema"));
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getParameters()).thenReturn(Collections.singletonList("param_0"));
        SQLStatement sqlStatement = mock(SQLStatement.class);
        CompilerContext compilerContext = mock(CompilerContext.class, RETURNS_DEEP_STUBS);
        when(compilerContext.getSqlParserRule().getSQLParserEngine(databaseType).parse("SELECT 1", false)).thenReturn(sqlStatement);
        ExecutorContext executorContext = mock(ExecutorContext.class);
        when(executorContext.isPreview()).thenReturn(true);
        when(executorContext.getPreviewExecutionUnits()).thenReturn(new LinkedList<>());
        when(executorContext.getCurrentDatabaseName()).thenReturn("foo_db");
        ExecutionUnit executionUnit = new ExecutionUnit("ds_0", new SQLUnit("SELECT 1", Collections.emptyList()));
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singleton(executionUnit));
        SQLStatementContext boundStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(boundStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(boundStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        try (
                MockedConstruction<SystemDatabase> mockedSystemDatabase = mockConstruction(SystemDatabase.class,
                        (constructed, context) -> when(constructed.getSystemSchemas()).thenReturn(Collections.singletonList("pg_catalog")));
                MockedConstruction<SQLBindEngine> ignoredSQLBindEngine = mockConstruction(SQLBindEngine.class,
                        (constructed, context) -> when(constructed.bind(sqlStatement)).thenReturn(boundStatementContext));
                MockedConstruction<KernelProcessor> ignoredKernelProcessor = mockConstruction(KernelProcessor.class,
                        (constructed, context) -> when(constructed.generateExecutionContext(any(), any(), any())).thenReturn(executionContext))) {
            Enumerable<Object> enumerable = new EnumerableScanImplementor(queryContext, compilerContext, executorContext)
                    .implement(mock(ShardingSphereTable.class), new ScanImplementorContext(mock(), "SELECT 1", null));
            assertThat(executorContext.getPreviewExecutionUnits(), is(Collections.singletonList(executionUnit)));
            try (Enumerator<Object> actual = enumerable.enumerator()) {
                assertFalse(actual.moveNext());
            }
            assertFalse(mockedSystemDatabase.constructed().isEmpty());
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void assertImplementWithJDBCEnumerable() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.isUseCache()).thenReturn(true);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(metaData.getGlobalRuleMetaData()).thenReturn(mock(RuleMetaData.class));
        when(queryContext.getMetaData()).thenReturn(metaData);
        when(queryContext.getParameters()).thenReturn(Collections.singletonList("param_0"));
        SQLStatement sqlStatement = mock(SQLStatement.class);
        CompilerContext compilerContext = mock(CompilerContext.class, RETURNS_DEEP_STUBS);
        when(compilerContext.getSqlParserRule().getSQLParserEngine(databaseType).parse("SELECT ? FROM tbl", true)).thenReturn(sqlStatement);
        SQLStatementContext boundStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(boundStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(boundStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ExecutorContext executorContext = mock(ExecutorContext.class);
        Map<String, Integer> connectionOffsets = new LinkedHashMap<>();
        when(executorContext.getConnectionOffsets()).thenReturn(connectionOffsets);
        when(executorContext.getCurrentDatabaseName()).thenReturn("foo_db");
        when(executorContext.getProcessId()).thenReturn("process_id");
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        when(executorContext.getPrepareEngine()).thenReturn(prepareEngine);
        JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
        when(executorContext.getJdbcExecutor()).thenReturn(jdbcExecutor);
        JDBCExecutorCallback<QueryResult> queryCallback = (JDBCExecutorCallback<QueryResult>) mock(JDBCExecutorCallback.class);
        when(executorContext.getQueryCallback()).thenReturn((JDBCExecutorCallback) queryCallback);
        ExecutionUnit executionUnit = new ExecutionUnit("ds_0", new SQLUnit("SELECT 1", Collections.emptyList()));
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(executionContext.getExecutionUnits()).thenReturn(Arrays.asList(executionUnit, new ExecutionUnit("ds_0", new SQLUnit("SELECT 2", Collections.emptyList()))));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        JDBCExecutionUnit jdbcExecutionUnit = new JDBCExecutionUnit(executionUnit, ConnectionMode.MEMORY_STRICTLY, mock());
        JDBCExecutionUnit jdbcPreparedExecutionUnit = new JDBCExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit("SELECT ?", Collections.singletonList("bar_param"))),
                ConnectionMode.CONNECTION_STRICTLY, preparedStatement);
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(
                Collections.singleton(new ExecutionGroup<>(Arrays.asList(jdbcExecutionUnit, jdbcPreparedExecutionUnit))), new ExecutionGroupReportContext("process_id", "foo_db"));
        doAnswer(invocation -> executionGroupContext).when(prepareEngine).prepare(anyString(), any(), anyMap(), anyCollection(), any());
        when(jdbcExecutor.execute(executionGroupContext, queryCallback)).thenReturn(Collections.singletonList(mock(QueryResult.class)));
        ProcessRegistry.getInstance().add(new Process(new ExecutionGroupContext<>(Collections.emptyList(), new ExecutionGroupReportContext("process_id", "foo_db"))));
        ScanImplementorContext scanContext = new ScanImplementorContext(mock(DataContext.class), "SELECT ? FROM tbl", new int[]{0});
        ShardingSphereTable table = mock(ShardingSphereTable.class, RETURNS_DEEP_STUBS);
        when(table.getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.INTEGER, true, false, "int", false, false, true, false)));
        try (
                MockedConstruction<SQLBindEngine> ignoredSQLBindEngine = mockConstruction(SQLBindEngine.class,
                        (constructed, context) -> when(constructed.bind(sqlStatement)).thenReturn(boundStatementContext));
                MockedConstruction<KernelProcessor> ignoredKernelProcessor = mockConstruction(KernelProcessor.class,
                        (constructed, context) -> when(constructed.generateExecutionContext(any(), any(), any())).thenReturn(executionContext));
                MockedConstruction<MergeEngine> mergeEngineMockedConstruction = mockConstruction(MergeEngine.class,
                        (constructed, context) -> when(constructed.merge(anyList(), any(QueryContext.class))).thenReturn(mock(MergedResult.class)))) {
            Enumerable<Object> enumerable = new EnumerableScanImplementor(queryContext, compilerContext, executorContext).implement(table, scanContext);
            try (Enumerator<Object> ignored = enumerable.enumerator()) {
                assertThat(connectionOffsets.get("ds_0"), is(1));
                verify(preparedStatement).setObject(1, "bar_param");
                assertFalse(mergeEngineMockedConstruction.constructed().isEmpty());
            }
        } finally {
            ProcessRegistry.getInstance().remove("process_id");
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertImplementWithInterruptedProcess() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.isUseCache()).thenReturn(true);
        when(queryContext.getParameters()).thenReturn(Collections.singletonList("param_0"));
        CompilerContext compilerContext = mock(CompilerContext.class, RETURNS_DEEP_STUBS);
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(compilerContext.getSqlParserRule().getSQLParserEngine(databaseType).parse("SELECT ? FROM tbl", true)).thenReturn(sqlStatement);
        SQLStatementContext boundStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(boundStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(boundStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ExecutorContext executorContext = mock(ExecutorContext.class);
        when(executorContext.getConnectionOffsets()).thenReturn(new HashMap<>());
        when(executorContext.getCurrentDatabaseName()).thenReturn("foo_db");
        when(executorContext.getProcessId()).thenReturn("process_interrupted");
        ExecutionUnit executionUnit = new ExecutionUnit("ds_0", new SQLUnit("SELECT ?", Collections.singletonList("bar_param")));
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singleton(executionUnit));
        JDBCExecutionUnit jdbcExecutionUnit = new JDBCExecutionUnit(executionUnit, ConnectionMode.CONNECTION_STRICTLY, mock());
        ExecutionGroup<JDBCExecutionUnit> executionGroup = new ExecutionGroup<>(Collections.singletonList(jdbcExecutionUnit));
        ExecutionGroupReportContext reportContext = new ExecutionGroupReportContext("process_interrupted", "foo_db");
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(Collections.singleton(executionGroup), reportContext);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        when(prepareEngine.prepare(any(), any(), anyMap(), anyCollection(), any())).thenReturn(executionGroupContext);
        when(executorContext.getPrepareEngine()).thenReturn(prepareEngine);
        ScanImplementorContext scanContext = new ScanImplementorContext(mock(DataContext.class), "SELECT ? FROM tbl", new int[]{0});
        ShardingSphereTable table = mock(ShardingSphereTable.class, RETURNS_DEEP_STUBS);
        when(table.getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.INTEGER, true, false, "int", false, false, true, false)));
        ProcessRegistry processRegistry = mock(ProcessRegistry.class);
        Process interruptedProcess = new Process(executionGroupContext);
        interruptedProcess.setInterrupted(true);
        when(processRegistry.get("process_interrupted")).thenReturn(interruptedProcess);
        try (
                MockedStatic<ProcessRegistry> mockedStatic = mockStatic(ProcessRegistry.class);
                MockedConstruction<SQLBindEngine> ignoredSQLBindEngine = mockConstruction(SQLBindEngine.class,
                        (constructed, context) -> when(constructed.bind(sqlStatement)).thenReturn(boundStatementContext));
                MockedConstruction<KernelProcessor> ignoredKernelProcessor = mockConstruction(KernelProcessor.class,
                        (constructed, context) -> when(constructed.generateExecutionContext(any(), any(), any())).thenReturn(executionContext))) {
            mockedStatic.when(ProcessRegistry::getInstance).thenReturn(processRegistry);
            Enumerable<Object> enumerable = new EnumerableScanImplementor(queryContext, compilerContext, executorContext).implement(table, scanContext);
            assertThrows(SQLExecutionInterruptedException.class, enumerable::enumerator);
        }
    }
}

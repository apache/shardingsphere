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

package org.apache.shardingsphere.sqlfederation.engine.processor.impl;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.lookup.Lookup;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.resultset.SQLFederationResultSet;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StandardSQLFederationProcessorTest {
    
    private final ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class);
    
    private final JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
    
    @Test
    void assertPrepareWithNullSchema() {
        SQLFederationProcessor processor = new StandardSQLFederationProcessor(statistics, jdbcExecutor);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationContext federationContext = mock(SQLFederationContext.class, RETURNS_DEEP_STUBS);
        when(federationContext.getQueryContext()).thenReturn(mock(QueryContext.class, RETURNS_DEEP_STUBS));
        assertDoesNotThrow(() -> processor.prepare(prepareEngine, callback, "db", "schema", federationContext, mock(CompilerContext.class), null));
    }
    
    @Test
    void assertPrepareAndReleaseSkipNonFederationTable() {
        StandardSQLFederationProcessor processor = new StandardSQLFederationProcessor(statistics, jdbcExecutor);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationContext federationContext = mockFederationContext("SELECT 1", "db", "schema");
        org.apache.calcite.schema.Table table = mock(org.apache.calcite.schema.Table.class);
        SchemaPlus rootSchema = mockFlatSchemaWithTable("db", "t_order", table);
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of("schema"));
        try (MockedConstruction<DatabaseTypeRegistry> mockedTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    DialectDriverQuerySystemCatalogOption driverOption = mock(DialectDriverQuerySystemCatalogOption.class);
                    DialectDataTypeOption dataTypeOption = mock(DialectDataTypeOption.class, RETURNS_DEEP_STUBS);
                    when(dialectMeta.getDriverQuerySystemCatalogOption()).thenReturn(Optional.of(driverOption));
                    when(dialectMeta.getDataTypeOption()).thenReturn(dataTypeOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(prepareEngine, callback, "db", "schema", federationContext, mock(CompilerContext.class), rootSchema);
            verifyNoInteractions(table);
            processor.release("db", "schema", federationContext.getQueryContext(), rootSchema);
        }
    }
    
    @Test
    void assertPrepareWithDefaultSchemaEmpty() {
        StandardSQLFederationProcessor processor = new StandardSQLFederationProcessor(statistics, jdbcExecutor);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationContext federationContext = mockFederationContext("SELECT 1", "db", "schema");
        SchemaPlus rootSchema = mockFlatSchemaWithTable("db", "t_order", spy(new SQLFederationTable(
                mock(org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable.class),
                TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))));
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.empty());
        try (MockedConstruction<DatabaseTypeRegistry> mockedTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    DialectDriverQuerySystemCatalogOption driverOption = mock(DialectDriverQuerySystemCatalogOption.class);
                    DialectDataTypeOption dataTypeOption = mock(DialectDataTypeOption.class, RETURNS_DEEP_STUBS);
                    when(dialectMeta.getDriverQuerySystemCatalogOption()).thenReturn(Optional.of(driverOption));
                    when(dialectMeta.getDataTypeOption()).thenReturn(dataTypeOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(prepareEngine, callback, "db", "schema", federationContext, mock(CompilerContext.class), rootSchema);
            SQLFederationTable table = (SQLFederationTable) rootSchema.subSchemas().get("db").tables().get("t_order");
            verify(table, times(1)).setScanImplementor(any());
        }
    }
    
    @Test
    void assertExecutePlanPreviewAndNonPreview() {
        StandardSQLFederationProcessor processor = new StandardSQLFederationProcessor(statistics, jdbcExecutor);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationContext federationContext = mockFederationContext("SELECT 1", "db", "schema");
        Collection<ExecutionUnit> previewExecutionUnits = federationContext.getPreviewExecutionUnits();
        previewExecutionUnits.clear();
        CompilerContext compilerContext = mock(CompilerContext.class);
        SchemaPlus rootSchema = mockSchemaTreeWithTable("db", "schema", "t_order");
        prepareForExecutePlan(processor, prepareEngine, callback, federationContext, compilerContext, rootSchema);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        when(executionPlan.getPhysicalPlan()).thenReturn(mock(org.apache.calcite.adapter.enumerable.EnumerableRel.class));
        when(executionPlan.getResultColumnType()).thenReturn(mock(org.apache.calcite.rel.type.RelDataType.class));
        SQLFederationRelConverter converter = mock(SQLFederationRelConverter.class);
        Bindable<Object> bindable = mock(Bindable.class);
        Enumerator<Object> enumerator = mock(Enumerator.class);
        when(enumerator.moveNext()).thenReturn(false);
        org.apache.calcite.linq4j.Enumerable<Object> enumerable = mock(org.apache.calcite.linq4j.Enumerable.class);
        when(enumerable.enumerator()).thenReturn(enumerator);
        when(bindable.bind(any())).thenReturn(enumerable);
        try (MockedStatic<EnumerableInterpretable> ignoredInterpretable = mockStatic(EnumerableInterpretable.class);
                MockedStatic<org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader> mockedSpiLoader =
                        mockStatic(org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader.class)) {
            ignoredInterpretable.when(() -> EnumerableInterpretable.toBindable(any(Map.class), any(), any(), any())).thenReturn(bindable);
            mockedSpiLoader.when(() -> org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader
                    .getService(eq(SQLFederationColumnTypeConverter.class), any(DatabaseType.class))).thenReturn(mock(SQLFederationColumnTypeConverter.class));
            ResultSet previewResult = processor.executePlan(prepareEngine, callback, executionPlan, converter, federationContext, rootSchema);
            verify(previewExecutionUnits, times(1)).addAll(anyCollection());
            ((SQLFederationResultSet) previewResult).close();
            when(federationContext.isPreview()).thenReturn(false);
            ResultSet normalResult = processor.executePlan(prepareEngine, callback, executionPlan, converter, federationContext, rootSchema);
            verify(previewExecutionUnits, times(1)).addAll(anyCollection());
            ((SQLFederationResultSet) normalResult).close();
        }
    }
    
    @Test
    void assertGetConvention() {
        assertThat(new StandardSQLFederationProcessor(statistics, jdbcExecutor).getConvention(), is(EnumerableConvention.INSTANCE));
    }
    
    private void prepareForExecutePlan(final StandardSQLFederationProcessor processor,
                                       final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                       final JDBCExecutorCallback<? extends ExecuteResult> callback,
                                       final SQLFederationContext federationContext, final CompilerContext compilerContext, final SchemaPlus schemaPlus) {
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of("schema"));
        try (MockedConstruction<DatabaseTypeRegistry> mockedTypeRegistry = mockConstruction(DatabaseTypeRegistry.class,
                (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    DialectDriverQuerySystemCatalogOption driverOption = mock(DialectDriverQuerySystemCatalogOption.class);
                    DialectDataTypeOption dataTypeOption = mock(DialectDataTypeOption.class, RETURNS_DEEP_STUBS);
                    when(dialectMeta.getDriverQuerySystemCatalogOption()).thenReturn(Optional.of(driverOption));
                    when(dialectMeta.getDataTypeOption()).thenReturn(dataTypeOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(prepareEngine, callback, "db", "schema", federationContext, compilerContext, schemaPlus);
        }
    }
    
    private SQLFederationContext mockFederationContext(final String sql, final String dbName, final String schemaName) {
        SQLFederationContext result = mock(SQLFederationContext.class, RETURNS_DEEP_STUBS);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        SimpleTableSegment tableSegment = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        when(tableSegment.getTableName().getTableBoundInfo()).thenReturn(Optional.empty());
        when(tableSegment.getTableName().getIdentifier().getValue()).thenReturn("t_order");
        when(sqlStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singletonList(tableSegment));
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singleton("pg_catalog"));
        SelectStatement sqlStatement = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        when(sqlStatement.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn(sql);
        when(queryContext.getMetaData()).thenReturn(mock(org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
        when(queryContext.getConnectionContext()).thenReturn(mock(ConnectionContext.class, RETURNS_DEEP_STUBS));
        when(queryContext.getParameters()).thenReturn(new ArrayList<>(Collections.singletonList(1)));
        when(result.getQueryContext()).thenReturn(queryContext);
        when(result.isPreview()).thenReturn(true);
        when(result.getProcessId()).thenReturn("pid");
        when(result.getPreviewExecutionUnits()).thenReturn(spy(new ArrayList<>()));
        return result;
    }
    
    private SchemaPlus mockSchemaTreeWithTable(final String databaseName, final String schemaName, final String tableName) {
        SchemaPlus root = mock(SchemaPlus.class, RETURNS_DEEP_STUBS);
        SchemaPlus databaseSchema = mock(SchemaPlus.class, RETURNS_DEEP_STUBS);
        SchemaPlus logicSchema = mock(SchemaPlus.class, RETURNS_DEEP_STUBS);
        Lookup rootSchemas = mock(Lookup.class);
        when(rootSchemas.get(databaseName)).thenReturn(databaseSchema);
        Lookup schemaLookup = mock(Lookup.class);
        when(schemaLookup.get(schemaName)).thenReturn(logicSchema);
        Lookup tableLookup = mock(Lookup.class);
        SQLFederationTable federationTable = spy(new SQLFederationTable(mock(org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable.class),
                TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
        when(tableLookup.get(tableName)).thenReturn(federationTable);
        when(root.subSchemas()).thenReturn(rootSchemas);
        when(databaseSchema.subSchemas()).thenReturn(schemaLookup);
        when(logicSchema.tables()).thenReturn(tableLookup);
        return root;
    }
    
    private SchemaPlus mockFlatSchemaWithTable(final String databaseName, final String tableName, final org.apache.calcite.schema.Table table) {
        SchemaPlus root = mock(SchemaPlus.class, RETURNS_DEEP_STUBS);
        SchemaPlus databaseSchema = mock(SchemaPlus.class, RETURNS_DEEP_STUBS);
        Lookup rootSchemas = mock(Lookup.class);
        when(rootSchemas.get(databaseName)).thenReturn(databaseSchema);
        Lookup tableLookup = mock(Lookup.class);
        when(tableLookup.get(tableName)).thenReturn(table);
        when(root.subSchemas()).thenReturn(rootSchemas);
        when(databaseSchema.tables()).thenReturn(tableLookup);
        return root;
    }
}

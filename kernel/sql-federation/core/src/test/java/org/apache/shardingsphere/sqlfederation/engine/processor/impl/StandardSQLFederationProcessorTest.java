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
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.lookup.Lookup;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.exception.SQLFederationSchemaNotFoundException;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.resultset.SQLFederationResultSet;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StandardSQLFederationProcessorTest {
    
    private final String databaseName = "foo_db";
    
    private final String schemaName = "foo_schema";
    
    private SQLFederationProcessor processor;
    
    @BeforeEach
    void setUp() {
        processor = new StandardSQLFederationProcessor(mock(), mock());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertPrepareWithNullSchema() {
        assertDoesNotThrow(() -> processor.prepare(mock(), mock(), databaseName, schemaName, mock(), mock(), null));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertPrepareAndReleaseSkipNonFederationTable() {
        SQLFederationContext federationContext = createFederationContext(true, null);
        Table table = mock(Table.class);
        SchemaPlus rootSchema = mockFlatSchemaWithTable(table);
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of(schemaName));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(mock(), mock(), databaseName, schemaName, federationContext, mock(), rootSchema);
            verifyNoInteractions(table);
            processor.release(databaseName, schemaName, federationContext.getQueryContext(), rootSchema);
        }
    }
    
    @Test
    void assertReleaseClearImplementorForFederationTable() {
        SQLFederationContext federationContext = createFederationContext(true, null);
        SQLFederationTable federationTable = mock(SQLFederationTable.class);
        SchemaPlus rootSchema = mockSchemaTreeWithTable(databaseName, schemaName, federationTable);
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of(schemaName));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.release(databaseName, schemaName, federationContext.getQueryContext(), rootSchema);
            verify(federationTable).clearScanImplementor();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertPrepareWithDefaultSchemaEmpty() {
        SQLFederationContext federationContext = createFederationContext(true, null);
        SchemaPlus rootSchema = mockFlatSchemaWithTable(mock(SQLFederationTable.class));
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.empty());
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(mock(), mock(), databaseName, schemaName, federationContext, mock(CompilerContext.class), rootSchema);
            SQLFederationTable table = (SQLFederationTable) rootSchema.subSchemas().get(databaseName).tables().get("foo_tbl");
            verify(table).setScanImplementor(any());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertPrepareThrowsWhenSchemaMissing() {
        SQLFederationContext federationContext = createFederationContext(true, null);
        SchemaPlus rootSchema = mock(SchemaPlus.class);
        when(rootSchema.subSchemas()).thenReturn(mock(Lookup.class));
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of(schemaName));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            assertThrows(SQLFederationSchemaNotFoundException.class, () -> processor.prepare(mock(), mock(), databaseName, schemaName, federationContext, mock(CompilerContext.class), rootSchema));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertPrepareUsesTableBoundInfo() {
        SQLFederationContext federationContext = createFederationContext(true, new TableSegmentBoundInfo(new IdentifierValue("origin_db"), new IdentifierValue("origin_schema")));
        SQLFederationTable federationTable = mock(SQLFederationTable.class);
        SchemaPlus rootSchema = mockSchemaTreeWithTable("origin_db", "origin_schema", federationTable);
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of(schemaName));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(mock(), mock(), "logic_db", "logic_schema", federationContext, mock(CompilerContext.class), rootSchema);
            verify(federationTable).setScanImplementor(any());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecutePlanWithEmptyParameters() {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationContext federationContext = createFederationContext(true, null);
        when(federationContext.getQueryContext().getParameters()).thenReturn(new LinkedList<>());
        CompilerContext compilerContext = mock(CompilerContext.class);
        SchemaPlus rootSchema = mockSchemaTreeWithTable();
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of(schemaName));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(prepareEngine, callback, databaseName, schemaName, federationContext, compilerContext, rootSchema);
        }
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        when(executionPlan.getPhysicalPlan()).thenReturn(mock(EnumerableRel.class));
        when(executionPlan.getResultColumnType()).thenReturn(mock(org.apache.calcite.rel.type.RelDataType.class));
        SQLFederationRelConverter converter = mock(SQLFederationRelConverter.class);
        Bindable<Object> bindable = mock(Bindable.class);
        Enumerator<Object> enumerator = mock(Enumerator.class);
        Enumerable<Object> enumerable = mock(Enumerable.class);
        when(enumerable.enumerator()).thenReturn(enumerator);
        when(bindable.bind(any())).thenReturn(enumerable);
        try (
                MockedStatic<EnumerableInterpretable> ignoredInterpretable = mockStatic(EnumerableInterpretable.class);
                MockedStatic<DatabaseTypedSPILoader> mockedSpiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            ignoredInterpretable.when(() -> EnumerableInterpretable.toBindable(any(Map.class), any(), any(), any())).thenReturn(bindable);
            mockedSpiLoader.when(() -> DatabaseTypedSPILoader
                    .getService(eq(SQLFederationColumnTypeConverter.class), any(DatabaseType.class))).thenReturn(mock(SQLFederationColumnTypeConverter.class));
            ResultSet result = processor.executePlan(prepareEngine, callback, executionPlan, converter, federationContext, rootSchema);
            ((SQLFederationResultSet) result).close();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecutePlanPreviewAndNonPreview() {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
        JDBCExecutorCallback<? extends ExecuteResult> callback = mock(JDBCExecutorCallback.class);
        SQLFederationContext federationContext = createFederationContext(false, null);
        Collection<ExecutionUnit> previewExecutionUnits = federationContext.getPreviewExecutionUnits();
        previewExecutionUnits.clear();
        CompilerContext compilerContext = mock(CompilerContext.class);
        SchemaPlus rootSchema = mockSchemaTreeWithTable();
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of(schemaName));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (constructed, context) -> {
                    DialectDatabaseMetaData dialectMeta = mock(DialectDatabaseMetaData.class);
                    when(dialectMeta.getSchemaOption()).thenReturn(schemaOption);
                    when(constructed.getDialectDatabaseMetaData()).thenReturn(dialectMeta);
                })) {
            processor.prepare(prepareEngine, callback, databaseName, schemaName, federationContext, compilerContext, rootSchema);
        }
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        when(executionPlan.getPhysicalPlan()).thenReturn(mock(EnumerableRel.class));
        when(executionPlan.getResultColumnType()).thenReturn(mock(org.apache.calcite.rel.type.RelDataType.class));
        SQLFederationRelConverter converter = mock(SQLFederationRelConverter.class);
        Bindable<Object> bindable = mock(Bindable.class);
        Enumerator<Object> enumerator = mock(Enumerator.class);
        Enumerable<Object> enumerable = mock(Enumerable.class);
        when(enumerable.enumerator()).thenReturn(enumerator);
        when(bindable.bind(any())).thenReturn(enumerable);
        try (
                MockedStatic<EnumerableInterpretable> ignoredInterpretable = mockStatic(EnumerableInterpretable.class);
                MockedStatic<DatabaseTypedSPILoader> mockedSpiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            ignoredInterpretable.when(() -> EnumerableInterpretable.toBindable(any(Map.class), any(), any(), any())).thenReturn(bindable);
            mockedSpiLoader.when(() -> DatabaseTypedSPILoader
                    .getService(eq(SQLFederationColumnTypeConverter.class), any(DatabaseType.class))).thenReturn(mock(SQLFederationColumnTypeConverter.class));
            ResultSet previewResult = processor.executePlan(prepareEngine, callback, executionPlan, converter, federationContext, rootSchema);
            ((SQLFederationResultSet) previewResult).close();
            ResultSet normalResult = processor.executePlan(prepareEngine, callback, executionPlan, converter, federationContext, rootSchema);
            ((SQLFederationResultSet) normalResult).close();
        }
    }
    
    @Test
    void assertGetConvention() {
        assertThat(processor.getConvention(), is(EnumerableConvention.INSTANCE));
    }
    
    private SQLFederationContext createFederationContext(final boolean preview, final TableSegmentBoundInfo tableSegmentBoundInfo) {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singleton("pg_catalog"));
        SimpleTableSegment tableSegment = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        if (null == tableSegmentBoundInfo) {
            when(tableSegment.getTableName().getTableBoundInfo()).thenReturn(Optional.empty());
        } else {
            when(tableSegment.getTableName().getTableBoundInfo()).thenReturn(Optional.of(tableSegmentBoundInfo));
        }
        when(sqlStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(tableSegment));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn("SELECT 1");
        when(queryContext.getParameters()).thenReturn(new ArrayList<>(Collections.singletonList(1)));
        return new SQLFederationContext(preview, queryContext, mock(), "pid");
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private SchemaPlus mockFlatSchemaWithTable(final Table table) {
        Lookup tableLookup = mock(Lookup.class);
        when(tableLookup.get(any())).thenReturn(table);
        SchemaPlus databaseSchema = mock(SchemaPlus.class, RETURNS_DEEP_STUBS);
        when(databaseSchema.tables()).thenReturn(tableLookup);
        Lookup rootSchemas = mock(Lookup.class);
        when(rootSchemas.get(databaseName)).thenReturn(databaseSchema);
        SchemaPlus result = mock(SchemaPlus.class);
        when(result.subSchemas()).thenReturn(rootSchemas);
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private SchemaPlus mockSchemaTreeWithTable(final String databaseName, final String schemaName, final Table table) {
        SchemaPlus logicSchema = mock(SchemaPlus.class, RETURNS_DEEP_STUBS);
        when(logicSchema.tables().get(any())).thenReturn(table);
        Lookup schemaLookup = mock(Lookup.class);
        when(schemaLookup.get(schemaName)).thenReturn(logicSchema);
        SchemaPlus databaseSchema = mock(SchemaPlus.class);
        when(databaseSchema.subSchemas()).thenReturn(schemaLookup);
        Lookup rootSchemas = mock(Lookup.class);
        when(rootSchemas.get(databaseName)).thenReturn(databaseSchema);
        SchemaPlus result = mock(SchemaPlus.class);
        when(result.subSchemas()).thenReturn(rootSchemas);
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private SchemaPlus mockSchemaTreeWithTable() {
        Lookup schemaLookup = mock(Lookup.class);
        when(schemaLookup.get(schemaName)).thenReturn(mock(SchemaPlus.class, RETURNS_DEEP_STUBS));
        SchemaPlus databaseSchema = mock(SchemaPlus.class);
        when(databaseSchema.subSchemas()).thenReturn(schemaLookup);
        Lookup rootSchemas = mock(Lookup.class);
        when(rootSchemas.get(databaseName)).thenReturn(databaseSchema);
        SchemaPlus result = mock(SchemaPlus.class);
        when(result.subSchemas()).thenReturn(rootSchemas);
        return result;
    }
}

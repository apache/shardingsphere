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

package org.apache.shardingsphere.mcp.support.database.metadata.query;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.context.RequestScopedMetadataContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.fixture.SupportDatabaseTypeFactoryMocker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MetadataQueryServiceTest {
    
    private MockedStatic<DatabaseTypeFactory> databaseTypeFactory;
    
    private MockedStatic<TypedSPILoader> typedSPILoader;
    
    private MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader;
    
    private MetadataQueryService metadataQueryService;
    
    @BeforeEach
    void setUp() {
        databaseTypeFactory = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata();
        typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
        databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class);
        mockDatabaseType("MySQL", false);
        mockDatabaseType("PostgreSQL", true);
        mockDatabaseType("Hive", false);
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = createRuntimeDatabases();
        MCPDatabaseCapabilityProvider databaseCapabilityProvider = new MCPDatabaseCapabilityProvider(runtimeDatabases);
        metadataQueryService = new MetadataQueryService(databaseCapabilityProvider, new RequestScopedMetadataContext(runtimeDatabases, databaseCapabilityProvider));
    }
    
    @AfterEach
    void closeMocks() {
        databaseTypedSPILoader.close();
        typedSPILoader.close();
        databaseTypeFactory.close();
    }
    
    private void mockDatabaseType(final String databaseType, final boolean sequenceSupported) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getType()).thenReturn(databaseType);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        typedSPILoader.when(() -> TypedSPILoader.getService(DatabaseType.class, databaseType)).thenReturn(databaseTypeFromSPI);
        MCPDatabaseCapabilityOption capabilityOption = mock(MCPDatabaseCapabilityOption.class);
        when(capabilityOption.getType()).thenReturn(databaseType);
        when(capabilityOption.getSequenceQuery()).thenReturn(
                sequenceSupported ? Optional.of("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM TEST_SEQUENCES") : Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, databaseType)).thenReturn(Optional.of(capabilityOption));
        mockDialectDatabaseMetaData(databaseTypeFromSPI);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseTypeFromSPI)).thenReturn(Optional.empty());
    }
    
    private void mockDialectDatabaseMetaData(final DatabaseType databaseType) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null, DialectSchemaSemantics.NATIVE_SCHEMA));
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseType)).thenReturn(Optional.of(result));
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(result);
    }
    
    @Test
    void assertQueryDatabases() {
        List<RuntimeDatabaseProfile> actual = metadataQueryService.queryDatabases();
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0).getDatabase(), is("logic_db"));
        assertThat(actual.get(1).getDatabase(), is("runtime_db"));
        assertThat(actual.get(2).getDatabase(), is("warehouse"));
    }
    
    @Test
    void assertQueryDatabase() {
        Optional<RuntimeDatabaseProfile> actual = metadataQueryService.queryDatabase("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabase(), is("logic_db"));
        assertThat(actual.get().getDatabaseType(), is("MySQL"));
    }
    
    @Test
    void assertQuerySchemas() {
        List<ShardingSphereSchema> actual = metadataQueryService.querySchemas("logic_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("public"));
        assertThat(countTables(actual.get(0), TableType.TABLE), is(2L));
    }
    
    @Test
    void assertQuerySchemasWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.querySchemas("unknown_db").isEmpty());
    }
    
    @Test
    void assertQuerySchema() {
        Optional<ShardingSphereSchema> actual = metadataQueryService.querySchema("logic_db", "public");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("public"));
        assertThat(countTables(actual.get(), TableType.TABLE), is(2L));
        assertThat(countTables(actual.get(), TableType.VIEW), is(1L));
    }
    
    @Test
    void assertQuerySchemaWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.querySchema("unknown_db", "public").isPresent());
    }
    
    @Test
    void assertQueryTablesBySchema() {
        List<ShardingSphereTable> actual = metadataQueryService.queryTables("logic_db", "public");
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getName(), is("order_items"));
        assertThat(actual.get(1).getName(), is("orders"));
        assertTrue(actual.get(1).getAllColumns().isEmpty());
    }
    
    @Test
    void assertQueryTablesWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryTables("unknown_db", "public").isEmpty());
    }
    
    @Test
    void assertQueryTable() {
        Optional<ShardingSphereTable> actual = metadataQueryService.queryTable("logic_db", "public", "orders");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("orders"));
        assertTrue(actual.get().getAllColumns().isEmpty());
        assertTrue(actual.get().getAllIndexes().isEmpty());
    }
    
    @Test
    void assertQueryTableWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryTable("unknown_db", "public", "orders").isPresent());
    }
    
    @Test
    void assertQueryTableColumns() {
        List<MCPColumnMetadata> actual = metadataQueryService.queryTableColumns("logic_db", "public", "orders");
        assertThat(actual.stream().map(MCPColumnMetadata::getName).toList(), is(List.of("amount", "order_id")));
        assertThat(actual.get(1).getJdbcType(), is(Types.INTEGER));
    }
    
    @Test
    void assertQueryTableColumnsWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryTableColumns("unknown_db", "public", "orders").isEmpty());
    }
    
    @Test
    void assertQueryTableColumnsWithMissingTable() {
        assertTrue(metadataQueryService.queryTableColumns("logic_db", "public", "missing_table").isEmpty());
    }
    
    @Test
    void assertQueryTableColumn() {
        Optional<MCPColumnMetadata> actual = metadataQueryService.queryTableColumn("logic_db", "public", "orders", "order_id");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("order_id"));
    }
    
    @Test
    void assertQueryTableColumnWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryTableColumn("unknown_db", "public", "orders", "order_id").isPresent());
    }
    
    @Test
    void assertQueryViews() {
        List<ShardingSphereTable> actual = metadataQueryService.queryViews("logic_db", "public");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("orders_view"));
        assertTrue(actual.get(0).getAllColumns().isEmpty());
    }
    
    @Test
    void assertQueryViewsWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryViews("unknown_db", "public").isEmpty());
    }
    
    @Test
    void assertQueryView() {
        Optional<ShardingSphereTable> actual = metadataQueryService.queryView("logic_db", "public", "orders_view");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("orders_view"));
        assertTrue(actual.get().getAllColumns().isEmpty());
    }
    
    @Test
    void assertQueryViewWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryView("unknown_db", "public", "orders_view").isPresent());
    }
    
    @Test
    void assertQueryViewColumns() {
        List<MCPColumnMetadata> actual = metadataQueryService.queryViewColumns("logic_db", "public", "orders_view");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("order_id"));
    }
    
    @Test
    void assertQueryViewColumnsWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryViewColumns("unknown_db", "public", "orders_view").isEmpty());
    }
    
    @Test
    void assertQueryViewColumnsWithMissingView() {
        assertTrue(metadataQueryService.queryViewColumns("logic_db", "public", "missing_view").isEmpty());
    }
    
    @Test
    void assertQueryViewColumn() {
        Optional<MCPColumnMetadata> actual = metadataQueryService.queryViewColumn("logic_db", "public", "orders_view", "order_id");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("order_id"));
    }
    
    @Test
    void assertQueryViewColumnWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryViewColumn("unknown_db", "public", "orders_view", "order_id").isPresent());
    }
    
    @Test
    void assertQuerySchemaColumns() {
        List<MCPColumnMetadata> actual = metadataQueryService.querySchemaColumns("logic_db", "public");
        assertThat(actual.stream().map(each -> each.getRelationName() + "." + each.getName()).toList(),
                is(List.of("order_items.item_id", "orders.amount", "orders.order_id", "orders_view.order_id")));
    }
    
    @Test
    void assertQuerySchemaColumnsWithMissingSchema() {
        assertTrue(metadataQueryService.querySchemaColumns("logic_db", "missing_schema").isEmpty());
    }
    
    @Test
    void assertQuerySchemaColumnsWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.querySchemaColumns("unknown_db", "public").isEmpty());
    }
    
    @Test
    void assertQueryIndexes() {
        List<ShardingSphereIndex> actual = metadataQueryService.queryIndexes("logic_db", "public", "orders");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("order_idx"));
    }
    
    @Test
    void assertQueryIndexesWithoutIndexMetadata() {
        assertTrue(metadataQueryService.queryIndexes("warehouse", "warehouse", "facts").isEmpty());
    }
    
    @Test
    void assertQueryIndexesWithMissingTable() {
        assertTrue(metadataQueryService.queryIndexes("logic_db", "public", "missing_table").isEmpty());
    }
    
    @Test
    void assertQueryIndex() {
        Optional<ShardingSphereIndex> actual = metadataQueryService.queryIndex("logic_db", "public", "orders", "order_idx");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("order_idx"));
    }
    
    @Test
    void assertQueryIndexWithoutIndexMetadata() {
        assertFalse(metadataQueryService.queryIndex("warehouse", "warehouse", "facts", "facts_idx").isPresent());
    }
    
    @Test
    void assertQuerySequences() {
        List<MCPSequenceMetadata> actual = metadataQueryService.querySequences("runtime_db", "public");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getDatabase(), is("runtime_db"));
        assertThat(actual.get(0).getSchema(), is("public"));
        assertThat(actual.get(0).getSequence(), is("order_seq"));
    }
    
    @Test
    void assertQuerySequencesWithUnsupportedSequenceType() {
        assertThat(assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.querySequences("logic_db", "public")).getMessage(),
                is("Sequence resources are not supported for the current database."));
    }
    
    @Test
    void assertQuerySequence() {
        Optional<MCPSequenceMetadata> actual = metadataQueryService.querySequence("runtime_db", "public", "order_seq");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSequence(), is("order_seq"));
    }
    
    @Test
    void assertQuerySequenceWithUnsupportedSequenceType() {
        assertThat(assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.querySequence("logic_db", "public", "order_seq")).getMessage(),
                is("Sequence resources are not supported for the current database."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedMetadataObjectTypeArguments")
    void assertIsSupportedMetadataObjectType(final String name, final String databaseName, final SupportedMCPMetadataObjectType objectType, final boolean expected) {
        assertThat(metadataQueryService.isSupportedMetadataObjectType(databaseName, objectType), is(expected));
    }
    
    private long countTables(final ShardingSphereSchema schema, final TableType tableType) {
        return schema.getAllTables().stream().filter(each -> tableType == each.getType()).count();
    }
    
    private static Stream<Arguments> supportedMetadataObjectTypeArguments() {
        return Stream.of(
                Arguments.of("supported table", "logic_db", SupportedMCPMetadataObjectType.TABLE, true),
                Arguments.of("unsupported sequence", "logic_db", SupportedMCPMetadataObjectType.SEQUENCE, false),
                Arguments.of("missing database", "unknown_db", SupportedMCPMetadataObjectType.TABLE, false));
    }
    
    private static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(3, 1F);
        for (DatabaseFixture each : createDatabaseMetadata()) {
            result.put(each.database(), createRuntimeDatabaseConfiguration(each));
        }
        return result;
    }
    
    private static List<DatabaseFixture> createDatabaseMetadata() {
        return List.of(
                new DatabaseFixture("logic_db", "MySQL", "", List.of(
                        new SchemaFixture("public", List.of(
                                new TableFixture("orders", List.of("order_id", "amount"), List.of("order_idx")),
                                new TableFixture("order_items", List.of("item_id"), List.of())),
                                List.of(new TableFixture("orders_view", List.of("order_id"), List.of())), List.of()))),
                new DatabaseFixture("runtime_db", "PostgreSQL", "", List.of(
                        new SchemaFixture("public", List.of(), List.of(), List.of("order_seq")))),
                new DatabaseFixture("warehouse", "Hive", "", List.of(
                        new SchemaFixture("warehouse", List.of(new TableFixture("facts", List.of(), List.of())), List.of(), List.of()))));
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final DatabaseFixture databaseMetadata) {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        try {
            when(result.openConnection(databaseMetadata.database())).thenAnswer(invocation -> createConnection(databaseMetadata));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }
    
    private static Connection createConnection(final DatabaseFixture databaseMetadata) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseMetadata.databaseVersion());
        when(databaseMetaData.getURL()).thenReturn(SupportDatabaseTypeFactoryMocker.createJdbcUrl(databaseMetadata.databaseType()));
        when(databaseMetaData.getSearchStringEscape()).thenReturn("\\");
        when(databaseMetaData.getTables(nullable(String.class), nullable(String.class), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3, String[].class);
            return createResultSet("TABLE".equals(tableTypes[0]) ? createTableRows(databaseMetadata) : createViewRows(databaseMetadata));
        });
        when(databaseMetaData.getColumns(nullable(String.class), nullable(String.class), anyString(), eq("%")))
                .thenAnswer(invocation -> createResultSet(createColumnRows(databaseMetadata, invocation.getArgument(2, String.class))));
        when(databaseMetaData.getIndexInfo(nullable(String.class), nullable(String.class), anyString(), eq(false), eq(false)))
                .thenAnswer(invocation -> createResultSet(createIndexRows(databaseMetadata, invocation.getArgument(2, String.class))));
        ResultSet sequenceResultSet = createResultSet(createSequenceRows(databaseMetadata));
        when(statement.executeQuery(anyString())).thenReturn(sequenceResultSet);
        return result;
    }
    
    private static List<Map<String, Object>> createTableRows(final DatabaseFixture databaseMetadata) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas()) {
            for (TableFixture table : each.tables()) {
                result.add(Map.of("TABLE_SCHEM", each.schema(), "TABLE_CAT", "", "TABLE_NAME", table.name()));
            }
        }
        return result;
    }
    
    private static List<Map<String, Object>> createViewRows(final DatabaseFixture databaseMetadata) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas()) {
            for (TableFixture view : each.views()) {
                result.add(Map.of("TABLE_SCHEM", each.schema(), "TABLE_CAT", "", "TABLE_NAME", view.name()));
            }
        }
        return result;
    }
    
    private static List<Map<String, Object>> createColumnRows(final DatabaseFixture databaseMetadata, final String objectName) {
        List<Map<String, Object>> result = new LinkedList<>();
        String relationName = "%".equals(objectName) ? "" : unescapePattern(objectName);
        for (SchemaFixture each : databaseMetadata.schemas()) {
            appendColumnRows(result, each.tables(), relationName);
            appendColumnRows(result, each.views(), relationName);
        }
        return result;
    }
    
    private static void appendColumnRows(final List<Map<String, Object>> result, final List<TableFixture> relations, final String relationName) {
        for (TableFixture each : relations) {
            if (relationName.isEmpty() || each.name().equals(relationName)) {
                for (int i = 0; i < each.columns().size(); i++) {
                    result.add(createColumnRow(each.name(), each.columns().get(i), i + 1));
                }
            }
        }
    }
    
    private static String unescapePattern(final String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean escaped = false;
        for (char each : value.toCharArray()) {
            if (escaped) {
                result.append(each);
                escaped = false;
            } else if ('\\' == each) {
                escaped = true;
            } else {
                result.append(each);
            }
        }
        return escaped ? result.append('\\').toString() : result.toString();
    }
    
    private static Map<String, Object> createColumnRow(final String relationName, final String columnName, final int ordinalPosition) {
        return Map.of("TABLE_NAME", relationName, "COLUMN_NAME", columnName, "ORDINAL_POSITION", ordinalPosition,
                "DATA_TYPE", Types.INTEGER, "TYPE_NAME", "INT", "NULLABLE", DatabaseMetaData.columnNullable);
    }
    
    private static List<Map<String, Object>> createIndexRows(final DatabaseFixture databaseMetadata, final String tableName) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas()) {
            for (TableFixture table : each.tables()) {
                if (table.name().equals(tableName)) {
                    for (String index : table.indexes()) {
                        result.add(Map.of("INDEX_NAME", index, "TYPE", DatabaseMetaData.tableIndexOther, "NON_UNIQUE", false,
                                "ORDINAL_POSITION", 1, "COLUMN_NAME", table.columns().isEmpty() ? "" : table.columns().getFirst()));
                    }
                }
            }
        }
        return result;
    }
    
    private static List<Map<String, Object>> createSequenceRows(final DatabaseFixture databaseMetadata) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas()) {
            for (String sequence : each.sequences()) {
                result.add(Map.of("SEQUENCE_SCHEMA", each.schema(), "SEQUENCE_NAME", sequence));
            }
        }
        return result;
    }
    
    private static ResultSet createResultSet(final List<Map<String, Object>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getString(anyString())).thenAnswer(invocation -> {
            Object value = rows.get(rowIndex.get()).get(invocation.getArgument(0, String.class));
            return null == value ? null : value.toString();
        });
        when(result.getInt(anyString())).thenAnswer(invocation -> getNumber(rows, rowIndex.get(), invocation.getArgument(0, String.class)).intValue());
        when(result.getShort(anyString())).thenAnswer(invocation -> getNumber(rows, rowIndex.get(), invocation.getArgument(0, String.class)).shortValue());
        when(result.getBoolean(anyString())).thenAnswer(invocation -> Boolean.TRUE.equals(rows.get(rowIndex.get()).get(invocation.getArgument(0, String.class))));
        return result;
    }
    
    private static Number getNumber(final List<Map<String, Object>> rows, final int rowIndex, final String columnLabel) {
        Object value = rows.get(rowIndex).get(columnLabel);
        return value instanceof Number ? (Number) value : 0;
    }
    
    private record DatabaseFixture(String database, String databaseType, String databaseVersion, List<SchemaFixture> schemas) {
    }
    
    private record SchemaFixture(String schema, List<TableFixture> tables, List<TableFixture> views, List<String> sequences) {
    }
    
    private record TableFixture(String name, List<String> columns, List<String> indexes) {
    }
}

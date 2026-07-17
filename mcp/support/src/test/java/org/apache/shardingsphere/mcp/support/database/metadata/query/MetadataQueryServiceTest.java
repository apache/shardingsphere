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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.context.RequestScopedMetadataContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.fixture.SupportDatabaseTypeFactoryMocker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.sql.Types;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MetadataQueryServiceTest {
    
    private Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private MockedStatic<DatabaseTypeFactory> databaseTypeFactory;
    
    private MockedStatic<TypedSPILoader> typedSPILoader;
    
    private MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader;
    
    private RequestScopedMetadataContext metadataContext;
    
    private MetadataQueryService metadataQueryService;
    
    @BeforeEach
    void setUp() {
        runtimeDatabases = DatabaseTestDataFactory.createRuntimeDatabases();
        databaseTypeFactory = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata();
        typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
        databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class);
        mockDatabaseType("MySQL", false);
        mockDatabaseType("PostgreSQL", true);
        mockDatabaseType("Hive", false);
        MCPDatabaseCapabilityProvider databaseCapabilityProvider = new MCPDatabaseCapabilityProvider(runtimeDatabases);
        metadataContext = new RequestScopedMetadataContext(runtimeDatabases, databaseCapabilityProvider);
        metadataQueryService = new MetadataQueryService(databaseCapabilityProvider, metadataContext);
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
        mockDialectDatabaseMetaData(databaseTypeFromSPI, sequenceSupported);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseTypeFromSPI)).thenReturn(Optional.empty());
    }
    
    private void mockDialectDatabaseMetaData(final DatabaseType databaseType, final boolean sequenceSupported) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null, DialectSchemaSemantics.NATIVE_SCHEMA));
        when(result.getSequenceOption()).thenReturn(
                sequenceSupported ? Optional.of(new DialectSequenceOption("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM TEST_SEQUENCES")) : Optional.empty());
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
        List<ShardingSphereSequence> actual = metadataQueryService.querySequences("runtime_db", "public");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("order_seq"));
    }
    
    @Test
    void assertQuerySequencesWithUnsupportedSequenceType() {
        assertThat(assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.querySequences("logic_db", "public")).getMessage(),
                is("Sequence resources are not supported for the current database."));
    }
    
    @Test
    void assertQuerySequence() {
        Optional<ShardingSphereSequence> actual = metadataQueryService.querySequence("runtime_db", "public", "order_seq");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("order_seq"));
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
}

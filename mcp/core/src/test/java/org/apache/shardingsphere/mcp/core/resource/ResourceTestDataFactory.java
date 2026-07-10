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

package org.apache.shardingsphere.mcp.core.resource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceTestDataFactory {
    
    private static final String JDBC_URL_PREFIX = "jdbc:mcp-fixture:";
    
    /**
     * Create default database metadata.
     *
     * @return default database metadata
     */
    public static List<DatabaseMetadataFixture> createDatabaseMetadata() {
        return List.of(
                createDatabase("logic_db", "MySQL", "",
                        List.of(createSchema("public",
                                List.of(createTable("orders", List.of("order_id"), List.of("order_idx")),
                                        createTable("order_items", List.of("item_id"), List.of())),
                                List.of(createTable("orders_view", List.of("order_id"), List.of())), List.of()))),
                createDatabase("runtime_db", "PostgreSQL", "",
                        List.of(createSchema("public", List.of(), List.of(), List.of("order_seq")))),
                createDatabase("warehouse", "Hive", "",
                        List.of(createSchema("warehouse", List.of(createTable("facts", List.of(), List.of())), List.of(), List.of()))));
    }
    
    /**
     * Create database fixture.
     *
     * @param database database
     * @param databaseType database type
     * @param databaseVersion database version
     * @param schemas schemas
     * @return database fixture
     */
    public static DatabaseMetadataFixture createDatabase(final String database, final String databaseType, final String databaseVersion,
                                                         final List<SchemaMetadataFixture> schemas) {
        return new DatabaseMetadataFixture(database, databaseType, databaseVersion, schemas);
    }
    
    /**
     * Create schema fixture.
     *
     * @param schema schema
     * @param tables tables
     * @param views views
     * @param sequences sequences
     * @return schema fixture
     */
    public static SchemaMetadataFixture createSchema(final String schema, final List<TableMetadataFixture> tables,
                                                     final List<TableMetadataFixture> views, final List<String> sequences) {
        return new SchemaMetadataFixture(schema, tables, views, sequences);
    }
    
    /**
     * Create table or view fixture.
     *
     * @param name table or view name
     * @param columns columns
     * @param indexes indexes
     * @return table or view fixture
     */
    public static TableMetadataFixture createTable(final String name, final List<String> columns, final List<String> indexes) {
        return new TableMetadataFixture(name, columns, indexes);
    }
    
    /**
     * Create runtime context from metadata.
     *
     * @param databaseMetadataList database metadata list
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext(final List<DatabaseMetadataFixture> databaseMetadataList) {
        return createRuntimeContext(databaseMetadataList, "http");
    }
    
    /**
     * Create runtime context from metadata.
     *
     * @param databaseMetadataList database metadata list
     * @param activeTransport active MCP transport
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext(final List<DatabaseMetadataFixture> databaseMetadataList, final String activeTransport) {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>(databaseMetadataList.size(), 1F);
        for (DatabaseMetadataFixture each : databaseMetadataList) {
            runtimeDatabases.put(each.database, createRuntimeDatabaseConfiguration(each));
        }
        return new MCPRuntimeContext(new MCPSessionManager(runtimeDatabases), createDatabaseCapabilityProvider(runtimeDatabases, databaseMetadataList), activeTransport);
    }
    
    /**
     * Create default runtime context.
     *
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext() {
        return createRuntimeContext(createDatabaseMetadata());
    }
    
    /**
     * Create request scope fixture from metadata.
     *
     * @param databaseMetadataList database metadata list
     * @return request scope fixture
     */
    public static RequestScopeFixture createRequestScopeFixture(final List<DatabaseMetadataFixture> databaseMetadataList) {
        return createRequestScopeFixture(createRuntimeContext(databaseMetadataList), databaseMetadataList);
    }
    
    /**
     * Create request scope fixture from runtime context and metadata.
     *
     * @param runtimeContext runtime context
     * @param databaseMetadataList database metadata list
     * @return request scope fixture
     */
    public static RequestScopeFixture createRequestScopeFixture(final MCPRuntimeContext runtimeContext, final List<DatabaseMetadataFixture> databaseMetadataList) {
        MetadataSPIMocks metadataSPIMocks = mockMetadataSPI(databaseMetadataList);
        return new RequestScopeFixture(new MCPRequestScope(runtimeContext), metadataSPIMocks);
    }
    
    private static MCPDatabaseCapabilityProvider createDatabaseCapabilityProvider(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases,
                                                                                  final List<DatabaseMetadataFixture> databaseMetadataList) {
        try (
                MockedStatic<DatabaseTypeFactory> ignored = mockDatabaseTypeFactoryByConnectionMetadata();
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockDatabaseTypes(createSequenceSupportByDatabaseType(databaseMetadataList), typedSPILoader, databaseTypedSPILoader);
            return new MCPDatabaseCapabilityProvider(runtimeDatabases);
        }
    }
    
    private static MetadataSPIMocks mockMetadataSPI(final List<DatabaseMetadataFixture> databaseMetadataList) {
        MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
        MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class);
        mockDatabaseTypes(createSequenceSupportByDatabaseType(databaseMetadataList), typedSPILoader, databaseTypedSPILoader);
        return new MetadataSPIMocks(typedSPILoader, databaseTypedSPILoader);
    }
    
    private static Map<String, Boolean> createSequenceSupportByDatabaseType(final List<DatabaseMetadataFixture> databaseMetadataList) {
        Map<String, Boolean> result = new LinkedHashMap<>(databaseMetadataList.size(), 1F);
        for (DatabaseMetadataFixture each : databaseMetadataList) {
            result.merge(each.databaseType, containsSequence(each), Boolean::logicalOr);
        }
        return result;
    }
    
    private static boolean containsSequence(final DatabaseMetadataFixture databaseMetadata) {
        for (SchemaMetadataFixture each : databaseMetadata.schemas) {
            if (!each.sequences.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    private static void mockDatabaseTypes(final Map<String, Boolean> sequenceSupportByDatabaseType, final MockedStatic<TypedSPILoader> typedSPILoader,
                                          final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        for (Entry<String, Boolean> entry : sequenceSupportByDatabaseType.entrySet()) {
            mockDatabaseType(entry.getKey(), entry.getValue(), typedSPILoader, databaseTypedSPILoader);
        }
    }
    
    private static void mockDatabaseType(final String databaseType, final boolean sequenceSupported, final MockedStatic<TypedSPILoader> typedSPILoader,
                                         final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getType()).thenReturn(databaseType);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        typedSPILoader.when(() -> TypedSPILoader.getService(DatabaseType.class, databaseType)).thenReturn(databaseTypeFromSPI);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getTransactionOption()).thenReturn(
                new DialectTransactionOption(false, false, true, false, true, Connection.TRANSACTION_READ_COMMITTED, false, false, List.of()));
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(new DefaultSchemaOption(true, null));
        when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(
                sequenceSupported ? Optional.of(new DialectSequenceOption("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM TEST_SEQUENCES")) : Optional.empty());
        when(dialectDatabaseMetaData.getExplainOption()).thenReturn(() -> true);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseTypeFromSPI)).thenReturn(Optional.of(dialectDatabaseMetaData));
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseTypeFromSPI)).thenReturn(dialectDatabaseMetaData);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseTypeFromSPI)).thenReturn(Optional.empty());
    }
    
    private static MockedStatic<DatabaseTypeFactory> mockDatabaseTypeFactoryByConnectionMetadata() {
        MockedStatic<DatabaseTypeFactory> result = mockStatic(DatabaseTypeFactory.class, CALLS_REAL_METHODS);
        result.when(() -> DatabaseTypeFactory.get(any(DatabaseMetaData.class)))
                .thenAnswer(invocation -> createDatabaseType(invocation.getArgument(0, DatabaseMetaData.class).getURL()));
        return result;
    }
    
    private static DatabaseType createDatabaseType(final String url) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getType()).thenReturn(resolveTypeByURL(url));
        return result;
    }
    
    private static String resolveTypeByURL(final String url) {
        String actualURL = Objects.toString(url, "");
        if (!actualURL.startsWith(JDBC_URL_PREFIX)) {
            return "";
        }
        String databaseType = actualURL.substring(JDBC_URL_PREFIX.length());
        int delimiterIndex = databaseType.indexOf(':');
        return -1 == delimiterIndex ? databaseType : databaseType.substring(0, delimiterIndex);
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final DatabaseMetadataFixture databaseMetadata) {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        try {
            when(result.openConnection(databaseMetadata.database)).thenAnswer(invocation -> createConnection(databaseMetadata));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }
    
    private static Connection createConnection(final DatabaseMetadataFixture databaseMetadata) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseMetadata.databaseVersion);
        when(databaseMetaData.getURL()).thenReturn(createJdbcUrl(databaseMetadata.databaseType));
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
    
    private static String createJdbcUrl(final String databaseType) {
        return JDBC_URL_PREFIX + databaseType + ":test";
    }
    
    private static List<Map<String, String>> createTableRows(final DatabaseMetadataFixture databaseMetadata) {
        List<Map<String, String>> result = new LinkedList<>();
        for (SchemaMetadataFixture each : databaseMetadata.schemas) {
            for (TableMetadataFixture table : each.tables) {
                result.add(Map.of("TABLE_SCHEM", each.schema, "TABLE_CAT", "", "TABLE_NAME", table.name));
            }
        }
        return result;
    }
    
    private static List<Map<String, String>> createViewRows(final DatabaseMetadataFixture databaseMetadata) {
        List<Map<String, String>> result = new LinkedList<>();
        for (SchemaMetadataFixture each : databaseMetadata.schemas) {
            for (TableMetadataFixture view : each.views) {
                result.add(Map.of("TABLE_SCHEM", each.schema, "TABLE_CAT", "", "TABLE_NAME", view.name));
            }
        }
        return result;
    }
    
    private static List<Map<String, String>> createColumnRows(final DatabaseMetadataFixture databaseMetadata, final String objectName) {
        List<Map<String, String>> result = new LinkedList<>();
        for (SchemaMetadataFixture each : databaseMetadata.schemas) {
            appendColumnRows(result, each.tables, objectName);
            appendColumnRows(result, each.views, objectName);
        }
        return result;
    }
    
    private static void appendColumnRows(final List<Map<String, String>> result, final List<TableMetadataFixture> objects, final String objectName) {
        for (TableMetadataFixture each : objects) {
            if (each.name.equals(objectName)) {
                for (String column : each.columns) {
                    result.add(Map.of("COLUMN_NAME", column));
                }
            }
        }
    }
    
    private static List<Map<String, String>> createIndexRows(final DatabaseMetadataFixture databaseMetadata, final String tableName) {
        List<Map<String, String>> result = new LinkedList<>();
        for (SchemaMetadataFixture each : databaseMetadata.schemas) {
            for (TableMetadataFixture table : each.tables) {
                if (table.name.equals(tableName)) {
                    for (String index : table.indexes) {
                        result.add(Map.of("INDEX_NAME", index));
                    }
                }
            }
        }
        return result;
    }
    
    private static List<Map<String, String>> createSequenceRows(final DatabaseMetadataFixture databaseMetadata) {
        List<Map<String, String>> result = new LinkedList<>();
        for (SchemaMetadataFixture each : databaseMetadata.schemas) {
            for (String sequence : each.sequences) {
                result.add(Map.of("SEQUENCE_SCHEMA", each.schema, "SEQUENCE_NAME", sequence));
            }
        }
        return result;
    }
    
    private static ResultSet createResultSet(final List<Map<String, String>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getString(anyString())).thenAnswer(invocation -> rows.get(rowIndex.get()).get(invocation.getArgument(0, String.class)));
        return result;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class RequestScopeFixture implements AutoCloseable {
        
        @Getter
        private final MCPRequestScope requestScope;
        
        private final MetadataSPIMocks metadataSPIMocks;
        
        @Override
        public void close() {
            try {
                requestScope.close();
            } finally {
                metadataSPIMocks.close();
            }
        }
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MetadataSPIMocks implements AutoCloseable {
        
        private final MockedStatic<TypedSPILoader> typedSPILoader;
        
        private final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader;
        
        @Override
        public void close() {
            databaseTypedSPILoader.close();
            typedSPILoader.close();
        }
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class DatabaseMetadataFixture {
        
        private final String database;
        
        private final String databaseType;
        
        private final String databaseVersion;
        
        private final List<SchemaMetadataFixture> schemas;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SchemaMetadataFixture {
        
        private final String schema;
        
        private final List<TableMetadataFixture> tables;
        
        private final List<TableMetadataFixture> views;
        
        private final List<String> sequences;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TableMetadataFixture {
        
        private final String name;
        
        private final List<String> columns;
        
        private final List<String> indexes;
    }
}

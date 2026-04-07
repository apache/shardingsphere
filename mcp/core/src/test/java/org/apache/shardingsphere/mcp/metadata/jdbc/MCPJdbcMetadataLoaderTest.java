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

package org.apache.shardingsphere.mcp.metadata.jdbc;

import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPJdbcMetadataLoaderTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoad() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        assertThat(actual.findMetadata("logic_db").map(MCPDatabaseMetadata::getDatabaseType).orElseThrow(), is("H2"));
        assertFalse(actual.findMetadata("logic_db").orElseThrow().getDatabaseVersion().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadTypedMetadataArguments")
    void assertLoadWithTypedMetadata(final String name, final MetadataObjectType objectType, final String objectName) throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-" + objectName);
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        assertTrue(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), objectType, objectName));
    }
    
    @Test
    void assertLoadWithMultipleLogicalDatabases() throws SQLException {
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        Map<String, RuntimeDatabaseConfiguration> connectionConfigs = Map.of(
                "logic_db", createRuntimeDatabaseConfiguration(firstJdbcUrl), "analytics_db", createRuntimeDatabaseConfiguration(secondJdbcUrl));
        MCPDatabaseMetadataCatalog actual = metadataLoader.load(connectionConfigs);
        assertThat(actual.getDatabaseMetadataMap().size(), is(2));
        assertTrue(actual.findMetadata("analytics_db").isPresent());
    }
    
    @Test
    void assertLoadWithoutSchemaObjects() throws SQLException {
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        Driver mockDriver = new MockDriver("jdbc:mock:no-schema", createConnectionWithoutSchema());
        DriverManager.registerDriver(mockDriver);
        try {
            MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", new RuntimeDatabaseConfiguration("MySQL", "jdbc:mock:no-schema", "", "", "")));
            MCPDatabaseMetadata databaseMetadata = actual.findMetadata("logic_db").orElseThrow();
            assertThat(databaseMetadata.getSchemas().size(), is(1));
            assertThat(databaseMetadata.getSchemas().get(0).getSchema(), is(""));
            assertTrue(containsMetadata(databaseMetadata, MetadataObjectType.TABLE, "orders"));
            assertTrue(containsMetadata(databaseMetadata, MetadataObjectType.COLUMN, "order_id"));
            assertThat(databaseMetadata.getDatabaseVersion(), is(""));
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }
    
    @Test
    void assertLoadWithSchemaRegisteredOnce() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-shared-schema");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        MCPDatabaseMetadata databaseMetadata = actual.findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(databaseMetadata, MetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(databaseMetadata, MetadataObjectType.VIEW, "active_orders"));
        assertThat(databaseMetadata.getSchemas().size(), is(1));
    }
    
    @Test
    void assertLoadWithoutDatabaseTypeSequenceMetadata() throws SQLException {
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        Driver mockDriver = new MockDriver("jdbc:mock:null-database-type", createConnectionWithoutSchema());
        DriverManager.registerDriver(mockDriver);
        try {
            MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", new RuntimeDatabaseConfiguration("MySQL", "jdbc:mock:null-database-type", "", "", "")));
            assertTrue(actual.findMetadata("logic_db").isPresent());
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }
    
    @Test
    void assertLoadWithFailedSequenceMetadataQuery() throws SQLException {
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        Driver mockDriver = new MockDriver("jdbc:mock:failed-sequence-query", createConnectionWithFailedSequenceMetadataQuery());
        DriverManager.registerDriver(mockDriver);
        try {
            MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", new RuntimeDatabaseConfiguration("PostgreSQL", "jdbc:mock:failed-sequence-query", "", "", "")));
            assertTrue(actual.findMetadata("logic_db").isPresent());
            assertFalse(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), MetadataObjectType.SEQUENCE, "order_seq"));
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadSequenceDialectArguments")
    void assertLoadWithDialectSequenceMetadata(final String name, final String databaseType, final String sequenceSchema,
                                               final String sequenceName, final String sequenceQuery) throws SQLException {
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        String jdbcUrl = "jdbc:mock:sequence:" + databaseType.toLowerCase(Locale.ENGLISH);
        Driver mockDriver = new MockDriver(jdbcUrl, createConnectionWithSequenceMetadata(sequenceSchema, sequenceName, sequenceQuery));
        DriverManager.registerDriver(mockDriver);
        try {
            MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", new RuntimeDatabaseConfiguration(databaseType, jdbcUrl, "", "", "")));
            assertTrue(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), MetadataObjectType.SEQUENCE, sequenceName));
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver");
    }
    
    private static Stream<Arguments> loadTypedMetadataArguments() {
        return Stream.of(
                Arguments.of("table orders", MetadataObjectType.TABLE, "orders"),
                Arguments.of("table order_items", MetadataObjectType.TABLE, "order_items"),
                Arguments.of("view active_orders", MetadataObjectType.VIEW, "active_orders"),
                Arguments.of("column status", MetadataObjectType.COLUMN, "status"),
                Arguments.of("index idx_orders_status", MetadataObjectType.INDEX, "idx_orders_status"),
                Arguments.of("sequence order_seq", MetadataObjectType.SEQUENCE, "order_seq"));
    }
    
    private static Stream<Arguments> loadSequenceDialectArguments() {
        return Stream.of(
                Arguments.of("postgresql", "PostgreSQL", "public", "order_seq",
                        "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences"),
                Arguments.of("open gauss", "openGauss", "public", "order_seq",
                        "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences"),
                Arguments.of("sql server", "SQLServer", "dbo", "order_seq",
                        "SELECT schemas.name AS SEQUENCE_SCHEMA, seq.name AS SEQUENCE_NAME FROM sys.sequences seq INNER JOIN sys.schemas schemas ON seq.schema_id = schemas.schema_id"),
                Arguments.of("oracle", "Oracle", "APP", "ORDER_SEQ",
                        "SELECT USER AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM USER_SEQUENCES"),
                Arguments.of("mariadb", "MariaDB", "logic_db", "order_seq",
                        "SELECT TABLE_SCHEMA AS SEQUENCE_SCHEMA, TABLE_NAME AS SEQUENCE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'SEQUENCE'"),
                Arguments.of("firebird", "Firebird", "", "ORDER_SEQ",
                        "SELECT '' AS SEQUENCE_SCHEMA, TRIM(RDB$GENERATOR_NAME) AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE COALESCE(RDB$SYSTEM_FLAG, 0) = 0"),
                Arguments.of("h2", "H2", "PUBLIC", "ORDER_SEQ",
                        "SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES"));
    }
    
    private Connection createConnectionWithoutSchema() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME", "orders");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        ResultSet columnResultSet = mockResultSet("COLUMN_NAME", "order_id");
        ResultSet indexResultSet = mockResultSet("INDEX_NAME");
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(databaseMetaData.getColumns(isNull(), isNull(), eq("orders"), eq("%"))).thenReturn(columnResultSet);
        when(databaseMetaData.getIndexInfo(isNull(), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(indexResultSet);
        return result;
    }
    
    private Connection createConnectionWithSequenceMetadata(final String sequenceSchema, final String sequenceName, final String sequenceQuery) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        ResultSet sequenceResultSet = mockSingleRowResultSet(Map.of("SEQUENCE_SCHEMA", sequenceSchema, "SEQUENCE_NAME", sequenceName));
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(statement.executeQuery(sequenceQuery)).thenReturn(sequenceResultSet);
        return result;
    }
    
    private Connection createConnectionWithFailedSequenceMetadataQuery() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(statement.executeQuery("SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences"))
                .thenThrow(new SQLException("sequence metadata query failed"));
        return result;
    }
    
    private ResultSet mockResultSet(final String columnName, final String... values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger nextIndex = new AtomicInteger();
        AtomicInteger valueIndex = new AtomicInteger();
        when(result.next()).thenAnswer(invocation -> nextIndex.getAndIncrement() < values.length);
        when(result.getString(columnName)).thenAnswer(invocation -> values[valueIndex.getAndIncrement()]);
        return result;
    }
    
    private ResultSet mockSingleRowResultSet(final Map<String, String> values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        for (Entry<String, String> entry : values.entrySet()) {
            when(result.getString(entry.getKey())).thenReturn(entry.getValue());
        }
        return result;
    }
    
    private boolean containsMetadata(final MCPDatabaseMetadata databaseMetadata, final MetadataObjectType objectType, final String objectName) {
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            if (MetadataObjectType.SCHEMA == objectType && objectName.equals(each.getSchema())) {
                return true;
            }
            for (MCPTableMetadata table : each.getTables()) {
                if (MetadataObjectType.TABLE == objectType && objectName.equals(table.getTable())) {
                    return true;
                }
                for (MCPColumnMetadata column : table.getColumns()) {
                    if (MetadataObjectType.COLUMN == objectType && objectName.equals(column.getColumn())) {
                        return true;
                    }
                }
                for (MCPIndexMetadata index : table.getIndexes()) {
                    if (MetadataObjectType.INDEX == objectType && objectName.equals(index.getIndex())) {
                        return true;
                    }
                }
            }
            for (MCPViewMetadata view : each.getViews()) {
                if (MetadataObjectType.VIEW == objectType && objectName.equals(view.getView())) {
                    return true;
                }
                for (MCPColumnMetadata column : view.getColumns()) {
                    if (MetadataObjectType.COLUMN == objectType && objectName.equals(column.getColumn())) {
                        return true;
                    }
                }
            }
            for (MCPSequenceMetadata sequence : each.getSequences()) {
                if (MetadataObjectType.SEQUENCE == objectType && objectName.equals(sequence.getSequence())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static final class MockDriver implements Driver {
        
        private final String jdbcUrl;
        
        private final Connection connection;
        
        private MockDriver(final String jdbcUrl, final Connection connection) {
            this.jdbcUrl = jdbcUrl;
            this.connection = connection;
        }
        
        @Override
        public Connection connect(final String url, final Properties info) {
            return acceptsURL(url) ? connection : null;
        }
        
        @Override
        public boolean acceptsURL(final String url) {
            return jdbcUrl.equals(url);
        }
        
        @Override
        public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
            return new DriverPropertyInfo[0];
        }
        
        @Override
        public int getMajorVersion() {
            return 1;
        }
        
        @Override
        public int getMinorVersion() {
            return 0;
        }
        
        @Override
        public boolean jdbcCompliant() {
            return false;
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("Mock driver does not expose a parent logger.");
        }
    }
}

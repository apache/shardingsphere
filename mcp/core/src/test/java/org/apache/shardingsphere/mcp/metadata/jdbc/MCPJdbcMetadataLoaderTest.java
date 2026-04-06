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
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
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
import java.util.Map;
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
        assertThat(actual.findDatabaseType("logic_db").orElseThrow(), is("H2"));
        assertFalse(actual.findDatabaseMetadata("logic_db").orElseThrow().getDatabaseVersion().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadTypedMetadataArguments")
    void assertLoadWithTypedMetadata(final String name, final MetadataObjectType objectType, final String objectName) throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-" + objectName);
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        MCPDatabaseMetadataCatalog actual = metadataLoader.load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        assertTrue(containsMetadata(actual.findDatabaseMetadata("logic_db").orElseThrow(), objectType, objectName));
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
            MCPDatabaseMetadata databaseMetadata = actual.findDatabaseMetadata("logic_db").orElseThrow();
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
        MCPDatabaseMetadata databaseMetadata = actual.findDatabaseMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(databaseMetadata, MetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(databaseMetadata, MetadataObjectType.VIEW, "active_orders"));
        assertThat(databaseMetadata.getSchemas().size(), is(1));
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
                Arguments.of("index idx_orders_status", MetadataObjectType.INDEX, "idx_orders_status"));
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
    
    private ResultSet mockResultSet(final String columnName, final String... values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger nextIndex = new AtomicInteger();
        AtomicInteger valueIndex = new AtomicInteger();
        when(result.next()).thenAnswer(invocation -> nextIndex.getAndIncrement() < values.length);
        when(result.getString(columnName)).thenAnswer(invocation -> values[valueIndex.getAndIncrement()]);
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

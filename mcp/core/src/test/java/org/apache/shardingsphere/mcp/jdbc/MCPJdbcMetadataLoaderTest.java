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

package org.apache.shardingsphere.mcp.jdbc;

import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.RuntimeDatabaseDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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
        MetadataCatalog actual = metadataLoader.load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        assertThat(actual.getDatabaseTypes().get("logic_db"), is("H2"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.TABLE, "order_items"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.VIEW, "active_orders"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.COLUMN, "status"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.INDEX, "idx_orders_status"));
        RuntimeDatabaseDescriptor runtimeDatabaseDescriptor = actual.findRuntimeDatabaseDescriptor("logic_db").orElseThrow();
        assertThat(runtimeDatabaseDescriptor.getDefaultSchema(), is("public"));
        assertTrue(runtimeDatabaseDescriptor.getDiscoveredMetadataObjectTypes().contains(MetadataObjectType.TABLE));
        assertTrue(runtimeDatabaseDescriptor.getDiscoveredMetadataObjectTypes().contains(MetadataObjectType.VIEW));
        assertTrue(runtimeDatabaseDescriptor.getDiscoveredMetadataObjectTypes().contains(MetadataObjectType.INDEX));
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
        MetadataCatalog actual = metadataLoader.load(connectionConfigs);
        assertThat(actual.getDatabaseTypes().size(), is(2));
        assertThat(actual.getRuntimeDatabaseDescriptors().size(), is(2));
        assertTrue(actual.findRuntimeDatabaseDescriptor("analytics_db").isPresent());
    }
    
    @Test
    void assertLoadWithoutSchemaObjects() throws SQLException {
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        Driver mockDriver = new MockDriver("jdbc:mock:no-schema", createConnectionWithoutSchema());
        DriverManager.registerDriver(mockDriver);
        try {
            MetadataCatalog actual = metadataLoader.load(Map.of("logic_db", new RuntimeDatabaseConfiguration("MySQL", "jdbc:mock:no-schema", "", "", "")));
            assertFalse(actual.getMetadataObjects().stream().anyMatch(each -> MetadataObjectType.SCHEMA == each.getObjectType()));
            assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.TABLE, "orders"));
            assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.COLUMN, "order_id"));
            RuntimeDatabaseDescriptor runtimeDatabaseDescriptor = actual.findRuntimeDatabaseDescriptor("logic_db").orElseThrow();
            assertThat(runtimeDatabaseDescriptor.getDefaultSchema(), is(""));
            assertFalse(runtimeDatabaseDescriptor.getDiscoveredMetadataObjectTypes().contains(MetadataObjectType.SCHEMA));
            assertTrue(runtimeDatabaseDescriptor.getDiscoveredMetadataObjectTypes().contains(MetadataObjectType.TABLE));
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver");
    }
    
    private Connection createConnectionWithoutSchema() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet schemaResultSet = mockResultSet("TABLE_SCHEM");
        ResultSet tableResultSet = mockResultSet("TABLE_NAME", "orders");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        ResultSet columnResultSet = mockResultSet("COLUMN_NAME", "order_id");
        ResultSet indexResultSet = mockResultSet("INDEX_NAME");
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.getSchema()).thenReturn(null);
        when(databaseMetaData.getSchemas()).thenReturn(schemaResultSet);
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
    
    private boolean containsMetadataObject(final List<MetadataObject> metadataObjects, final MetadataObjectType objectType, final String objectName) {
        return metadataObjects.stream().anyMatch(each -> objectType == each.getObjectType() && objectName.equals(each.getName()));
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

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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory.DatabaseConnectionConfiguration;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.SupportedObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.RuntimeDatabaseDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcMetadataLoaderTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoad() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
        MetadataCatalog actual = metadataLoader.load(Map.of("logic_db", createConnectionConfiguration("logic_db", jdbcUrl)));
        assertThat(actual.getDatabaseTypes().get("logic_db"), is("H2"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.TABLE, "order_items"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.VIEW, "active_orders"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.COLUMN, "status"));
        assertTrue(containsMetadataObject(actual.getMetadataObjects(), MetadataObjectType.INDEX, "idx_orders_status"));
        RuntimeDatabaseDescriptor runtimeDatabaseDescriptor = actual.findRuntimeDatabaseDescriptor("logic_db").orElseThrow();
        assertThat(runtimeDatabaseDescriptor.getDefaultSchema(), is("public"));
        assertTrue(runtimeDatabaseDescriptor.getSupportedObjectTypes().contains(SupportedObjectType.TABLE));
        assertTrue(runtimeDatabaseDescriptor.getSupportedObjectTypes().contains(SupportedObjectType.VIEW));
        assertTrue(runtimeDatabaseDescriptor.getSupportedObjectTypes().contains(SupportedObjectType.INDEX));
    }
    
    @Test
    void assertLoadWithMultipleLogicalDatabases() throws SQLException {
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
        Map<String, DatabaseConnectionConfiguration> connectionConfigs = new LinkedHashMap<>();
        connectionConfigs.put("logic_db", createConnectionConfiguration("logic_db", firstJdbcUrl));
        connectionConfigs.put("analytics_db", createConnectionConfiguration("analytics_db", secondJdbcUrl));
        MetadataCatalog actual = metadataLoader.load(connectionConfigs);
        assertThat(actual.getDatabaseTypes().size(), is(2));
        assertThat(actual.getRuntimeDatabaseDescriptors().size(), is(2));
        assertTrue(actual.findRuntimeDatabaseDescriptor("analytics_db").isPresent());
    }
    
    private DatabaseConnectionConfiguration createConnectionConfiguration(final String database, final String jdbcUrl) {
        return new DatabaseConnectionConfiguration(database, "H2", jdbcUrl, "", "", "org.h2.Driver", "public", "public", true, false);
    }
    
    private boolean containsMetadataObject(final List<MetadataObject> metadataObjects, final MetadataObjectType objectType, final String objectName) {
        return metadataObjects.stream().anyMatch(each -> objectType == each.getObjectType() && objectName.equals(each.getName()));
    }
}

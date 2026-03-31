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

import org.apache.shardingsphere.mcp.execute.DatabaseExecutionBackend;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPJdbcDatabaseRuntimeFactoryTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertCreateDatabaseRuntimeRefreshMetadataForTargetDatabase() throws SQLException {
        MCPJdbcDatabaseRuntimeFactory databaseRuntimeFactory = new MCPJdbcDatabaseRuntimeFactory();
        MCPJdbcMetadataLoader jdbcMetadataLoader = new MCPJdbcMetadataLoader();
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "factory-refresh-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "factory-refresh-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        Map<String, RuntimeDatabaseConfiguration> connectionConfigs = Map.of(
                "logic_db", new RuntimeDatabaseConfiguration("H2", firstJdbcUrl, "", "", "org.h2.Driver"),
                "analytics_db", new RuntimeDatabaseConfiguration("H2", secondJdbcUrl, "", "", "org.h2.Driver"));
        DatabaseMetadataSnapshots databaseMetadataSnapshots = jdbcMetadataLoader.load(connectionConfigs);
        DatabaseExecutionBackend actual = databaseRuntimeFactory.create(connectionConfigs, databaseMetadataSnapshots);
        H2RuntimeTestSupport.executeStatements(firstJdbcUrl, "CREATE TABLE public.orders_archive (order_id INT PRIMARY KEY)");
        actual.refreshMetadata("logic_db");
        assertTrue(databaseMetadataSnapshots.getDatabaseTypes().containsKey("analytics_db"));
        assertThat(databaseMetadataSnapshots.getMetadataObjects().stream().filter(each -> "logic_db".equals(each.getDatabase())).map(MetadataObject::getName).toList(), hasItems("orders_archive"));
        assertThat(databaseMetadataSnapshots.getMetadataObjects().stream().filter(each -> "analytics_db".equals(each.getDatabase())).map(MetadataObject::getName).toList(), hasItems("orders"));
    }
    
    @Test
    void assertCreateDatabaseRuntimePreserveSnapshotWhenRefreshFails() {
        MCPJdbcDatabaseRuntimeFactory databaseRuntimeFactory = new MCPJdbcDatabaseRuntimeFactory();
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabaseConfigs = Map.of(
                "logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.example.MissingDriver"));
        DatabaseMetadataSnapshots databaseMetadataSnapshots = new DatabaseMetadataSnapshots(Map.of("logic_db",
                new DatabaseMetadataSnapshot("H2", "", List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", "")))));
        DatabaseExecutionBackend actual = databaseRuntimeFactory.create(runtimeDatabaseConfigs, databaseMetadataSnapshots);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> actual.refreshMetadata("logic_db"));
        assertThat(ex.getMessage(), is("JDBC driver `org.example.MissingDriver` is not available for database `logic_db`."));
        assertTrue(databaseMetadataSnapshots.getDatabaseTypes().containsKey("logic_db"));
        assertThat(databaseMetadataSnapshots.getMetadataObjects().stream().map(MetadataObject::getName).toList(), hasItems("orders"));
    }
}

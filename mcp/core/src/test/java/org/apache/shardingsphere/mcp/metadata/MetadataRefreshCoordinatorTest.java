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

package org.apache.shardingsphere.mcp.metadata;

import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataRefreshCoordinatorTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertRefresh() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-refresh-coordinator");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        DatabaseMetadataSnapshots databaseMetadataSnapshots = metadataLoader.load(runtimeDatabases);
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator(runtimeDatabases, databaseMetadataSnapshots, metadataLoader);
        H2RuntimeTestSupport.executeStatements(jdbcUrl, "CREATE TABLE public.orders_archive (order_id INT PRIMARY KEY)");
        coordinator.refresh("logic_db");
        assertTrue(databaseMetadataSnapshots.getDatabaseTypes().containsKey("logic_db"));
        assertThat(databaseMetadataSnapshots.getMetadataObjects().stream().map(MetadataObject::getName).toList(), hasItems("orders_archive"));
    }
    
    @Test
    void assertRefreshWithUnconfiguredDatabase() {
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator(Map.of(), new DatabaseMetadataSnapshots(Map.of()), new MCPJdbcMetadataLoader());
        assertThat(assertThrows(IllegalArgumentException.class, () -> coordinator.refresh("logic_db")).getMessage(), is("Database `logic_db` is not configured."));
    }
}

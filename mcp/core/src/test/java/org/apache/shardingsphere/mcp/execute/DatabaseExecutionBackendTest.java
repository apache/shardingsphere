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

package org.apache.shardingsphere.mcp.execute;

import org.apache.shardingsphere.mcp.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseExecutionBackendTest {
    
    @Test
    void assertRefreshMetadata() {
        MCPJdbcExecutionAdapter executionAdapter = mock(MCPJdbcExecutionAdapter.class);
        MCPJdbcMetadataLoader metadataLoader = mock(MCPJdbcMetadataLoader.class);
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "");
        Map<String, DatabaseMetadataSnapshot> databaseSnapshots = new LinkedHashMap<>(1, 1F);
        databaseSnapshots.put("logic_db", new DatabaseMetadataSnapshot("H2", "", List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""))));
        DatabaseMetadataSnapshots databaseMetadataSnapshots = new DatabaseMetadataSnapshots(databaseSnapshots);
        when(metadataLoader.load(anyMap())).thenReturn(new DatabaseMetadataSnapshots(Map.of("logic_db",
                new DatabaseMetadataSnapshot("H2", "", List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders_archive", "", ""))))));
        DatabaseExecutionBackend backend = new DatabaseExecutionBackend(executionAdapter, metadataLoader, Map.of("logic_db", runtimeDatabaseConfig), databaseMetadataSnapshots);
        backend.refreshMetadata("logic_db");
        assertTrue(databaseMetadataSnapshots.getDatabaseTypes().containsKey("logic_db"));
        assertThat(databaseMetadataSnapshots.getMetadataObjects().stream().map(MetadataObject::getName).toList(), hasItems("orders_archive"));
    }
    
    @Test
    void assertRefreshMetadataWithUnconfiguredDatabase() {
        DatabaseExecutionBackend backend = new DatabaseExecutionBackend(mock(MCPJdbcExecutionAdapter.class), mock(MCPJdbcMetadataLoader.class),
                Map.of(), new DatabaseMetadataSnapshots(Map.of()));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> backend.refreshMetadata("logic_db"));
        assertThat(actual.getMessage(), is("Database `logic_db` is not configured."));
    }
}

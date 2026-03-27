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

import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseRuntimeFactoryTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertCreateConnectionConfigurationsWithRuntimeDatabases() {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        
        Map<String, DatabaseConnectionConfiguration> actual = databaseRuntimeFactory.createConnectionConfigurations(Map.of(
                "logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"),
                "analytics_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:analytics", "", "", "org.h2.Driver")));
        
        assertThat(actual.size(), is(2));
        assertThat(actual.get("logic_db").getJdbcUrl(), is("jdbc:h2:mem:logic"));
        assertThat(actual.get("analytics_db").getJdbcUrl(), is("jdbc:h2:mem:analytics"));
    }
    
    @Test
    void assertCreateConnectionConfigurationsPreserveConfiguredValues() {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        
        Map<String, DatabaseConnectionConfiguration> actual = databaseRuntimeFactory.createConnectionConfigurations(Map.of(
                " logic_db ", new RuntimeDatabaseConfiguration(" H2 ", " jdbc:h2:mem:logic ", " demo ", " secret ", " org.h2.Driver ")));
        
        assertThat(actual.get(" logic_db ").getDatabase(), is(" logic_db "));
        assertThat(actual.get(" logic_db ").getDatabaseType(), is(" H2 "));
        assertThat(actual.get(" logic_db ").getJdbcUrl(), is(" jdbc:h2:mem:logic "));
        assertThat(actual.get(" logic_db ").getUsername(), is(" demo "));
        assertThat(actual.get(" logic_db ").getPassword(), is(" secret "));
        assertThat(actual.get(" logic_db ").getDriverClassName(), is(" org.h2.Driver "));
    }
    
    @Test
    void assertCreateConnectionConfigurationsWithNoRuntimeDatabases() {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> databaseRuntimeFactory.createConnectionConfigurations(
                Map.of()));
        
        assertThat(actual.getMessage(), is("At least one runtime database must be configured."));
    }
    
    @Test
    void assertCreateDatabaseRuntimeRefreshMetadataForTargetDatabase() throws Exception {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        JdbcMetadataLoader jdbcMetadataLoader = new JdbcMetadataLoader();
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "factory-refresh-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "factory-refresh-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = Map.of(
                "logic_db", new DatabaseConnectionConfiguration("logic_db", "H2", firstJdbcUrl, "", "", "org.h2.Driver"),
                "analytics_db", new DatabaseConnectionConfiguration("analytics_db", "H2", secondJdbcUrl, "", "", "org.h2.Driver"));
        MetadataCatalog metadataCatalog = jdbcMetadataLoader.load(connectionConfigurations);
        DatabaseRuntime actual = databaseRuntimeFactory.createDatabaseRuntime(connectionConfigurations, metadataCatalog, jdbcMetadataLoader);
        H2RuntimeTestSupport.executeStatements(firstJdbcUrl, "CREATE TABLE public.orders_archive (order_id INT PRIMARY KEY)");
        
        actual.refreshMetadata("logic_db");
        
        assertTrue(metadataCatalog.getDatabaseTypes().containsKey("analytics_db"));
        assertThat(metadataCatalog.getMetadataObjects().stream().filter(each -> "logic_db".equals(each.getDatabase())).map(each -> each.getName()).toList(),
                hasItems("orders_archive"));
        assertThat(metadataCatalog.getMetadataObjects().stream().filter(each -> "analytics_db".equals(each.getDatabase())).map(each -> each.getName()).toList(),
                hasItems("orders"));
    }
    
    @Test
    void assertCreateDatabaseRuntimePreserveSnapshotWhenRefreshFails() {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        JdbcMetadataLoader jdbcMetadataLoader = mock(JdbcMetadataLoader.class);
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = Map.of(
                "logic_db", new DatabaseConnectionConfiguration("logic_db", "H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        MetadataCatalog metadataCatalog = new MetadataCatalog(Map.of("logic_db", "H2"), List.of());
        DatabaseRuntime actual = databaseRuntimeFactory.createDatabaseRuntime(connectionConfigurations, metadataCatalog, jdbcMetadataLoader);
        when(jdbcMetadataLoader.load(anyMap())).thenThrow(new IllegalStateException("refresh failed"));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> actual.refreshMetadata("logic_db"));
        
        assertThat(ex.getMessage(), is("refresh failed"));
        assertTrue(metadataCatalog.getDatabaseTypes().containsKey("logic_db"));
    }
}

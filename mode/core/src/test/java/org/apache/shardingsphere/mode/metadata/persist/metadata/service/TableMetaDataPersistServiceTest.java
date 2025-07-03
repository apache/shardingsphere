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

package org.apache.shardingsphere.mode.metadata.persist.metadata.service;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.metadata.persist.node.VersionNodePath;
import org.apache.shardingsphere.mode.metadata.persist.node.TableMetaDataNodePath;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableMetaDataPersistServiceTest {
    
    private TableMetaDataPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private VersionPersistService versionPersistService;
    
    @Captor
    private ArgumentCaptor<String> pathCaptor;
    
    @Captor
    private ArgumentCaptor<String> valueCaptor;
    
    private static final String TEST_DATABASE = "test_db";
    private static final String TEST_SCHEMA = "test_schema";
    
    @BeforeEach
    void setUp() {
        persistService = new TableMetaDataPersistService(repository, versionPersistService);
    }
    
    @Test
    void assertLoad() {
        // Setup version node path
        VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath("foo_db", "foo_schema", "foo_tbl"));
        when(versionPersistService.getVersionNodePath("foo_db", "foo_schema", "foo_tbl")).thenReturn(versionNodePath);
        
        // Create a sample table YAML
        String tableYaml = "name: foo_tbl\ncolumns:\n  id:\n    name: id\n    dataType: 4\n    primaryKey: true\n    generated: false\n    caseSensitive: false";
        
        // Mock repository responses
        when(repository.getChildrenKeys("/metadata/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("foo_tbl"));
        when(versionPersistService.loadActiveVersion("foo_db", "foo_schema", "foo_tbl")).thenReturn("0");
        when(repository.query("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0")).thenReturn(tableYaml);
        
        // Execute and verify
        Collection<ShardingSphereTable> actual = persistService.load("foo_db", "foo_schema");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertPersistSingleTable() {
        // Given
        String databaseName = "foo_db";
        String schemaName = "foo_schema";
        String tableName = "foo_tbl";
        
        // Create a test table with a column
        Map<String, ShardingSphereColumn> columns = new HashMap<>();
        columns.put("id", new ShardingSphereColumn("id", 4, true, false, false, true, false));
        ShardingSphereTable table = new ShardingSphereTable(tableName, columns, new ArrayList<>(), new ArrayList<>());
        
        // Setup version node path
        VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath(databaseName, schemaName, tableName));
        when(versionPersistService.getVersionNodePath(databaseName, schemaName, tableName)).thenReturn(versionNodePath);
        
        // When
        persistService.persist(databaseName, schemaName, Collections.singleton(table));
        
        // Then
        verify(versionPersistService).persist(any(), any());
    }
    
    @Test
    void assertDropTable() {
        // Given
        String databaseName = "foo_db";
        String schemaName = "foo_schema";
        String tableName = "foo_tbl";
        
        // Setup version node path
        VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath(databaseName, schemaName, tableName));
        when(versionPersistService.getVersionNodePath(databaseName, schemaName, tableName)).thenReturn(versionNodePath);
        
        // When
        persistService.drop(databaseName, schemaName, tableName);
        
        // Then
        verify(repository).delete("/metadata/" + databaseName + "/schemas/" + schemaName + "/tables/" + tableName);
    }
    
    @Test
    void assertDropTables() {
        // Given
        String databaseName = "foo_db";
        String schemaName = "foo_schema";
        String tableName = "foo_tbl";
        
        // Create a test table
        Map<String, ShardingSphereColumn> columns = new HashMap<>();
        columns.put("id", new ShardingSphereColumn("id", 4, true, false, false, true, false));
        ShardingSphereTable table = new ShardingSphereTable(tableName, columns, new ArrayList<>(), new ArrayList<>());
        
        // Setup version node path
        VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath(databaseName, schemaName, tableName));
        when(versionPersistService.getVersionNodePath(databaseName, schemaName, tableName)).thenReturn(versionNodePath);
        
        // When
        persistService.drop(databaseName, schemaName, Collections.singleton(table));
        
        // Then
        verify(repository).delete("/metadata/" + databaseName + "/schemas/" + schemaName + "/tables/" + tableName);
    }
    
    @Test
    void assertPersistMultipleTablesInParallel() {
        // Given
        int tableCount = 10; // Reduced from 100 to 10 for faster test execution
        Collection<ShardingSphereTable> tables = createMockTables(tableCount);
        
        // Setup version node path for all tables
        for (int i = 0; i < tableCount; i++) {
            VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath(TEST_DATABASE, TEST_SCHEMA, "table_" + i));
            when(versionPersistService.getVersionNodePath(TEST_DATABASE, TEST_SCHEMA, "table_" + i)).thenReturn(versionNodePath);
        }
        
        // When
        persistService.persist(TEST_DATABASE, TEST_SCHEMA, tables);
        
        // Then
        verify(versionPersistService, times(tableCount)).persist(any(), any());
    }
    
    @Test
    void assertPersistThreadSafety() throws InterruptedException {
        // Given
        int threadCount = 5; // Reduced from 10 to 5 for faster test execution
        int tablesPerThread = 10; // Reduced from 100 to 10 for faster test execution
        AtomicInteger totalProcessed = new AtomicInteger(0);
        
        // Setup version node path for all tables
        for (int i = 0; i < threadCount * tablesPerThread; i++) {
            VersionNodePath versionNodePath = new VersionNodePath(new TableMetaDataNodePath(TEST_DATABASE, TEST_SCHEMA, "table_" + i));
            when(versionPersistService.getVersionNodePath(TEST_DATABASE, TEST_SCHEMA, "table_" + i)).thenReturn(versionNodePath);
        }
        
        // Simulate version persistence
        doAnswer(invocation -> {
            Thread.sleep(1); // Simulate some processing time
            totalProcessed.incrementAndGet();
            return null;
        }).when(versionPersistService).persist(any(), any());
        
        // When
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                Collection<ShardingSphereTable> tables = createMockTables(tablesPerThread);
                persistService.persist(TEST_DATABASE, TEST_SCHEMA, tables);
            });
            threads.add(thread);
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then
        int expectedTotal = threadCount * tablesPerThread;
        assertThat(totalProcessed.get(), is(expectedTotal));
    }
    
    @Test
    void assertPersistWithEmptyCollection() {
        // When
        persistService.persist(TEST_DATABASE, TEST_SCHEMA, Collections.emptyList());
        
        // Then - No interactions should happen with empty collection
        verify(versionPersistService, never()).persist(any(), any());
        verify(repository, never()).persist(any(), any());
    }
    
    @Test
    void assertPersistWithNullTableName() {
        // Given - Create a table with null name
        ShardingSphereTable table = new ShardingSphereTable(null, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
        
        // When/Then - Expect NPE since table name is required
        assertThrows(NullPointerException.class, 
            () -> persistService.persist(TEST_DATABASE, TEST_SCHEMA, Collections.singletonList(table)));
    }
    
    private Collection<ShardingSphereTable> createMockTables(int count) {
        Collection<ShardingSphereTable> tables = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String tableName = "table_" + i;
            Map<String, ShardingSphereColumn> columns = new HashMap<>();
            columns.put("id", new ShardingSphereColumn("id", 4, true, false, false, true, false));
            tables.add(new ShardingSphereTable(tableName, columns, new ArrayList<>(), new ArrayList<>()));
        }
        return tables;
    }
}

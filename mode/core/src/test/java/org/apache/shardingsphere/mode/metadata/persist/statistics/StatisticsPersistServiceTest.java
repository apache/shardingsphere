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

package org.apache.shardingsphere.mode.metadata.persist.statistics;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.TableRowDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsPersistServiceTest {
    
    private StatisticsPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private TableRowDataPersistService tableRowDataPersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        persistService = new StatisticsPersistService(repository);
        Plugins.getMemberAccessor().set(StatisticsPersistService.class.getDeclaredField("tableRowDataPersistService"), persistService, tableRowDataPersistService);
    }
    
    @Test
    void assertLoadWithEmptyDatabases() {
        assertTrue(persistService.load(mock(ShardingSphereMetaData.class)).getDatabaseStatisticsMap().isEmpty());
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/statistics/databases")).thenReturn(Arrays.asList("foo_db", "bar_db"));
        when(repository.getChildrenKeys("/statistics/databases/foo_db/schemas")).thenReturn(Collections.singletonList("foo_schema"));
        when(repository.getChildrenKeys("/statistics/databases/foo_db/schemas/foo_schema/tables")).thenReturn(Collections.singletonList("foo_tbl"));
        assertFalse(persistService.load(mockMetaData()).getDatabaseStatisticsMap().isEmpty());
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db").getName()).thenReturn("foo_db");
        when(result.getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_schema").getName()).thenReturn("foo_schema");
        when(result.getDatabase("foo_db").getSchema("foo_schema").containsTable("foo_tbl")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl").getAllColumns()).thenReturn(Collections.emptyList());
        when(result.containsDatabase("bar_db")).thenReturn(true);
        when(result.getDatabase("bar_db").getName()).thenReturn("bar_db");
        return result;
    }
    
    @Test
    void assertPersistWithEmptyTableData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        persistService.persist(database, "foo_schema", mock(SchemaStatistics.class));
        verify(repository).persist("/statistics/databases/foo_db/schemas/foo_schema", "");
    }
    
    @Test
    void assertPersist() {
        SchemaStatistics schemaStatistics = mock(SchemaStatistics.class, RETURNS_DEEP_STUBS);
        when(schemaStatistics.getTableStatisticsMap().isEmpty()).thenReturn(false);
        TableStatistics tableStatistics = mock(TableStatistics.class);
        when(tableStatistics.getName()).thenReturn("foo_tbl");
        when(schemaStatistics.getTableStatisticsMap().values()).thenReturn(Collections.singleton(tableStatistics));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getSchema("foo_schema").getTable("foo_tbl").getAllColumns()).thenReturn(Collections.singleton(mock(ShardingSphereColumn.class)));
        persistService.persist(database, "foo_schema", schemaStatistics);
        verify(tableRowDataPersistService).persist("foo_db", "foo_schema", "foo_tbl", Collections.emptyList());
    }
    
    @Test
    void assertUpdate() {
        Collection<YamlRowStatistics> addedRows = Collections.singletonList(mock(YamlRowStatistics.class));
        Collection<YamlRowStatistics> updatedRows = Collections.singletonList(mock(YamlRowStatistics.class));
        Collection<YamlRowStatistics> deletedRows = Collections.singletonList(mock(YamlRowStatistics.class));
        AlteredDatabaseStatistics alteredDatabaseStatistics = new AlteredDatabaseStatistics("foo_db", "foo_schema", "foo_tbl");
        alteredDatabaseStatistics.getAddedRows().addAll(addedRows);
        alteredDatabaseStatistics.getUpdatedRows().addAll(updatedRows);
        alteredDatabaseStatistics.getDeletedRows().addAll(deletedRows);
        persistService.update(alteredDatabaseStatistics);
        verify(tableRowDataPersistService).persist("foo_db", "foo_schema", "foo_tbl", addedRows);
        verify(tableRowDataPersistService).persist("foo_db", "foo_schema", "foo_tbl", updatedRows);
        verify(tableRowDataPersistService).delete("foo_db", "foo_schema", "foo_tbl", deletedRows);
    }
    
    @Test
    void assertDelete() {
        persistService.delete("foo_db");
        verify(repository).delete("/statistics/databases/foo_db");
    }
}

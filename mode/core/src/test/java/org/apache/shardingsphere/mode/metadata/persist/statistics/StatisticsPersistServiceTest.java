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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
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

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
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
        when(repository.getChildrenKeys("/statistics/databases")).thenReturn(Collections.emptyList());
        ShardingSphereStatistics actual = persistService.load(mock(ShardingSphereMetaData.class));
        assertTrue(actual.getDatabaseStatisticsMap().isEmpty());
    }
    
    @Test
    void assertLoadFiltersUnavailableMetadata() {
        when(repository.getChildrenKeys("/statistics/databases")).thenReturn(Arrays.asList("foo_db", "miss_db"));
        when(repository.getChildrenKeys("/statistics/databases/foo_db/schemas")).thenReturn(Arrays.asList("foo_schema", "miss_schema"));
        when(repository.getChildrenKeys("/statistics/databases/foo_db/schemas/foo_schema/tables")).thenReturn(Arrays.asList("foo_tbl", "miss_tbl"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.containsDatabase("miss_db")).thenReturn(false);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(database.getName()).thenReturn("foo_db");
        when(database.containsSchema("foo_schema")).thenReturn(true);
        when(database.containsSchema("miss_schema")).thenReturn(false);
        when(database.getSchema("foo_schema")).thenReturn(schema);
        when(schema.getName()).thenReturn("foo_schema");
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(schema.containsTable("miss_tbl")).thenReturn(false);
        when(schema.getTable("foo_tbl")).thenReturn(table);
        TableStatistics tableStatistics = new TableStatistics("foo_tbl");
        when(tableRowDataPersistService.load("foo_db", "foo_schema", table)).thenReturn(tableStatistics);
        ShardingSphereStatistics statistics = persistService.load(metaData);
        assertTrue(statistics.getDatabaseStatisticsMap().containsKey("foo_db"));
        assertFalse(statistics.getDatabaseStatisticsMap().containsKey("miss_db"));
        assertTrue(statistics.getDatabaseStatisticsMap().get("foo_db").getSchemaStatisticsMap().containsKey("foo_schema"));
        assertFalse(statistics.getDatabaseStatisticsMap().get("foo_db").getSchemaStatisticsMap().containsKey("miss_schema"));
        assertTrue(statistics.getDatabaseStatisticsMap().get("foo_db").getSchemaStatisticsMap().get("foo_schema").getTableStatisticsMap().containsKey("foo_tbl"));
        assertFalse(statistics.getDatabaseStatisticsMap().get("foo_db").getSchemaStatisticsMap().get("foo_schema").getTableStatisticsMap().containsKey("miss_tbl"));
        verify(tableRowDataPersistService).load("foo_db", "foo_schema", table);
    }
    
    @Test
    void assertPersistWithEmptyTableData() {
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        persistService.persist(database, "foo_schema", schemaStatistics);
        verify(repository).persist("/statistics/databases/foo_db/schemas/foo_schema", "");
    }
    
    @Test
    void assertPersistWithExistingAndMissingTables() {
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        TableStatistics existingTableStatistics = new TableStatistics("foo_tbl");
        existingTableStatistics.getRows().add(new RowStatistics(Collections.singletonList("foo_value")));
        schemaStatistics.putTableStatistics("foo_tbl", existingTableStatistics);
        schemaStatistics.putTableStatistics("missing_tbl", new TableStatistics("missing_tbl"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("foo_col", Types.VARCHAR, false, false, "varchar", true, true, false, true)));
        when(database.getName()).thenReturn("foo_db");
        when(database.getSchema("foo_schema")).thenReturn(schema);
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(schema.getTable("foo_tbl")).thenReturn(table);
        persistService.persist(database, "foo_schema", schemaStatistics);
        verify(tableRowDataPersistService).persist(eq("foo_db"), eq("foo_schema"), eq("foo_tbl"), argThat(rows -> !rows.isEmpty()));
        verify(tableRowDataPersistService).persist("foo_db", "foo_schema", "missing_tbl", Collections.emptyList());
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

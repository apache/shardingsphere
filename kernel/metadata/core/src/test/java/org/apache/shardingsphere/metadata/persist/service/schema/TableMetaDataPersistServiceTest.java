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

package org.apache.shardingsphere.metadata.persist.service.schema;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TableMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    private TableMetaDataPersistService tableMetaDataService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        tableMetaDataService = new TableMetaDataPersistService(repository);
    }
    
    @Test
    void testPersist() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String tableName = "testTable";
        Map<String, ShardingSphereTable> expect = createMetadataMap(tableName);
        
        when(repository.getChildrenKeys(anyString())).thenReturn(Collections.singletonList("0"));
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        tableMetaDataService.persist(databaseName, schemaName, expect);
        
        verify(repository, times(1)).persist(anyString(), anyString());
    }
    
    @Test
    void testPersistSchemaMetaData() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String tableName = "testTable";
        Map<String, ShardingSphereTable> expect = createMetadataMap(tableName);
        when(repository.getChildrenKeys(anyString())).thenReturn(Collections.singletonList("0"));
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        Collection<MetaDataVersion> actual = tableMetaDataService.persistSchemaMetaData(databaseName, schemaName, expect);
        verify(repository, times(1)).persist(anyString(), anyString());
        assertEquals(expect.size(), actual.size());
        MetaDataVersion metaDataVersion = actual.iterator().next();
        assertEquals("0", metaDataVersion.getCurrentActiveVersion());
        assertEquals("1", metaDataVersion.getNextActiveVersion());
    }
    
    @Test
    void testLoad() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String tableName = "testTable";
        when(repository.getChildrenKeys(anyString())).thenReturn(Collections.singletonList(tableName));
        when(repository.getDirectly(anyString()))
                .thenReturn("0")
                .thenReturn(createTableString());
        Map<String, ShardingSphereTable> actual = tableMetaDataService.load(databaseName, schemaName);
        assertEquals(1, actual.size());
        assertTrue(actual.containsKey(tableName.toLowerCase(Locale.ROOT)));
    }
    
    @Test
    void testLoadTable() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String tableName = "testTable";
        when(repository.getDirectly(anyString()))
                .thenReturn("0")
                .thenReturn(createTableString());
        Map<String, ShardingSphereTable> actual = tableMetaDataService.load(databaseName, schemaName, tableName);
        assertEquals(1, actual.size());
        assertTrue(actual.containsKey(tableName.toLowerCase(Locale.ROOT)));
    }
    
    @Test
    void testDelete() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String tableName = "testTable";
        tableMetaDataService.delete(databaseName, schemaName, tableName);
        
        verify(repository, times(1)).delete(anyString());
    }
    
    private String createTableString() {
        Map<String, Object> tableMap = Maps.newHashMap();
        Map<String, Object> columns = Maps.newHashMap();
        Map<String, Object> column = Maps.newHashMap();
        column.put("name", "id");
        column.put("dataType", Types.VARCHAR);
        columns.put("id", column);
        tableMap.put("columns", columns);
        return JsonUtils.toJsonString(tableMap);
    }
    
    private static Map<String, ShardingSphereTable> createMetadataMap(final String tableName) {
        ShardingSphereTable shardingSphereTable = createTable(tableName);
        Map<String, ShardingSphereTable> map = Maps.newHashMap();
        map.put(tableName, shardingSphereTable);
        return map;
    }
    
    private static ShardingSphereTable createTable(String tableName) {
        return new ShardingSphereTable(tableName, Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("warehouse_name", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList());
    }
    
    
}

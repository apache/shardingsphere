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

package org.apache.shardingsphere.metadata.persist.service.database;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
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
public class DatabaseMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private MetaDataVersionPersistService metaDataVersionPersistService;
    
    private DatabaseMetaDataPersistService databaseMetaDataPersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseMetaDataPersistService = new DatabaseMetaDataPersistService(repository,
                metaDataVersionPersistService);
    }
    
    @Test
    void testAddDatabase() {
        String databaseName = "testDatabase";
        databaseMetaDataPersistService.addDatabase(databaseName);
        
        verify(repository, times(1)).persist(anyString(), anyString());
    }
    
    @Test
    void testDropDatabase() {
        String databaseName = "testDatabase";
        databaseMetaDataPersistService.dropDatabase(databaseName);
        
        verify(repository, times(1)).delete(anyString());
    }
    
    @Test
    void testLoadAllDatabaseNames() {
        when(repository.getChildrenKeys(anyString()))
                .thenReturn(Collections.singletonList("0"))
                .thenReturn(Collections.emptyList());
        Collection<String> actual = databaseMetaDataPersistService.loadAllDatabaseNames();
        assertEquals(1, actual.size());
        
        Collection<String> actualNew = databaseMetaDataPersistService.loadAllDatabaseNames();
        assertEquals(0, actualNew.size());
    }
    
    @Test
    void testAddSchema() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        databaseMetaDataPersistService.addSchema(databaseName, schemaName);
        
        verify(repository, times(1)).persist(anyString(), anyString());
    }
    
    @Test
    void testDropSchema() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        databaseMetaDataPersistService.dropSchema(databaseName, schemaName);
        
        verify(repository, times(1)).delete(anyString());
    }
    
    @Test
    void testCompareAndPersist() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        ShardingSphereSchema schema = createSchema();
        when(repository.getChildrenKeys(anyString()))
                .thenReturn(Collections.singletonList("t_warehouse"))
                .thenReturn(Collections.singletonList("0"));
        when(repository.getDirectly(anyString()))
                .thenReturn("0")
                .thenReturn(createTableString());
        
        databaseMetaDataPersistService.compareAndPersist(databaseName, schemaName, schema);
    }
    
    @Test
    void testPersist() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        ShardingSphereSchema schema = createSchema();
        databaseMetaDataPersistService.persist(databaseName, schemaName, schema);
        
        verify(repository, times(1)).persist(anyString(), anyString());
        verify(repository, times(0)).delete(anyString());
        
    }
    
    @Test
    void testDelete() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        ShardingSphereSchema schema = createSchema();
        databaseMetaDataPersistService.delete(databaseName, schemaName, schema);
        
        verify(repository, times(1)).delete(anyString());
    }
    
    @Test
    void testLoadSchemas() {
        String databaseName = "testDatabase";
        when(repository.getChildrenKeys(anyString()))
                .thenReturn(Collections.singletonList("testSchema"))
                .thenReturn(Collections.singletonList("t_warehouse"))
                .thenReturn(Collections.emptyList());
        
        when(repository.getDirectly(anyString()))
                .thenReturn("0")
                .thenReturn(createTableString());
        
        Map<String, ShardingSphereSchema> actual = databaseMetaDataPersistService.loadSchemas(databaseName);
        
        assertEquals(1, actual.size());
        assertTrue(actual.get("testSchema".toLowerCase(Locale.ROOT)).containsTable("t_warehouse"));
        assertTrue(actual.get("testSchema".toLowerCase(Locale.ROOT)).getViews().isEmpty());
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
    
    private static ShardingSphereSchema createSchema() {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("t_warehouse", new ShardingSphereTable("t_warehouse", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("warehouse_name", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        return schema;
    }
    
    
}

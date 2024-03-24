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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
public class ViewMetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    private ViewMetaDataPersistService viewMetaDataService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        viewMetaDataService = new ViewMetaDataPersistService(repository);
    }
    
    @Test
    void testPersist() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String viewName = "foo_view";
        Map<String, ShardingSphereView> expect = createMetadataMap(viewName);
        
        when(repository.getChildrenKeys(anyString())).thenReturn(Collections.singletonList("0"));
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        viewMetaDataService.persist(databaseName, schemaName, expect);
        
        verify(repository, times(1)).persist(anyString(), anyString());
    }
    
    @Test
    void testPersistSchemaMetaData() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String viewName = "foo_view";
        Map<String, ShardingSphereView> expect = createMetadataMap(viewName);
        when(repository.getChildrenKeys(anyString())).thenReturn(Collections.singletonList("0"));
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        Collection<MetaDataVersion> actual = viewMetaDataService.persistSchemaMetaData(databaseName, schemaName, expect);
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
        String viewName = "foo_view";
        when(repository.getChildrenKeys(anyString())).thenReturn(Collections.singletonList(viewName));
        when(repository.getDirectly(anyString()))
                .thenReturn("0")
                .thenReturn(createViewString());
        Map<String, ShardingSphereView> actual = viewMetaDataService.load(databaseName, schemaName);
        assertEquals(1, actual.size());
        assertTrue(actual.containsKey(viewName.toLowerCase(Locale.ROOT)));
    }
    
    @Test
    void testLoadView() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String viewName = "foo_view";
        when(repository.getDirectly(anyString()))
                .thenReturn("0")
                .thenReturn(createViewString());
        Map<String, ShardingSphereView> actual = viewMetaDataService.load(databaseName, schemaName, viewName);
        assertEquals(1, actual.size());
        assertTrue(actual.containsKey(viewName.toLowerCase(Locale.ROOT)));
    }
    
    @Test
    void testDelete() {
        String databaseName = "testDatabase";
        String schemaName = "testSchema";
        String viewName = "foo_view";
        viewMetaDataService.delete(databaseName, schemaName, viewName);
        
        verify(repository, times(1)).delete(anyString());
    }
    
    private String createViewString() {
        Map<String, Object> viewMap = Maps.newHashMap();
        viewMap.put("name", "foo_view");
        viewMap.put("viewDefinition", "select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`");
        return JsonUtils.toJsonString(viewMap);
    }
    
    private static Map<String, ShardingSphereView> createMetadataMap(final String viewName) {
        ShardingSphereView shardingSphereView = createView(viewName);
        Map<String, ShardingSphereView> map = Maps.newHashMap();
        map.put(viewName, shardingSphereView);
        return map;
    }
    
    private static ShardingSphereView createView(String viewName) {
        return new ShardingSphereView(viewName, "select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`");
    }
    
}

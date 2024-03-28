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

package org.apache.shardingsphere.metadata.persist.service.config.database.datasource;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataSourceUnitPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    private DataSourceUnitPersistService dataSourceUnitService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        dataSourceUnitService = new DataSourceUnitPersistService(repository);
    }
    
    @Test
    void testPersistVersionNotExists() {
        // Arrange
        String databaseName = "testDatabase";
        Map<String, DataSourcePoolProperties> dataSourceConfigs = createDataSourcePoolConfigs();
        // Act
        dataSourceUnitService.persist(databaseName, dataSourceConfigs);
        
        // Assert
        verify(repository, times(4)).persist(anyString(), anyString());
    }
    
    @Test
    void testPersistVersionExists() {
        // Arrange
        String databaseName = "testDatabase";
        Map<String, DataSourcePoolProperties> dataSourceConfigs = createDataSourcePoolConfigs();
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        // Act
        dataSourceUnitService.persist(databaseName, dataSourceConfigs);
        
        // Assert
        verify(repository, times(2)).persist(anyString(), anyString());
    }
    
    @Test
    void testLoad() {
        // Arrange
        String databaseName = "testDatabase";
        Map<String, DataSourcePoolProperties> expectedDataSourceConfigs = createDataSourcePoolConfigs();
        when(repository.getChildrenKeys(anyString())).thenReturn(Lists.newArrayList(expectedDataSourceConfigs.keySet()));
        when(repository.getDirectly(anyString())).thenReturn(JsonUtils.toJsonString(createDataSourceProperties()));
        
        // Act
        Map<String, DataSourcePoolProperties> actual = dataSourceUnitService.load(databaseName);
        
        // Assert
        assertEquals(expectedDataSourceConfigs.size(), actual.size());
        for (String key : expectedDataSourceConfigs.keySet()) {
            assertEquals(expectedDataSourceConfigs.get(key).getPoolClassName(), actual.get(key).getPoolClassName());
        }
    }
    
    @Test
    void testLoadDataSource() {
        // Arrange
        String databaseName = "testDatabase";
        String dataSourceName = "testDataSource";
        Map<String, Object> sourceProperties = createDataSourceProperties();
        when(repository.getDirectly(anyString())).thenReturn(JsonUtils.toJsonString(sourceProperties));
        
        // Act
        Map<String, DataSourcePoolProperties> actual = dataSourceUnitService.load(databaseName, dataSourceName);
        
        // Assert
        assertEquals(1, actual.size());
        assertEquals(sourceProperties.get("dataSourceClassName"), actual.get(dataSourceName).getPoolClassName());
    }
    
    @Test
    void testDelete() {
        // Arrange
        String databaseName = "testDatabase";
        String dataSourceName = "testDataSource";
        
        // Act
        dataSourceUnitService.delete(databaseName, dataSourceName);
        
        // Assert
        verify(repository, times(1)).delete(anyString());
    }
    
    @Test
    void testDeleteConfig() {
        // Arrange
        String databaseName = "testDatabase";
        Map<String, DataSourcePoolProperties> expectedDataSourceConfigs = createDataSourcePoolConfigs();
        
        // Act
        Collection<MetaDataVersion> actual = dataSourceUnitService.deleteConfig(databaseName, expectedDataSourceConfigs);
        
        // Assert
        assertEquals(expectedDataSourceConfigs.size(), actual.size());
        verify(repository, times(2)).delete(anyString());
    }
    
    @Test
    void testPersistConfig() {
        // Arrange
        String databaseName = "testDatabase";
        Map<String, DataSourcePoolProperties> expectedDataSourceConfigs = createDataSourcePoolConfigs();
        when(repository.getChildrenKeys(anyString())).thenReturn(Collections.emptyList());
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        // Act
        Collection<MetaDataVersion> actual = dataSourceUnitService.persistConfig(databaseName, expectedDataSourceConfigs);
        
        // Assert
        assertEquals(expectedDataSourceConfigs.size(), actual.size());
        verify(repository, times(2)).persist(anyString(), anyString());
    }
    
    private Map<String, DataSourcePoolProperties> createDataSourcePoolConfigs() {
        Map<String, DataSourcePoolProperties> dataSourceConfigs = new HashMap<>();
        dataSourceConfigs.put("dataSource1", createDataSourcePoolProperties());
        dataSourceConfigs.put("dataSource2", createDataSourcePoolProperties());
        return dataSourceConfigs;
    }
    
    private DataSourcePoolProperties createDataSourcePoolProperties() {
        return new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties());
    }
    
    private Map<String, Object> createDataSourceProperties() {
        Map<String, Object> yamlConfig = new HashMap<>(3, 1F);
        yamlConfig.put("dataSourceClassName", MockedDataSource.class.getName());
        yamlConfig.put("url", "xx:xxx");
        yamlConfig.put("username", "root");
        return yamlConfig;
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("url", "xx:xxx");
        result.put("username", "root");
        return result;
    }
    
}

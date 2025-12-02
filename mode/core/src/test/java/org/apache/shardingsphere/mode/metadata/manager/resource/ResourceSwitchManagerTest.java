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

package org.apache.shardingsphere.mode.metadata.manager.resource;

import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(AutoMockExtension.class)
class ResourceSwitchManagerTest {
    
    private final ResourceSwitchManager resourceSwitchManager = new ResourceSwitchManager();
    
    @Test
    void assertSwitchByRegisterStorageUnit() {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithSingleUnit("ds_existing");
        Map<String, DataSourcePoolProperties> toBeRegistered = new LinkedHashMap<>(2, 1F);
        toBeRegistered.put("ds_existing", createDataSourcePoolProperties());
        toBeRegistered.put("ds_new", createDataSourcePoolProperties());
        DataSource newDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourcePoolCreator> mocked = mockStatic(DataSourcePoolCreator.class)) {
            mocked.when(() -> DataSourcePoolCreator.create(any(DataSourcePoolProperties.class))).thenReturn(newDataSource);
            SwitchingResource actual = resourceSwitchManager.switchByRegisterStorageUnit(resourceMetaData, toBeRegistered);
            assertThat(actual.getNewDataSources().size(), is(1));
            assertThat(actual.getNewDataSources(), hasKey(new StorageNode("ds_new")));
            verifyNoInteractions(resourceMetaData.getDataSources().get(new StorageNode("ds_existing")));
        }
    }
    
    @Test
    void assertSwitchByAlterStorageUnit() {
        StorageNode existingNode = new StorageNode("ds_altered");
        ResourceMetaData resourceMetaData = createResourceMetaData(existingNode, "unused_ds");
        Map<String, DataSourcePoolProperties> toBeAltered = Collections.singletonMap("ds_altered", createDataSourcePoolProperties());
        DataSource newDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourcePoolCreator> mocked = mockStatic(DataSourcePoolCreator.class)) {
            mocked.when(() -> DataSourcePoolCreator.create(any(DataSourcePoolProperties.class))).thenReturn(newDataSource);
            SwitchingResource actual = resourceSwitchManager.switchByAlterStorageUnit(resourceMetaData, toBeAltered);
            assertThat(actual.getNewDataSources().get(existingNode), is(newDataSource));
            assertThat(actual.getStaleDataSources().get(existingNode), is(resourceMetaData.getDataSources().get(existingNode)));
            assertThat(actual.getStaleDataSources(), not(hasKey(new StorageNode("extra_only"))));
            assertTrue(actual.getStaleStorageUnitNames().contains("ds_altered"));
            assertThat(actual.getMergedDataSourcePoolPropertiesMap(), hasEntry("ds_altered", createDataSourcePoolProperties()));
        }
    }
    
    @Test
    void assertSwitchByUnregisterStorageUnit() {
        StorageNode sharedNode = new StorageNode("shared");
        StorageNode orphanNode = new StorageNode("orphan");
        Map<StorageNode, DataSource> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put(sharedNode, mock(DataSource.class));
        dataSources.put(orphanNode, mock(DataSource.class));
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(3, 1F);
        storageUnits.put("in_use", new StorageUnit(sharedNode, createDataSourcePoolProperties(), dataSources.get(sharedNode)));
        storageUnits.put("shared_copy", new StorageUnit(sharedNode, createDataSourcePoolProperties(), dataSources.get(sharedNode)));
        storageUnits.put("to_remove", new StorageUnit(orphanNode, createDataSourcePoolProperties(), dataSources.get(orphanNode)));
        ResourceMetaData resourceMetaData = new ResourceMetaData(dataSources, storageUnits);
        SwitchingResource actual = resourceSwitchManager.switchByUnregisterStorageUnit(resourceMetaData, new LinkedHashSet<>(Arrays.asList("to_remove", "shared_copy", "missing")));
        assertThat(actual.getStaleDataSources().get(orphanNode), is(dataSources.get(orphanNode)));
        assertThat(actual.getStaleDataSources(), not(hasKey(sharedNode)));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), not(hasKey("to_remove")));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), not(hasKey("shared_copy")));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), hasKey("in_use"));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), not(hasKey("missing")));
    }
    
    @Test
    void assertCreateByUnregisterStorageUnit() {
        StorageNode sharedNode = new StorageNode("shared");
        StorageNode orphanNode = new StorageNode("orphan");
        Map<StorageNode, DataSource> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put(sharedNode, mock(DataSource.class));
        dataSources.put(orphanNode, mock(DataSource.class));
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("keep", new StorageUnit(sharedNode, createDataSourcePoolProperties(), dataSources.get(sharedNode)));
        storageUnits.put("drop", new StorageUnit(orphanNode, createDataSourcePoolProperties(), dataSources.get(orphanNode)));
        ResourceMetaData resourceMetaData = new ResourceMetaData(dataSources, storageUnits);
        SwitchingResource actual = resourceSwitchManager.createByUnregisterStorageUnit(resourceMetaData, Collections.singleton("drop"));
        assertThat(actual.getStaleDataSources().get(orphanNode), is(dataSources.get(orphanNode)));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), hasKey("keep"));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), not(hasKey("drop")));
        assertThat(resourceMetaData.getStorageUnits(), hasKey("drop"));
    }
    
    private ResourceMetaData createResourceMetaDataWithSingleUnit(final String name) {
        StorageNode storageNode = new StorageNode(name);
        DataSource dataSource = mock(DataSource.class);
        return new ResourceMetaData(Collections.singletonMap(storageNode, dataSource), Collections.singletonMap(name, new StorageUnit(storageNode, createDataSourcePoolProperties(), dataSource)));
    }
    
    private ResourceMetaData createResourceMetaData(final StorageNode existingNode, final String anotherName) {
        DataSource existingDataSource = mock(DataSource.class);
        DataSource anotherDataSource = mock(DataSource.class);
        Map<StorageNode, DataSource> dataSources = new LinkedHashMap<>(3, 1F);
        dataSources.put(existingNode, existingDataSource);
        dataSources.put(new StorageNode(anotherName), anotherDataSource);
        dataSources.put(new StorageNode("extra_only"), mock(DataSource.class));
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put(existingNode.getName(), new StorageUnit(existingNode, createDataSourcePoolProperties(), existingDataSource));
        storageUnits.put(anotherName, new StorageUnit(new StorageNode(anotherName), createDataSourcePoolProperties(), anotherDataSource));
        return new ResourceMetaData(dataSources, storageUnits);
    }
    
    private DataSourcePoolProperties createDataSourcePoolProperties() {
        Map<String, Object> props = new LinkedHashMap<>(2, 1F);
        props.put("url", "jdbc:mock://localhost:3306/foo_db");
        props.put("username", "root");
        return new DataSourcePoolProperties("Foo", props);
    }
}

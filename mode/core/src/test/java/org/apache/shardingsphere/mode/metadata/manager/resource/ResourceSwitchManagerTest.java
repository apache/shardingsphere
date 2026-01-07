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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
    
    @ParameterizedTest(name = "Register storage units with {0} existing units and {1} new units")
    @MethodSource("provideRegisterStorageUnitArguments")
    void assertSwitchByRegisterStorageUnitWithVariousScenarios(final int existingUnitsCount, final int newUnitsCount) {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithMultipleUnits(existingUnitsCount);
        Map<String, DataSourcePoolProperties> toBeRegistered = createDataSourcePoolPropertiesMap(newUnitsCount, "new_");
        DataSource newDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourcePoolCreator> mocked = mockStatic(DataSourcePoolCreator.class)) {
            mocked.when(() -> DataSourcePoolCreator.create(any(DataSourcePoolProperties.class))).thenReturn(newDataSource);
            SwitchingResource actual = resourceSwitchManager.switchByRegisterStorageUnit(resourceMetaData, toBeRegistered);
            assertThat(actual.getNewDataSources().size(), is(newUnitsCount));
            assertThat(actual.getNewDataSources().size(), is(newUnitsCount));
            for (int i = 0; i < newUnitsCount; i++) {
                assertThat(actual.getNewDataSources(), hasKey(new StorageNode("new_" + i)));
            }
        }
    }
    
    @ParameterizedTest(name = "Alter storage units with {0} total units and alter {1} units")
    @MethodSource("provideAlterStorageUnitArguments")
    void assertSwitchByAlterStorageUnitWithVariousScenarios(final int totalUnits, final int alterUnitsCount) {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithMultipleUnits(totalUnits);
        Map<String, DataSourcePoolProperties> toBeAltered = createDataSourcePoolPropertiesMap(alterUnitsCount, "");
        DataSource newDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourcePoolCreator> mocked = mockStatic(DataSourcePoolCreator.class)) {
            mocked.when(() -> DataSourcePoolCreator.create(any(DataSourcePoolProperties.class))).thenReturn(newDataSource);
            SwitchingResource actual = resourceSwitchManager.switchByAlterStorageUnit(resourceMetaData, toBeAltered);
            assertThat(actual.getNewDataSources().size(), is(totalUnits));
            assertThat(actual.getStaleDataSources().size(), is(totalUnits));
            assertTrue(actual.getStaleStorageUnitNames().containsAll(toBeAltered.keySet()));
        }
    }
    
    @ParameterizedTest(name = "Unregister storage units with {0} total units and remove {1} units")
    @MethodSource("provideUnregisterStorageUnitArguments")
    void assertSwitchByUnregisterStorageUnitWithVariousScenarios(final int totalUnits, final int removeUnitsCount) {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithMultipleUnits(totalUnits);
        Collection<String> toBeRemoved =
                Arrays.asList(String.valueOf(0), String.valueOf(1), String.valueOf(2), String.valueOf(3), String.valueOf(4)).subList(0, Math.min(removeUnitsCount, totalUnits));
        SwitchingResource actual = resourceSwitchManager.switchByUnregisterStorageUnit(resourceMetaData, toBeRemoved);
        assertThat(actual.getStaleStorageUnitNames(), is(toBeRemoved));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap().size(), is(totalUnits - toBeRemoved.size()));
        for (String each : toBeRemoved) {
            assertThat(actual.getMergedDataSourcePoolPropertiesMap(), not(hasKey(each)));
        }
    }
    
    @Test
    void assertSwitchByRegisterStorageUnitWithEmptyInput() {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithSingleUnit("ds_existing");
        Map<String, DataSourcePoolProperties> toBeRegistered = Collections.emptyMap();
        SwitchingResource actual = resourceSwitchManager.switchByRegisterStorageUnit(resourceMetaData, toBeRegistered);
        assertThat(actual.getNewDataSources(), aMapWithSize(0));
        assertThat(actual.getStaleDataSources(), aMapWithSize(0));
        assertThat(actual.getStaleStorageUnitNames().isEmpty(), is(true));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), hasKey("ds_existing"));
    }
    
    @Test
    void assertSwitchByAlterStorageUnitWithEmptyInput() {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithSingleUnit("ds_existing");
        Map<String, DataSourcePoolProperties> toBeAltered = Collections.emptyMap();
        DataSource newDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourcePoolCreator> mocked = mockStatic(DataSourcePoolCreator.class)) {
            mocked.when(() -> DataSourcePoolCreator.create(any(DataSourcePoolProperties.class))).thenReturn(newDataSource);
            SwitchingResource actual = resourceSwitchManager.switchByAlterStorageUnit(resourceMetaData, toBeAltered);
            assertThat(actual.getNewDataSources().size(), is(1));
            assertThat(actual.getStaleDataSources().size(), is(1));
            assertThat(actual.getStaleStorageUnitNames().isEmpty(), is(false));
            assertThat(actual.getMergedDataSourcePoolPropertiesMap(), hasKey("ds_existing"));
        }
    }
    
    @Test
    void assertSwitchByUnregisterStorageUnitWithEmptyInput() {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithSingleUnit("ds_existing");
        SwitchingResource actual = resourceSwitchManager.switchByUnregisterStorageUnit(resourceMetaData, Collections.emptySet());
        assertThat(actual.getStaleDataSources(), aMapWithSize(0));
        assertThat(actual.getStaleStorageUnitNames().isEmpty(), is(true));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), hasKey("ds_existing"));
    }
    
    @Test
    void assertCreateByUnregisterStorageUnitWithEmptyInput() {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithSingleUnit("ds_existing");
        SwitchingResource actual = resourceSwitchManager.createByUnregisterStorageUnit(resourceMetaData, Collections.emptySet());
        assertThat(actual.getStaleDataSources(), aMapWithSize(0));
        assertThat(actual.getStaleStorageUnitNames().isEmpty(), is(true));
        assertThat(actual.getMergedDataSourcePoolPropertiesMap(), hasKey("ds_existing"));
    }
    
    @Test
    void assertSwitchByRegisterStorageUnitWithDuplicateDataSourceName() {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithMultipleUnits(3);
        Map<String, DataSourcePoolProperties> toBeRegistered = new LinkedHashMap<>(2, 1F);
        toBeRegistered.put("0", createDataSourcePoolProperties());
        toBeRegistered.put("new_0", createDataSourcePoolProperties());
        DataSource newDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourcePoolCreator> mocked = mockStatic(DataSourcePoolCreator.class)) {
            mocked.when(() -> DataSourcePoolCreator.create(any(DataSourcePoolProperties.class))).thenReturn(newDataSource);
            SwitchingResource actual = resourceSwitchManager.switchByRegisterStorageUnit(resourceMetaData, toBeRegistered);
            assertThat(actual.getNewDataSources().size(), is(1));
            assertThat(actual.getNewDataSources(), hasKey(new StorageNode("new_0")));
        }
    }
    
    @Test
    void assertSwitchByAlterStorageUnitWithMultipleUnits() {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithMultipleUnits(5);
        Map<String, DataSourcePoolProperties> toBeAltered = new LinkedHashMap<>(2, 1F);
        toBeAltered.put("1", createDataSourcePoolProperties());
        toBeAltered.put("3", createDataSourcePoolProperties());
        DataSource newDataSource = mock(DataSource.class);
        try (MockedStatic<DataSourcePoolCreator> mocked = mockStatic(DataSourcePoolCreator.class)) {
            mocked.when(() -> DataSourcePoolCreator.create(any(DataSourcePoolProperties.class))).thenReturn(newDataSource);
            SwitchingResource actual = resourceSwitchManager.switchByAlterStorageUnit(resourceMetaData, toBeAltered);
            assertThat(actual.getNewDataSources().size(), is(5));
            assertThat(actual.getStaleDataSources().size(), is(5));
            assertThat(actual.getStaleStorageUnitNames(), containsInAnyOrder("0", "1", "2", "3", "4"));
        }
    }
    
    private static Stream<Arguments> provideRegisterStorageUnitArguments() {
        return Stream.of(
                Arguments.of(0, 1),
                Arguments.of(1, 1),
                Arguments.of(1, 2),
                Arguments.of(3, 1),
                Arguments.of(3, 3));
    }
    
    private static Stream<Arguments> provideAlterStorageUnitArguments() {
        return Stream.of(
                Arguments.of(1, 1),
                Arguments.of(3, 1),
                Arguments.of(3, 2),
                Arguments.of(5, 3));
    }
    
    private static Stream<Arguments> provideUnregisterStorageUnitArguments() {
        return Stream.of(
                Arguments.of(1, 0),
                Arguments.of(1, 1),
                Arguments.of(3, 1),
                Arguments.of(3, 2),
                Arguments.of(5, 5));
    }
    
    private ResourceMetaData createResourceMetaDataWithMultipleUnits(final int count) {
        Map<StorageNode, DataSource> dataSources = new LinkedHashMap<>(count, 1F);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(count, 1F);
        for (int i = 0; i < count; i++) {
            String name = String.valueOf(i);
            StorageNode storageNode = new StorageNode(name);
            DataSource dataSource = mock(DataSource.class);
            dataSources.put(storageNode, dataSource);
            storageUnits.put(name, new StorageUnit(storageNode, createDataSourcePoolProperties(), dataSource));
        }
        return new ResourceMetaData(dataSources, storageUnits);
    }
    
    private Map<String, DataSourcePoolProperties> createDataSourcePoolPropertiesMap(final int count, final String prefix) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(count, 1F);
        for (int i = 0; i < count; i++) {
            result.put(prefix + i, createDataSourcePoolProperties());
        }
        return result;
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

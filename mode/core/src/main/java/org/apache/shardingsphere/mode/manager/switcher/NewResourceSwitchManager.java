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

package org.apache.shardingsphere.mode.manager.switcher;

import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * TODO Rename ResourceSwitchManager when metadata structure adjustment completed. #25485
 * Resource switch manager.
 */
public final class NewResourceSwitchManager {
    
    /**
     * Register storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitDataSourcePoolPropsMap storage unit grouped data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource registerStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolPropsMap) {
        Map<String, DataSourcePoolProperties> mergedPropsMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        mergedPropsMap.putAll(storageUnitDataSourcePoolPropsMap);
        Map<String, StorageNode> toBeCreatedStorageUintNodeMap = StorageUnitNodeMapCreator.create(storageUnitDataSourcePoolPropsMap);
        Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap = StorageNodeAggregator.aggregateDataSourcePoolProperties(storageUnitDataSourcePoolPropsMap);
        return new SwitchingResource(getNewDataSources(resourceMetaData, toBeCreatedStorageUintNodeMap, dataSourcePoolPropsMap), Collections.emptyMap(), Collections.emptyList(), mergedPropsMap);
    }
    
    private Map<StorageNode, DataSource> getNewDataSources(final ResourceMetaData resourceMetaData,
                                                           final Map<String, StorageNode> storageUintNodeMap, final Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageUintNodeMap.size(), 1F);
        for (StorageNode each : storageUintNodeMap.values()) {
            if (!resourceMetaData.getDataSources().containsKey(each)) {
                result.put(each, DataSourcePoolCreator.create(dataSourcePoolPropsMap.get(each)));
            }
        }
        return result;
    }
    
    /**
     * Alter storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param propsMap data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource alterStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> propsMap) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropsMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        mergedDataSourcePoolPropsMap.putAll(propsMap);
        Map<String, StorageNode> toBeAlteredStorageUintNodeMap = StorageUnitNodeMapCreator.create(mergedDataSourcePoolPropsMap);
        Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap = StorageNodeAggregator.aggregateDataSourcePoolProperties(mergedDataSourcePoolPropsMap);
        return new SwitchingResource(getAlterNewDataSources(toBeAlteredStorageUintNodeMap, dataSourcePoolPropsMap),
                getStaleDataSources(resourceMetaData, toBeAlteredStorageUintNodeMap), new LinkedHashSet<>(toBeAlteredStorageUintNodeMap.keySet()), mergedDataSourcePoolPropsMap);
    }
    
    private Map<StorageNode, DataSource> getAlterNewDataSources(final Map<String, StorageNode> storageUintNodeMap, final Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageUintNodeMap.size(), 1F);
        for (StorageNode each : storageUintNodeMap.values()) {
            result.put(each, DataSourcePoolCreator.create(dataSourcePoolPropsMap.get(each)));
        }
        return result;
    }
    
    private Map<StorageNode, DataSource> getStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> storageUintNodeMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageUintNodeMap.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : resourceMetaData.getDataSources().entrySet()) {
            if (storageUintNodeMap.containsValue(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Unregister storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitName storage unit name
     * @return created switching resource
     */
    public SwitchingResource unregisterStorageUnit(final ResourceMetaData resourceMetaData, final String storageUnitName) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        mergedDataSourcePoolPropertiesMap.keySet().removeIf(each -> each.equals(storageUnitName));
        resourceMetaData.getStorageUnits().remove(storageUnitName);
        return new SwitchingResource(
                Collections.emptyMap(), getToBeRemovedStaleDataSource(resourceMetaData, storageUnitName), Collections.singleton(storageUnitName), mergedDataSourcePoolPropertiesMap);
    }
    
    private Map<StorageNode, DataSource> getToBeRemovedStaleDataSource(final ResourceMetaData resourceMetaData, final String storageUnitName) {
        Map<String, StorageNode> metaDataMap = new LinkedHashMap<>(
                resourceMetaData.getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageNode(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        StorageNode storageNode = metaDataMap.remove(storageUnitName);
        Map<String, StorageNode> reservedStorageUintNodeMap = resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageNode()));
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(1, 1F);
        if (reservedStorageUintNodeMap.values().stream().noneMatch(each -> each.equals(storageNode))) {
            result.put(storageNode, resourceMetaData.getDataSources().get(storageNode));
        }
        return result;
    }
}

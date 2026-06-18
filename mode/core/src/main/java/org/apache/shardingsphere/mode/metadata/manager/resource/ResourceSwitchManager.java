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
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Resource switch manager.
 */
public final class ResourceSwitchManager {
    
    /**
     * Switch resource by register storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param toBeRegisteredProps to be registered storage unit grouped data source pool properties map
     * @param isInstanceConnectionEnabled is instance connection enabled
     * @return created switching resource
     */
    public SwitchingResource switchByRegisterStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> toBeRegisteredProps,
                                                         final boolean isInstanceConnectionEnabled) {
        Map<String, DataSourcePoolProperties> mergedPropsMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        mergedPropsMap.putAll(toBeRegisteredProps);
        Map<String, StorageNode> toBeCreatedStorageUintNodeMap = StorageUnitNodeMapCreator.create(toBeRegisteredProps, isInstanceConnectionEnabled);
        Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap = StorageNodeAggregator.aggregateDataSourcePoolProperties(toBeRegisteredProps, isInstanceConnectionEnabled);
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
     * Switch resource by alter storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param toBeAlteredProps to be altered data source pool properties map
     * @param isInstanceConnectionEnabled is instance connection enabled
     * @return created switching resource
     */
    public SwitchingResource switchByAlterStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> toBeAlteredProps,
                                                      final boolean isInstanceConnectionEnabled) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropsMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        mergedDataSourcePoolPropsMap.putAll(toBeAlteredProps);
        Map<String, StorageNode> toBeAlteredStorageUintNodeMap = StorageUnitNodeMapCreator.create(mergedDataSourcePoolPropsMap, isInstanceConnectionEnabled);
        Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap = StorageNodeAggregator.aggregateDataSourcePoolProperties(mergedDataSourcePoolPropsMap, isInstanceConnectionEnabled);
        return new SwitchingResource(getAlterNewDataSources(toBeAlteredStorageUintNodeMap, dataSourcePoolPropsMap),
                getStaleDataSources(resourceMetaData, toBeAlteredStorageUintNodeMap.values()), new LinkedHashSet<>(toBeAlteredStorageUintNodeMap.keySet()), mergedDataSourcePoolPropsMap);
    }
    
    private Map<StorageNode, DataSource> getAlterNewDataSources(final Map<String, StorageNode> storageUintNodeMap, final Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageUintNodeMap.size(), 1F);
        for (StorageNode each : storageUintNodeMap.values()) {
            result.put(each, DataSourcePoolCreator.create(dataSourcePoolPropsMap.get(each)));
        }
        return result;
    }
    
    private Map<StorageNode, DataSource> getStaleDataSources(final ResourceMetaData resourceMetaData, final Collection<StorageNode> storageNodes) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : resourceMetaData.getDataSources().entrySet()) {
            if (storageNodes.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Switch resource by unregister storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitNames storage unit names
     * @return created switching resource
     */
    public SwitchingResource switchByUnregisterStorageUnit(final ResourceMetaData resourceMetaData, final Collection<String> storageUnitNames) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        SwitchingResource result = new SwitchingResource(Collections.emptyMap(),
                getToBeRemovedStaleDataSource(resourceMetaData, storageUnitNames), storageUnitNames, mergedDataSourcePoolPropertiesMap);
        for (String each : storageUnitNames) {
            mergedDataSourcePoolPropertiesMap.remove(each);
            resourceMetaData.getStorageUnits().remove(each);
        }
        return result;
    }
    
    /**
     * Create switching resource by unregister storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitNames storage unit names
     * @return created switching resource
     */
    public SwitchingResource createByUnregisterStorageUnit(final ResourceMetaData resourceMetaData, final Collection<String> storageUnitNames) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        storageUnitNames.forEach(mergedDataSourcePoolPropertiesMap::remove);
        return new SwitchingResource(Collections.emptyMap(), getToBeRemovedStaleDataSource(resourceMetaData, storageUnitNames), storageUnitNames, mergedDataSourcePoolPropertiesMap);
    }
    
    private Map<StorageNode, DataSource> getToBeRemovedStaleDataSource(final ResourceMetaData resourceMetaData, final Collection<String> storageUnitNames) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageUnitNames.size(), 1F);
        Map<String, StorageUnit> reservedStorageUnits = getReservedStorageUnits(resourceMetaData, storageUnitNames);
        for (String each : storageUnitNames) {
            if (!resourceMetaData.getStorageUnits().containsKey(each)) {
                continue;
            }
            StorageNode storageNode = resourceMetaData.getStorageUnits().get(each).getStorageNode();
            if (isStorageNodeInUsed(reservedStorageUnits, storageNode)) {
                continue;
            }
            result.put(storageNode, resourceMetaData.getDataSources().get(storageNode));
        }
        return result;
    }
    
    private Map<String, StorageUnit> getReservedStorageUnits(final ResourceMetaData resourceMetaData, final Collection<String> storageUnitNames) {
        Map<String, StorageUnit> result = new HashMap<>(resourceMetaData.getStorageUnits());
        result.keySet().removeIf(storageUnitNames::contains);
        return result;
    }
    
    private boolean isStorageNodeInUsed(final Map<String, StorageUnit> reservedStorageUnits, final StorageNode storageNode) {
        return reservedStorageUnits.values().stream().anyMatch(each -> each.getStorageNode().equals(storageNode));
    }
}

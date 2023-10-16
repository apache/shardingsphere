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
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
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
     * Create switching resource.
     * 
     * @param resourceMetaData resource meta data
     * @param toBeChangedPropsMap to be changed data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource create(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> toBeChangedPropsMap) {
        Map<String, StorageNode> toBeChangedStorageUnitNodeMap = StorageUnitNodeMapCreator.create(toBeChangedPropsMap);
        Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap = StorageNodeAggregator.aggregateDataSourcePoolProperties(toBeChangedPropsMap);
        Map<StorageNode, DataSource> newDataSources = createNewDataSources(resourceMetaData, toBeChangedStorageUnitNodeMap, dataSourcePoolPropsMap);
        Map<StorageNode, DataSource> staleDataSources = getStaleDataSources(resourceMetaData, toBeChangedPropsMap);
        return new SwitchingResource(newDataSources, staleDataSources, getStaleStorageUnitNames(resourceMetaData, toBeChangedStorageUnitNodeMap), toBeChangedPropsMap);
    }
    
    /**
     * Create switching resource by drop resource.
     *
     * @param resourceMetaData resource meta data
     * @param toBeDeletedPropsMap to be deleted data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource createByDropResource(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> toBeDeletedPropsMap) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        mergedDataSourcePoolPropertiesMap.keySet().removeIf(toBeDeletedPropsMap::containsKey);
        Map<String, StorageNode> toRemovedStorageUnitNodeMap = StorageUnitNodeMapCreator.create(toBeDeletedPropsMap);
        return new SwitchingResource(Collections.emptyMap(), getToBeRemovedStaleDataSources(resourceMetaData, toRemovedStorageUnitNodeMap),
                getToBeRemovedStaleStorageUnitNames(resourceMetaData, toRemovedStorageUnitNodeMap), mergedDataSourcePoolPropertiesMap);
    }
    
    /**
     * Create switching resource by alter data source pool properties.
     *
     * @param resourceMetaData resource meta data
     * @param toBeChangedPropsMap to be changed data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource createByAlterDataSourcePoolProperties(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> toBeChangedPropsMap) {
        Map<String, StorageNode> toBeChangedStorageUnitNodeMap = StorageUnitNodeMapCreator.create(toBeChangedPropsMap);
        Map<StorageNode, DataSource> staleStorageDataSources = getStaleDataSources(resourceMetaData, toBeChangedPropsMap);
        staleStorageDataSources.putAll(getToBeDeletedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageUnitNodeMap.values()));
        Collection<String> staleStorageUnitNames = getStaleStorageUnitNames(resourceMetaData, toBeChangedStorageUnitNodeMap);
        staleStorageUnitNames.addAll(getToBeDeletedStorageUnitNames(resourceMetaData.getStorageUnits(), toBeChangedStorageUnitNodeMap.keySet()));
        Map<StorageNode, DataSourcePoolProperties> dataSourcePoolPropsMap = StorageNodeAggregator.aggregateDataSourcePoolProperties(toBeChangedPropsMap);
        return new SwitchingResource(
                createNewDataSources(resourceMetaData, toBeChangedStorageUnitNodeMap, dataSourcePoolPropsMap), staleStorageDataSources, staleStorageUnitNames, toBeChangedPropsMap);
    }
    
    private Map<StorageNode, DataSource> createNewDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> toBeChangedStorageUnitNodeMap,
                                                              final Map<StorageNode, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(resourceMetaData.getDataSources());
        Collection<StorageNode> toBeChangedStorageNodes = toBeChangedStorageUnitNodeMap.values();
        result.keySet().removeAll(getToBeDeletedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodes).keySet());
        result.putAll(getChangedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodes, propsMap));
        result.putAll(getToBeAddedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodes, propsMap));
        return result;
    }
    
    private Map<StorageNode, DataSource> getChangedDataSources(final Map<StorageNode, DataSource> storageNodes,
                                                               final Collection<StorageNode> toBeChangedStorageNodes, final Map<StorageNode, DataSourcePoolProperties> propsMap) {
        Collection<StorageNode> toBeChangedDataSourceNames = toBeChangedStorageNodes.stream()
                .filter(each -> isModifiedDataSource(storageNodes, each, propsMap.get(each))).collect(Collectors.toList());
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(toBeChangedStorageNodes.size(), 1F);
        for (StorageNode each : toBeChangedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each)));
        }
        return result;
    }
    
    private boolean isModifiedDataSource(final Map<StorageNode, DataSource> originalDataSources,
                                         final StorageNode storageNode, final DataSourcePoolProperties propsMap) {
        return originalDataSources.containsKey(storageNode) && !propsMap.equals(DataSourcePoolPropertiesCreator.create(originalDataSources.get(storageNode)));
    }
    
    private Map<StorageNode, DataSource> getToBeAddedDataSources(final Map<StorageNode, DataSource> storageNodes,
                                                                 final Collection<StorageNode> toBeChangedStorageNodes, final Map<StorageNode, DataSourcePoolProperties> propsMap) {
        Collection<StorageNode> toBeAddedDataSourceNames = toBeChangedStorageNodes.stream().filter(each -> !storageNodes.containsKey(each)).collect(Collectors.toList());
        Map<StorageNode, DataSource> result = new LinkedHashMap<>();
        for (StorageNode each : toBeAddedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each)));
        }
        return result;
    }
    
    private Map<StorageNode, DataSource> getToBeRemovedStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> toRemovedStorageUnitNodeMap) {
        Map<String, StorageNode> reservedStorageUnitNodeMap = resourceMetaData.getStorageUnits().entrySet().stream()
                .filter(entry -> !toRemovedStorageUnitNodeMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageNode()));
        return resourceMetaData.getDataSources().entrySet().stream()
                .filter(entry -> toRemovedStorageUnitNodeMap.containsValue(entry.getKey()) && !reservedStorageUnitNodeMap.containsValue(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Collection<String> getToBeRemovedStaleStorageUnitNames(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> toRemovedStorageUnitNodeMap) {
        Map<String, StorageNode> reservedStorageUnitNodeMap = resourceMetaData.getStorageUnits().entrySet().stream()
                .filter(entry -> !toRemovedStorageUnitNodeMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageNode()));
        Map<String, StorageNode> staleStorageUnitNodeMap = resourceMetaData.getStorageUnits().entrySet().stream()
                .filter(entry -> !reservedStorageUnitNodeMap.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageNode()));
        return new LinkedHashSet<>(staleStorageUnitNodeMap.keySet());
    }
    
    private Map<StorageNode, DataSource> getStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolPropsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(resourceMetaData.getDataSources().size(), 1F);
        result.putAll(getToBeChangedDataSources(resourceMetaData.getDataSources(), StorageNodeAggregator.aggregateDataSourcePoolProperties(storageUnitDataSourcePoolPropsMap)));
        return result;
    }
    
    private Collection<String> getStaleStorageUnitNames(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> toBeChangedStorageUnitNodeMap) {
        Map<String, StorageNode> storageUnitNodeMap = new LinkedHashMap<>(resourceMetaData.getStorageUnits().size(), 1F);
        storageUnitNodeMap.putAll(getChangedStorageUnitNodeMap(resourceMetaData.getStorageUnits(), toBeChangedStorageUnitNodeMap));
        return new LinkedHashSet<>(storageUnitNodeMap.keySet());
    }
    
    private Map<StorageNode, DataSource> getToBeChangedDataSources(final Map<StorageNode, DataSource> storageNodes, final Map<StorageNode, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<StorageNode, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageNode storageNode = entry.getKey();
            if (isModifiedDataSource(storageNodes, storageNode, entry.getValue())) {
                result.put(storageNode, storageNodes.get(storageNode));
            }
        }
        return result;
    }
    
    private Map<StorageNode, DataSource> getToBeDeletedDataSources(final Map<StorageNode, DataSource> storageNodes, final Collection<StorageNode> toBeChangedDataSourceNames) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : storageNodes.entrySet()) {
            if (!toBeChangedDataSourceNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Collection<String> getToBeDeletedStorageUnitNames(final Map<String, StorageUnit> storageUnits, final Collection<String> toBeChangedStorageUnitNames) {
        return storageUnits.keySet().stream().filter(storageUnit -> !toBeChangedStorageUnitNames.contains(storageUnit)).collect(Collectors.toList());
    }
    
    private Map<String, StorageNode> getChangedStorageUnitNodeMap(final Map<String, StorageUnit> storageUnits, final Map<String, StorageNode> toBeChangedStorageUnitNodeMap) {
        return toBeChangedStorageUnitNodeMap.entrySet().stream().filter(entry -> isModifiedStorageUnitNodeMap(storageUnits, entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedStorageUnitNodeMap(final Map<String, StorageUnit> originalStorageUnits, final String dataSourceName, final StorageNode storageNode) {
        return originalStorageUnits.containsKey(dataSourceName) && !storageNode.equals(originalStorageUnits.get(dataSourceName).getStorageNode());
    }
}

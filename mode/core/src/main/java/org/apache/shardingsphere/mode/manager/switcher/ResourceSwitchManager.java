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
import org.apache.shardingsphere.infra.metadata.database.resource.StorageResource;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        Map<String, DataSourcePoolProperties> mergedPropsMap = new HashMap<>(resourceMetaData.getStorageUnitMetaData().getDataSourcePoolPropertiesMap());
        mergedPropsMap.putAll(toBeChangedPropsMap);
        Map<String, StorageNode> toBeChangedStorageUnitNodeMap = StorageUnitNodeMapUtils.fromDataSourcePoolProperties(toBeChangedPropsMap);
        return new SwitchingResource(resourceMetaData, createNewStorageResource(resourceMetaData, toBeChangedStorageUnitNodeMap, toBeChangedPropsMap),
                getStaleDataSources(resourceMetaData, toBeChangedStorageUnitNodeMap, mergedPropsMap), mergedPropsMap);
    }
    
    /**
     * Create switching resource by drop resource.
     *
     * @param resourceMetaData resource meta data
     * @param toBeDeletedPropsMap to be deleted data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource createByDropResource(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> toBeDeletedPropsMap) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new HashMap<>(resourceMetaData.getStorageUnitMetaData().getDataSourcePoolPropertiesMap());
        mergedDataSourcePoolPropertiesMap.keySet().removeIf(toBeDeletedPropsMap::containsKey);
        Map<String, StorageNode> toRemovedStorageUnitNodeMap = StorageUnitNodeMapUtils.fromDataSourcePoolProperties(toBeDeletedPropsMap);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleDataSources(resourceMetaData, toRemovedStorageUnitNodeMap), mergedDataSourcePoolPropertiesMap);
    }
    
    /**
     * Create switching resource by alter data source pool properties.
     *
     * @param resourceMetaData resource meta data
     * @param toBeChangedPropsMap to be changed data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource createByAlterDataSourcePoolProperties(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> toBeChangedPropsMap) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new HashMap<>(resourceMetaData.getStorageUnitMetaData().getDataSourcePoolPropertiesMap());
        mergedDataSourcePoolPropertiesMap.keySet().removeIf(each -> !toBeChangedPropsMap.containsKey(each));
        mergedDataSourcePoolPropertiesMap.putAll(toBeChangedPropsMap);
        Map<String, StorageNode> toBeChangedStorageUnitNodeMap = StorageUnitNodeMapUtils.fromDataSourcePoolProperties(toBeChangedPropsMap);
        StorageResource staleStorageResource = getStaleDataSources(resourceMetaData, toBeChangedStorageUnitNodeMap, toBeChangedPropsMap);
        Collection<StorageNodeName> toBeChangedStorageNodeNames = toBeChangedStorageUnitNodeMap.values().stream().map(StorageNode::getName).collect(Collectors.toSet());
        staleStorageResource.getDataSources().putAll(getToBeDeletedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodeNames));
        staleStorageResource.getStorageUnitNodeMap().putAll(
                getToBeDeletedStorageUnitNodeMap(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMap.keySet()));
        return new SwitchingResource(resourceMetaData,
                createNewStorageResource(resourceMetaData, toBeChangedStorageUnitNodeMap, toBeChangedPropsMap), staleStorageResource, mergedDataSourcePoolPropertiesMap);
    }
    
    private StorageResource createNewStorageResource(final ResourceMetaData resourceMetaData,
                                                     final Map<String, StorageNode> toBeChangedStorageUnitNodeMap, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Collection<StorageNodeName> toBeChangedStorageNodeName = toBeChangedStorageUnitNodeMap.values().stream().map(StorageNode::getName).collect(Collectors.toSet());
        Map<StorageNodeName, DataSourcePoolProperties> storageNodeDataSourcePoolProps = StorageUnitNodeMapUtils.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps);
        Map<StorageNodeName, DataSource> storageNodes =
                getNewStorageNodes(resourceMetaData, toBeChangedStorageNodeName, storageNodeDataSourcePoolProps);
        Map<String, StorageNode> storageUnitNodeMap = getNewStorageUnitNodeMap(resourceMetaData, toBeChangedStorageUnitNodeMap);
        return new StorageResource(storageNodes, storageUnitNodeMap);
    }
    
    private Map<StorageNodeName, DataSource> getNewStorageNodes(final ResourceMetaData resourceMetaData, final Collection<StorageNodeName> toBeChangedStorageNodeName,
                                                                final Map<StorageNodeName, DataSourcePoolProperties> propsMap) {
        Map<StorageNodeName, DataSource> result = new LinkedHashMap<>(resourceMetaData.getDataSources());
        result.keySet().removeAll(getToBeDeletedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodeName).keySet());
        result.putAll(getChangedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodeName, propsMap));
        result.putAll(getToBeAddedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodeName, propsMap));
        return result;
    }
    
    private Map<String, StorageNode> getNewStorageUnitNodeMap(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> toBeChangedStorageUnitNodeMap) {
        Map<String, StorageNode> result = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getStorageUnitNodeMap());
        result.keySet().removeAll(getToBeDeletedStorageUnitNodeMap(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMap.keySet()).keySet());
        result.putAll(getChangedStorageUnitNodeMap(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMap));
        result.putAll(getToBeAddedStorageUnitNodeMap(resourceMetaData.getStorageUnitMetaData().getStorageUnitNodeMap(), toBeChangedStorageUnitNodeMap));
        return result;
    }
    
    private Map<StorageNodeName, DataSource> getChangedDataSources(final Map<StorageNodeName, DataSource> storageNodes,
                                                                   final Collection<StorageNodeName> toBeChangedStorageNodeName,
                                                                   final Map<StorageNodeName, DataSourcePoolProperties> propsMap) {
        Collection<StorageNodeName> toBeChangedDataSourceNames = toBeChangedStorageNodeName.stream()
                .filter(each -> isModifiedDataSource(storageNodes, each, propsMap.get(each))).collect(Collectors.toList());
        Map<StorageNodeName, DataSource> result = new LinkedHashMap<>(toBeChangedStorageNodeName.size(), 1F);
        for (StorageNodeName each : toBeChangedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each)));
        }
        return result;
    }
    
    private boolean isModifiedDataSource(final Map<StorageNodeName, DataSource> originalDataSources,
                                         final StorageNodeName storageNodeName, final DataSourcePoolProperties propsMap) {
        return originalDataSources.containsKey(storageNodeName) && !propsMap.equals(DataSourcePoolPropertiesCreator.create(originalDataSources.get(storageNodeName)));
    }
    
    private Map<StorageNodeName, DataSource> getToBeAddedDataSources(final Map<StorageNodeName, DataSource> storageNodes,
                                                                     final Collection<StorageNodeName> toBeChangedStorageNodeName,
                                                                     final Map<StorageNodeName, DataSourcePoolProperties> propsMap) {
        Collection<StorageNodeName> toBeAddedDataSourceNames = toBeChangedStorageNodeName.stream().filter(each -> !storageNodes.containsKey(each)).collect(Collectors.toList());
        Map<StorageNodeName, DataSource> result = new LinkedHashMap<>();
        for (StorageNodeName each : toBeAddedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each)));
        }
        return result;
    }
    
    private StorageResource getToBeRemovedStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> toRemovedStorageUnitNodeMap) {
        Map<String, StorageNode> reservedStorageUnitNodeMap = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !toRemovedStorageUnitNodeMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageNode()));
        Collection<StorageNodeName> toBeRemovedStorageNodeNames = toRemovedStorageUnitNodeMap.values().stream().map(StorageNode::getName).collect(Collectors.toSet());
        Collection<StorageNodeName> inUsedDataSourceNames = reservedStorageUnitNodeMap.values().stream().map(StorageNode::getName).collect(Collectors.toSet());
        Map<StorageNodeName, DataSource> staleStorageNodes = resourceMetaData.getDataSources().entrySet().stream()
                .filter(entry -> toBeRemovedStorageNodeNames.contains(entry.getKey()) && !inUsedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, StorageNode> staleStorageUnitNodeMap = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !reservedStorageUnitNodeMap.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageNode()));
        return new StorageResource(staleStorageNodes, staleStorageUnitNodeMap);
    }
    
    private StorageResource getStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> toBeChangedStorageUnitNodeMap,
                                                final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Map<StorageNodeName, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getDataSources().size(), 1F);
        Map<String, StorageNode> storageUnitNodeMap = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getStorageUnitNodeMap().size(), 1F);
        storageNodes.putAll(getToBeChangedDataSources(resourceMetaData.getDataSources(), StorageUnitNodeMapUtils.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps)));
        storageUnitNodeMap.putAll(getChangedStorageUnitNodeMap(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMap));
        return new StorageResource(storageNodes, storageUnitNodeMap);
    }
    
    private Map<StorageNodeName, DataSource> getToBeChangedDataSources(final Map<StorageNodeName, DataSource> storageNodes,
                                                                       final Map<StorageNodeName, DataSourcePoolProperties> propsMap) {
        Map<StorageNodeName, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<StorageNodeName, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageNodeName storageNodeName = entry.getKey();
            if (isModifiedDataSource(storageNodes, storageNodeName, entry.getValue())) {
                result.put(storageNodeName, storageNodes.get(storageNodeName));
            }
        }
        return result;
    }
    
    private Map<StorageNodeName, DataSource> getToBeDeletedDataSources(final Map<StorageNodeName, DataSource> storageNodes,
                                                                       final Collection<StorageNodeName> toBeChangedDataSourceNames) {
        Map<StorageNodeName, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<StorageNodeName, DataSource> entry : storageNodes.entrySet()) {
            if (!toBeChangedDataSourceNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, StorageNode> getToBeDeletedStorageUnitNodeMap(final Map<String, StorageUnit> storageUnits,
                                                                      final Collection<String> toBeChangedStorageUnitNames) {
        Map<String, StorageNode> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            if (!toBeChangedStorageUnitNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue().getStorageNode());
            }
        }
        return result;
    }
    
    private Map<String, StorageNode> getChangedStorageUnitNodeMap(final Map<String, StorageUnit> storageUnits, final Map<String, StorageNode> toBeChangedStorageUnitNodeMap) {
        return toBeChangedStorageUnitNodeMap.entrySet().stream().filter(entry -> isModifiedStorageUnitNodeMap(storageUnits, entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedStorageUnitNodeMap(final Map<String, StorageUnit> originalStorageUnits,
                                                 final String dataSourceName, final StorageNode storageNode) {
        return originalStorageUnits.containsKey(dataSourceName) && !storageNode.equals(originalStorageUnits.get(dataSourceName).getStorageNode());
    }
    
    private Map<String, StorageNode> getToBeAddedStorageUnitNodeMap(final Map<String, StorageNode> storageUnitNodeMap, final Map<String, StorageNode> toBeChangedStorageUnitNodeMap) {
        return toBeChangedStorageUnitNodeMap.entrySet().stream().filter(entry -> !storageUnitNodeMap.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}

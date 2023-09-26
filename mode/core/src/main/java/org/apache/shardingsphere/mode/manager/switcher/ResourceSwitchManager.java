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
import org.apache.shardingsphere.infra.metadata.database.resource.StorageResourceCreator;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapper;

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
        Map<String, StorageUnitNodeMapper> toBeChangedMappers = StorageResourceCreator.getStorageUnitNodeMappers(toBeChangedPropsMap);
        return new SwitchingResource(resourceMetaData, createNewStorageResource(resourceMetaData, toBeChangedMappers, toBeChangedPropsMap),
                getStaleDataSources(resourceMetaData, toBeChangedMappers, mergedPropsMap), mergedPropsMap);
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
        Map<String, StorageUnitNodeMapper> toRemovedMappers = StorageResourceCreator.getStorageUnitNodeMappers(toBeDeletedPropsMap);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleDataSources(resourceMetaData, toRemovedMappers), mergedDataSourcePoolPropertiesMap);
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
        Map<String, StorageUnitNodeMapper> toBeChangedMappers = StorageResourceCreator.getStorageUnitNodeMappers(toBeChangedPropsMap);
        StorageResource staleStorageResource = getStaleDataSources(resourceMetaData, toBeChangedMappers, toBeChangedPropsMap);
        Collection<StorageNode> toBeChangedStorageNodes = toBeChangedMappers.values().stream().map(StorageUnitNodeMapper::getStorageNode).collect(Collectors.toSet());
        staleStorageResource.getDataSourceMap().putAll(getToBeDeletedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNodes));
        staleStorageResource.getStorageUnitNodeMappers().putAll(
                getToBeDeletedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedMappers.keySet()));
        return new SwitchingResource(resourceMetaData,
                createNewStorageResource(resourceMetaData, toBeChangedMappers, toBeChangedPropsMap), staleStorageResource, mergedDataSourcePoolPropertiesMap);
    }
    
    private StorageResource createNewStorageResource(final ResourceMetaData resourceMetaData,
                                                     final Map<String, StorageUnitNodeMapper> toBeChangedMappers, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Collection<StorageNode> toBeChangedStorageNode = toBeChangedMappers.values().stream().map(StorageUnitNodeMapper::getStorageNode).collect(Collectors.toSet());
        Map<StorageNode, DataSourcePoolProperties> storageNodeDataSourcePoolProps = StorageResourceCreator.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps);
        Map<StorageNode, DataSource> storageNodes =
                getNewStorageNodes(resourceMetaData, toBeChangedStorageNode, storageNodeDataSourcePoolProps);
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = getNewStorageUnitNodeMappers(resourceMetaData, toBeChangedMappers);
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    private Map<StorageNode, DataSource> getNewStorageNodes(final ResourceMetaData resourceMetaData, final Collection<StorageNode> toBeChangedStorageNode,
                                                            final Map<StorageNode, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(resourceMetaData.getDataSourceMap());
        result.keySet().removeAll(getToBeDeletedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNode).keySet());
        result.putAll(getChangedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNode, propsMap));
        result.putAll(getToBeAddedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNode, propsMap));
        return result;
    }
    
    private Map<String, StorageUnitNodeMapper> getNewStorageUnitNodeMappers(final ResourceMetaData resourceMetaData, final Map<String, StorageUnitNodeMapper> toBeChangedStorageUnitNodeMappers) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers());
        result.keySet().removeAll(getToBeDeletedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMappers.keySet()).keySet());
        result.putAll(getChangedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMappers));
        result.putAll(getToBeAddedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers(), toBeChangedStorageUnitNodeMappers));
        return result;
    }
    
    private Map<StorageNode, DataSource> getChangedDataSources(final Map<StorageNode, DataSource> storageNodes,
                                                               final Collection<StorageNode> toBeChangedStorageNode, final Map<StorageNode, DataSourcePoolProperties> propsMap) {
        Collection<StorageNode> toBeChangedDataSourceNames = toBeChangedStorageNode.stream()
                .filter(each -> isModifiedDataSource(storageNodes, each, propsMap.get(each))).collect(Collectors.toList());
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(toBeChangedStorageNode.size(), 1F);
        for (StorageNode each : toBeChangedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each)));
        }
        return result;
    }
    
    private boolean isModifiedDataSource(final Map<StorageNode, DataSource> originalDataSources, final StorageNode storageNode, final DataSourcePoolProperties propsMap) {
        return originalDataSources.containsKey(storageNode) && !propsMap.equals(DataSourcePoolPropertiesCreator.create(originalDataSources.get(storageNode)));
    }
    
    private Map<StorageNode, DataSource> getToBeAddedDataSources(final Map<StorageNode, DataSource> storageNodes, final Collection<StorageNode> toBeChangedStorageNode,
                                                                 final Map<StorageNode, DataSourcePoolProperties> propsMap) {
        Collection<StorageNode> toBeAddedDataSourceNames = toBeChangedStorageNode.stream().filter(each -> !storageNodes.containsKey(each)).collect(Collectors.toList());
        Map<StorageNode, DataSource> result = new LinkedHashMap<>();
        for (StorageNode each : toBeAddedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each)));
        }
        return result;
    }
    
    private StorageResource getToBeRemovedStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageUnitNodeMapper> toRemovedMappers) {
        Map<String, StorageUnitNodeMapper> reservedStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !toRemovedMappers.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getUnitNodeMapper()));
        Collection<StorageNode> toBeRemovedStorageNodes = toRemovedMappers.values().stream().map(StorageUnitNodeMapper::getStorageNode).collect(Collectors.toSet());
        Collection<StorageNode> inUsedDataSourceNames = reservedStorageUnitNodeMappers.values().stream().map(StorageUnitNodeMapper::getStorageNode).collect(Collectors.toSet());
        Map<StorageNode, DataSource> staleStorageNodes = resourceMetaData.getDataSourceMap().entrySet().stream()
                .filter(entry -> toBeRemovedStorageNodes.contains(entry.getKey()) && !inUsedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, StorageUnitNodeMapper> staleStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !reservedStorageUnitNodeMappers.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getUnitNodeMapper()));
        return new StorageResource(staleStorageNodes, staleStorageUnitNodeMappers);
    }
    
    private StorageResource getStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageUnitNodeMapper> toBeChangedMappers,
                                                final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getDataSourceMap().size(), 1F);
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers().size(), 1F);
        storageNodes.putAll(getToBeChangedDataSources(resourceMetaData.getDataSourceMap(), StorageResourceCreator.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps)));
        storageUnitNodeMappers.putAll(getChangedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedMappers));
        return new StorageResource(storageNodes, storageUnitNodeMappers);
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
    
    private Map<String, StorageUnitNodeMapper> getToBeDeletedStorageUnitNodeMappers(final Map<String, StorageUnit> storageUnits,
                                                                                    final Collection<String> toBeChangedStorageUnitNames) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            if (!toBeChangedStorageUnitNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue().getUnitNodeMapper());
            }
        }
        return result;
    }
    
    private Map<String, StorageUnitNodeMapper> getChangedStorageUnitNodeMappers(final Map<String, StorageUnit> storageUnits,
                                                                                final Map<String, StorageUnitNodeMapper> toBeChangedStorageUnitNodeMappers) {
        return toBeChangedStorageUnitNodeMappers.entrySet().stream().filter(entry -> isModifiedStorageUnitNodeMapper(storageUnits, entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedStorageUnitNodeMapper(final Map<String, StorageUnit> originalStorageUnits,
                                                    final String dataSourceName, final StorageUnitNodeMapper storageUnitNodeMapper) {
        return originalStorageUnits.containsKey(dataSourceName) && !storageUnitNodeMapper.equals(originalStorageUnits.get(dataSourceName).getUnitNodeMapper());
    }
    
    private Map<String, StorageUnitNodeMapper> getToBeAddedStorageUnitNodeMappers(final Map<String, StorageUnitNodeMapper> storageUnitNodeMappers,
                                                                                  final Map<String, StorageUnitNodeMapper> toBeChangedStorageUnitNodeMappers) {
        return toBeChangedStorageUnitNodeMappers.entrySet().stream().filter(entry -> !storageUnitNodeMappers.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}

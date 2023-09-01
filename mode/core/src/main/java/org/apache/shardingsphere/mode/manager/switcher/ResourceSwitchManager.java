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
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageResource;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageResourceCreator;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageResourceWithProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageUnitNodeMapper;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;

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
        StorageResourceWithProperties toBeChangedStorageResource = StorageResourceCreator.createStorageResourceWithoutDataSource(toBeChangedPropsMap);
        return new SwitchingResource(resourceMetaData, createNewStorageResource(resourceMetaData, toBeChangedStorageResource),
                getStaleDataSources(resourceMetaData, toBeChangedStorageResource), mergedPropsMap);
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
        StorageResourceWithProperties toToBeRemovedStorageResource = StorageResourceCreator.createStorageResourceWithoutDataSource(toBeDeletedPropsMap);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleDataSources(resourceMetaData, toToBeRemovedStorageResource), mergedDataSourcePoolPropertiesMap);
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
        StorageResourceWithProperties toBeChangedStorageResource = StorageResourceCreator.createStorageResourceWithoutDataSource(toBeChangedPropsMap);
        StorageResource staleStorageResource = getStaleDataSources(resourceMetaData, toBeChangedStorageResource);
        staleStorageResource.getStorageNodeDataSources()
                .putAll(getToBeDeletedDataSources(resourceMetaData.getStorageNodeDataSources(), toBeChangedStorageResource.getStorageNodeDataSources().keySet()));
        staleStorageResource.getStorageUnitNodeMappers().putAll(
                getToBeDeletedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageResource.getStorageUnitNodeMappers().keySet()));
        return new SwitchingResource(resourceMetaData, createNewStorageResource(resourceMetaData, toBeChangedStorageResource), staleStorageResource, mergedDataSourcePoolPropertiesMap);
    }
    
    private StorageResource createNewStorageResource(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeChangedStorageResource) {
        Map<StorageNode, DataSource> storageNodes =
                getNewStorageNodes(resourceMetaData, toBeChangedStorageResource.getStorageNodeDataSources(), toBeChangedStorageResource.getDataSourcePoolPropertiesMap());
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = getNewStorageUnitNodeMappers(resourceMetaData, toBeChangedStorageResource.getStorageUnitNodeMappers());
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    private Map<StorageNode, DataSource> getNewStorageNodes(final ResourceMetaData resourceMetaData,
                                                            final Map<StorageNode, DataSource> toBeChangedStorageNodes, final Map<String, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(resourceMetaData.getStorageNodeDataSources());
        result.keySet().removeAll(getToBeDeletedDataSources(resourceMetaData.getStorageNodeDataSources(), toBeChangedStorageNodes.keySet()).keySet());
        result.putAll(getChangedDataSources(resourceMetaData.getStorageNodeDataSources(), toBeChangedStorageNodes, propsMap));
        result.putAll(getToBeAddedDataSources(resourceMetaData.getStorageNodeDataSources(), toBeChangedStorageNodes, propsMap));
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
                                                               final Map<StorageNode, DataSource> toBeChangedStorageNodes, final Map<String, DataSourcePoolProperties> propsMap) {
        Collection<StorageNode> toBeChangedDataSourceNames = toBeChangedStorageNodes.keySet().stream()
                .filter(each -> isModifiedDataSource(storageNodes, each, propsMap.get(each.getName()))).collect(Collectors.toList());
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(toBeChangedStorageNodes.size(), 1F);
        for (StorageNode each : toBeChangedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each.getName())));
        }
        return result;
    }
    
    private boolean isModifiedDataSource(final Map<StorageNode, DataSource> originalDataSources, final StorageNode storageNode, final DataSourcePoolProperties propsMap) {
        return originalDataSources.containsKey(storageNode) && !propsMap.equals(DataSourcePoolPropertiesCreator.create(originalDataSources.get(storageNode)));
    }
    
    private Map<StorageNode, DataSource> getToBeAddedDataSources(final Map<StorageNode, DataSource> storageNodes, final Map<StorageNode, DataSource> toBeChangedStorageNodes,
                                                                 final Map<String, DataSourcePoolProperties> propsMap) {
        Collection<StorageNode> toBeAddedDataSourceNames = toBeChangedStorageNodes.keySet().stream().filter(each -> !storageNodes.containsKey(each)).collect(Collectors.toList());
        Map<StorageNode, DataSource> result = new LinkedHashMap<>();
        for (StorageNode each : toBeAddedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(propsMap.get(each.getName())));
        }
        return result;
    }
    
    private StorageResource getToBeRemovedStaleDataSources(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeRemovedStorageResource) {
        Map<String, StorageUnitNodeMapper> reservedStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !toBeRemovedStorageResource.getStorageUnitNodeMappers().containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getUnitNodeMapper()));
        Collection<StorageNode> inUsedDataSourceNames = reservedStorageUnitNodeMappers.values().stream().map(StorageUnitNodeMapper::getStorageNode).collect(Collectors.toSet());
        Map<StorageNode, DataSource> staleStorageNodes = resourceMetaData.getStorageNodeDataSources().entrySet().stream()
                .filter(entry -> toBeRemovedStorageResource.getStorageNodeDataSources().containsKey(entry.getKey()) && !inUsedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, StorageUnitNodeMapper> staleStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !reservedStorageUnitNodeMappers.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getUnitNodeMapper()));
        return new StorageResource(staleStorageNodes, staleStorageUnitNodeMappers);
    }
    
    private StorageResource getStaleDataSources(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeChangedStorageResource) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getStorageNodeDataSources().size(), 1F);
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers().size(), 1F);
        storageNodes.putAll(getToBeChangedDataSources(resourceMetaData.getStorageNodeDataSources(), toBeChangedStorageResource.getDataSourcePoolPropertiesMap()));
        storageUnitNodeMappers.putAll(getChangedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageResource.getStorageUnitNodeMappers()));
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    private Map<StorageNode, DataSource> getToBeChangedDataSources(final Map<StorageNode, DataSource> storageNodes, final Map<String, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageNode storageNode = new StorageNode(entry.getKey());
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

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
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.datasource.storage.StorageResourceWithProperties;
import org.apache.shardingsphere.infra.datasource.storage.StorageUnitNodeMapper;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
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
     * @param toBeChangedDataSourceProps to be changed data source properties map
     * @return created switching resource
     */
    public SwitchingResource create(final ResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().putAll(toBeChangedDataSourceProps);
        StorageResourceWithProperties toBeChangedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(toBeChangedDataSourceProps);
        return new SwitchingResource(resourceMetaData, createNewStorageResource(resourceMetaData, toBeChangedStorageResource),
                getStaleDataSources(resourceMetaData, toBeChangedStorageResource), toBeChangedDataSourceProps);
    }
    
    /**
     * Create switching resource by drop resource.
     *
     * @param resourceMetaData resource meta data
     * @param toBeDeletedDataSourceProps to be deleted data source properties map
     * @return created switching resource
     */
    public SwitchingResource createByDropResource(final ResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> toBeDeletedDataSourceProps) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().keySet().removeIf(toBeDeletedDataSourceProps::containsKey);
        StorageResourceWithProperties toToBeRemovedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(toBeDeletedDataSourceProps);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleDataSources(resourceMetaData, toToBeRemovedStorageResource),
                getToBeReversedDataSourcePropsMap(resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap(), toBeDeletedDataSourceProps.keySet()));
    }
    
    private Map<String, DataSourceProperties> getToBeReversedDataSourcePropsMap(final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<String> toBeDroppedResourceNames) {
        return dataSourcePropsMap.entrySet().stream().filter(entry -> !toBeDroppedResourceNames.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Create switching resource by alter data source props.
     *
     * @param resourceMetaData resource meta data
     * @param toBeChangedDataSourceProps to be changed data source properties map
     * @return created switching resource
     */
    public SwitchingResource createByAlterDataSourceProps(final ResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().keySet().removeIf(each -> !toBeChangedDataSourceProps.containsKey(each));
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().putAll(toBeChangedDataSourceProps);
        StorageResourceWithProperties toBeChangedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(toBeChangedDataSourceProps);
        StorageResource staleStorageResource = getStaleDataSources(resourceMetaData, toBeChangedStorageResource);
        staleStorageResource.getStorageNodes().putAll(getToBeDeletedDataSources(resourceMetaData.getStorageNodeMetaData().getDataSources(), toBeChangedStorageResource.getStorageNodes().keySet()));
        staleStorageResource.getStorageUnitNodeMappers().putAll(
                getToBeDeletedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers(), toBeChangedStorageResource.getStorageUnitNodeMappers().keySet()));
        return new SwitchingResource(resourceMetaData, createNewStorageResource(resourceMetaData, toBeChangedStorageResource), staleStorageResource, toBeChangedDataSourceProps);
    }
    
    private StorageResource createNewStorageResource(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeChangedStorageResource) {
        Map<String, DataSource> storageNodes = getNewStorageNodes(resourceMetaData, toBeChangedStorageResource.getStorageNodes(), toBeChangedStorageResource.getDataSourcePropertiesMap());
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = getNewStorageUnitNodeMappers(resourceMetaData, toBeChangedStorageResource.getStorageUnitNodeMappers());
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    private Map<String, DataSource> getNewStorageNodes(final ResourceMetaData resourceMetaData, final Map<String, DataSource> toBeChangedStorageNodes,
                                                       final Map<String, DataSourceProperties> dataSourcePropertiesMap) {
        Map<String, DataSource> result = new LinkedHashMap<>(resourceMetaData.getStorageNodeMetaData().getDataSources());
        result.keySet().removeAll(getToBeDeletedDataSources(resourceMetaData.getStorageNodeMetaData().getDataSources(), toBeChangedStorageNodes.keySet()).keySet());
        result.putAll(getChangedDataSources(resourceMetaData.getStorageNodeMetaData().getDataSources(), toBeChangedStorageNodes, dataSourcePropertiesMap));
        result.putAll(getToBeAddedDataSources(resourceMetaData.getStorageNodeMetaData().getDataSources(), toBeChangedStorageNodes, dataSourcePropertiesMap));
        return result;
    }
    
    private Map<String, StorageUnitNodeMapper> getNewStorageUnitNodeMappers(final ResourceMetaData resourceMetaData, final Map<String, StorageUnitNodeMapper> toBeChangedStorageUnitNodeMappers) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers());
        result.keySet().removeAll(getToBeDeletedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers(), toBeChangedStorageUnitNodeMappers.keySet()).keySet());
        result.putAll(getChangedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers(), toBeChangedStorageUnitNodeMappers));
        result.putAll(getToBeAddedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers(), toBeChangedStorageUnitNodeMappers));
        return result;
    }
    
    private Map<String, DataSource> getChangedDataSources(final Map<String, DataSource> storageNodes, final Map<String, DataSource> toBeChangedStorageNodes,
                                                          final Map<String, DataSourceProperties> dataSourcePropertiesMap) {
        Collection<String> toBeChangedDataSourceNames = toBeChangedStorageNodes.keySet().stream()
                .filter(each -> isModifiedDataSource(storageNodes, each, dataSourcePropertiesMap.get(each))).collect(Collectors.toList());
        Map<String, DataSource> result = new LinkedHashMap<>(toBeChangedStorageNodes.size(), 1F);
        for (String each : toBeChangedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(dataSourcePropertiesMap.get(each)));
        }
        return result;
    }
    
    private boolean isModifiedDataSource(final Map<String, DataSource> originalDataSources, final String dataSourceName, final DataSourceProperties dataSourceProps) {
        return originalDataSources.containsKey(dataSourceName) && !dataSourceProps.equals(DataSourcePropertiesCreator.create(originalDataSources.get(dataSourceName)));
    }
    
    private Map<String, DataSource> getToBeAddedDataSources(final Map<String, DataSource> storageNodes, final Map<String, DataSource> toBeChangedStorageNodes,
                                                            final Map<String, DataSourceProperties> dataSourcePropertiesMap) {
        Collection<String> toBeAddedDataSourceNames = toBeChangedStorageNodes.keySet().stream().filter(each -> !storageNodes.containsKey(each)).collect(Collectors.toList());
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (String each : toBeAddedDataSourceNames) {
            result.put(each, DataSourcePoolCreator.create(dataSourcePropertiesMap.get(each)));
        }
        return result;
    }
    
    private StorageResource getToBeRemovedStaleDataSources(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeRemovedStorageResource) {
        Map<String, StorageUnitNodeMapper> reservedStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers().entrySet().stream()
                .filter(entry -> !toBeRemovedStorageResource.getStorageUnitNodeMappers().containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Collection<String> inUsedDataSourceNames = reservedStorageUnitNodeMappers.values().stream().map(StorageUnitNodeMapper::getNodeName).collect(Collectors.toSet());
        Map<String, DataSource> staleStorageNodes = resourceMetaData.getStorageNodeMetaData().getDataSources().entrySet().stream()
                .filter(entry -> toBeRemovedStorageResource.getStorageNodes().containsKey(entry.getKey()) && !inUsedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, StorageUnitNodeMapper> staleStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers().entrySet().stream()
                .filter(entry -> !reservedStorageUnitNodeMappers.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new StorageResource(staleStorageNodes, staleStorageUnitNodeMappers);
    }
    
    private StorageResource getStaleDataSources(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeChangedStorageResource) {
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getStorageNodeMetaData().getDataSources().size(), 1F);
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers().size(), 1F);
        storageNodes.putAll(getToBeChangedDataSources(resourceMetaData.getStorageNodeMetaData().getDataSources(), toBeChangedStorageResource.getDataSourcePropertiesMap()));
        storageUnitNodeMappers.putAll(getChangedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers(), toBeChangedStorageResource.getStorageUnitNodeMappers()));
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    private Map<String, DataSource> getToBeChangedDataSources(final Map<String, DataSource> storageNodes, final Map<String, DataSourceProperties> dataSourcePropertiesMap) {
        Map<String, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<String, DataSourceProperties> entry : dataSourcePropertiesMap.entrySet()) {
            if (isModifiedDataSource(storageNodes, entry.getKey(), entry.getValue())) {
                result.put(entry.getKey(), storageNodes.get(entry.getKey()));
            }
        }
        return result;
    }
    
    private Map<String, DataSource> getToBeDeletedDataSources(final Map<String, DataSource> storageNodes, final Collection<String> toBeChangedDataSourceNames) {
        Map<String, DataSource> result = new LinkedHashMap<>(storageNodes.size(), 1F);
        for (Entry<String, DataSource> entry : storageNodes.entrySet()) {
            if (!toBeChangedDataSourceNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, StorageUnitNodeMapper> getToBeDeletedStorageUnitNodeMappers(final Map<String, StorageUnitNodeMapper> storageUnitNodeMappers,
                                                                                    final Collection<String> toBeChangedStorageUnitNames) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>(storageUnitNodeMappers.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : storageUnitNodeMappers.entrySet()) {
            if (!toBeChangedStorageUnitNames.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, StorageUnitNodeMapper> getChangedStorageUnitNodeMappers(final Map<String, StorageUnitNodeMapper> storageUnitNodeMappers,
                                                                                final Map<String, StorageUnitNodeMapper> toBeChangedStorageUnitNodeMappers) {
        return toBeChangedStorageUnitNodeMappers.entrySet().stream().filter(entry -> isModifiedStorageUnitNodeMapper(storageUnitNodeMappers, entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedStorageUnitNodeMapper(final Map<String, StorageUnitNodeMapper> originalStorageUnitNodeMappers,
                                                    final String dataSourceName, final StorageUnitNodeMapper storageUnitNodeMapper) {
        return originalStorageUnitNodeMappers.containsKey(dataSourceName) && !storageUnitNodeMapper.equals(originalStorageUnitNodeMappers.get(dataSourceName));
    }
    
    private Map<String, StorageUnitNodeMapper> getToBeAddedStorageUnitNodeMappers(final Map<String, StorageUnitNodeMapper> storageUnitNodeMappers,
                                                                                  final Map<String, StorageUnitNodeMapper> toBeChangedStorageUnitNodeMappers) {
        return toBeChangedStorageUnitNodeMappers.entrySet().stream()
                .filter(entry -> !storageUnitNodeMappers.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}

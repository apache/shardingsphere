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
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapperUtils;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
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
        Map<String, StorageUnitNodeMapper> toBeChangedMappers = StorageUnitNodeMapperUtils.fromDataSourcePoolProperties(toBeChangedPropsMap);
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
        Map<String, StorageUnitNodeMapper> toRemovedMappers = StorageUnitNodeMapperUtils.fromDataSourcePoolProperties(toBeDeletedPropsMap);
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
        Map<String, StorageUnitNodeMapper> toBeChangedMappers = StorageUnitNodeMapperUtils.fromDataSourcePoolProperties(toBeChangedPropsMap);
        StorageResource staleStorageResource = getStaleDataSources(resourceMetaData, toBeChangedMappers, toBeChangedPropsMap);
        Collection<StorageNodeName> toBeChangedStorageNodeNames = toBeChangedMappers.values().stream().map(each -> each.getStorageNode().getName()).collect(Collectors.toSet());
        staleStorageResource.getDataSourceMap().putAll(getToBeDeletedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNodeNames));
        staleStorageResource.getStorageUnitNodeMappers().putAll(
                getToBeDeletedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedMappers.keySet()));
        return new SwitchingResource(resourceMetaData,
                createNewStorageResource(resourceMetaData, toBeChangedMappers, toBeChangedPropsMap), staleStorageResource, mergedDataSourcePoolPropertiesMap);
    }
    
    private StorageResource createNewStorageResource(final ResourceMetaData resourceMetaData,
                                                     final Map<String, StorageUnitNodeMapper> toBeChangedMappers, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Collection<StorageNodeName> toBeChangedStorageNodeName = toBeChangedMappers.values().stream().map(each -> each.getStorageNode().getName()).collect(Collectors.toSet());
        Map<StorageNodeName, DataSourcePoolProperties> storageNodeDataSourcePoolProps = StorageUnitNodeMapperUtils.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps);
        Map<StorageNodeName, DataSource> storageNodes =
                getNewStorageNodes(resourceMetaData, toBeChangedStorageNodeName, storageNodeDataSourcePoolProps);
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = getNewStorageUnitNodeMappers(resourceMetaData, toBeChangedMappers);
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    private Map<StorageNodeName, DataSource> getNewStorageNodes(final ResourceMetaData resourceMetaData, final Collection<StorageNodeName> toBeChangedStorageNodeName,
                                                                final Map<StorageNodeName, DataSourcePoolProperties> propsMap) {
        Map<StorageNodeName, DataSource> result = new LinkedHashMap<>(resourceMetaData.getDataSourceMap());
        result.keySet().removeAll(getToBeDeletedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNodeName).keySet());
        result.putAll(getChangedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNodeName, propsMap));
        result.putAll(getToBeAddedDataSources(resourceMetaData.getDataSourceMap(), toBeChangedStorageNodeName, propsMap));
        return result;
    }
    
    private Map<String, StorageUnitNodeMapper> getNewStorageUnitNodeMappers(final ResourceMetaData resourceMetaData, final Map<String, StorageUnitNodeMapper> toBeChangedStorageUnitNodeMappers) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers());
        result.keySet().removeAll(getToBeDeletedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMappers.keySet()).keySet());
        result.putAll(getChangedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedStorageUnitNodeMappers));
        result.putAll(getToBeAddedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers(), toBeChangedStorageUnitNodeMappers));
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
    
    private StorageResource getToBeRemovedStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageUnitNodeMapper> toRemovedMappers) {
        Map<String, StorageUnitNodeMapper> reservedStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !toRemovedMappers.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getUnitNodeMapper()));
        Collection<StorageNodeName> toBeRemovedStorageNodeNames = toRemovedMappers.values().stream().map(each -> each.getStorageNode().getName()).collect(Collectors.toSet());
        Collection<StorageNodeName> inUsedDataSourceNames = reservedStorageUnitNodeMappers.values().stream().map(each -> each.getStorageNode().getName()).collect(Collectors.toSet());
        Map<StorageNodeName, DataSource> staleStorageNodes = resourceMetaData.getDataSourceMap().entrySet().stream()
                .filter(entry -> toBeRemovedStorageNodeNames.contains(entry.getKey()) && !inUsedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, StorageUnitNodeMapper> staleStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getStorageUnits().entrySet().stream()
                .filter(entry -> !reservedStorageUnitNodeMappers.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getUnitNodeMapper()));
        return new StorageResource(staleStorageNodes, staleStorageUnitNodeMappers);
    }
    
    private StorageResource getStaleDataSources(final ResourceMetaData resourceMetaData, final Map<String, StorageUnitNodeMapper> toBeChangedMappers,
                                                final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Map<StorageNodeName, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getDataSourceMap().size(), 1F);
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>(resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers().size(), 1F);
        storageNodes.putAll(getToBeChangedDataSources(resourceMetaData.getDataSourceMap(), StorageUnitNodeMapperUtils.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps)));
        storageUnitNodeMappers.putAll(getChangedStorageUnitNodeMappers(resourceMetaData.getStorageUnitMetaData().getStorageUnits(), toBeChangedMappers));
        return new StorageResource(storageNodes, storageUnitNodeMappers);
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

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
import org.apache.shardingsphere.infra.datasource.storage.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;

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
    public SwitchingResource create(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
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
    public SwitchingResource createByDropResource(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> toBeDeletedDataSourceProps) {
        StorageResourceWithProperties toToBeRemovedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(toBeDeletedDataSourceProps);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleDataSources(resourceMetaData, toToBeRemovedStorageResource),
                getToBeReversedDataSourcePropsMap(resourceMetaData.getDataSourcePropsMap(), toBeDeletedDataSourceProps.keySet()));
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
    public SwitchingResource createByAlterDataSourceProps(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        StorageResourceWithProperties toBeChangedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(toBeChangedDataSourceProps);
        StorageResource staleStorageResource = getStaleDataSources(resourceMetaData, toBeChangedStorageResource);
        staleStorageResource.getStorageNodes().putAll(getToBeDeletedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageResource.getStorageNodes().keySet()));
        return new SwitchingResource(resourceMetaData, createNewStorageResource(resourceMetaData, toBeChangedStorageResource), staleStorageResource, toBeChangedDataSourceProps);
    }
    
    private StorageResource createNewStorageResource(final ShardingSphereResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeChangedStorageResource) {
        Map<String, DataSource> storageNodes = getNewStorageNodes(resourceMetaData, toBeChangedStorageResource.getStorageNodes(), toBeChangedStorageResource.getDataSourcePropertiesMap());
        Map<String, StorageUnit> storageUnits = getNewStorageUnits(resourceMetaData, toBeChangedStorageResource.getStorageUnits());
        return new StorageResource(storageNodes, Collections.emptyMap(), storageUnits);
    }
    
    private Map<String, DataSource> getNewStorageNodes(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSource> toBeChangedStorageNodes,
                                                       final Map<String, DataSourceProperties> dataSourcePropertiesMap) {
        Map<String, DataSource> result = new LinkedHashMap<>(resourceMetaData.getDataSources());
        result.keySet().removeAll(getToBeDeletedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodes.keySet()).keySet());
        result.putAll(getChangedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodes, dataSourcePropertiesMap));
        result.putAll(getToBeAddedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageNodes, dataSourcePropertiesMap));
        return result;
    }
    
    private Map<String, StorageUnit> getNewStorageUnits(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, StorageUnit> toBeChangedStorageUnits) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(resourceMetaData.getStorageUnits());
        result.keySet().removeAll(getToBeDeletedStorageUnits(resourceMetaData.getStorageUnits(), toBeChangedStorageUnits.keySet()).keySet());
        result.putAll(getChangedStorageUnits(resourceMetaData.getStorageUnits(), toBeChangedStorageUnits));
        result.putAll(getToBeAddedStorageUnits(resourceMetaData.getStorageUnits(), toBeChangedStorageUnits));
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
    
    private StorageResource getToBeRemovedStaleDataSources(final ShardingSphereResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeRemovedStorageResource) {
        Map<String, StorageUnit> reservedStorageUnits = resourceMetaData.getStorageUnits().entrySet().stream()
                .filter(entry -> !toBeRemovedStorageResource.getStorageUnits().containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Collection<String> inUsedDataSourceNames = reservedStorageUnits.values().stream().map(StorageUnit::getNodeName).collect(Collectors.toSet());
        Map<String, DataSource> staleStorageNodes = resourceMetaData.getDataSources().entrySet().stream().filter(entry -> toBeRemovedStorageResource.getStorageNodes().containsKey(entry.getKey())
                && !inUsedDataSourceNames.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, StorageUnit> staleStorageUnits = resourceMetaData.getStorageUnits().entrySet().stream()
                .filter(entry -> !reservedStorageUnits.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new StorageResource(staleStorageNodes, Collections.emptyMap(), staleStorageUnits);
    }
    
    private StorageResource getStaleDataSources(final ShardingSphereResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeChangedStorageResource) {
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getDataSources().size(), 1F);
        storageNodes.putAll(getToBeChangedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageResource.getDataSourcePropertiesMap()));
        return new StorageResource(storageNodes, Collections.emptyMap(), Collections.emptyMap());
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
        return storageNodes.entrySet().stream().filter(entry -> !toBeChangedDataSourceNames.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, StorageUnit> getToBeDeletedStorageUnits(final Map<String, StorageUnit> storageUnits, final Collection<String> toBeChangedStorageUnitNames) {
        return storageUnits.entrySet().stream().filter(entry -> !toBeChangedStorageUnitNames.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, StorageUnit> getChangedStorageUnits(final Map<String, StorageUnit> storageUnits, final Map<String, StorageUnit> toBeChangedStorageUnits) {
        return toBeChangedStorageUnits.entrySet().stream().filter(entry -> isModifiedStorageUnit(storageUnits, entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedStorageUnit(final Map<String, StorageUnit> originalStorageUnits, final String dataSourceName, final StorageUnit storageUnit) {
        return originalStorageUnits.containsKey(dataSourceName) && !storageUnit.equals(originalStorageUnits.get(dataSourceName));
    }
    
    private Map<String, StorageUnit> getToBeAddedStorageUnits(final Map<String, StorageUnit> storageUnits, final Map<String, StorageUnit> toBeChangedStorageUnits) {
        return toBeChangedStorageUnits.entrySet().stream()
                .filter(entry -> !storageUnits.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}

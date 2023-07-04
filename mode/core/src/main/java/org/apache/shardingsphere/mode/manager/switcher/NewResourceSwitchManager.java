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
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
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
     * @param dataSourceProps data source properties
     * @return created switching resource
     */
    public SwitchingResource registerStorageUnit(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> dataSourceProps) {
        return new SwitchingResource(resourceMetaData, DataSourcePoolCreator.createStorageResource(dataSourceProps),
                new StorageResource(Collections.emptyMap(), Collections.emptyMap()), dataSourceProps);
    }
    
    /**
     * Alter storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param dataSourceProps data source properties
     * @return created switching resource
     */
    public SwitchingResource alterStorageUnit(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> dataSourceProps) {
        return new SwitchingResource(resourceMetaData, DataSourcePoolCreator.createStorageResource(dataSourceProps),
                getStaleStorageResource(resourceMetaData, dataSourceProps), dataSourceProps);
    }
    
    private StorageResource getStaleStorageResource(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> dataSourceProps) {
        StorageResourceWithProperties toBeChangedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(dataSourceProps);
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getDataSources().size(), 1F);
        storageNodes.putAll(getToBeChangedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageResource.getStorageNodes()));
        return new StorageResource(storageNodes, Collections.emptyMap());
    }
    
    private Map<String, DataSource> getToBeChangedDataSources(final Map<String, DataSource> storageNodes, final Map<String, DataSource> toBeChangedStorageNodes) {
        return storageNodes.entrySet().stream().filter(entry -> toBeChangedStorageNodes.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    /**
     * Unregister storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitName storage unit name
     * @return created switching resource
     */
    public SwitchingResource unregisterStorageUnit(final ShardingSphereResourceMetaData resourceMetaData, final String storageUnitName) {
        DataSource dataSource = resourceMetaData.getDataSources().get(resourceMetaData.getStorageUnitMetaData().getStorageUnits().get(storageUnitName).getNodeName());
        DataSourceProperties dataSourceProperties = DataSourcePropertiesCreator.create(dataSource);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleStorageResource(resourceMetaData, storageUnitName, dataSourceProperties), Collections.emptyMap());
    }
    
    private StorageResource getToBeRemovedStaleStorageResource(final ShardingSphereResourceMetaData resourceMetaData, final String storageUnitName, final DataSourceProperties dataSourceProps) {
        StorageResourceWithProperties toBeChangedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(Collections.singletonMap(storageUnitName, dataSourceProps));
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(resourceMetaData.getDataSources().size(), 1F);
        storageNodes.putAll(getToBeRemovedDataSources(resourceMetaData.getDataSources(), toBeChangedStorageResource.getStorageNodes()));
        return new StorageResource(storageNodes, Collections.emptyMap());
    }
    
    private Map<String, DataSource> getToBeRemovedDataSources(final Map<String, DataSource> storageNodes, final Map<String, DataSource> toBeChangedStorageNodes) {
        return storageNodes.entrySet().stream().filter(entry -> toBeChangedStorageNodes.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}

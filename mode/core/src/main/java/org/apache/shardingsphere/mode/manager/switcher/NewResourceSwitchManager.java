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
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.datasource.storage.StorageResourceWithProperties;
import org.apache.shardingsphere.infra.datasource.storage.StorageUnitNodeMapper;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    public SwitchingResource registerStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> dataSourceProps) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().putAll(dataSourceProps);
        StorageResourceWithProperties toBeCreatedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(dataSourceProps);
        return new SwitchingResource(resourceMetaData, getRegisterNewStorageResource(resourceMetaData, toBeCreatedStorageResource),
                new StorageResource(Collections.emptyMap(), Collections.emptyMap()), resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap());
    }
    
    private StorageResource getRegisterNewStorageResource(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeCreatedStorageResource) {
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(toBeCreatedStorageResource.getStorageNodes().size(), 1F);
        for (String each : toBeCreatedStorageResource.getStorageNodes().keySet()) {
            if (!resourceMetaData.getStorageNodeMetaData().getDataSources().containsKey(each)) {
                storageNodes.put(each, DataSourcePoolCreator.create(toBeCreatedStorageResource.getDataSourcePropertiesMap().get(each)));
            }
        }
        return new StorageResource(storageNodes, toBeCreatedStorageResource.getStorageUnitNodeMappers());
    }
    
    /**
     * Alter storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param dataSourceProps data source properties
     * @return created switching resource
     */
    public SwitchingResource alterStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> dataSourceProps) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().putAll(dataSourceProps);
        StorageResourceWithProperties toBeAlteredStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(dataSourceProps);
        return new SwitchingResource(resourceMetaData, getAlterNewStorageResource(toBeAlteredStorageResource),
                getStaleStorageResource(resourceMetaData, toBeAlteredStorageResource), resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap());
    }
    
    private StorageResource getAlterNewStorageResource(final StorageResourceWithProperties toBeAlteredStorageResource) {
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(toBeAlteredStorageResource.getStorageNodes().size(), 1F);
        for (String each : toBeAlteredStorageResource.getStorageNodes().keySet()) {
            storageNodes.put(each, DataSourcePoolCreator.create(toBeAlteredStorageResource.getDataSourcePropertiesMap().get(each)));
        }
        return new StorageResource(storageNodes, toBeAlteredStorageResource.getStorageUnitNodeMappers());
    }
    
    private StorageResource getStaleStorageResource(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeAlteredStorageResource) {
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(toBeAlteredStorageResource.getStorageNodes().size(), 1F);
        for (Entry<String, DataSource> entry : resourceMetaData.getStorageNodeMetaData().getDataSources().entrySet()) {
            if (toBeAlteredStorageResource.getStorageNodes().containsKey(entry.getKey())) {
                storageNodes.put(entry.getKey(), entry.getValue());
            }
        }
        return new StorageResource(storageNodes, toBeAlteredStorageResource.getStorageUnitNodeMappers());
    }
    
    /**
     * Unregister storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitName storage unit name
     * @return created switching resource
     */
    public SwitchingResource unregisterStorageUnit(final ResourceMetaData resourceMetaData, final String storageUnitName) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().remove(storageUnitName);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleStorageResource(resourceMetaData, storageUnitName), resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap());
    }
    
    private StorageResource getToBeRemovedStaleStorageResource(final ResourceMetaData resourceMetaData, final String storageUnitName) {
        StorageUnitNodeMapper storageUnitNodeMapper = resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers().remove(storageUnitName);
        Map<String, StorageUnitNodeMapper> reservedStorageUnitNodeMappers = resourceMetaData.getStorageUnitMetaData().getUnitNodeMappers();
        Map<String, DataSource> storageNodes = new LinkedHashMap<>(1, 1F);
        if (reservedStorageUnitNodeMappers.values().stream().noneMatch(each -> each.getNodeName().equals(storageUnitNodeMapper.getNodeName()))) {
            storageNodes.put(storageUnitNodeMapper.getNodeName(), resourceMetaData.getStorageNodeMetaData().getDataSources().get(storageUnitNodeMapper.getNodeName()));
        }
        return new StorageResource(storageNodes, Collections.singletonMap(storageUnitName, storageUnitNodeMapper));
    }
}

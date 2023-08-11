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
import org.apache.shardingsphere.infra.datasource.pool.props.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.storage.StorageNode;
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
    public SwitchingResource registerStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> dataSourceProps) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().putAll(dataSourceProps);
        StorageResourceWithProperties toBeCreatedStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(dataSourceProps);
        return new SwitchingResource(resourceMetaData, getRegisterNewStorageResource(resourceMetaData, toBeCreatedStorageResource),
                new StorageResource(Collections.emptyMap(), Collections.emptyMap()), resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap());
    }
    
    private StorageResource getRegisterNewStorageResource(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeCreatedStorageResource) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>(toBeCreatedStorageResource.getStorageNodeDataSources().size(), 1F);
        for (StorageNode each : toBeCreatedStorageResource.getStorageNodeDataSources().keySet()) {
            if (!resourceMetaData.getStorageNodeDataSources().containsKey(each)) {
                storageNodes.put(each, DataSourcePoolCreator.create(toBeCreatedStorageResource.getDataSourcePropertiesMap().get(each.getName())));
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
    public SwitchingResource alterStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> dataSourceProps) {
        resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap().putAll(dataSourceProps);
        StorageResourceWithProperties toBeAlteredStorageResource = DataSourcePoolCreator.createStorageResourceWithoutDataSource(dataSourceProps);
        return new SwitchingResource(resourceMetaData, getAlterNewStorageResource(toBeAlteredStorageResource),
                getStaleStorageResource(resourceMetaData, toBeAlteredStorageResource), resourceMetaData.getStorageUnitMetaData().getDataSourcePropsMap());
    }
    
    private StorageResource getAlterNewStorageResource(final StorageResourceWithProperties toBeAlteredStorageResource) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>(toBeAlteredStorageResource.getStorageNodeDataSources().size(), 1F);
        for (StorageNode each : toBeAlteredStorageResource.getStorageNodeDataSources().keySet()) {
            storageNodes.put(each, DataSourcePoolCreator.create(toBeAlteredStorageResource.getDataSourcePropertiesMap().get(each.getName())));
        }
        return new StorageResource(storageNodes, toBeAlteredStorageResource.getStorageUnitNodeMappers());
    }
    
    private StorageResource getStaleStorageResource(final ResourceMetaData resourceMetaData, final StorageResourceWithProperties toBeAlteredStorageResource) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>(toBeAlteredStorageResource.getStorageNodeDataSources().size(), 1F);
        for (Entry<StorageNode, DataSource> entry : resourceMetaData.getStorageNodeDataSources().entrySet()) {
            if (toBeAlteredStorageResource.getStorageNodeDataSources().containsKey(entry.getKey())) {
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
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>(1, 1F);
        if (reservedStorageUnitNodeMappers.values().stream().noneMatch(each -> each.getStorageNode().equals(storageUnitNodeMapper.getStorageNode()))) {
            storageNodes.put(storageUnitNodeMapper.getStorageNode(), resourceMetaData.getStorageNodeDataSources().get(storageUnitNodeMapper.getStorageNode()));
        }
        return new StorageResource(storageNodes, Collections.singletonMap(storageUnitName, storageUnitNodeMapper));
    }
}

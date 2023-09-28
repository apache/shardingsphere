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
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.StorageResource;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
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
 * TODO Rename ResourceSwitchManager when metadata structure adjustment completed. #25485
 * Resource switch manager.
 */
public final class NewResourceSwitchManager {
    
    /**
     * Register storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitDataSourcePoolProps storage unit grouped data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource registerStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Map<String, DataSourcePoolProperties> mergedPropsMap = new HashMap<>(resourceMetaData.getStorageUnitMetaData().getDataSourcePoolPropertiesMap());
        mergedPropsMap.putAll(storageUnitDataSourcePoolProps);
        Map<String, StorageNode> toBeCreatedStorageUintNodeMap = StorageUnitNodeMapUtils.fromDataSourcePoolProperties(storageUnitDataSourcePoolProps);
        return new SwitchingResource(resourceMetaData, getRegisterNewStorageResource(resourceMetaData, toBeCreatedStorageUintNodeMap, storageUnitDataSourcePoolProps),
                new StorageResource(Collections.emptyMap(), Collections.emptyMap()), mergedPropsMap);
    }
    
    private StorageResource getRegisterNewStorageResource(final ResourceMetaData resourceMetaData,
                                                          final Map<String, StorageNode> storageUintNodeMap, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Collection<StorageNodeName> storageNodeNames = storageUintNodeMap.values().stream().map(StorageNode::getName).collect(Collectors.toSet());
        Map<StorageNodeName, DataSourcePoolProperties> storageNodeDataSourcePoolProps = StorageUnitNodeMapUtils.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps);
        Map<StorageNodeName, DataSource> newStorageNodes = new LinkedHashMap<>(storageNodeNames.size(), 1F);
        for (StorageNodeName each : storageNodeNames) {
            if (!resourceMetaData.getDataSources().containsKey(each)) {
                newStorageNodes.put(each, DataSourcePoolCreator.create(storageNodeDataSourcePoolProps.get(each)));
            }
        }
        return new StorageResource(newStorageNodes, storageUintNodeMap);
    }
    
    /**
     * Alter storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param propsMap data source pool properties map
     * @return created switching resource
     */
    public SwitchingResource alterStorageUnit(final ResourceMetaData resourceMetaData, final Map<String, DataSourcePoolProperties> propsMap) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new HashMap<>(resourceMetaData.getStorageUnitMetaData().getDataSourcePoolPropertiesMap());
        mergedDataSourcePoolPropertiesMap.putAll(propsMap);
        Map<String, StorageNode> toBeAlteredStorageUintNodeMap = StorageUnitNodeMapUtils.fromDataSourcePoolProperties(mergedDataSourcePoolPropertiesMap);
        return new SwitchingResource(resourceMetaData, getAlterNewStorageResource(toBeAlteredStorageUintNodeMap, mergedDataSourcePoolPropertiesMap),
                getStaleStorageResource(resourceMetaData, toBeAlteredStorageUintNodeMap), mergedDataSourcePoolPropertiesMap);
    }
    
    private StorageResource getAlterNewStorageResource(final Map<String, StorageNode> storageUintNodeMap, final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Collection<StorageNodeName> toBeAlteredStorageNodeNames = storageUintNodeMap.values().stream().map(StorageNode::getName).collect(Collectors.toSet());
        Map<StorageNodeName, DataSourcePoolProperties> storageNodeDataSourcePoolProps = StorageUnitNodeMapUtils.getStorageNodeDataSourcePoolProperties(storageUnitDataSourcePoolProps);
        Map<StorageNodeName, DataSource> storageNodes = new LinkedHashMap<>(toBeAlteredStorageNodeNames.size(), 1F);
        for (StorageNodeName each : toBeAlteredStorageNodeNames) {
            storageNodes.put(each, DataSourcePoolCreator.create(storageNodeDataSourcePoolProps.get(each)));
        }
        return new StorageResource(storageNodes, storageUintNodeMap);
    }
    
    private StorageResource getStaleStorageResource(final ResourceMetaData resourceMetaData, final Map<String, StorageNode> storageUintNodeMap) {
        Collection<StorageNodeName> toBeAlteredStorageNodeNames = storageUintNodeMap.values().stream().map(StorageNode::getName).collect(Collectors.toSet());
        Map<StorageNodeName, DataSource> storageNodes = new LinkedHashMap<>(toBeAlteredStorageNodeNames.size(), 1F);
        for (Entry<StorageNodeName, DataSource> entry : resourceMetaData.getDataSources().entrySet()) {
            if (toBeAlteredStorageNodeNames.contains(entry.getKey())) {
                storageNodes.put(entry.getKey(), entry.getValue());
            }
        }
        return new StorageResource(storageNodes, storageUintNodeMap);
    }
    
    /**
     * Unregister storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitName storage unit name
     * @return created switching resource
     */
    public SwitchingResource unregisterStorageUnit(final ResourceMetaData resourceMetaData, final String storageUnitName) {
        Map<String, DataSourcePoolProperties> mergedDataSourcePoolPropertiesMap = new HashMap<>(resourceMetaData.getStorageUnitMetaData().getDataSourcePoolPropertiesMap());
        mergedDataSourcePoolPropertiesMap.keySet().removeIf(each -> each.equals(storageUnitName));
        resourceMetaData.getStorageUnitMetaData().getDataSourcePoolPropertiesMap().remove(storageUnitName);
        return new SwitchingResource(resourceMetaData, new StorageResource(Collections.emptyMap(), Collections.emptyMap()),
                getToBeRemovedStaleStorageResource(resourceMetaData, storageUnitName), mergedDataSourcePoolPropertiesMap);
    }
    
    private StorageResource getToBeRemovedStaleStorageResource(final ResourceMetaData resourceMetaData, final String storageUnitName) {
        StorageNode storageNode = resourceMetaData.getStorageUnitMetaData().getStorageUnitNodeMap().remove(storageUnitName);
        Map<String, StorageNode> reservedStorageUintNodeMap = resourceMetaData.getStorageUnitMetaData().getStorageUnitNodeMap();
        Map<StorageNodeName, DataSource> storageNodes = new LinkedHashMap<>(1, 1F);
        if (reservedStorageUintNodeMap.values().stream().noneMatch(each -> each.equals(storageNode))) {
            storageNodes.put(storageNode.getName(), resourceMetaData.getDataSources().get(storageNode.getName()));
        }
        return new StorageResource(storageNodes, Collections.singletonMap(storageUnitName, storageNode));
    }
}

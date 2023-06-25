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

package org.apache.shardingsphere.infra.metadata.database.resource;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.datasource.storage.StorageUnit;
import org.apache.shardingsphere.infra.datasource.storage.StorageUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ShardingSphere resource meta data.
 */
@Getter
public final class ShardingSphereResourceMetaData {
    
    private final Map<String, DataSource> dataSources;
    
    private final Map<String, DatabaseType> storageNodeTypes;
    
    private final Map<String, StorageUnit> storageUnits;
    
    private final Map<String, DatabaseType> storageUnitTypes;
    
    @Getter(AccessLevel.NONE)
    private final Map<String, DataSourceMetaData> dataSourceMetaDataMap;
    
    public ShardingSphereResourceMetaData(final String databaseName, final Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, dataSources);
        storageNodeTypes = createStorageNodeTypes(enabledDataSources);
        storageUnits = StorageUtils.getStorageUnits(dataSources);
        storageUnitTypes = createStorageUnitTypes();
        dataSourceMetaDataMap = createDataSourceMetaDataMap(enabledDataSources, storageNodeTypes);
    }
    
    public ShardingSphereResourceMetaData(final String databaseName, final StorageResource storageResource) {
        dataSources = storageResource.getStorageNodes();
        storageUnits = storageResource.getStorageUnits();
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, dataSources);
        storageNodeTypes = createStorageNodeTypes(enabledDataSources);
        storageUnitTypes = createStorageUnitTypes();
        dataSourceMetaDataMap = createDataSourceMetaDataMap(enabledDataSources, storageNodeTypes);
    }
    
    private Map<String, DatabaseType> createStorageNodeTypes(final Map<String, DataSource> enabledDataSources) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            DatabaseType storageType = enabledDataSources.containsKey(entry.getKey()) ? DatabaseTypeEngine.getStorageType(Collections.singletonList(entry.getValue()))
                    : DatabaseTypeEngine.getStorageType(Collections.emptyList());
            result.put(entry.getKey(), storageType);
        }
        return result;
    }
    
    private Map<String, DatabaseType> createStorageUnitTypes() {
        Map<String, DatabaseType> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            DatabaseType storageType = storageNodeTypes.containsKey(entry.getValue().getNodeName())
                    ? storageNodeTypes.get(entry.getValue().getNodeName())
                    : DatabaseTypeEngine.getStorageType(Collections.emptyList());
            result.put(entry.getKey(), storageType);
        }
        return result;
    }
    
    private Map<String, DataSourceMetaData> createDataSourceMetaDataMap(final Map<String, DataSource> dataSources, final Map<String, DatabaseType> storageTypes) {
        Map<String, DataSourceMetaData> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            Map<String, Object> standardProps = DataSourcePropertiesCreator.create(entry.getValue()).getConnectionPropertySynonyms().getStandardProperties();
            DatabaseType storageType = storageTypes.get(entry.getKey());
            result.put(entry.getKey(), storageType.getDataSourceMetaData(standardProps.get("url").toString(), standardProps.get("username").toString()));
        }
        return result;
    }
    
    /**
     * Get storage resource.
     *
     * @return storage resource
     */
    public StorageResource getStorageResource() {
        return new StorageResource(dataSources, storageNodeTypes, storageUnits);
    }
    
    /**
     * Get all instance data source names.
     *
     * @return instance data source names
     */
    public Collection<String> getAllInstanceDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, DataSourceMetaData> entry : dataSourceMetaDataMap.entrySet()) {
            if (!isExisted(entry.getKey(), result)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    private boolean isExisted(final String dataSourceName, final Collection<String> existedDataSourceNames) {
        return existedDataSourceNames.stream().anyMatch(each -> dataSourceMetaDataMap.get(dataSourceName).isInSameDatabaseInstance(dataSourceMetaDataMap.get(each)));
    }
    
    /**
     * Get data source meta data.
     *
     * @param storageNodeName storage node name
     * @return data source meta data
     */
    public DataSourceMetaData getDataSourceMetaData(final String storageNodeName) {
        return dataSourceMetaDataMap.get(storageNodeName);
    }
    
    /**
     * Get data source meta data by unit name.
     *
     * @param storageUnitName storage unit name
     * @return data source meta data
     */
    public DataSourceMetaData getDataSourceMetaDataByUnitName(final String storageUnitName) {
        return dataSourceMetaDataMap.get(storageUnits.get(storageUnitName).getNodeName());
    }
    
    /**
     * Get storage unit type.
     *
     * @param storageUnitName storage unit name
     * @return storage unit type
     */
    public DatabaseType getStorageUnitType(final String storageUnitName) {
        return storageUnitTypes.get(storageUnitName);
    }
    
    /**
     * Get not existed resource name.
     * 
     * @param resourceNames resource names to be judged
     * @return not existed resource names
     */
    public Collection<String> getNotExistedDataSources(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> !dataSources.containsKey(each)).collect(Collectors.toSet());
    }
    
    /**
     * Close data source.
     *
     * @param dataSource data source to be closed
     */
    public void close(final DataSource dataSource) {
        new DataSourcePoolDestroyer(dataSource).asyncDestroy();
    }
}

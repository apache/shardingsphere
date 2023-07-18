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

import lombok.Getter;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
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
    
    private final ShardingSphereStorageNodeMetaData storageNodeMetaData;
    
    private final ShardingSphereStorageUnitMetaData storageUnitMetaData;
    
    private final Map<String, DataSourceProperties> dataSourcePropsMap;
    
    public ShardingSphereResourceMetaData(final Map<String, DataSource> dataSources) {
        this(null, dataSources);
    }
    
    public ShardingSphereResourceMetaData(final String databaseName, final Map<String, DataSource> dataSources) {
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, dataSources);
        Map<String, DatabaseType> storageTypes = createStorageTypes(dataSources, enabledDataSources);
        this.dataSourcePropsMap = DataSourcePropertiesCreator.create(dataSources);
        storageNodeMetaData = new ShardingSphereStorageNodeMetaData(dataSources, storageTypes);
        storageUnitMetaData = new ShardingSphereStorageUnitMetaData(dataSources, storageTypes, StorageUtils.getStorageUnits(dataSources), enabledDataSources);
        
    }
    
    public ShardingSphereResourceMetaData(final String databaseName, final StorageResource storageResource, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, storageResource.getStorageNodes());
        Map<String, DatabaseType> storageTypes = createStorageTypes(storageResource.getStorageNodes(), enabledDataSources);
        storageNodeMetaData = new ShardingSphereStorageNodeMetaData(storageResource.getStorageNodes(), storageTypes);
        storageUnitMetaData = new ShardingSphereStorageUnitMetaData(storageResource.getStorageNodes(), storageTypes, storageResource.getStorageUnits(), enabledDataSources);
        this.dataSourcePropsMap = dataSourcePropsMap;
    }
    
    /**
     * Get data sources.
     *
     * @return data sources
     */
    public Map<String, DataSource> getDataSources() {
        return storageUnitMetaData.getDataSources();
    }
    
    /**
     * Get storage types.
     *
     * @return storage types
     */
    public Map<String, DatabaseType> getStorageTypes() {
        return storageUnitMetaData.getStorageTypes();
    }
    
    private Map<String, DatabaseType> createStorageTypes(final Map<String, DataSource> dataSources, final Map<String, DataSource> enabledDataSources) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            DatabaseType storageType = enabledDataSources.containsKey(entry.getKey()) ? DatabaseTypeEngine.getStorageType(Collections.singletonList(entry.getValue()))
                    : DatabaseTypeEngine.getStorageType(Collections.emptyList());
            result.put(entry.getKey(), storageType);
        }
        return result;
    }
    
    /**
     * Get all instance data source names.
     *
     * @return instance data source names
     */
    public Collection<String> getAllInstanceDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, DataSourceMetaData> entry : storageUnitMetaData.getDataSourceMetaDataMap().entrySet()) {
            if (!isExisted(entry.getKey(), result)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    private boolean isExisted(final String dataSourceName, final Collection<String> existedDataSourceNames) {
        return existedDataSourceNames.stream().anyMatch(each -> storageUnitMetaData.getDataSourceMetaDataMap().get(dataSourceName)
                .isInSameDatabaseInstance(storageUnitMetaData.getDataSourceMetaDataMap().get(each)));
    }
    
    /**
     * Get data source meta data.
     *
     * @param dataSourceName data source name
     * @return data source meta data
     */
    public DataSourceMetaData getDataSourceMetaData(final String dataSourceName) {
        return storageUnitMetaData.getDataSourceMetaDataMap().get(dataSourceName);
    }
    
    /**
     * Get storage type.
     *
     * @param dataSourceName data source name
     * @return storage type
     */
    public DatabaseType getStorageType(final String dataSourceName) {
        return storageUnitMetaData.getStorageTypes().get(dataSourceName);
    }
    
    /**
     * Get not existed resource name.
     * 
     * @param resourceNames resource names to be judged
     * @return not existed resource names
     */
    public Collection<String> getNotExistedDataSources(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> !storageUnitMetaData.getDataSources().containsKey(each)).collect(Collectors.toSet());
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

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
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeUtils;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Resource meta data.
 */
@Getter
public final class ResourceMetaData {
    
    private final Map<StorageNodeName, DataSource> dataSources;
    
    private final Map<String, StorageUnitMetaData> storageUnitMetaDataMap;
    
    public ResourceMetaData(final Map<String, DataSource> dataSources) {
        this.dataSources = StorageNodeUtils.getStorageNodeDataSources(dataSources);
        Map<String, StorageNode> storageNodes = StorageUnitNodeMapUtils.fromDataSources(dataSources);
        Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap = dataSources.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        storageUnitMetaDataMap = new LinkedHashMap<>();
        for (Entry<String, StorageNode> entry : storageNodes.entrySet()) {
            DataSource dataSource = dataSources.get(entry.getValue().getName().getName());
            if (!(dataSource instanceof CatalogSwitchableDataSource)) {
                dataSource = new CatalogSwitchableDataSource(dataSource, entry.getValue().getCatalog(), entry.getValue().getUrl());
            }
            storageUnitMetaDataMap.put(entry.getKey(), new StorageUnitMetaData(null, entry.getValue(), dataSourcePoolPropertiesMap.get(entry.getKey()), dataSource));
        }
    }
    
    public ResourceMetaData(final String databaseName, final Map<StorageNodeName, DataSource> dataSources,
                            final Map<String, StorageNode> storageNodes, final Map<String, DataSourcePoolProperties> propsMap) {
        this.dataSources = dataSources;
        storageUnitMetaDataMap = new LinkedHashMap<>();
        for (Entry<String, StorageNode> entry : storageNodes.entrySet()) {
            DataSource dataSource = dataSources.get(entry.getValue().getName());
            if (!(dataSource instanceof CatalogSwitchableDataSource)) {
                dataSource = new CatalogSwitchableDataSource(dataSource, entry.getValue().getCatalog(), entry.getValue().getUrl());
            }
            storageUnitMetaDataMap.put(entry.getKey(), new StorageUnitMetaData(databaseName, entry.getValue(), propsMap.get(entry.getKey()), dataSource));
        }
    }
    
    /**
     * Get all instance data source names.
     *
     * @return instance data source names
     */
    public Collection<String> getAllInstanceDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        for (String each : storageUnitMetaDataMap.keySet()) {
            if (!isExisted(each, result)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isExisted(final String dataSourceName, final Collection<String> existedDataSourceNames) {
        return existedDataSourceNames.stream().anyMatch(each -> storageUnitMetaDataMap.get(dataSourceName).getStorageUnit().getConnectionProperties()
                .isInSameDatabaseInstance(storageUnitMetaDataMap.get(each).getStorageUnit().getConnectionProperties()));
    }
    
    /**
     * Get connection properties.
     *
     * @param dataSourceName data source name
     * @return connection properties
     */
    public ConnectionProperties getConnectionProperties(final String dataSourceName) {
        return storageUnitMetaDataMap.get(dataSourceName).getStorageUnit().getConnectionProperties();
    }
    
    /**
     * Get storage type.
     *
     * @param dataSourceName data source name
     * @return storage type
     */
    public DatabaseType getStorageType(final String dataSourceName) {
        return storageUnitMetaDataMap.get(dataSourceName).getStorageUnit().getStorageType();
    }
    
    /**
     * Get not existed resource name.
     * 
     * @param resourceNames resource names to be judged
     * @return not existed resource names
     */
    public Collection<String> getNotExistedDataSources(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> !storageUnitMetaDataMap.containsKey(each)).collect(Collectors.toSet());
    }
}

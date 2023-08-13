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
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.storage.StorageUnitNodeMapper;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Storage unit meta data.
 */
@Getter
public final class StorageUnitMetaData {
    
    private final Map<String, DataSource> dataSources;
    
    private final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap;
    
    private final Map<String, DatabaseType> storageTypes;
    
    private final Map<String, StorageUnitNodeMapper> unitNodeMappers;
    
    private final Map<String, ConnectionProperties> connectionPropertiesMap;
    
    public StorageUnitMetaData(final String databaseName, final Map<StorageNode, DataSource> storageNodeDataSources,
                               final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap, final Map<String, StorageUnitNodeMapper> unitNodeMappers) {
        this.unitNodeMappers = unitNodeMappers;
        this.dataSources = getStorageUnitDataSources(storageNodeDataSources, unitNodeMappers);
        this.dataSourcePoolPropertiesMap = dataSourcePoolPropertiesMap;
        Map<StorageNode, DataSource> enabledStorageNodeDataSources = getEnabledStorageNodeDataSources(databaseName, storageNodeDataSources);
        storageTypes = createStorageTypes(enabledStorageNodeDataSources, unitNodeMappers);
        connectionPropertiesMap = createConnectionPropertiesMap(enabledStorageNodeDataSources, storageTypes, unitNodeMappers);
    }
    
    private Map<StorageNode, DataSource> getEnabledStorageNodeDataSources(final String databaseName, final Map<StorageNode, DataSource> storageNodeDataSources) {
        Map<String, DataSource> toBeCheckedDataSources = new LinkedHashMap<>(storageNodeDataSources.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : storageNodeDataSources.entrySet()) {
            toBeCheckedDataSources.put(entry.getKey().getName(), entry.getValue());
        }
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSources(databaseName, toBeCheckedDataSources);
        return storageNodeDataSources.entrySet().stream()
                .filter(entry -> enabledDataSources.containsKey(entry.getKey().getName())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, DataSource> getStorageUnitDataSources(final Map<StorageNode, DataSource> storageNodeDataSources, final Map<String, StorageUnitNodeMapper> unitNodeMappers) {
        Map<String, DataSource> result = new LinkedHashMap<>(unitNodeMappers.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : unitNodeMappers.entrySet()) {
            DataSource dataSource = storageNodeDataSources.get(entry.getValue().getStorageNode());
            result.put(entry.getKey(), new CatalogSwitchableDataSource(dataSource, entry.getValue().getCatalog(), entry.getValue().getUrl()));
        }
        return result;
    }
    
    private Map<String, DatabaseType> createStorageTypes(final Map<StorageNode, DataSource> enabledStorageNodeDataSources, final Map<String, StorageUnitNodeMapper> unitNodeMappers) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(unitNodeMappers.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : unitNodeMappers.entrySet()) {
            result.put(entry.getKey(), DatabaseTypeEngine.getStorageType(enabledStorageNodeDataSources.containsKey(entry.getValue().getStorageNode())
                    ? Collections.singleton(enabledStorageNodeDataSources.get(entry.getValue().getStorageNode()))
                    : Collections.emptyList()));
        }
        return result;
    }
    
    private Map<String, ConnectionProperties> createConnectionPropertiesMap(final Map<StorageNode, DataSource> enabledStorageNodeDataSources,
                                                                            final Map<String, DatabaseType> storageTypes, final Map<String, StorageUnitNodeMapper> unitNodeMappers) {
        Map<String, ConnectionProperties> result = new LinkedHashMap<>(unitNodeMappers.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : unitNodeMappers.entrySet()) {
            if (enabledStorageNodeDataSources.containsKey(entry.getValue().getStorageNode())) {
                Map<String, Object> standardProps = DataSourcePoolPropertiesCreator.create(enabledStorageNodeDataSources.get(entry.getValue().getStorageNode()))
                        .getConnectionPropertySynonyms().getStandardProperties();
                DatabaseType storageType = storageTypes.get(entry.getKey());
                ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, storageType);
                result.put(entry.getKey(), parser.parse(standardProps.get("url").toString(), standardProps.get("username").toString(), entry.getValue().getCatalog()));
            }
        }
        return result;
    }
}

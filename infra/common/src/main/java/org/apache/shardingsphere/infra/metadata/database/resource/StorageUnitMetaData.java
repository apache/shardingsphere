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
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.datasource.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.storage.StorageUnitNodeMapper;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage unit meta data.
 */
@Getter
public final class StorageUnitMetaData {
    
    private final Map<String, DataSource> dataSources;
    
    private final Map<String, DataSourceProperties> dataSourcePropsMap;
    
    private final Map<String, DatabaseType> storageTypes;
    
    private final Map<String, StorageUnitNodeMapper> unitNodeMappers;
    
    private final Map<String, ConnectionProperties> connectionPropsMap;
    
    public StorageUnitMetaData(final Map<String, DataSource> dataSources, final Map<String, DatabaseType> storageTypes, final Map<String, StorageUnitNodeMapper> unitNodeMappers,
                               final Map<String, DataSource> enabledDataSources) {
        this(dataSources, DataSourcePropertiesCreator.create(dataSources), storageTypes, unitNodeMappers, enabledDataSources);
    }
    
    public StorageUnitMetaData(final Map<String, DataSource> dataSources, final Map<String, DataSourceProperties> dataSourcePropsMap,
                               final Map<String, DatabaseType> storageTypes, final Map<String, StorageUnitNodeMapper> unitNodeMappers, final Map<String, DataSource> enabledDataSources) {
        this.unitNodeMappers = unitNodeMappers;
        this.dataSources = getStorageUnitDataSources(dataSources, unitNodeMappers);
        this.dataSourcePropsMap = dataSourcePropsMap;
        this.storageTypes = getStorageUnitTypes(storageTypes);
        connectionPropsMap = createConnectionPropertiesMap(enabledDataSources, storageTypes, unitNodeMappers);
    }
    
    private Map<String, DataSource> getStorageUnitDataSources(final Map<String, DataSource> storageNodes, final Map<String, StorageUnitNodeMapper> storageUnits) {
        Map<String, DataSource> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : storageUnits.entrySet()) {
            DataSource dataSource = storageNodes.get(entry.getValue().getNodeName());
            result.put(entry.getKey(), new CatalogSwitchableDataSource(dataSource, entry.getValue().getCatalog()));
        }
        return result;
    }
    
    private Map<String, DatabaseType> getStorageUnitTypes(final Map<String, DatabaseType> storageTypes) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(unitNodeMappers.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : unitNodeMappers.entrySet()) {
            DatabaseType storageType = storageTypes.containsKey(entry.getValue().getNodeName())
                    ? storageTypes.get(entry.getValue().getNodeName())
                    : DatabaseTypeEngine.getStorageType(Collections.emptyList());
            result.put(entry.getKey(), storageType);
        }
        return result;
    }
    
    private Map<String, ConnectionProperties> createConnectionPropertiesMap(final Map<String, DataSource> enabledDataSources,
                                                                            final Map<String, DatabaseType> storageTypes, final Map<String, StorageUnitNodeMapper> storageUnits) {
        Map<String, ConnectionProperties> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : storageUnits.entrySet()) {
            String nodeName = entry.getValue().getNodeName();
            if (enabledDataSources.containsKey(nodeName)) {
                Map<String, Object> standardProps = DataSourcePropertiesCreator.create(enabledDataSources.get(nodeName)).getConnectionPropertySynonyms().getStandardProperties();
                DatabaseType storageType = storageTypes.get(nodeName);
                ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, storageType);
                result.put(entry.getKey(), parser.parse(standardProps.get("url").toString(), standardProps.get("username").toString(), entry.getValue().getCatalog()));
            }
        }
        return result;
    }
}

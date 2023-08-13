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

package org.apache.shardingsphere.infra.metadata.database.resource.storage;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage unit meta data.
 */
@Getter
public final class StorageUnitMetaData {
    
    private final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap;
    
    // TODO zhangliang: should refactor
    private final Map<String, StorageUnitNodeMapper> unitNodeMappers;
    
    private final Map<String, StorageUnit> storageUnits;
    
    // TODO zhangliang: should refactor
    private final Map<String, DataSource> dataSources;
    
    // TODO zhangliang: should refactor
    private final Map<String, DatabaseType> storageTypes;
    
    public StorageUnitMetaData(final String databaseName, final Map<StorageNode, DataSource> storageNodeDataSources,
                               final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap, final Map<String, StorageUnitNodeMapper> unitNodeMappers) {
        this.dataSourcePoolPropertiesMap = dataSourcePoolPropertiesMap;
        this.unitNodeMappers = unitNodeMappers;
        storageUnits = new LinkedHashMap<>(unitNodeMappers.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : unitNodeMappers.entrySet()) {
            storageUnits.put(entry.getKey(), new StorageUnit(databaseName, storageNodeDataSources, dataSourcePoolPropertiesMap.get(entry.getKey()), entry.getValue()));
        }
        dataSources = createDataSources();
        storageTypes = createStorageTypes();
    }
    
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSource());
        }
        return result;
    }
    
    private Map<String, DatabaseType> createStorageTypes() {
        Map<String, DatabaseType> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getStorageType());
        }
        return result;
    }
}

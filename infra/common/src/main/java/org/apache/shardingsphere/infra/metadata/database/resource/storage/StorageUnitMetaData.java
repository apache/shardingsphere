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
    
    private final Map<String, StorageUnit> storageUnits;
    
    public StorageUnitMetaData(final String databaseName, final Map<StorageNode, DataSource> storageNodeDataSources,
                               final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap, final Map<String, StorageUnitNodeMapper> unitNodeMappers) {
        this.dataSourcePoolPropertiesMap = dataSourcePoolPropertiesMap;
        storageUnits = new LinkedHashMap<>(unitNodeMappers.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : dataSourcePoolPropertiesMap.entrySet()) {
            storageUnits.put(entry.getKey(), new StorageUnit(databaseName, storageNodeDataSources, entry.getValue(), unitNodeMappers.get(entry.getKey())));
        }
    }
    
    /**
     * Get unit node mappers.
     *
     * @return unit node mappers
     */
    public Map<String, StorageUnitNodeMapper> getUnitNodeMappers() {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getUnitNodeMapper());
        }
        return result;
    }
    
    /**
     * Get data sources.
     *
     * @return data sources
     */
    public Map<String, DataSource> getDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSource());
        }
        return result;
    }
    
    /**
     * Get storage types.
     *
     * @return storage types
     */
    public Map<String, DatabaseType> getStorageTypes() {
        Map<String, DatabaseType> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getStorageType());
        }
        return result;
    }
}

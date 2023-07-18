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
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.ShardingSphereStorageDataSourceWrapper;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.storage.StorageUnit;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ShardingSphere storage unit meta data.
 */
@Getter
public final class ShardingSphereStorageUnitMetaData {
    
    private final Map<String, DataSource> dataSources;
    
    private final Map<String, DatabaseType> storageTypes;
    
    private final Map<String, StorageUnit> storageUnits;
    
    private final Map<String, DataSourceMetaData> dataSourceMetaDataMap;
    
    public ShardingSphereStorageUnitMetaData(final Map<String, DataSource> dataSources, final Map<String, DatabaseType> storageTypes, final Map<String, StorageUnit> storageUnits,
                                             final Map<String, DataSource> enabledDataSources) {
        this.storageUnits = storageUnits;
        this.dataSources = getStorageUnitDataSources(dataSources, storageUnits);
        this.storageTypes = getStorageUnitTypes(storageTypes);
        this.dataSourceMetaDataMap = createDataSourceMetaDataMap(enabledDataSources, storageTypes, storageUnits);
    }
    
    private Map<String, DataSource> getStorageUnitDataSources(final Map<String, DataSource> storageNodes, final Map<String, StorageUnit> storageUnits) {
        Map<String, DataSource> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            DataSource dataSource = storageNodes.get(entry.getValue().getNodeName());
            result.put(entry.getKey(), new ShardingSphereStorageDataSourceWrapper(dataSource, entry.getValue().getCatalog(), entry.getValue().getUrl()));
        }
        return result;
    }
    
    private Map<String, DatabaseType> getStorageUnitTypes(final Map<String, DatabaseType> storageTypes) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            DatabaseType storageType = storageTypes.containsKey(entry.getValue().getNodeName())
                    ? storageTypes.get(entry.getValue().getNodeName())
                    : DatabaseTypeEngine.getStorageType(Collections.emptyList());
            result.put(entry.getKey(), storageType);
        }
        return result;
    }
    
    private Map<String, DataSourceMetaData> createDataSourceMetaDataMap(final Map<String, DataSource> enabledDataSources, final Map<String, DatabaseType> storageTypes,
                                                                        final Map<String, StorageUnit> storageUnits) {
        Map<String, DataSourceMetaData> result = new LinkedHashMap<>(storageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
            String nodeName = entry.getValue().getNodeName();
            if (enabledDataSources.containsKey(nodeName)) {
                Map<String, Object> standardProps = DataSourcePropertiesCreator.create(enabledDataSources.get(nodeName)).getConnectionPropertySynonyms().getStandardProperties();
                DatabaseType storageType = storageTypes.get(nodeName);
                result.put(entry.getKey(), storageType.getDataSourceMetaData(standardProps.get("url").toString(), standardProps.get("username").toString(), entry.getValue().getCatalog()));
            }
        }
        return result;
    }
}

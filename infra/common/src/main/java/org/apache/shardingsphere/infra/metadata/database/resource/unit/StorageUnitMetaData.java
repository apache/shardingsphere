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

package org.apache.shardingsphere.infra.metadata.database.resource.unit;

import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Storage unit meta data.
 */
@Getter
public final class StorageUnitMetaData {
    
    // TODO zhangliang: should refactor
    private final Map<String, StorageNode> storageNodes;
    
    private final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap;
    
    private final Map<String, StorageUnit> storageUnits;
    
    // TODO zhangliang: should refactor
    private final Map<String, DataSource> dataSources;
    
    public StorageUnitMetaData(final String databaseName, final Map<String, StorageNode> storageNodes, final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap,
                               final Map<StorageNodeName, DataSource> dataSources) {
        this.storageNodes = storageNodes;
        this.dataSourcePoolPropertiesMap = dataSourcePoolPropertiesMap;
        storageUnits = storageNodes.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> new StorageUnit(databaseName, dataSources.get(entry.getValue().getName()), dataSourcePoolPropertiesMap.get(entry.getKey()), entry.getValue()),
                        (oldValue, currentValue) -> currentValue, () -> new LinkedHashMap<>(this.storageNodes.size(), 1F)));
        this.dataSources = storageUnits.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> currentValue, () -> new LinkedHashMap<>(storageUnits.size(), 1F)));
    }
}

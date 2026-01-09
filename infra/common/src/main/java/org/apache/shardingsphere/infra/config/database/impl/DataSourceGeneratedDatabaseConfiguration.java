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

package org.apache.shardingsphere.infra.config.database.impl;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;
import org.apache.shardingsphere.infra.util.close.DataSourcesCloser;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source generated database configuration.
 */
@Getter
public final class DataSourceGeneratedDatabaseConfiguration implements DatabaseConfiguration {
    
    private final Collection<RuleConfiguration> ruleConfigurations;
    
    private final Map<String, StorageUnit> storageUnits;
    
    private final Map<StorageNode, DataSource> dataSources;
    
    public DataSourceGeneratedDatabaseConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigs, final Collection<RuleConfiguration> ruleConfigs,
                                                    final boolean isInstanceConnectionEnabled) {
        ruleConfigurations = ruleConfigs;
        Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap = dataSourceConfigs.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, StorageNode> storageUnitNodeMap = StorageUnitNodeMapCreator.create(dataSourcePoolPropertiesMap, isInstanceConnectionEnabled);
        Map<StorageNode, DataSource> storageNodeDataSources = getStorageNodeDataSourceMap(dataSourcePoolPropertiesMap, storageUnitNodeMap);
        storageUnits = new LinkedHashMap<>(dataSourceConfigs.size(), 1F);
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigs.entrySet()) {
            String storageUnitName = entry.getKey();
            StorageNode storageNode = storageUnitNodeMap.get(storageUnitName);
            DataSource dataSource = storageNodeDataSources.get(storageNode);
            StorageUnit storageUnit = new StorageUnit(storageNode, dataSourcePoolPropertiesMap.get(storageUnitName), dataSource);
            storageUnits.put(storageUnitName, storageUnit);
        }
        dataSources = storageNodeDataSources;
    }
    
    private Map<StorageNode, DataSource> getStorageNodeDataSourceMap(final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap, final Map<String, StorageNode> storageUnitNodeMap) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(storageUnitNodeMap.size(), 1F);
        try {
            for (Entry<String, StorageNode> entry : storageUnitNodeMap.entrySet()) {
                result.computeIfAbsent(entry.getValue(), key -> DataSourcePoolCreator.create(entry.getKey(), dataSourcePoolPropertiesMap.get(entry.getKey()), true, result.values()));
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            DataSourcesCloser.close(result.values());
            throw ex;
        }
        return result;
    }
}

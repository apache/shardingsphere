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
import org.apache.shardingsphere.infra.metadata.database.resource.StorageResource;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapperUtils;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapper;

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
    
    private final StorageResource storageResource;
    
    private final Collection<RuleConfiguration> ruleConfigurations;
    
    private final Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap;
    
    public DataSourceGeneratedDatabaseConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigs, final Collection<RuleConfiguration> ruleConfigs) {
        ruleConfigurations = ruleConfigs;
        dataSourcePoolPropertiesMap = dataSourceConfigs.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, StorageUnitNodeMapper> mappers = StorageUnitNodeMapperUtils.fromDataSourcePoolProperties(dataSourcePoolPropertiesMap);
        storageResource = new StorageResource(getStorageNodeDataSourceMap(mappers), mappers);
    }
    
    private Map<StorageNodeName, DataSource> getStorageNodeDataSourceMap(final Map<String, StorageUnitNodeMapper> mappers) {
        Map<StorageNodeName, DataSource> result = new LinkedHashMap<>(mappers.size(), 1F);
        for (Entry<String, StorageUnitNodeMapper> entry : mappers.entrySet()) {
            result.computeIfAbsent(entry.getValue().getStorageNode().getName(),
                    key -> DataSourcePoolCreator.create(entry.getKey(), dataSourcePoolPropertiesMap.get(entry.getKey()), true, result.values()));
        }
        return result;
    }
    
    @Override
    public Map<String, DataSource> getDataSources() {
        return storageResource.getWrappedDataSources();
    }
}

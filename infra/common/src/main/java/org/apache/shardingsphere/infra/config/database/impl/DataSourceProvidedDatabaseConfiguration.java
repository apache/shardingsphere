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
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.datasource.storage.StorageUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source provided database configuration.
 */
@Getter
public final class DataSourceProvidedDatabaseConfiguration implements DatabaseConfiguration {
    
    private final StorageResource storageResource;
    
    private final Collection<RuleConfiguration> ruleConfigurations;
    
    private final Map<String, DataSourceProperties> dataSourcePropsMap;
    
    public DataSourceProvidedDatabaseConfiguration(final Map<String, DataSource> dataSources, final Collection<RuleConfiguration> ruleConfigurations) {
        this.ruleConfigurations = ruleConfigurations;
        this.storageResource = new StorageResource(dataSources, StorageUtils.getStorageUnits(dataSources));
        dataSourcePropsMap = createDataSourcePropertiesMap(dataSources);
    }
    
    public DataSourceProvidedDatabaseConfiguration(final StorageResource storageResource, final Collection<RuleConfiguration> ruleConfigurations,
                                                   final Map<String, DataSourceProperties> dataSourcePropsMap) {
        this.ruleConfigurations = ruleConfigurations;
        this.storageResource = storageResource;
        this.dataSourcePropsMap = dataSourcePropsMap;
    }
    
    private Map<String, DataSourceProperties> createDataSourcePropertiesMap(final Map<String, DataSource> dataSources) {
        return dataSources.entrySet().stream().collect(Collectors
                .toMap(Entry::getKey, entry -> DataSourcePropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public Map<String, DataSource> getDataSources() {
        return storageResource.getWrappedDataSources();
    }
}

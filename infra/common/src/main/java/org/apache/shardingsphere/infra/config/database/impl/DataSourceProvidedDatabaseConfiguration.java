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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.datasource.storage.StorageUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source provided database configuration.
 */
@Getter
public final class DataSourceProvidedDatabaseConfiguration implements DatabaseConfiguration {
    
    private final StorageResource storageResource;
    
    private final Collection<RuleConfiguration> ruleConfigurations;
    
    public DataSourceProvidedDatabaseConfiguration(final Map<String, DataSource> dataSources, final Collection<RuleConfiguration> ruleConfigurations) {
        this.ruleConfigurations = ruleConfigurations;
        this.storageResource = new StorageResource(dataSources, createStorageTypes(dataSources), StorageUtils.getStorageUnits(dataSources));
    }
    
    public DataSourceProvidedDatabaseConfiguration(final StorageResource storageResource, final Collection<RuleConfiguration> ruleConfigurations) {
        this.ruleConfigurations = ruleConfigurations;
        this.storageResource = storageResource;
    }
    
    private Map<String, DatabaseType> createStorageTypes(final Map<String, DataSource> dataSources) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            DatabaseType storageType = DatabaseTypeEngine.getStorageType(Collections.singletonList(entry.getValue()));
            result.put(entry.getKey(), storageType);
        }
        return result;
    }
    
    @Override
    public Map<String, DataSource> getDataSources() {
        return storageResource.getStorageNodes();
    }
}

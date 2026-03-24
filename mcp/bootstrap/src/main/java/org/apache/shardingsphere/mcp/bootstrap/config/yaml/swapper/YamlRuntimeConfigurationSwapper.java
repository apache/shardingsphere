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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML runtime configuration swapper.
 */
public final class YamlRuntimeConfigurationSwapper implements YamlConfigurationSwapper<YamlRuntimeConfiguration, Map<String, RuntimeDatabaseConfiguration>> {
    
    private final YamlRuntimeDatabaseConfigurationSwapper databaseConfigSwapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    @Override
    public YamlRuntimeConfiguration swapToYamlConfiguration(final Map<String, RuntimeDatabaseConfiguration> data) {
        YamlRuntimeConfiguration result = new YamlRuntimeConfiguration();
        for (Entry<String, RuntimeDatabaseConfiguration> entry : data.entrySet()) {
            result.getDatabases().put(entry.getKey(), databaseConfigSwapper.swapToYamlConfiguration(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Map<String, RuntimeDatabaseConfiguration> swapToObject(final YamlRuntimeConfiguration yamlConfig) {
        YamlRuntimeConfiguration actualYamlConfig = null == yamlConfig ? new YamlRuntimeConfiguration() : yamlConfig;
        YamlRuntimeDatabaseConfiguration actualDatabaseDefaults = null == actualYamlConfig.getDatabaseDefaults() ? new YamlRuntimeDatabaseConfiguration() : actualYamlConfig.getDatabaseDefaults();
        Map<String, YamlRuntimeDatabaseConfiguration> actualDatabases = null == actualYamlConfig.getDatabases() ? new LinkedHashMap<>() : actualYamlConfig.getDatabases();
        return swapDatabases(actualDatabases, actualDatabaseDefaults);
    }
    
    private Map<String, RuntimeDatabaseConfiguration> swapDatabases(final Map<String, YamlRuntimeDatabaseConfiguration> yamlDatabaseConfigs,
                                                                    final YamlRuntimeDatabaseConfiguration databaseDefaults) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlDatabaseConfigs.size(), 1F);
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : yamlDatabaseConfigs.entrySet()) {
            String databaseName = normalizeText(entry.getKey());
            ShardingSpherePreconditions.checkState(!databaseName.isEmpty(),
                    () -> new IllegalArgumentException("Runtime logical database name cannot be blank."));
            ShardingSpherePreconditions.checkState(!result.containsKey(databaseName),
                    () -> new IllegalArgumentException(String.format("Runtime logical database `%s` is duplicated.", databaseName)));
            result.put(databaseName, databaseConfigSwapper.swapToObject(databaseName, entry.getValue(), databaseDefaults));
        }
        return result;
    }
    
    private String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
}

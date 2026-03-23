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
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * YAML runtime configuration swapper.
 */
public final class YamlRuntimeConfigurationSwapper implements YamlConfigurationSwapper<YamlRuntimeConfiguration, RuntimeConfiguration> {
    
    private final YamlRuntimeDatabaseConfigurationSwapper databaseConfigSwapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    @Override
    public YamlRuntimeConfiguration swapToYamlConfiguration(final RuntimeConfiguration data) {
        YamlRuntimeConfiguration result = new YamlRuntimeConfiguration();
        data.getProps().forEach((key, value) -> result.getProps().put(String.valueOf(key), null == value ? "" : String.valueOf(value)));
        for (Entry<String, RuntimeDatabaseConfiguration> entry : data.getDatabases().entrySet()) {
            result.getDatabases().put(entry.getKey(), databaseConfigSwapper.swapToYamlConfiguration(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public RuntimeConfiguration swapToObject(final YamlRuntimeConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptyMap());
    }
    
    RuntimeConfiguration swapToObject(final YamlRuntimeConfiguration yamlConfig, final Map<String, Collection<String>> configuredDatabaseFields) {
        YamlRuntimeConfiguration actualYamlConfig = null == yamlConfig ? new YamlRuntimeConfiguration() : yamlConfig;
        Map<String, String> actualProps = null == actualYamlConfig.getProps() ? new LinkedHashMap<>() : actualYamlConfig.getProps();
        Map<String, String> actualDefaults = null == actualYamlConfig.getDefaults() ? new LinkedHashMap<>() : actualYamlConfig.getDefaults();
        Map<String, YamlRuntimeDatabaseConfiguration> actualDatabases = null == actualYamlConfig.getDatabases() ? new LinkedHashMap<>() : actualYamlConfig.getDatabases();
        ShardingSpherePreconditions.checkState(actualProps.isEmpty() || actualDatabases.isEmpty(),
                () -> new IllegalArgumentException("`runtime.props` and `runtime.databases` cannot be configured together."));
        return new RuntimeConfiguration(swapProps(actualProps), swapDatabases(actualDatabases, actualDefaults, configuredDatabaseFields));
    }
    
    private Properties swapProps(final Map<String, String> yamlProps) {
        Properties result = new Properties();
        for (Entry<String, String> entry : yamlProps.entrySet()) {
            result.setProperty(entry.getKey(), null == entry.getValue() ? "" : entry.getValue());
        }
        return result;
    }
    
    private Map<String, RuntimeDatabaseConfiguration> swapDatabases(final Map<String, YamlRuntimeDatabaseConfiguration> yamlDatabaseConfigs, final Map<String, String> runtimeDefaults,
                                                                    final Map<String, Collection<String>> configuredDatabaseFields) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlDatabaseConfigs.size(), 1F);
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : yamlDatabaseConfigs.entrySet()) {
            String databaseName = normalizeText(entry.getKey());
            ShardingSpherePreconditions.checkState(!databaseName.isEmpty(),
                    () -> new IllegalArgumentException("Runtime logical database name cannot be blank."));
            ShardingSpherePreconditions.checkState(!result.containsKey(databaseName),
                    () -> new IllegalArgumentException(String.format("Runtime logical database `%s` is duplicated.", databaseName)));
            result.put(databaseName, databaseConfigSwapper.swapToObject(databaseName, entry.getValue(), runtimeDefaults,
                    configuredDatabaseFields.getOrDefault(databaseName, Collections.emptySet())));
        }
        return result;
    }
    
    private String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
}

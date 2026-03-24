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
        if (data.getDatabases().isEmpty() && !data.getProps().isEmpty()) {
            Entry<String, RuntimeDatabaseConfiguration> entry = createRuntimeDatabaseEntryFromLegacyProps(data.getProps());
            result.getDatabases().put(entry.getKey(), databaseConfigSwapper.swapToYamlConfiguration(entry.getValue()));
            return result;
        }
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
        YamlRuntimeDatabaseConfiguration actualDatabaseDefaults = null == actualYamlConfig.getDatabaseDefaults() ? new YamlRuntimeDatabaseConfiguration() : actualYamlConfig.getDatabaseDefaults();
        Map<String, YamlRuntimeDatabaseConfiguration> actualDatabases = null == actualYamlConfig.getDatabases() ? new LinkedHashMap<>() : actualYamlConfig.getDatabases();
        ShardingSpherePreconditions.checkState(actualProps.isEmpty() || actualDatabases.isEmpty(),
                () -> new IllegalArgumentException("`runtime.props` and `runtime.databases` cannot be configured together."));
        ShardingSpherePreconditions.checkState(actualDefaults.isEmpty() || isEmpty(actualDatabaseDefaults),
                () -> new IllegalArgumentException("`runtime.defaults` and `runtime.databaseDefaults` cannot be configured together."));
        validateCanonicalDatabaseDefaults(actualDatabaseDefaults);
        return actualProps.isEmpty()
                ? new RuntimeConfiguration(new Properties(), swapDatabases(actualDatabases, actualDatabaseDefaults, actualDefaults, configuredDatabaseFields))
                : new RuntimeConfiguration(new Properties(), createCanonicalDatabasesFromLegacyProps(actualProps, actualDatabaseDefaults, actualDefaults));
    }
    
    private Entry<String, RuntimeDatabaseConfiguration> createRuntimeDatabaseEntryFromLegacyProps(final Properties props) {
        String databaseName = normalizeText(props.getProperty("databaseName"));
        ShardingSpherePreconditions.checkState(!databaseName.isEmpty(),
                () -> new IllegalArgumentException("Runtime property `databaseName` is required."));
        YamlRuntimeDatabaseConfiguration databaseConfig = new YamlRuntimeDatabaseConfiguration();
        databaseConfig.setDatabaseType(normalizeText(props.getProperty("databaseType")));
        databaseConfig.setJdbcUrl(normalizeText(props.getProperty("jdbcUrl")));
        databaseConfig.setUsername(normalizeText(props.getProperty("username")));
        databaseConfig.setPassword(normalizeText(props.getProperty("password")));
        databaseConfig.setDriverClassName(normalizeText(props.getProperty("driverClassName")));
        if (props.containsKey("supportsCrossSchemaSql")) {
            databaseConfig.setSupportsCrossSchemaSql(Boolean.parseBoolean(normalizeText(props.getProperty("supportsCrossSchemaSql"))));
        }
        if (props.containsKey("supportsExplainAnalyze")) {
            databaseConfig.setSupportsExplainAnalyze(Boolean.parseBoolean(normalizeText(props.getProperty("supportsExplainAnalyze"))));
        }
        return Map.entry(databaseName, databaseConfigSwapper.swapToObject(databaseName, databaseConfig, new YamlRuntimeDatabaseConfiguration(), new LinkedHashMap<>()));
    }
    
    private void validateCanonicalDatabaseDefaults(final YamlRuntimeDatabaseConfiguration databaseDefaults) {
        ShardingSpherePreconditions.checkState(null == databaseDefaults.getSupportsCrossSchemaSql() && null == databaseDefaults.getSupportsExplainAnalyze(),
                () -> new IllegalArgumentException("`runtime.databaseDefaults` cannot configure legacy capability booleans. Use legacy `runtime.defaults` during migration."));
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createCanonicalDatabasesFromLegacyProps(final Map<String, String> actualProps,
                                                                                              final YamlRuntimeDatabaseConfiguration actualDatabaseDefaults,
                                                                                              final Map<String, String> actualDefaults) {
        Properties props = new Properties();
        for (Entry<String, String> entry : actualProps.entrySet()) {
            props.setProperty(entry.getKey(), null == entry.getValue() ? "" : entry.getValue());
        }
        Entry<String, RuntimeDatabaseConfiguration> databaseEntry = createRuntimeDatabaseEntryFromLegacyProps(props);
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(1, 1F);
        result.put(databaseEntry.getKey(), databaseConfigSwapper.swapToObject(databaseEntry.getKey(), createLegacyYamlDatabaseConfig(actualProps),
                actualDatabaseDefaults, actualDefaults));
        return result;
    }
    
    private YamlRuntimeDatabaseConfiguration createLegacyYamlDatabaseConfig(final Map<String, String> actualProps) {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setDatabaseType(normalizeText(actualProps.get("databaseType")));
        result.setJdbcUrl(normalizeText(actualProps.get("jdbcUrl")));
        result.setUsername(normalizeText(actualProps.get("username")));
        result.setPassword(normalizeText(actualProps.get("password")));
        result.setDriverClassName(normalizeText(actualProps.get("driverClassName")));
        if (actualProps.containsKey("supportsCrossSchemaSql")) {
            result.setSupportsCrossSchemaSql(Boolean.parseBoolean(normalizeText(actualProps.get("supportsCrossSchemaSql"))));
        }
        if (actualProps.containsKey("supportsExplainAnalyze")) {
            result.setSupportsExplainAnalyze(Boolean.parseBoolean(normalizeText(actualProps.get("supportsExplainAnalyze"))));
        }
        return result;
    }
    
    private Map<String, RuntimeDatabaseConfiguration> swapDatabases(final Map<String, YamlRuntimeDatabaseConfiguration> yamlDatabaseConfigs,
                                                                    final YamlRuntimeDatabaseConfiguration databaseDefaults, final Map<String, String> legacyRuntimeDefaults,
                                                                    final Map<String, Collection<String>> configuredDatabaseFields) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlDatabaseConfigs.size(), 1F);
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : yamlDatabaseConfigs.entrySet()) {
            String databaseName = normalizeText(entry.getKey());
            ShardingSpherePreconditions.checkState(!databaseName.isEmpty(),
                    () -> new IllegalArgumentException("Runtime logical database name cannot be blank."));
            ShardingSpherePreconditions.checkState(!result.containsKey(databaseName),
                    () -> new IllegalArgumentException(String.format("Runtime logical database `%s` is duplicated.", databaseName)));
            result.put(databaseName, databaseConfigSwapper.swapToObject(databaseName, entry.getValue(), databaseDefaults, legacyRuntimeDefaults));
        }
        return result;
    }
    
    private boolean isEmpty(final YamlRuntimeDatabaseConfiguration databaseDefaults) {
        if (null == databaseDefaults) {
            return true;
        }
        return normalizeText(databaseDefaults.getDatabaseType()).isEmpty()
                && normalizeText(databaseDefaults.getJdbcUrl()).isEmpty()
                && normalizeText(databaseDefaults.getUsername()).isEmpty()
                && normalizeText(databaseDefaults.getPassword()).isEmpty()
                && normalizeText(databaseDefaults.getDriverClassName()).isEmpty()
                && null == databaseDefaults.getSupportsCrossSchemaSql()
                && null == databaseDefaults.getSupportsExplainAnalyze();
    }
    
    private String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
}

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
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.yaml.MCPYamlConfigurationValidator;

import java.util.Map;

/**
 * YAML runtime database configuration swapper.
 */
public final class YamlRuntimeDatabaseConfigurationSwapper implements YamlConfigurationSwapper<YamlRuntimeDatabaseConfiguration, RuntimeDatabaseConfiguration> {
    
    @Override
    public YamlRuntimeDatabaseConfiguration swapToYamlConfiguration(final RuntimeDatabaseConfiguration data) {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setDatabaseType(data.getDatabaseType());
        result.setJdbcUrl(data.getJdbcUrl());
        result.setUsername(data.getUsername());
        result.setPassword(data.getPassword());
        result.setDriverClassName(data.getDriverClassName());
        return result;
    }
    
    @Override
    public RuntimeDatabaseConfiguration swapToObject(final YamlRuntimeDatabaseConfiguration yamlConfig) {
        return swapToObject(yamlConfig, System.getenv());
    }
    
    RuntimeDatabaseConfiguration swapToObject(final YamlRuntimeDatabaseConfiguration yamlConfig, final Map<String, String> environment) {
        MCPYamlConfigurationValidator.validate(yamlConfig, "MCP runtime database configuration");
        return new RuntimeDatabaseConfiguration(resolveRequiredText(yamlConfig.getDatabaseType(), "databaseType", environment), resolveRequiredText(yamlConfig.getJdbcUrl(), "jdbcUrl", environment),
                resolveExplicitText(yamlConfig.getUsername(), "username", environment), resolveExplicitText(yamlConfig.getPassword(), "password", environment),
                resolveExplicitText(yamlConfig.getDriverClassName(), "driverClassName", environment));
    }
    
    private String resolveRequiredText(final String value, final String fieldName, final Map<String, String> environment) {
        String result = YamlEnvironmentPlaceholderUtils.resolve(value, String.format("runtime.databases[].%s", fieldName), environment);
        ShardingSpherePreconditions.checkState(!result.isBlank(), () -> new IllegalArgumentException(formatRequiredMessage(fieldName)));
        return result;
    }
    
    private String resolveExplicitText(final String value, final String fieldName, final Map<String, String> environment) {
        return YamlEnvironmentPlaceholderUtils.resolve(value, String.format("runtime.databases[].%s", fieldName), environment);
    }
    
    private String formatRequiredMessage(final String fieldName) {
        return String.format("Runtime database property `%s` is required.", fieldName);
    }
}

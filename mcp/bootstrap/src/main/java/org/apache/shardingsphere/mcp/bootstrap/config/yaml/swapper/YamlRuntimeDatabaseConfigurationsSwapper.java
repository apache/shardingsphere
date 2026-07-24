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
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML runtime databases configuration swapper.
 */
public final class YamlRuntimeDatabaseConfigurationsSwapper {
    
    private final YamlRuntimeDatabaseConfigurationSwapper runtimeDatabaseConfigSwapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    /**
     * Swap YAML runtime configuration to runtime configuration object.
     *
     * @param yamlRuntimeConfiguration YAML runtime configuration
     * @return runtime configuration object
     */
    public Map<String, RuntimeDatabaseConfiguration> swapToObject(final Map<String, YamlRuntimeDatabaseConfiguration> yamlRuntimeConfiguration) {
        if (null == yamlRuntimeConfiguration) {
            return Collections.emptyMap();
        }
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(yamlRuntimeConfiguration.size(), 1F);
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : yamlRuntimeConfiguration.entrySet()) {
            ShardingSpherePreconditions.checkNotNull(entry.getValue(), () -> new IllegalArgumentException("MCP runtime database configuration cannot be null."));
            result.put(entry.getKey(), runtimeDatabaseConfigSwapper.swapToObject(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Swap runtime configuration object to YAML runtime configuration.
     *
     * @param runtimeConfiguration runtime configuration
     * @return YAML runtime configuration
     */
    public Map<String, YamlRuntimeDatabaseConfiguration> swapToYamlConfiguration(final Map<String, RuntimeDatabaseConfiguration> runtimeConfiguration) {
        Map<String, YamlRuntimeDatabaseConfiguration> result = new LinkedHashMap<>(runtimeConfiguration.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeConfiguration.entrySet()) {
            ShardingSpherePreconditions.checkNotNull(entry.getValue(), () -> new IllegalArgumentException("Runtime database configuration cannot be null."));
            result.put(entry.getKey(), runtimeDatabaseConfigSwapper.swapToYamlConfiguration(entry.getValue()));
        }
        return result;
    }
}

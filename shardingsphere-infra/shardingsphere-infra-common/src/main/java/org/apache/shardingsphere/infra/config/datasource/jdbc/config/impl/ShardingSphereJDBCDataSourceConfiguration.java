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

package org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.datasource.JdbcUri;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * ShardingSphere-JDBC data source configuration.
 */
@Getter
@EqualsAndHashCode(of = "parameter", callSuper = false)
public final class ShardingSphereJDBCDataSourceConfiguration extends JDBCDataSourceConfiguration {
    
    public static final String TYPE = "ShardingSphereJDBC";
    
    private final String parameter;
    
    private final YamlRootConfiguration rootConfig;
    
    private final DatabaseType databaseType;
    
    public ShardingSphereJDBCDataSourceConfiguration(final String parameter) {
        this.parameter = parameter;
        rootConfig = YamlEngine.unmarshal(parameter, YamlRootConfiguration.class);
        Map<String, Object> props = rootConfig.getDataSources().values().iterator().next();
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(getJdbcUrl(props));
    }
    
    public ShardingSphereJDBCDataSourceConfiguration(final YamlRootConfiguration rootConfig) {
        YamlParameterConfiguration parameterConfig = new YamlParameterConfiguration(rootConfig.getDataSources(), rootConfig.getRules());
        this.parameter = YamlEngine.marshal(parameterConfig);
        this.rootConfig = rootConfig;
        Map<String, Object> props = rootConfig.getDataSources().values().iterator().next();
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(getJdbcUrl(props));
    }
    
    /**
     * Get jdbc url from parameters, the key can be url or jdbcUrl.
     *
     * @param parameters parameters
     * @return jdbc url
     */
    private String getJdbcUrl(final Map<String, Object> parameters) {
        Object result = parameters.getOrDefault("url", parameters.get("jdbcUrl"));
        Preconditions.checkNotNull(result, "url or jdbcUrl is required.");
        return result.toString();
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    protected Object getDataSourceConfiguration() {
        return rootConfig;
    }
    
    @Override
    public void appendJDBCParameters(final Map<String, String> parameters) {
        rootConfig.getDataSources()
                .forEach((key, value) -> {
                    String jdbcUrlKey = value.containsKey("url") ? "url" : "jdbcUrl";
                    value.replace(jdbcUrlKey, new JdbcUri(value.get(jdbcUrlKey).toString()).appendParameters(parameters));
                });
    }
    
    /**
     * Get actual data source configuration.
     *
     * @param actualDataSourceName actual data source name
     * @return actual data source configuration
     */
    public StandardJDBCDataSourceConfiguration getActualDataSourceConfig(final String actualDataSourceName) {
        Map<String, Object> yamlDataSourceConfig = rootConfig.getDataSources().get(actualDataSourceName);
        Preconditions.checkNotNull(yamlDataSourceConfig, "actualDataSourceName '{}' does not exist", actualDataSourceName);
        return new StandardJDBCDataSourceConfiguration(yamlDataSourceConfig);
    }
    
    /**
     * YAML parameter configuration.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    private static class YamlParameterConfiguration implements YamlConfiguration {
        
        private Map<String, Map<String, Object>> dataSources = new HashMap<>();
        
        private Collection<YamlRuleConfiguration> rules = new LinkedList<>();
    }
}

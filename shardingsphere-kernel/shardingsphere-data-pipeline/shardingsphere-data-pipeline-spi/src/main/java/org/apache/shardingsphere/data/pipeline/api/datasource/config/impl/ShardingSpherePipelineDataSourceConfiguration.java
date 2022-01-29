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

package org.apache.shardingsphere.data.pipeline.api.datasource.config.impl;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
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
import java.util.Properties;

/**
 * Pipeline data source configuration for ShardingSphere-JDBC.
 */
@Getter
@EqualsAndHashCode(of = "parameter")
public final class ShardingSpherePipelineDataSourceConfiguration implements PipelineDataSourceConfiguration {
    
    public static final String TYPE = "ShardingSphereJDBC";
    
    private final String parameter;
    
    private final YamlRootConfiguration rootConfig;
    
    private final DatabaseType databaseType;
    
    public ShardingSpherePipelineDataSourceConfiguration(final String parameter) {
        this.parameter = parameter;
        rootConfig = YamlEngine.unmarshal(parameter, YamlRootConfiguration.class, true);
        Map<String, Object> props = rootConfig.getDataSources().values().iterator().next();
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(getJdbcUrl(props));
    }
    
    public ShardingSpherePipelineDataSourceConfiguration(final YamlRootConfiguration rootConfig) {
        YamlParameterConfiguration parameterConfig = new YamlParameterConfiguration(rootConfig.getDataSources(), rootConfig.getRules());
        this.parameter = YamlEngine.marshal(parameterConfig);
        this.rootConfig = rootConfig;
        Map<String, Object> props = rootConfig.getDataSources().values().iterator().next();
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(getJdbcUrl(props));
    }
    
    private String getJdbcUrl(final Map<String, Object> props) {
        Object result = props.getOrDefault("url", props.get("jdbcUrl"));
        Preconditions.checkNotNull(result, "url or jdbcUrl is required.");
        return result.toString();
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public Object getDataSourceConfiguration() {
        return rootConfig;
    }
    
    @Override
    public void appendJDBCQueryProperties(final Properties queryProps) {
        rootConfig.getDataSources()
                .forEach((key, value) -> {
                    String jdbcUrlKey = value.containsKey("url") ? "url" : "jdbcUrl";
                    value.replace(jdbcUrlKey, new JdbcUrlAppender().appendQueryProperties(value.get(jdbcUrlKey).toString(), queryProps));
                });
    }
    
    /**
     * Get actual data source configuration.
     *
     * @param actualDataSourceName actual data source name
     * @return actual data source configuration
     */
    public StandardPipelineDataSourceConfiguration getActualDataSourceConfig(final String actualDataSourceName) {
        Map<String, Object> yamlDataSourceConfig = rootConfig.getDataSources().get(actualDataSourceName);
        Preconditions.checkNotNull(yamlDataSourceConfig, "actualDataSourceName '{}' does not exist", actualDataSourceName);
        return new StandardPipelineDataSourceConfiguration(yamlDataSourceConfig);
    }
    
    /**
     * YAML parameter configuration.
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class YamlParameterConfiguration implements YamlConfiguration {
        
        private Map<String, Map<String, Object>> dataSources = new HashMap<>();
        
        private Collection<YamlRuleConfiguration> rules = new LinkedList<>();
    }
}

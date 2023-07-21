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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.datasource.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.infra.database.core.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.core.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
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
    
    public ShardingSpherePipelineDataSourceConfiguration(final String param) {
        rootConfig = YamlEngine.unmarshal(param, YamlRootConfiguration.class, true);
        // Need remove dataSourceProperties, because if the parameter at dataSourceProperties will override parameter at jdbcUrl
        for (Map<String, Object> each : rootConfig.getDataSources().values()) {
            each.remove("dataSourceProperties");
        }
        parameter = YamlEngine.marshal(rootConfig);
        Map<String, Object> props = rootConfig.getDataSources().values().iterator().next();
        databaseType = DatabaseTypeFactory.get(getJdbcUrl(props));
        appendJdbcQueryProperties(databaseType);
        adjustDataSourceProperties(rootConfig.getDataSources());
    }
    
    public ShardingSpherePipelineDataSourceConfiguration(final YamlRootConfiguration rootConfig) {
        this(YamlEngine.marshal(getYamlParameterConfiguration(rootConfig)));
    }
    
    private static YamlParameterConfiguration getYamlParameterConfiguration(final YamlRootConfiguration rootConfig) {
        YamlParameterConfiguration result = new YamlParameterConfiguration();
        result.setDatabaseName(rootConfig.getDatabaseName());
        result.setDataSources(rootConfig.getDataSources());
        result.setRules(rootConfig.getRules());
        return result;
    }
    
    private String getJdbcUrl(final Map<String, Object> props) {
        Object result = props.getOrDefault("url", props.get("jdbcUrl"));
        Preconditions.checkNotNull(result, "url or jdbcUrl is required.");
        return result.toString();
    }
    
    private void appendJdbcQueryProperties(final DatabaseType databaseType) {
        Optional<JdbcQueryPropertiesExtension> extension = DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType);
        if (!extension.isPresent()) {
            return;
        }
        StandardJdbcUrlParser standardJdbcUrlParser = new StandardJdbcUrlParser();
        rootConfig.getDataSources().forEach((key, value) -> {
            String jdbcUrlKey = value.containsKey("url") ? "url" : "jdbcUrl";
            String jdbcUrl = value.get(jdbcUrlKey).toString();
            Properties queryProperties = standardJdbcUrlParser.parseQueryProperties(jdbcUrl.contains("?") ? jdbcUrl.substring(jdbcUrl.indexOf("?") + 1) : "");
            extension.get().extendQueryProperties(queryProperties);
            value.replace(jdbcUrlKey, new JdbcUrlAppender().appendQueryProperties(jdbcUrl, queryProperties));
        });
    }
    
    private void adjustDataSourceProperties(final Map<String, Map<String, Object>> dataSources) {
        for (Map<String, Object> queryProps : dataSources.values()) {
            for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
                queryProps.put(each, "1");
            }
        }
    }
    
    @Override
    public Object getDataSourceConfiguration() {
        return rootConfig;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    /**
     * Get actual data source configuration.
     *
     * @param actualDataSourceName actual data source name
     * @return actual data source configuration
     */
    public StandardPipelineDataSourceConfiguration getActualDataSourceConfiguration(final String actualDataSourceName) {
        Map<String, Object> yamlDataSourceConfig = rootConfig.getDataSources().get(actualDataSourceName);
        Preconditions.checkNotNull(yamlDataSourceConfig, "actualDataSourceName '{}' does not exist", actualDataSourceName);
        return new StandardPipelineDataSourceConfiguration(yamlDataSourceConfig);
    }
    
    /**
     * YAML parameter configuration.
     */
    @Getter
    @Setter
    private static class YamlParameterConfiguration implements YamlConfiguration {
        
        private String databaseName;
        
        private Map<String, Map<String, Object>> dataSources = new HashMap<>();
        
        private Collection<YamlRuleConfiguration> rules = new LinkedList<>();
    }
}

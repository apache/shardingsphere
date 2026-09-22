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

package org.apache.shardingsphere.data.pipeline.api.type;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.StandardJdbcUrlParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    
    private final String databaseName;
    
    private volatile Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap;
    
    private volatile Collection<RuleConfiguration> ruleConfigurations;
    
    private final DatabaseType databaseType;
    
    public ShardingSpherePipelineDataSourceConfiguration(final String param) {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(param, YamlRootConfiguration.class, true);
        // Need remove dataSourceProperties, because if the parameter at dataSourceProperties will override parameter at jdbcUrl
        for (Map<String, Object> each : rootConfig.getDataSources().values()) {
            each.remove("dataSourceProperties");
        }
        parameter = YamlEngine.marshal(rootConfig);
        Map<String, Object> props = rootConfig.getDataSources().values().iterator().next();
        databaseType = DatabaseTypeFactory.get(getJdbcUrl(props));
        databaseName = rootConfig.getDatabaseName();
    }
    
    public ShardingSpherePipelineDataSourceConfiguration(final YamlRootConfiguration rootConfig) {
        this(YamlEngine.marshal(getYamlParameterConfiguration(rootConfig)));
    }
    
    private static YamlParameterConfiguration getYamlParameterConfiguration(final YamlRootConfiguration rootConfig) {
        YamlParameterConfiguration result = new YamlParameterConfiguration();
        result.setDatabaseName(rootConfig.getDatabaseName());
        result.setDataSources(rootConfig.getDataSources());
        result.setRules(rootConfig.getRules());
        result.getProps().putAll(rootConfig.getProps());
        return result;
    }
    
    private String getJdbcUrl(final Map<String, Object> props) {
        Object result = props.getOrDefault("url", props.get("jdbcUrl"));
        Preconditions.checkNotNull(result, "url or jdbcUrl is required.");
        return result.toString();
    }
    
    private void appendJdbcQueryProperties(final Map<String, Object> dataSourceProps, final JdbcQueryPropertiesExtension extension) {
        String jdbcUrlKey = dataSourceProps.containsKey("url") ? "url" : "jdbcUrl";
        String jdbcUrl = dataSourceProps.get(jdbcUrlKey).toString();
        Properties queryProps = new StandardJdbcUrlParser().parseQueryProperties(jdbcUrl.contains("?") ? jdbcUrl.substring(jdbcUrl.indexOf("?") + 1) : "");
        extension.extendQueryProperties(queryProps);
        dataSourceProps.put(jdbcUrlKey, new JdbcUrlAppender().appendQueryProperties(jdbcUrl, queryProps));
    }
    
    private DataSourcePoolProperties adjustDataSourcePoolProperties(final DataSourcePoolProperties dataSourcePoolProps, final Optional<JdbcQueryPropertiesExtension> extension,
                                                                    final Map<String, Object> customPoolProps) {
        Map<String, Object> localProps = dataSourcePoolProps.getAllLocalProperties();
        extension.ifPresent(optional -> appendJdbcQueryProperties(localProps, optional));
        for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
            localProps.put(each, "1");
        }
        localProps.putAll(customPoolProps);
        return new DataSourcePoolProperties(dataSourcePoolProps.getPoolClassName(), localProps);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, DataSourcePoolProperties> createDataSourcePoolPropertiesMap(final Map<String, Map<String, Object>> dataSources) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(dataSources.size(), 1F);
        YamlDataSourceConfigurationSwapper swapper = new YamlDataSourceConfigurationSwapper();
        Optional<JdbcQueryPropertiesExtension> extension = DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType);
        dataSources.forEach((key, value) -> {
            Map<String, Object> customPoolProps = (Map<String, Object>) value.get("customPoolProps");
            result.put(key, adjustDataSourcePoolProperties(swapper.swapToDataSourcePoolProperties(value), extension,
                    null == customPoolProps ? Collections.emptyMap() : customPoolProps));
        });
        return result;
    }
    
    /**
     * Get data source pool properties.
     *
     * @return data source pool properties
     */
    public Map<String, DataSourcePoolProperties> getDataSourcePoolPropertiesMap() {
        Map<String, DataSourcePoolProperties> result = dataSourcePoolPropertiesMap;
        if (null == result) {
            synchronized (this) {
                result = dataSourcePoolPropertiesMap;
                if (null == result) {
                    YamlRootConfiguration rootConfig = YamlEngine.unmarshal(parameter, YamlRootConfiguration.class, true);
                    result = createDataSourcePoolPropertiesMap(rootConfig.getDataSources());
                    dataSourcePoolPropertiesMap = result;
                }
            }
        }
        return result;
    }
    
    /**
     * Get rule configurations.
     *
     * @return rule configurations
     */
    public Collection<RuleConfiguration> getRuleConfigurations() {
        Collection<RuleConfiguration> result = ruleConfigurations;
        if (null == result) {
            synchronized (this) {
                result = ruleConfigurations;
                if (null == result) {
                    result = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(parameter, YamlRootConfiguration.class, true).getRules());
                    ruleConfigurations = result;
                }
            }
        }
        return result;
    }
    
    /**
     * Get rule configurations for data source creation.
     *
     * @return independent rule configurations
     */
    public Collection<RuleConfiguration> getCreationRuleConfigurations() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(parameter, YamlRootConfiguration.class, true).getRules());
    }
    
    @Override
    public Object getDataSourceConfiguration() {
        getDataSourcePoolPropertiesMap();
        return this;
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
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(parameter, YamlRootConfiguration.class, true);
        Map<String, Object> dataSourceConfig = rootConfig.getDataSources().get(actualDataSourceName);
        Preconditions.checkNotNull(dataSourceConfig, "actualDataSourceName '{}' does not exist", actualDataSourceName);
        StandardPipelineDataSourceConfiguration adjusted = new StandardPipelineDataSourceConfiguration(dataSourceConfig);
        String jdbcUrlKey = dataSourceConfig.containsKey("url") ? "url" : "jdbcUrl";
        dataSourceConfig.put(jdbcUrlKey, adjusted.getUrl());
        for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
            dataSourceConfig.put(each, "1");
        }
        return new StandardPipelineDataSourceConfiguration(dataSourceConfig);
    }
    
    /**
     * YAML parameter configuration.
     */
    @Getter
    @Setter
    private static final class YamlParameterConfiguration implements YamlConfiguration {
        
        private String databaseName;
        
        private Map<String, Map<String, Object>> dataSources = new HashMap<>();
        
        private Collection<YamlRuleConfiguration> rules = new LinkedList<>();
        
        private Properties props = new Properties();
    }
}

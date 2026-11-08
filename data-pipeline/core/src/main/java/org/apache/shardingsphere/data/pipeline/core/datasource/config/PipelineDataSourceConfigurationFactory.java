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

package org.apache.shardingsphere.data.pipeline.core.datasource.config;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.StandardJdbcUrlParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Pipeline data source configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineDataSourceConfigurationFactory {
    
    private static final String DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
    
    /**
     * Create new instance of pipeline data source configuration.
     *
     * @param type type of pipeline data source configuration
     * @param param parameter of pipeline data source configuration
     * @return created instance
     * @throws UnsupportedSQLOperationException unsupported SQL operation exception
     */
    @SuppressWarnings("unchecked")
    public static PipelineDataSourceConfiguration newInstance(final String type, final String param) {
        switch (type) {
            case StandardPipelineDataSourceConfiguration.TYPE:
                return newInstance(YamlEngine.unmarshal(param, Map.class));
            case ShardingSpherePipelineDataSourceConfiguration.TYPE:
                return createShardingSphereConfiguration(param);
            default:
                throw new UnsupportedSQLOperationException(String.format("Unsupported data source type `%s`", type));
        }
    }
    
    /**
     * Create a standard pipeline configuration from pool properties.
     *
     * @param poolProps pool properties
     * @return standard pipeline data source configuration
     */
    public static StandardPipelineDataSourceConfiguration newInstance(final Map<String, Object> poolProps) {
        String parameter = YamlEngine.marshal(poolProps);
        Map<String, Object> newPoolProps = new HashMap<>(poolProps);
        for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
            newPoolProps.put(each, "1");
        }
        if (newPoolProps.containsKey("jdbcUrl")) {
            newPoolProps.put("url", newPoolProps.get("jdbcUrl"));
            newPoolProps.remove("jdbcUrl");
        }
        DatabaseType databaseType = DatabaseTypeFactory.get(String.valueOf(newPoolProps.get("url")));
        newPoolProps.remove(DATA_SOURCE_CLASS_NAME);
        newPoolProps.put(DATA_SOURCE_CLASS_NAME, "com.zaxxer.hikari.HikariDataSource");
        appendStandardJdbcQueryProperties(newPoolProps, databaseType);
        return new StandardPipelineDataSourceConfiguration(parameter, databaseType, String.valueOf(newPoolProps.get("url")), String.valueOf(newPoolProps.get("username")),
                String.valueOf(newPoolProps.get("password")), new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(newPoolProps));
    }
    
    /**
     * Create a ShardingSphere pipeline configuration from root configuration.
     *
     * @param rootConfig root configuration
     * @return ShardingSphere pipeline data source configuration
     */
    public static ShardingSpherePipelineDataSourceConfiguration newInstance(final YamlRootConfiguration rootConfig) {
        return createShardingSphereConfiguration(YamlEngine.marshal(getYamlParameterConfiguration(rootConfig)));
    }
    
    @SuppressWarnings("unchecked")
    private static ShardingSpherePipelineDataSourceConfiguration createShardingSphereConfiguration(final String param) {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(param, YamlRootConfiguration.class, true);
        for (Map<String, Object> each : rootConfig.getDataSources().values()) {
            each.remove("dataSourceProperties");
        }
        String parameter = YamlEngine.marshal(rootConfig);
        Map<String, Object> props = rootConfig.getDataSources().values().iterator().next();
        DatabaseType databaseType = DatabaseTypeFactory.get(getJdbcUrl(props));
        String databaseName = rootConfig.getDatabaseName();
        Map<String, DataSourcePoolProperties> dataSourcePoolPropsMap = createDataSourcePoolPropertiesMap(rootConfig.getDataSources(), databaseType);
        Map<String, String> jdbcUrlKeys = new HashMap<>(rootConfig.getDataSources().size(), 1F);
        Map<String, Collection<String>> customPoolPropsKeys = new HashMap<>(rootConfig.getDataSources().size(), 1F);
        rootConfig.getDataSources().forEach((key, value) -> {
            jdbcUrlKeys.put(key, value.containsKey("url") ? "url" : "jdbcUrl");
            if (value.containsKey("customPoolProps")) {
                customPoolPropsKeys.put(key, new LinkedHashSet<>(((Map<String, Object>) value.get("customPoolProps")).keySet()));
            }
        });
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules());
        return new ShardingSpherePipelineDataSourceConfiguration(parameter, databaseName, dataSourcePoolPropsMap, jdbcUrlKeys, customPoolPropsKeys, ruleConfigs, databaseType);
    }
    
    private static String getJdbcUrl(final Map<String, Object> props) {
        Object result = props.getOrDefault("url", props.get("jdbcUrl"));
        Preconditions.checkNotNull(result, "url or jdbcUrl is required.");
        return result.toString();
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, DataSourcePoolProperties> createDataSourcePoolPropertiesMap(final Map<String, Map<String, Object>> dataSources, final DatabaseType databaseType) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(dataSources.size(), 1F);
        YamlDataSourceConfigurationSwapper swapper = new YamlDataSourceConfigurationSwapper();
        Optional<JdbcQueryPropertiesExtension> extension = DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType);
        dataSources.forEach((key, value) -> {
            Map<String, Object> customPoolProps = (Map<String, Object>) value.get("customPoolProps");
            DataSourcePoolProperties dataSourcePoolProps = swapper.swapToDataSourcePoolProperties(value);
            Map<String, Object> localProps = dataSourcePoolProps.getAllLocalProperties();
            extension.ifPresent(each -> appendJdbcQueryProperties(localProps, each));
            result.put(key, adjustDataSourcePoolProperties(dataSourcePoolProps.getPoolClassName(), localProps, null == customPoolProps ? Collections.emptyMap() : customPoolProps));
        });
        return result;
    }
    
    private static void appendJdbcQueryProperties(final Map<String, Object> dataSourceProps, final JdbcQueryPropertiesExtension extension) {
        String jdbcUrlKey = dataSourceProps.containsKey("url") ? "url" : "jdbcUrl";
        String jdbcUrl = dataSourceProps.get(jdbcUrlKey).toString();
        Properties queryProps = new StandardJdbcUrlParser().parseQueryProperties(jdbcUrl.contains("?") ? jdbcUrl.substring(jdbcUrl.indexOf("?") + 1) : "");
        extension.extendQueryProperties(queryProps);
        dataSourceProps.put(jdbcUrlKey, new JdbcUrlAppender().appendQueryProperties(jdbcUrl, queryProps));
    }
    
    private static DataSourcePoolProperties adjustDataSourcePoolProperties(final String poolClassName, final Map<String, Object> localProps, final Map<String, Object> customPoolProps) {
        for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
            localProps.put(each, "1");
        }
        localProps.putAll(customPoolProps);
        return new DataSourcePoolProperties(poolClassName, localProps);
    }
    
    private static void appendStandardJdbcQueryProperties(final Map<String, Object> poolProps, final DatabaseType databaseType) {
        Optional<JdbcQueryPropertiesExtension> extension = DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType);
        if (!extension.isPresent()) {
            return;
        }
        String jdbcUrl = String.valueOf(poolProps.get("url"));
        Properties queryProps = new StandardJdbcUrlParser().parseQueryProperties(jdbcUrl.contains("?") ? jdbcUrl.substring(jdbcUrl.indexOf("?") + 1) : "");
        extension.get().extendQueryProperties(queryProps);
        String url = new JdbcUrlAppender().appendQueryProperties(jdbcUrl, queryProps);
        poolProps.put("url", url);
    }
    
    private static YamlParameterConfiguration getYamlParameterConfiguration(final YamlRootConfiguration rootConfig) {
        YamlParameterConfiguration result = new YamlParameterConfiguration();
        result.setDatabaseName(rootConfig.getDatabaseName());
        result.setDataSources(rootConfig.getDataSources());
        result.setRules(rootConfig.getRules());
        result.getProps().putAll(rootConfig.getProps());
        return result;
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

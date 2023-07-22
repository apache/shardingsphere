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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlJdbcConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.datasource.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.infra.database.core.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.core.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Pipeline data source configuration for standard JDBC.
 */
@EqualsAndHashCode(of = "parameter")
public final class StandardPipelineDataSourceConfiguration implements PipelineDataSourceConfiguration {
    
    public static final String TYPE = "JDBC";
    
    private static final String DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
    
    @Getter
    private final String parameter;
    
    private final DataSourceProperties dataSourceProperties;
    
    @Getter
    private final YamlJdbcConfiguration jdbcConfig;
    
    @Getter
    private final DatabaseType databaseType;
    
    @SuppressWarnings("unchecked")
    public StandardPipelineDataSourceConfiguration(final String param) {
        this(param, YamlEngine.unmarshal(param, Map.class));
    }
    
    public StandardPipelineDataSourceConfiguration(final Map<String, Object> yamlDataSourceConfig) {
        this(YamlEngine.marshal(yamlDataSourceConfig), new HashMap<>(yamlDataSourceConfig));
    }
    
    private StandardPipelineDataSourceConfiguration(final String param, final Map<String, Object> yamlConfig) {
        parameter = param;
        for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
            yamlConfig.put(each, "1");
        }
        if (yamlConfig.containsKey("jdbcUrl")) {
            yamlConfig.put("url", yamlConfig.get("jdbcUrl"));
            yamlConfig.remove("jdbcUrl");
        }
        yamlConfig.remove(DATA_SOURCE_CLASS_NAME);
        jdbcConfig = YamlEngine.unmarshal(YamlEngine.marshal(yamlConfig), YamlJdbcConfiguration.class, true);
        databaseType = DatabaseTypeFactory.get(jdbcConfig.getUrl());
        yamlConfig.put(DATA_SOURCE_CLASS_NAME, "com.zaxxer.hikari.HikariDataSource");
        appendJdbcQueryProperties(databaseType, yamlConfig);
        dataSourceProperties = new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(yamlConfig);
    }
    
    public StandardPipelineDataSourceConfiguration(final String jdbcUrl, final String username, final String password) {
        this(wrapParameter(jdbcUrl, username, password));
    }
    
    private static Map<String, Object> wrapParameter(final String jdbcUrl, final String username, final String password) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        // Reference ConnectionPropertySynonyms
        result.put("url", jdbcUrl);
        result.put("username", username);
        result.put("password", password);
        return result;
    }
    
    private void appendJdbcQueryProperties(final DatabaseType databaseType, final Map<String, Object> yamlConfig) {
        Optional<JdbcQueryPropertiesExtension> extension = DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType);
        if (!extension.isPresent()) {
            return;
        }
        String jdbcUrl = jdbcConfig.getUrl();
        Properties queryProperties = new StandardJdbcUrlParser().parseQueryProperties(jdbcUrl.contains("?") ? jdbcUrl.substring(jdbcUrl.indexOf("?") + 1) : "");
        extension.get().extendQueryProperties(queryProperties);
        String url = new JdbcUrlAppender().appendQueryProperties(jdbcUrl, queryProperties);
        jdbcConfig.setUrl(url);
        yamlConfig.put("url", url);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public Object getDataSourceConfiguration() {
        return dataSourceProperties;
    }
}

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

package org.apache.shardingsphere.data.pipeline.core.datasource.config.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.JdbcUri;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pipeline data source configuration for standard JDBC.
 */
@EqualsAndHashCode(of = "parameter")
public final class StandardPipelineDataSourceConfiguration implements PipelineDataSourceConfiguration {
    
    public static final String TYPE = "JDBC";
    
    private static final String DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
    
    @Getter
    private final String parameter;
    
    private final DataSourceConfiguration dataSourceConfig;
    
    @Getter
    private final HikariConfig hikariConfig;
    
    @Getter
    private final DatabaseType databaseType;
    
    @SuppressWarnings("unchecked")
    public StandardPipelineDataSourceConfiguration(final String parameter) {
        this(YamlEngine.unmarshal(parameter, Map.class), parameter);
    }
    
    public StandardPipelineDataSourceConfiguration(final Map<String, Object> yamlDataSourceConfig) {
        this(yamlDataSourceConfig, YamlEngine.marshal(yamlDataSourceConfig));
    }
    
    private StandardPipelineDataSourceConfiguration(final Map<String, Object> yamlConfig, final String parameter) {
        this.parameter = parameter;
        if (!yamlConfig.containsKey(DATA_SOURCE_CLASS_NAME)) {
            yamlConfig.put(DATA_SOURCE_CLASS_NAME, HikariDataSource.class.getName());
        }
        dataSourceConfig = new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(yamlConfig);
        yamlConfig.remove(DATA_SOURCE_CLASS_NAME);
        hikariConfig = YamlEngine.unmarshal(YamlEngine.marshal(yamlConfig), HikariConfig.class, true);
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(hikariConfig.getJdbcUrl());
    }
    
    public StandardPipelineDataSourceConfiguration(final String jdbcUrl, final String username, final String password) {
        this(wrapParameter(jdbcUrl, username, password));
    }
    
    private static Map<String, Object> wrapParameter(final String jdbcUrl, final String username, final String password) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1);
        result.put("jdbcUrl", jdbcUrl);
        result.put("username", username);
        result.put("password", password);
        return result;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public Object getDataSourceConfiguration() {
        return dataSourceConfig;
    }
    
    @Override
    public void appendJDBCParameters(final Map<String, String> parameters) {
        hikariConfig.setJdbcUrl(new JdbcUri(hikariConfig.getJdbcUrl()).appendParameters(parameters));
    }
    
    // TODO toShardingSphereJDBCDataSource(final String actualDataSourceName, final String logicTableName, final String actualTableName)
}

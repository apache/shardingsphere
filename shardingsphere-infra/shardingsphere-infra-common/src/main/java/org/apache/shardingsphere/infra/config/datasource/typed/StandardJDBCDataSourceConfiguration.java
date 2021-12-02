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

package org.apache.shardingsphere.infra.config.datasource.typed;

import com.zaxxer.hikari.HikariConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.JdbcUri;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard JDBC data source configuration.
 */
@Getter
@EqualsAndHashCode(of = "parameter")
public final class StandardJDBCDataSourceConfiguration implements TypedDataSourceConfiguration {
    
    private static final String TYPE = "JDBC";
    
    private static final String DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
    
    private volatile String parameter;
    
    private volatile DataSourceConfiguration dataSourceConfig;
    
    private volatile HikariConfig hikariConfig;
    
    private volatile DatabaseType databaseType;
    
    public StandardJDBCDataSourceConfiguration() {
    }
    
    public StandardJDBCDataSourceConfiguration(final String parameter) {
        init(parameter);
    }
    
    public StandardJDBCDataSourceConfiguration(final String jdbcUrl, final String username, final String password) {
        this(wrapParameter(jdbcUrl, username, password));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void init(final String parameter) {
        this.parameter = parameter;
        Map<String, Object> yamlConfig = YamlEngine.unmarshal(parameter, Map.class);
        if (!yamlConfig.containsKey(DATA_SOURCE_CLASS_NAME)) {
            yamlConfig.put(DATA_SOURCE_CLASS_NAME, "com.zaxxer.hikari.HikariDataSource");
        }
        dataSourceConfig = new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(yamlConfig);
        yamlConfig.remove(DATA_SOURCE_CLASS_NAME);
        hikariConfig = YamlEngine.unmarshal(YamlEngine.marshal(yamlConfig), HikariConfig.class, true);
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(hikariConfig.getJdbcUrl());
    }
    
    @Override
    public void appendJDBCParameters(final Map<String, String> parameters) {
        hikariConfig.setJdbcUrl(new JdbcUri(hikariConfig.getJdbcUrl()).appendParameters(parameters));
    }
    
    private static String wrapParameter(final String jdbcUrl, final String username, final String password) {
        Map<String, String> parameter = new HashMap<>(3, 1);
        parameter.put("jdbcUrl", jdbcUrl);
        parameter.put("username", username);
        parameter.put("password", password);
        return YamlEngine.marshal(parameter);
    }
    
    @Override
    public TypedDataSourceConfigurationWrap wrap() {
        TypedDataSourceConfigurationWrap result = new TypedDataSourceConfigurationWrap();
        result.setType(TYPE);
        result.setParameter(parameter);
        return result;
    }
    
    @Override
    public DataSource toDataSource() {
        return DataSourceConverter.getDataSource(dataSourceConfig);
    }
}

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

package org.apache.shardingsphere.scaling.core.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard JDBC data source configuration.
 */
@Getter
@EqualsAndHashCode(of = "parameter")
public final class StandardJDBCDataSourceConfiguration implements ScalingDataSourceConfiguration {
    
    /**
     * Type.
     */
    public static final String TYPE = "JDBC";
    
    private static final String DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
    
    private final String parameter;
    
    private final DataSourceConfiguration dataSourceConfig;
    
    private final HikariConfig hikariConfig;
    
    private final DatabaseType databaseType;
    
    @SuppressWarnings("unchecked")
    public StandardJDBCDataSourceConfiguration(final String parameter) {
        this.parameter = parameter;
        Map<String, Object> yamlConfig = YamlEngine.unmarshal(parameter, Map.class);
        if (!yamlConfig.containsKey(DATA_SOURCE_CLASS_NAME)) {
            yamlConfig.put(DATA_SOURCE_CLASS_NAME, "com.zaxxer.hikari.HikariDataSource");
        }
        dataSourceConfig = new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(yamlConfig);
        yamlConfig.remove(DATA_SOURCE_CLASS_NAME);
        hikariConfig = unmarshalSkipMissingProperties(YamlEngine.marshal(yamlConfig), HikariConfig.class);
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(hikariConfig.getJdbcUrl());
    }
    
    public StandardJDBCDataSourceConfiguration(final String jdbcUrl, final String username, final String password) {
        this(wrapParameter(jdbcUrl, username, password));
    }
    
    private <T> T unmarshalSkipMissingProperties(final String yamlContent, final Class<T> classType) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(classType), representer);
        return yaml.loadAs(yamlContent, classType);
    }
    
    private static String wrapParameter(final String jdbcUrl, final String username, final String password) {
        Map<String, String> parameter = new HashMap<>(3, 1);
        parameter.put("jdbcUrl", jdbcUrl);
        parameter.put("username", username);
        parameter.put("password", password);
        return YamlEngine.marshal(parameter);
    }
    
    @Override
    public ScalingDataSourceConfigurationWrap wrap() {
        ScalingDataSourceConfigurationWrap result = new ScalingDataSourceConfigurationWrap();
        result.setType(TYPE);
        result.setParameter(parameter);
        return result;
    }
    
    @Override
    public DataSource toDataSource() {
        return DataSourceConverter.getDataSource(dataSourceConfig);
    }
}

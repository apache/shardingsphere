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
import com.zaxxer.hikari.HikariDataSource;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import javax.sql.DataSource;

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
    
    private final String parameter;
    
    private final HikariConfig hikariConfig;
    
    private final DatabaseType databaseType;
    
    public StandardJDBCDataSourceConfiguration(final String parameter) {
        this.parameter = parameter;
        hikariConfig = YamlEngine.unmarshal(parameter, HikariConfig.class);
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(hikariConfig.getJdbcUrl());
    }
    
    public StandardJDBCDataSourceConfiguration(final String jdbcUrl, final String username, final String password) {
        HikariConfig hikariConfig = getHikariConfig(jdbcUrl, username, password);
        this.hikariConfig = hikariConfig;
        this.parameter = YamlEngine.marshal(hikariConfig);
        databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcUrl);
    }
    
    private HikariConfig getHikariConfig(final String jdbcUrl, final String username, final String password) {
        HikariConfig result = new HikariConfig();
        result.setJdbcUrl(jdbcUrl);
        result.setUsername(username);
        result.setPassword(password);
        return result;
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
        return new HikariDataSource(hikariConfig);
    }
}

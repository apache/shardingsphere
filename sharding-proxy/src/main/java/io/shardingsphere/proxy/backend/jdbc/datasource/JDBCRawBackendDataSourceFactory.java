/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.backend.jdbc.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;

/**
 * Backend data source factory using {@code HikariDataSource} for JDBC raw.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class JDBCRawBackendDataSourceFactory implements JDBCBackendDataSourceFactory {
    
    @Override
    public DataSource build(final String dataSourceName, final DataSourceParameter dataSourceParameter) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(dataSourceParameter.getUrl());
        config.setUsername(dataSourceParameter.getUsername());
        config.setPassword(dataSourceParameter.getPassword());
        config.setAutoCommit(dataSourceParameter.getAutoCommit());
        config.setConnectionTimeout(dataSourceParameter.getConnectionTimeout());
        config.setIdleTimeout(dataSourceParameter.getIdleTimeout());
        config.setMaxLifetime(dataSourceParameter.getMaxLifetime());
        config.setMaximumPoolSize(dataSourceParameter.getMaximumPoolSize());
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        return new HikariDataSource(config);
    }
}

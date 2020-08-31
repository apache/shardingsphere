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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;

import javax.sql.DataSource;

/**
 * Backend data source factory using {@code HikariDataSource} for JDBC raw.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCRawBackendDataSourceFactory implements JDBCBackendDataSourceFactory {
    
    private static final JDBCRawBackendDataSourceFactory INSTANCE = new JDBCRawBackendDataSourceFactory();
    
    /**
     * Get instance of {@code JDBCBackendDataSourceFactory}.
     *
     * @return JDBC backend data source factory
     */
    public static JDBCBackendDataSourceFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public DataSource build(final String dataSourceName, final DataSourceParameter dataSourceParameter) {
        HikariConfig config = new HikariConfig();
        String driverClassName = JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(dataSourceParameter.getUrl()).getDriverClassName();
        validateDriverClassName(driverClassName);
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(dataSourceParameter.getUrl());
        config.setUsername(dataSourceParameter.getUsername());
        config.setPassword(dataSourceParameter.getPassword());
        config.setConnectionTimeout(dataSourceParameter.getConnectionTimeoutMilliseconds());
        config.setIdleTimeout(dataSourceParameter.getIdleTimeoutMilliseconds());
        config.setMaxLifetime(dataSourceParameter.getMaxLifetimeMilliseconds());
        config.setMaximumPoolSize(dataSourceParameter.getMaxPoolSize());
        config.setMinimumIdle(dataSourceParameter.getMinPoolSize());
        config.setReadOnly(dataSourceParameter.isReadOnly());
        config.addDataSourceProperty("useServerPrepStmts", Boolean.TRUE.toString());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useLocalSessionState", Boolean.TRUE.toString());
        config.addDataSourceProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        config.addDataSourceProperty("cacheResultSetMetadata", Boolean.FALSE.toString());
        config.addDataSourceProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        config.addDataSourceProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        config.addDataSourceProperty("maintainTimeStats", Boolean.FALSE.toString());
        config.addDataSourceProperty("netTimeoutForStreamingResults", 0);
        config.addDataSourceProperty("tinyInt1isBit", Boolean.FALSE.toString());
        return new HikariDataSource(config);
    }
    
    private void validateDriverClassName(final String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (final ClassNotFoundException ex) {
            throw new ShardingSphereException("Cannot load JDBC driver class `%s`, make sure it in ShardingSphere-Proxy's classpath.", driverClassName);
        }
    }
}

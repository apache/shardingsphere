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

package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * Backend data source factory using {@code HikariDataSource} for JDBC raw.
 *
 * @author zhaojun
 * @author zhangliang
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
        String driverClassName = getDriverClassName(dataSourceParameter.getUrl());
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
        config.addDataSourceProperty("useServerPrepStmts", Boolean.TRUE.toString());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useLocalSessionState", Boolean.TRUE.toString());
        config.addDataSourceProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        config.addDataSourceProperty("cacheResultSetMetadata", Boolean.TRUE.toString());
        config.addDataSourceProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        config.addDataSourceProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        config.addDataSourceProperty("maintainTimeStats", Boolean.FALSE.toString());
        config.addDataSourceProperty("netTimeoutForStreamingResults", 0);
        return new HikariDataSource(config);
    }
    
    private String getDriverClassName(final String url) {
        try {
            new MySQLDataSourceMetaData(url);
            return "com.mysql.jdbc.Driver";
        } catch (final ShardingException ignore) {
        }
        try {
            new PostgreSQLDataSourceMetaData(url);
            return "org.postgresql.Driver";
        } catch (final ShardingException ignore) {
        }
        throw new UnsupportedOperationException(String.format("Cannot support url `%s`", url));
    }
    
    private void validateDriverClassName(final String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (final ClassNotFoundException ex) {
            throw new ShardingException("Cannot load JDBC driver class `%s`, make sure it in Sharding-Proxy's classpath.", driverClassName);
        }
    }
}

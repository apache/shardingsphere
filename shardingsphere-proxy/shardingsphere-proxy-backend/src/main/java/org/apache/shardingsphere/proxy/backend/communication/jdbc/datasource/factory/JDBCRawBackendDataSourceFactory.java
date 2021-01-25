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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.factory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.datasource.JDBCParameterDecorator;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Backend data source factory using {@code HikariDataSource} for JDBC raw.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class JDBCRawBackendDataSourceFactory implements JDBCBackendDataSourceFactory {
    
    private static final JDBCRawBackendDataSourceFactory INSTANCE = new JDBCRawBackendDataSourceFactory();
    
    static {
        ShardingSphereServiceLoader.register(JDBCParameterDecorator.class);
    }
    
    /**
     * Get instance of {@code JDBCBackendDataSourceFactory}.
     *
     * @return JDBC backend data source factory
     */
    public static JDBCBackendDataSourceFactory getInstance() {
        return INSTANCE;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
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
        DataSource result;
        try {
            result = new HikariDataSource(config);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Exception occur: ", ex);
            return null;
        }
        Optional<JDBCParameterDecorator> decorator = findJDBCParameterDecorator(result);
        return decorator.isPresent() ? decorator.get().decorate(result) : result;
    }
    
    private void validateDriverClassName(final String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (final ClassNotFoundException ex) {
            throw new ShardingSphereException("Cannot load JDBC driver class `%s`, make sure it in ShardingSphere-Proxy's classpath.", driverClassName);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private Optional<JDBCParameterDecorator> findJDBCParameterDecorator(final DataSource dataSource) {
        return ShardingSphereServiceLoader.newServiceInstances(JDBCParameterDecorator.class).stream().filter(each -> each.getType() == dataSource.getClass()).findFirst();
    }
}

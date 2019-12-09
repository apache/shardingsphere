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

package org.apache.shardingsphere.shardingscaling.core.util;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source factory.
 *
 * @author avalon566
 */
public final class DataSourceFactory {
    
    private final ConcurrentHashMap<JdbcDataSourceConfiguration, HikariDataSource> cachedDataSources = new ConcurrentHashMap<>();

    /**
     * Get data source by {@code DataSourceConfiguration}.
     *
     * @param dataSourceConfiguration data source configuration
     * @return data source
     */
    public DataSource getDataSource(final DataSourceConfiguration dataSourceConfiguration) {
        if (JdbcDataSourceConfiguration.class.equals(dataSourceConfiguration.getClass())) {
            JdbcDataSourceConfiguration jdbcDataSourceConfiguration = (JdbcDataSourceConfiguration) dataSourceConfiguration;
            if (cachedDataSources.containsKey(jdbcDataSourceConfiguration)) {
                return cachedDataSources.get(jdbcDataSourceConfiguration);
            }
            synchronized (cachedDataSources) {
                if (cachedDataSources.containsKey(jdbcDataSourceConfiguration)) {
                    return cachedDataSources.get(jdbcDataSourceConfiguration);
                }
                HikariDataSource hikariDataSource = new HikariDataSource();
                hikariDataSource.setJdbcUrl(jdbcDataSourceConfiguration.getJdbcUrl());
                hikariDataSource.setUsername(jdbcDataSourceConfiguration.getUsername());
                hikariDataSource.setPassword(jdbcDataSourceConfiguration.getPassword());
                cachedDataSources.put(jdbcDataSourceConfiguration, hikariDataSource);
                return hikariDataSource;
            }
        }
        throw new UnsupportedOperationException();
    }
    
    /**
     * Close, close cached data source.
     */
    public void close() {
        for (HikariDataSource each : cachedDataSources.values()) {
            if (!each.isClosed()) {
                each.close();
            }
        }
    }
}

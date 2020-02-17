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

package org.apache.shardingsphere.shardingscaling.core.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source factory.
 *
 * @author avalon566
 * @author ssxlulu
 */
@NoArgsConstructor
public final class DataSourceManager {

    @Getter
    private final ConcurrentHashMap<JdbcDataSourceConfiguration, HikariDataSource> cachedDataSources = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentHashMap<JdbcDataSourceConfiguration, HikariDataSource> sourceDatasources = new ConcurrentHashMap<>();

    public DataSourceManager(final List<SyncConfiguration> syncConfigurations) {
        createDatasources(syncConfigurations);
    }

    /**
     * Use for check datasources, close after check.
     *
     * @param syncConfigurations syncConfigurations
     */
    private void createDatasources(final List<SyncConfiguration> syncConfigurations) {
        //create reader datasources
        for (SyncConfiguration syncConfiguration : syncConfigurations) {
            JdbcDataSourceConfiguration jdbcDataSourceConfiguration = (JdbcDataSourceConfiguration) syncConfiguration.getReaderConfiguration().getDataSourceConfiguration();
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setJdbcUrl(jdbcDataSourceConfiguration.getJdbcUrl());
            hikariDataSource.setUsername(jdbcDataSourceConfiguration.getUsername());
            hikariDataSource.setPassword(jdbcDataSourceConfiguration.getPassword());
            cachedDataSources.put(jdbcDataSourceConfiguration, hikariDataSource);
            sourceDatasources.put(jdbcDataSourceConfiguration, hikariDataSource);
        }
        //create writer datasource
        JdbcDataSourceConfiguration jdbcDataSourceConfiguration = (JdbcDataSourceConfiguration) syncConfigurations.get(0).getWriterConfiguration().getDataSourceConfiguration();
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(jdbcDataSourceConfiguration.getJdbcUrl());
        hikariDataSource.setUsername(jdbcDataSourceConfiguration.getUsername());
        hikariDataSource.setPassword(jdbcDataSourceConfiguration.getPassword());
        cachedDataSources.put(jdbcDataSourceConfiguration, hikariDataSource);
    }

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
        cachedDataSources.clear();
        sourceDatasources.clear();
    }
}

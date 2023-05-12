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

package org.apache.shardingsphere.driver.jdbc.context;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JDBC context.
 */
public final class JDBCContext {
    
    private final AtomicReference<CachedDatabaseMetaData> cachedDatabaseMetaData = new AtomicReference<>();
    
    public JDBCContext(final Map<String, DataSource> dataSources) throws SQLException {
        cachedDatabaseMetaData.set(createCachedDatabaseMetaData(dataSources).orElse(null));
    }
    
    /**
     * Get cached database meta data.
     * 
     * @return cached database meta data
     */
    public CachedDatabaseMetaData getCachedDatabaseMetaData() {
        return cachedDatabaseMetaData.get();
    }
    
    /**
     * Refresh cached database meta data.
     * 
     * @param event data source changed event
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void refreshCachedDatabaseMetaData(final DataSourceChangedEvent event) throws SQLException {
        cachedDatabaseMetaData.set(createCachedDatabaseMetaData(DataSourcePoolCreator.create(event.getDataSourcePropertiesMap())).orElse(null));
    }
    
    private Optional<CachedDatabaseMetaData> createCachedDatabaseMetaData(final Map<String, DataSource> dataSources) throws SQLException {
        if (dataSources.isEmpty()) {
            return Optional.empty();
        }
        try (Connection connection = dataSources.values().iterator().next().getConnection()) {
            return Optional.of(new CachedDatabaseMetaData(connection.getMetaData()));
        }
    }
}

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

package org.apache.shardingsphere.mode.state;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.exception.UnavailableDataSourceException;
import org.apache.shardingsphere.mode.storage.QualifiedDataSourceState;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * State context.
 */
@Slf4j
public final class StateContext {
    
    private final AtomicReference<ClusterState> currentClusterState = new AtomicReference<>(ClusterState.OK);
    
    private final Map<String, DataSourceState> dataSourceStates = new ConcurrentHashMap<>();
    
    public StateContext(final ShardingSphereMetaData metaData, final Map<String, QualifiedDataSourceState> qualifiedDataSourceStates, final boolean force) {
        initDataSourceState(metaData, convert(qualifiedDataSourceStates), force);
    }
    
    private void initDataSourceState(final ShardingSphereMetaData metaData, final Map<String, DataSourceState> storageDataSourceStates, final boolean force) {
        metaData.getDatabases().forEach((key, value) -> {
            if (value.getResourceMetaData() != null && !value.getResourceMetaData().getStorageUnits().isEmpty()) {
                initDataSourceState(key, value.getResourceMetaData().getStorageUnits(), storageDataSourceStates, force);
            }
        });
    }
    
    private void initDataSourceState(final String databaseName, final Map<String, StorageUnit> storageUnits, final Map<String, DataSourceState> storageDataSourceStates, final boolean force) {
        storageUnits.forEach((key, value) -> initDataSourceState(databaseName, storageDataSourceStates, key, value.getDataSource(), force));
    }
    
    private void initDataSourceState(final String databaseName, final Map<String, DataSourceState> storageDataSourceStates, final String actualDataSourceName, final DataSource dataSource,
                                     final boolean force) {
        DataSourceState storageState = storageDataSourceStates.get(getCacheKey(databaseName, actualDataSourceName));
        if (DataSourceState.DISABLED == storageState) {
            dataSourceStates.put(getCacheKey(databaseName, actualDataSourceName), storageState);
        } else {
            checkState(databaseName, actualDataSourceName, dataSource, force);
        }
    }
    
    private static Map<String, DataSourceState> convert(final Map<String, QualifiedDataSourceState> qualifiedDataSourceStates) {
        Map<String, DataSourceState> result = new HashMap<>(qualifiedDataSourceStates.size(), 1F);
        qualifiedDataSourceStates.forEach((key, value) -> {
            List<String> values = Splitter.on(".").splitToList(key);
            Preconditions.checkArgument(3 == values.size(), "Illegal data source of storage node.");
            String databaseName = values.get(0);
            String dataSourceName = values.get(2);
            result.put(databaseName + "." + dataSourceName, DataSourceState.valueOf(value.getStatus().name()));
        });
        return result;
    }
    
    private void checkState(final String databaseName, final String actualDataSourceName, final DataSource dataSource, final boolean force) {
        try (Connection ignored = dataSource.getConnection()) {
            dataSourceStates.put(getCacheKey(databaseName, actualDataSourceName), DataSourceState.ENABLED);
        } catch (final SQLException ex) {
            ShardingSpherePreconditions.checkState(force, () -> new UnavailableDataSourceException(actualDataSourceName, ex));
            log.error("Data source unavailable, ignored with the -f parameter.", ex);
        }
    }
    
    private String getCacheKey(final String databaseName, final String dataSourceName) {
        return databaseName + "." + dataSourceName;
    }
    
    /**
     * Get current cluster state.
     * 
     * @return current cluster state
     */
    public ClusterState getCurrentClusterState() {
        return currentClusterState.get();
    }
    
    /**
     * Switch current cluster state.
     * 
     * @param state to be switched cluster state
     */
    public void switchCurrentClusterState(final ClusterState state) {
        currentClusterState.set(state);
    }
    
    /**
     * Update data source state.
     *
     * @param qualifiedDataSource qualified data source
     * @param dataSourceState data source state
     */
    public void updateDataSourceState(final QualifiedDataSource qualifiedDataSource, final DataSourceState dataSourceState) {
        dataSourceStates.put(getCacheKey(qualifiedDataSource.getDatabaseName(), qualifiedDataSource.getDataSourceName()), dataSourceState);
    }
}

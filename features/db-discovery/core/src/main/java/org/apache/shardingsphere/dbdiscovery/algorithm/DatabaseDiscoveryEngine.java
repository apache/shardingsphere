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

package org.apache.shardingsphere.dbdiscovery.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.mysql.type.MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.DataSourceDisabledEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database discovery engine.
 */
@RequiredArgsConstructor
@Slf4j
public final class DatabaseDiscoveryEngine {
    
    private final DatabaseDiscoveryProviderAlgorithm databaseDiscoveryProviderAlgorithm;
    
    private final EventBusContext eventBusContext;
    
    /**
     * Check environment of database cluster.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     */
    public void checkEnvironment(final String databaseName, final Map<String, DataSource> dataSourceMap) {
        databaseDiscoveryProviderAlgorithm.checkEnvironment(databaseName, dataSourceMap.values());
    }
    
    /**
     * Change primary data source.
     *
     * @param databaseName database name
     * @param groupName group name
     * @param originalPrimaryDataSourceName original primary data source name
     * @param dataSourceMap data source map
     * @param disabledDataSourceNames disabled data source names
     * @return changed primary data source name
     */
    public String changePrimaryDataSource(final String databaseName, final String groupName, final String originalPrimaryDataSourceName,
                                          final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames) {
        Optional<String> newPrimaryDataSourceName = findPrimaryDataSourceName(dataSourceMap, disabledDataSourceNames);
        if (newPrimaryDataSourceName.isPresent() && !newPrimaryDataSourceName.get().equals(originalPrimaryDataSourceName)) {
            eventBusContext.post(new PrimaryDataSourceChangedEvent(new QualifiedDatabase(databaseName, groupName, newPrimaryDataSourceName.get())));
        }
        String result = newPrimaryDataSourceName.orElse(originalPrimaryDataSourceName);
        postReplicaDataSourceDisabledEvent(databaseName, groupName, result, dataSourceMap, disabledDataSourceNames);
        return result;
    }
    
    private Optional<String> findPrimaryDataSourceName(final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try {
                if (databaseDiscoveryProviderAlgorithm.isPrimaryInstance(entry.getValue())) {
                    return Optional.of(entry.getKey());
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while judge primary data source: ", ex);
            }
        }
        return Optional.empty();
    }
    
    private void postReplicaDataSourceDisabledEvent(final String databaseName, final String groupName, final String primaryDataSourceName,
                                                    final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames) {
        int enabledReplicasCount = dataSourceMap.size() - disabledDataSourceNames.size() - 1;
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (!entry.getKey().equals(primaryDataSourceName)) {
                StorageNodeDataSource storageNodeDataSource = createStorageNodeDataSource(loadReplicaStatus(entry.getValue()));
                if (StorageNodeStatus.isEnable(storageNodeDataSource.getStatus())) {
                    enabledReplicasCount += disabledDataSourceNames.contains(entry.getKey()) ? 1 : 0;
                    eventBusContext.post(new DataSourceDisabledEvent(databaseName, groupName, entry.getKey(), storageNodeDataSource));
                    continue;
                }
                if (!(databaseDiscoveryProviderAlgorithm instanceof MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm)
                        || enabledReplicasCount > Integer.parseInt(databaseDiscoveryProviderAlgorithm.getProps().getProperty("min-enabled-replicas", "0"))) {
                    enabledReplicasCount -= disabledDataSourceNames.contains(entry.getKey()) ? 0 : 1;
                    eventBusContext.post(new DataSourceDisabledEvent(databaseName, groupName, entry.getKey(), storageNodeDataSource));
                }
            }
        }
    }
    
    private StorageNodeDataSource createStorageNodeDataSource(final ReplicaDataSourceStatus replicaStatus) {
        return new StorageNodeDataSource(StorageNodeRole.MEMBER, replicaStatus.isOnline() ? StorageNodeStatus.ENABLED : StorageNodeStatus.DISABLED, replicaStatus.getReplicationDelayMilliseconds());
    }
    
    private ReplicaDataSourceStatus loadReplicaStatus(final DataSource replicaDataSource) {
        try {
            return databaseDiscoveryProviderAlgorithm.loadReplicaStatus(replicaDataSource);
        } catch (SQLException ex) {
            log.error("Load data source replica status error: ", ex);
            return new ReplicaDataSourceStatus(false, 0L);
        }
    }
}

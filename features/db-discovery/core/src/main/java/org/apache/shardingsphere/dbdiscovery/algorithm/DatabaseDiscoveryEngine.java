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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProvider;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;
import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.mode.metadata.compute.event.ComputeNodeStatusChangedEvent;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.metadata.storage.event.DataSourceDisabledEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database discovery engine.
 */
@RequiredArgsConstructor
@Slf4j
public final class DatabaseDiscoveryEngine {
    
    private final DatabaseDiscoveryProvider provider;
    
    private final InstanceContext instanceContext;
    
    /**
     * Check environment of database cluster.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     */
    public void checkEnvironment(final String databaseName, final Map<String, DataSource> dataSourceMap) {
        provider.checkEnvironment(databaseName, dataSourceMap.values());
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
        Optional<String> newPrimaryDataSourceName = findPrimaryDataSourceName(dataSourceMap);
        postComputeNodeStatusChangedEvent(newPrimaryDataSourceName.orElse(""));
        newPrimaryDataSourceName.ifPresent(optional -> postPrimaryChangedEvent(databaseName, groupName, originalPrimaryDataSourceName, optional));
        Map<String, DataSource> replicaDataSourceMap = new HashMap<>(dataSourceMap);
        newPrimaryDataSourceName.ifPresent(replicaDataSourceMap::remove);
        postReplicaDisabledEvent(databaseName, groupName, replicaDataSourceMap, disabledDataSourceNames);
        return newPrimaryDataSourceName.orElse("");
    }
    
    private Optional<String> findPrimaryDataSourceName(final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try {
                if (provider.isPrimaryInstance(entry.getValue())) {
                    return Optional.of(entry.getKey());
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while judge primary data source: ", ex);
            }
        }
        return Optional.empty();
    }
    
    private void postComputeNodeStatusChangedEvent(final String newPrimaryDataSourceName) {
        if (Strings.isNullOrEmpty(newPrimaryDataSourceName) && StateType.OK.equals(instanceContext.getInstance().getState().getCurrentState())) {
            instanceContext.getEventBusContext().post(new ComputeNodeStatusChangedEvent(instanceContext.getInstance().getCurrentInstanceId(), StateType.READ_ONLY));
            return;
        }
        if (!Strings.isNullOrEmpty(newPrimaryDataSourceName) && StateType.READ_ONLY.equals(instanceContext.getInstance().getState().getCurrentState())) {
            instanceContext.getEventBusContext().post(new ComputeNodeStatusChangedEvent(instanceContext.getInstance().getCurrentInstanceId(), StateType.OK));
        }
    }
    
    private void postPrimaryChangedEvent(final String databaseName, final String groupName, final String originalPrimaryDataSourceName, final String newPrimaryDataSourceName) {
        if (!newPrimaryDataSourceName.equals(originalPrimaryDataSourceName)) {
            instanceContext.getEventBusContext().post(new PrimaryDataSourceChangedEvent(new QualifiedDatabase(databaseName, groupName, newPrimaryDataSourceName)));
        }
    }
    
    private void postReplicaDisabledEvent(final String databaseName, final String groupName,
                                          final Map<String, DataSource> replicaDataSourceMap, final Collection<String> disabledDataSourceNames) {
        int enabledReplicasCount = replicaDataSourceMap.size() - disabledDataSourceNames.size() - 1;
        for (Entry<String, DataSource> entry : replicaDataSourceMap.entrySet()) {
            StorageNodeDataSource replicaStorageNode = createReplicaStorageNode(loadReplicaStatus(entry.getValue()));
            if (DataSourceState.ENABLED == replicaStorageNode.getStatus()) {
                enabledReplicasCount += disabledDataSourceNames.contains(entry.getKey()) ? 1 : 0;
                instanceContext.getEventBusContext().post(new DataSourceDisabledEvent(databaseName, groupName, entry.getKey(), replicaStorageNode));
                continue;
            }
            if (provider.getMinEnabledReplicas().isPresent() && 0 == provider.getMinEnabledReplicas().get()) {
                instanceContext.getEventBusContext().post(new DataSourceDisabledEvent(databaseName, groupName, entry.getKey(), replicaStorageNode));
                continue;
            }
            if (enabledReplicasCount > provider.getMinEnabledReplicas().get()) {
                enabledReplicasCount -= disabledDataSourceNames.contains(entry.getKey()) ? 0 : 1;
                instanceContext.getEventBusContext().post(new DataSourceDisabledEvent(databaseName, groupName, entry.getKey(), replicaStorageNode));
            }
        }
    }
    
    private StorageNodeDataSource createReplicaStorageNode(final ReplicaDataSourceStatus replicaStatus) {
        return new StorageNodeDataSource(StorageNodeRole.MEMBER, replicaStatus.isOnline() ? DataSourceState.ENABLED : DataSourceState.DISABLED, replicaStatus.getReplicationDelayMilliseconds());
    }
    
    private ReplicaDataSourceStatus loadReplicaStatus(final DataSource replicaDataSource) {
        try {
            return provider.loadReplicaStatus(replicaDataSource);
        } catch (final SQLException ex) {
            log.error("Load data source replica status error: ", ex);
            return new ReplicaDataSourceStatus(false, 0L);
        }
    }
}

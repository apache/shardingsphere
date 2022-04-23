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

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.spi.status.GlobalHighlyAvailableStatus;
import org.apache.shardingsphere.dbdiscovery.spi.status.HighlyAvailableStatus;
import org.apache.shardingsphere.dbdiscovery.spi.status.RoleSeparatedHighlyAvailableStatus;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database discovery engine.
 */
@RequiredArgsConstructor
public final class DatabaseDiscoveryEngine {
    
    private final DatabaseDiscoveryType databaseDiscoveryType;
    
    /**
     * Check highly available status of database cluster.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @throws SQLException SQL exception
     */
    public void checkHighlyAvailableStatus(final String databaseName, final Map<String, DataSource> dataSourceMap) throws SQLException {
        Collection<HighlyAvailableStatus> statuses = loadHighlyAvailableStatuses(dataSourceMap);
        Preconditions.checkState(!statuses.isEmpty(), "No database instance in database cluster `%s`.", databaseName);
        HighlyAvailableStatus sample = statuses.iterator().next();
        if (sample instanceof GlobalHighlyAvailableStatus) {
            checkGlobalHighlyAvailableStatus(databaseName, dataSourceMap, statuses);
        } else if (sample instanceof RoleSeparatedHighlyAvailableStatus) {
            checkRoleSeparatedHighlyAvailableStatus(databaseName, dataSourceMap, statuses);
        }
    }
    
    private Collection<HighlyAvailableStatus> loadHighlyAvailableStatuses(final Map<String, DataSource> dataSourceMap) throws SQLException {
        Collection<HighlyAvailableStatus> result = new HashSet<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            // TODO query with multiple threads
            result.add(databaseDiscoveryType.loadHighlyAvailableStatus(entry.getValue()));
        }
        return result;
    }
    
    private void checkGlobalHighlyAvailableStatus(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<HighlyAvailableStatus> statuses) throws SQLException {
        Preconditions.checkState(1 == statuses.size(), "Different status in highly available cluster in database `%s`.", databaseName);
        statuses.iterator().next().validate(databaseName, dataSourceMap, databaseDiscoveryType.getProps());
    }
    
    private void checkRoleSeparatedHighlyAvailableStatus(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<HighlyAvailableStatus> statuses) throws SQLException {
        for (HighlyAvailableStatus each : statuses) {
            each.validate(databaseName, dataSourceMap, databaseDiscoveryType.getProps());
        }
    }
    
    /**
     * Update primary data source.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @param disabledDataSourceNames disabled data source names
     * @param groupName group name
     */
    public void updatePrimaryDataSource(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames, final String groupName) {
        Optional<String> newPrimaryDataSource = databaseDiscoveryType.determinePrimaryDataSource(getActiveDataSourceMap(dataSourceMap, disabledDataSourceNames));
        if (!newPrimaryDataSource.isPresent()) {
            return;
        }
        if (!newPrimaryDataSource.get().equals(databaseDiscoveryType.getOldPrimaryDataSource())) {
            databaseDiscoveryType.setOldPrimaryDataSource(newPrimaryDataSource.get());
            ShardingSphereEventBus.getInstance().post(new PrimaryDataSourceChangedEvent(new QualifiedDatabase(databaseName, groupName, newPrimaryDataSource.get())));
        }
    }
    
    private Map<String, DataSource> getActiveDataSourceMap(final Map<String, DataSource> dataSourceMap, final Collection<String> disabledDataSourceNames) {
        Map<String, DataSource> result = new HashMap<>(dataSourceMap);
        if (!disabledDataSourceNames.isEmpty()) {
            result.entrySet().removeIf(each -> disabledDataSourceNames.contains(each.getKey()));
        }
        return result;
    }
    
    /**
     * Update member state.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @param groupName group name
     */
    public void updateMemberState(final String databaseName, final Map<String, DataSource> dataSourceMap, final String groupName) {
        databaseDiscoveryType.updateMemberState(databaseName, dataSourceMap, groupName);
    }
    
    /**
     * Get primary data source.
     *
     * @return primary data source
     */
    public String getPrimaryDataSource() {
        return databaseDiscoveryType.getPrimaryDataSource();
    }
}

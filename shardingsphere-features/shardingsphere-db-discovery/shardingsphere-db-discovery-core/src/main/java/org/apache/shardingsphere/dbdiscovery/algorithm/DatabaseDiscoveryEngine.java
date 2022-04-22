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
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Database discovery engine.
 */
@RequiredArgsConstructor
public final class DatabaseDiscoveryEngine {
    
    private final DatabaseDiscoveryType databaseDiscoveryType;
    
    /**
     * Check database discovery configuration.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @throws SQLException SQL exception
     */
    public void checkDatabaseDiscoveryConfiguration(final String databaseName, final Map<String, DataSource> dataSourceMap) throws SQLException {
        databaseDiscoveryType.checkDatabaseDiscoveryConfiguration(databaseName, dataSourceMap);
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
        databaseDiscoveryType.updatePrimaryDataSource(databaseName, getActiveDataSourceMap(dataSourceMap, disabledDataSourceNames), groupName);
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

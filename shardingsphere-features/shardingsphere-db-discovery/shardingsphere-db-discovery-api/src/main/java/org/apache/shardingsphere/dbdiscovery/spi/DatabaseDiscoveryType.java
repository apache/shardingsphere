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

package org.apache.shardingsphere.dbdiscovery.spi;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Database discovery type.
 */
public interface DatabaseDiscoveryType extends ShardingSphereAlgorithm {
    
    /**
     * Check highly available status of database cluster.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @throws SQLException SQL exception
     */
    void checkHighlyAvailableStatus(String databaseName, Map<String, DataSource> dataSourceMap) throws SQLException;
    
    /**
     * Determine primary data source.
     * 
     * @param dataSourceMap data source map
     * @return determined name of primary data source
     */
    Optional<String> determinePrimaryDataSource(Map<String, DataSource> dataSourceMap);
    
    /**
     * Update member state.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @param groupName group name
     */
    void updateMemberState(String databaseName, Map<String, DataSource> dataSourceMap, String groupName);
    
    /**
     * Get primary data source.
     *
     * @return primary data source
     */
    String getPrimaryDataSource();
    
    /**
     * Get old primary data source.
     * 
     * @return old primary data source
     */
    String getOldPrimaryDataSource();
    
    /**
     * Get old primary data source.
     * 
     * @param oldPrimaryDataSource old primary data source
     */
    void setOldPrimaryDataSource(String oldPrimaryDataSource);
}

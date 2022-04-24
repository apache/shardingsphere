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

import org.apache.shardingsphere.dbdiscovery.spi.instance.PrimaryDatabaseInstance;
import org.apache.shardingsphere.dbdiscovery.spi.status.HighlyAvailableStatus;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Database discovery provider algorithm.
 */
public interface DatabaseDiscoveryProviderAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Load highly available status.
     * 
     * @param dataSource data source
     * @return loaded highly available status
     * @throws SQLException SQL exception
     */
    HighlyAvailableStatus loadHighlyAvailableStatus(DataSource dataSource) throws SQLException;
    
    /**
     * Find primary instance.
     * 
     * @param dataSourceMap data source map
     * @return found primary instance
     */
    Optional<? extends PrimaryDatabaseInstance> findPrimaryInstance(Map<String, DataSource> dataSourceMap);
    
    /**
     * Get storage node data source.
     * 
     * @param replicaDataSource replica data source
     * @return storage node data source
     */
    StorageNodeDataSource getStorageNodeDataSource(DataSource replicaDataSource);
}

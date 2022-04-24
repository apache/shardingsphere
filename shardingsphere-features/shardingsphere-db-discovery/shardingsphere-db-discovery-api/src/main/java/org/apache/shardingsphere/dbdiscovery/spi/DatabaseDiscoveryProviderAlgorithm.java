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

import org.apache.shardingsphere.dbdiscovery.spi.status.HighlyAvailableStatus;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

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
     * Judge whether database instance is primary.
     * 
     * @param dataSource data source to be judged
     * @return is primary database instance or not
     */
    boolean isPrimaryInstance(DataSource dataSource);
    
    /**
     * Get storage node data source.
     * 
     * @param replicaDataSource replica data source
     * @return storage node data source
     */
    StorageNodeDataSource getStorageNodeDataSource(DataSource replicaDataSource);
}

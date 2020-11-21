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

package org.apache.shardingsphere.infra.executor.sql.execute.driver;

import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;

import java.sql.SQLException;
import java.util.List;

/**
 * Driver executor manager.
 * 
 * @param <C> type of resource connection
 * @param <R> type of storage resource
 * @param <O> type of storage resource option
 */
public interface DriverExecutorManager<C, R, O> {
    
    /**
     * Get connections.
     *
     * @param dataSourceName data source name
     * @param connectionSize connection size
     * @param connectionMode connection mode
     * @return connections
     * @throws SQLException SQL exception
     */
    List<C> getConnections(String dataSourceName, int connectionSize, ConnectionMode connectionMode) throws SQLException;
    
    /**
     * Create storage resource.
     *
     * @param connection connection
     * @param connectionMode connection mode
     * @param option storage resource option
     * @return storage resource
     * @throws SQLException SQL exception
     */
    R createStorageResource(C connection, ConnectionMode connectionMode, O option) throws SQLException;
    
    /**
     * Create storage resource.
     *
     * @param sql SQL
     * @param parameters SQL parameters
     * @param connection connection
     * @param connectionMode connection mode
     * @param option storage resource option
     * @return storage resource
     * @throws SQLException SQL exception
     */
    R createStorageResource(String sql, List<Object> parameters, C connection, ConnectionMode connectionMode, O option) throws SQLException;
}

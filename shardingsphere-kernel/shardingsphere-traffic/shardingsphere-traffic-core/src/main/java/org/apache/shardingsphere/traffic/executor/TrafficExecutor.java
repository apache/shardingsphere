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

package org.apache.shardingsphere.traffic.executor;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Traffic executor.
 */
public interface TrafficExecutor extends AutoCloseable {
    
    /**
     * Execute query.
     * 
     * @param logicSQL logic SQL
     * @param dataSourceConfig dataSource config
     * @return result set
     * @throws SQLException SQL exception
     */
    ResultSet executeQuery(LogicSQL logicSQL, DataSourceConfiguration dataSourceConfig) throws SQLException;
    
    /**
     * Execute update.
     * 
     * @param logicSQL logic SQL
     * @param dataSourceConfig dataSource config
     * @return update count
     * @throws SQLException SQL exception
     */
    int executeUpdate(LogicSQL logicSQL, DataSourceConfiguration dataSourceConfig) throws SQLException;
    
    /**
     * Execute.
     * 
     * @param logicSQL logic SQL
     * @param dataSourceConfig dataSource config
     * @return whether execute success or not
     */
    boolean execute(LogicSQL logicSQL, DataSourceConfiguration dataSourceConfig);
    
    /**
     * Get result set.
     *
     * @return result set
     * @throws SQLException SQL exception
     */
    ResultSet getResultSet() throws SQLException;
    
    @Override
    void close() throws SQLException;
}

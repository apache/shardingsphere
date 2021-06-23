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

package org.apache.shardingsphere.infra.executor.sql.federate.execute;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Federate executor.
 */
public interface FederateExecutor {
    
    /**
     * Execute query.
     *
     * @param executionContext execution context
     * @param callback callback
     * @param prepareEngine prepare engine
     * @return execute result
     * @throws SQLException SQL exception
     */
    List<QueryResult> executeQuery(ExecutionContext executionContext, JDBCExecutorCallback<? extends ExecuteResult> callback, 
                                   DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException;
    
    /**
     * Close.
     * 
     * @throws SQLException SQL exception
     */
    void close() throws SQLException;
    
    /**
     * Get result set.
     *
     * @return result set
     * @throws SQLException sql exception
     */
    ResultSet getResultSet() throws SQLException;
}

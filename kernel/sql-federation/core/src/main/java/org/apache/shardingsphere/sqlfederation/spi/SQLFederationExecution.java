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

package org.apache.shardingsphere.sqlfederation.spi;

import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Per-engine SQL federation execution provided by a selected provider.
 */
public interface SQLFederationExecution extends AutoCloseable {
    
    /**
     * Execute a federated query.
     *
     * @param prepareEngine prepare engine
     * @param callback execution callback
     * @param federationContext federation context
     * @return query result set
     */
    ResultSet executeQuery(DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                           JDBCExecutorCallback<? extends ExecuteResult> callback, SQLFederationContext federationContext);
    
    @Override
    void close() throws SQLException;
}

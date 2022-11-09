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

package org.apache.shardingsphere.proxy.backend.communication;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.JDBCBackendTransactionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Proxy SQL executor wrapper.
 */
public final class ProxySQLExecutorWrapper implements IProxySQLExecutor {
    
    private final JDBCBackendTransactionManager transactionManager;
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    public ProxySQLExecutorWrapper(final String driverType, final JDBCBackendConnection backendConnection, final JDBCDatabaseCommunicationEngine databaseCommunicationEngine) {
        transactionManager = new JDBCBackendTransactionManager(backendConnection);
        transactionManager.begin();
        proxySQLExecutor = new ProxySQLExecutor(driverType, backendConnection, databaseCommunicationEngine);
    }
    
    /**
     * Check execute prerequisites.
     *
     * @param executionContext execution context
     */
    public void checkExecutePrerequisites(final ExecutionContext executionContext) {
        proxySQLExecutor.checkExecutePrerequisites(executionContext);
    }
    
    /**
     * Execute SQL within a transaction.
     *
     * @param executionContext execution context
     * @return execute results
     * @throws SQLException SQL exception
     */
    public List<ExecuteResult> execute(final ExecutionContext executionContext) throws SQLException {
        List<ExecuteResult> result;
        try {
            result = proxySQLExecutor.execute(executionContext);
            transactionManager.commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            transactionManager.rollback();
            throw ex;
        }
        return result;
    }
}

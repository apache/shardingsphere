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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;

/**
 * Proxy SQL executor factory.
 */
public final class ProxySQLExecutorFactory {
    
    /**
     * Create new instance of Proxy SQL executor.
     * 
     * @param executionContext execution context
     * @param databaseCommunicationEngine database communication engine
     * @return Proxy SQL executor
     */
    public static IProxySQLExecutor newInstance(final ExecutionContext executionContext, final JDBCDatabaseCommunicationEngine databaseCommunicationEngine) {
        TransactionStatus transactionStatus = databaseCommunicationEngine.getBackendConnection().getConnectionSession().getTransactionStatus();
        SQLStatement sqlStatement = executionContext.getSqlStatementContext().getSqlStatement();
        IProxySQLExecutor result;
        if (needAutoTransaction(executionContext, transactionStatus, sqlStatement)) {
            result = new ProxySQLExecutorWrapper(databaseCommunicationEngine.getDriverType(), (JDBCBackendConnection) databaseCommunicationEngine.getBackendConnection(), databaseCommunicationEngine);
        } else {
            result = new ProxySQLExecutor(databaseCommunicationEngine.getDriverType(), (JDBCBackendConnection) databaseCommunicationEngine.getBackendConnection(), databaseCommunicationEngine);
        }
        return result;
    }
    
    private static boolean needAutoTransaction(final ExecutionContext executionContext, final TransactionStatus transactionStatus, final SQLStatement sqlStatement) {
        return TransactionType.XA == transactionStatus.getTransactionType() && !transactionStatus.isInTransaction() && sqlStatement instanceof DMLStatement
                && !(sqlStatement instanceof SelectStatement) && executionContext.getExecutionUnits().size() > 1;
    }
}

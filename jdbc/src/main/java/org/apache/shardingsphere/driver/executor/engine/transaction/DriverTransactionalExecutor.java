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

package org.apache.shardingsphere.driver.executor.engine.transaction;

import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.sql.SQLException;

/**
 * Driver transactional executor.
 */
public final class DriverTransactionalExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final TransactionRule transactionRule;
    
    public DriverTransactionalExecutor(final ShardingSphereConnection connection) {
        this.connection = connection;
        transactionRule = connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
    }
    
    /**
     * Execute.
     *
     * @param database database
     * @param executionContext execution context
     * @param callback implicit transaction callback
     * @param <T> type of return value
     * @return execution result
     * @throws SQLException SQL exception
     */
    public <T> T execute(final ShardingSphereDatabase database, final ExecutionContext executionContext, final ImplicitTransactionCallback<T> callback) throws SQLException {
        boolean isImplicitCommitTransaction = transactionRule.isImplicitCommitTransaction(executionContext.getSqlStatementContext().getSqlStatement(),
                executionContext.getExecutionUnits().size() > 1, connection.getDatabaseConnectionManager().getConnectionTransaction(), connection.getAutoCommit());
        return isImplicitCommitTransaction ? executeWithImplicitCommit(database, callback) : callback.execute();
    }
    
    /**
     * Execute.
     *
     * @param database database
     * @param sqlStatement sql statement
     * @param multiExecutionUnits is multiple execution units
     * @param callback implicit transaction callback
     * @param <T> type of return value
     * @return execution result
     * @throws SQLException SQL exception
     */
    public <T> T execute(final ShardingSphereDatabase database, final SQLStatement sqlStatement, final boolean multiExecutionUnits, final ImplicitTransactionCallback<T> callback) throws SQLException {
        boolean isImplicitCommitTransaction = transactionRule.isImplicitCommitTransaction(
                sqlStatement, multiExecutionUnits, connection.getDatabaseConnectionManager().getConnectionTransaction(), connection.getAutoCommit());
        return isImplicitCommitTransaction ? executeWithImplicitCommit(database, callback) : callback.execute();
    }
    
    private <T> T executeWithImplicitCommit(final ShardingSphereDatabase database, final ImplicitTransactionCallback<T> callback) throws SQLException {
        try {
            connection.getDatabaseConnectionManager().begin();
            T result = callback.execute();
            connection.commit();
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            connection.rollback();
            throw SQLExceptionTransformEngine.toSQLException(ex, database.getProtocolType());
        }
    }
}

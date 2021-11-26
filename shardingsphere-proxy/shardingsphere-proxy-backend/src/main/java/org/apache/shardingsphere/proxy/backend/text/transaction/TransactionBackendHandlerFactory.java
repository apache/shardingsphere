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

package org.apache.shardingsphere.proxy.backend.text.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.BroadcastDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.skip.SkipBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackToSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

/**
 * Transaction backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionBackendHandlerFactory {
    
    /**
     * New instance of backend handler.
     * 
     * @param sqlStatementContext SQL statement context
     * @param sql SQL
     * @param backendConnection backend connection
     * @return backend handler
     */
    public static TextProtocolBackendHandler newInstance(final SQLStatementContext<? extends TCLStatement> sqlStatementContext, final String sql, final BackendConnection backendConnection) {
        TCLStatement tclStatement = sqlStatementContext.getSqlStatement();
        if (tclStatement instanceof BeginTransactionStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.BEGIN, backendConnection);
        }
        if (tclStatement instanceof SetAutoCommitStatement) {
            if (((SetAutoCommitStatement) tclStatement).isAutoCommit()) {
                return backendConnection.getTransactionStatus().isInTransaction()
                        ? new TransactionBackendHandler(tclStatement, TransactionOperationType.COMMIT, backendConnection) : new SkipBackendHandler(tclStatement);
            }
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.BEGIN, backendConnection);
        }
        if (tclStatement instanceof SavepointStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.SAVEPOINT, backendConnection);
        }
        if (tclStatement instanceof ReleaseSavepointStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.RELEASE_SAVEPOINT, backendConnection);
        }
        if (tclStatement instanceof RollbackToSavepointStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.ROLLBACK_TO_SAVEPOINT, backendConnection);
        }
        if (tclStatement instanceof CommitStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.COMMIT, backendConnection);
        }
        if (tclStatement instanceof RollbackStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.ROLLBACK, backendConnection);
        }
        return new BroadcastDatabaseBackendHandler(sqlStatementContext, sql, backendConnection);
    }
}

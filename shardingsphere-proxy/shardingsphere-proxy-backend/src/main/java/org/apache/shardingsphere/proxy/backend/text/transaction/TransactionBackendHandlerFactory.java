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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.BroadcastBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.skip.SkipBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
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
     * @param sql SQL
     * @param tclStatement TCL statement
     * @param backendConnection backend connection
     * @return backend handler
     */
    public static TextProtocolBackendHandler newInstance(final String sql, final TCLStatement tclStatement, final BackendConnection backendConnection) {
        if (tclStatement instanceof BeginTransactionStatement) {
            return new TransactionBackendHandler(TransactionOperationType.BEGIN, backendConnection);
        }
        if (tclStatement instanceof SetAutoCommitStatement) {
            if (((SetAutoCommitStatement) tclStatement).isAutoCommit()) {
                return backendConnection.getTransactionStatus().isInTransaction() ? new TransactionBackendHandler(TransactionOperationType.COMMIT, backendConnection) : new SkipBackendHandler();
            }
            return new TransactionBackendHandler(TransactionOperationType.BEGIN, backendConnection);
        }
        if (tclStatement instanceof CommitStatement) {
            return new TransactionBackendHandler(TransactionOperationType.COMMIT, backendConnection);
        }
        if (tclStatement instanceof RollbackStatement) {
            return new TransactionBackendHandler(TransactionOperationType.ROLLBACK, backendConnection);
        }
        return new BroadcastBackendHandler(sql, tclStatement, backendConnection);
    }
}

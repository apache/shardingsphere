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

package org.apache.shardingsphere.proxy.backend.handler.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OperationScope;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.StartTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.XAStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.util.Collections;

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
     * @param connectionSession connection session
     * @return backend handler
     */
    public static ProxyBackendHandler newInstance(final SQLStatementContext<? extends TCLStatement> sqlStatementContext, final String sql, final ConnectionSession connectionSession) {
        TCLStatement tclStatement = sqlStatementContext.getSqlStatement();
        if (tclStatement instanceof BeginTransactionStatement || tclStatement instanceof StartTransactionStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.BEGIN, connectionSession);
        }
        if (tclStatement instanceof SetAutoCommitStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.SET_AUTOCOMMIT, connectionSession);
        }
        if (tclStatement instanceof SavepointStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.SAVEPOINT, connectionSession);
        }
        if (tclStatement instanceof ReleaseSavepointStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.RELEASE_SAVEPOINT, connectionSession);
        }
        if (tclStatement instanceof CommitStatement) {
            return new TransactionBackendHandler(tclStatement, TransactionOperationType.COMMIT, connectionSession);
        }
        if (tclStatement instanceof RollbackStatement) {
            return ((RollbackStatement) tclStatement).getSavepointName().isPresent()
                    ? new TransactionBackendHandler(tclStatement, TransactionOperationType.ROLLBACK_TO_SAVEPOINT, connectionSession)
                    : new TransactionBackendHandler(tclStatement, TransactionOperationType.ROLLBACK, connectionSession);
        }
        if (tclStatement instanceof SetTransactionStatement && OperationScope.GLOBAL != ((SetTransactionStatement) tclStatement).getScope()) {
            return new TransactionSetHandler((SetTransactionStatement) tclStatement, connectionSession);
        }
        if (tclStatement instanceof XAStatement) {
            return new TransactionXAHandler(sqlStatementContext, sql, connectionSession);
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList());
        return DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(queryContext, connectionSession.getBackendConnection(), false);
    }
}

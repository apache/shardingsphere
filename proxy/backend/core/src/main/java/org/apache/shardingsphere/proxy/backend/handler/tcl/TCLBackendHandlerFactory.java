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

package org.apache.shardingsphere.proxy.backend.handler.tcl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OperationScope;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.util.Collections;

/**
 * TCL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TCLBackendHandlerFactory {
    
    /**
     * New instance of backend handler.
     *
     * @param sqlStatementContext SQL statement context
     * @param sql SQL
     * @param connectionSession connection session
     * @return backend handler
     */
    public static ProxyBackendHandler newInstance(final SQLStatementContext sqlStatementContext, final String sql, final ConnectionSession connectionSession) {
        TCLStatement tclStatement = (TCLStatement) sqlStatementContext.getSqlStatement();
        if (tclStatement instanceof BeginTransactionStatement) {
            return new TCLBackendHandler(tclStatement, TransactionOperationType.BEGIN, connectionSession);
        }
        if (tclStatement instanceof SetAutoCommitStatement) {
            return new TCLBackendHandler(tclStatement, TransactionOperationType.SET_AUTOCOMMIT, connectionSession);
        }
        if (tclStatement instanceof SavepointStatement) {
            return new TCLBackendHandler(tclStatement, TransactionOperationType.SAVEPOINT, connectionSession);
        }
        if (tclStatement instanceof ReleaseSavepointStatement) {
            return new TCLBackendHandler(tclStatement, TransactionOperationType.RELEASE_SAVEPOINT, connectionSession);
        }
        if (tclStatement instanceof CommitStatement) {
            return new TCLBackendHandler(tclStatement, TransactionOperationType.COMMIT, connectionSession);
        }
        if (tclStatement instanceof RollbackStatement) {
            return ((RollbackStatement) tclStatement).getSavepointName().isPresent()
                    ? new TCLBackendHandler(tclStatement, TransactionOperationType.ROLLBACK_TO_SAVEPOINT, connectionSession)
                    : new TCLBackendHandler(tclStatement, TransactionOperationType.ROLLBACK, connectionSession);
        }
        if (tclStatement instanceof SetTransactionStatement && !((SetTransactionStatement) tclStatement).isDesiredScope(OperationScope.GLOBAL)) {
            return new SetTransactionHandler((SetTransactionStatement) tclStatement, connectionSession, sqlStatementContext.getDatabaseType());
        }
        if (tclStatement instanceof XAStatement) {
            return new XATCLHandler(sqlStatementContext, sql, connectionSession);
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList(), new HintValueContext(), connectionSession.getConnectionContext(),
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData());
        return DatabaseConnectorFactory.getInstance().newInstance(queryContext, connectionSession.getDatabaseConnectionManager(), false);
    }
}

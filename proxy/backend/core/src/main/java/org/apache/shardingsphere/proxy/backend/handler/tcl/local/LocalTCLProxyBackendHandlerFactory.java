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

package org.apache.shardingsphere.proxy.backend.handler.tcl.local;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.BeginTransactionProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.CommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.ReleaseSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.RollbackProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.RollbackSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetAutoCommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetSavepointProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.type.SetTransactionProxyBackendHandler;
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

/**
 * Local TCL proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalTCLProxyBackendHandlerFactory {
    
    /**
     * New instance of local TCL proxy backend handler.
     *
     * @param queryContext query context
     * @param connectionSession connection session
     * @return created instance
     */
    public static ProxyBackendHandler newInstance(final QueryContext queryContext, final ConnectionSession connectionSession) {
        TCLStatement sqlStatement = (TCLStatement) queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof BeginTransactionStatement) {
            return new BeginTransactionProxyBackendHandler(sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof SetAutoCommitStatement) {
            return new SetAutoCommitProxyBackendHandler(sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof CommitStatement) {
            return new CommitProxyBackendHandler(sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof RollbackStatement) {
            return ((RollbackStatement) sqlStatement).getSavepointName().isPresent()
                    ? new RollbackSavepointProxyBackendHandler(sqlStatement, connectionSession)
                    : new RollbackProxyBackendHandler(sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof SavepointStatement) {
            return new SetSavepointProxyBackendHandler(sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof ReleaseSavepointStatement) {
            return new ReleaseSavepointProxyBackendHandler(sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof SetTransactionStatement && !((SetTransactionStatement) sqlStatement).isDesiredScope(OperationScope.GLOBAL)) {
            return new SetTransactionProxyBackendHandler((SetTransactionStatement) sqlStatement, connectionSession);
        }
        return DatabaseProxyConnectorFactory.newInstance(queryContext, connectionSession.getDatabaseConnectionManager(), false);
    }
}

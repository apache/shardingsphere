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

package org.apache.shardingsphere.proxy.backend.handler.tcl.xa;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XABeginProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XACommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XAOtherOperationProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XARecoveryProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XARollbackProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XABeginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARecoveryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAStatement;

/**
 * XA TCL proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XATCLProxyBackendHandlerFactory {
    
    /**
     * New instance of XA TCL proxy backend handler.
     *
     * @param queryContext query context
     * @param connectionSession connection session
     * @return created instance
     */
    public static ProxyBackendHandler newInstance(final QueryContext queryContext, final ConnectionSession connectionSession) {
        XAStatement sqlStatement = (XAStatement) queryContext.getSqlStatementContext().getSqlStatement();
        DatabaseProxyConnector databaseProxyConnector = DatabaseProxyConnectorFactory.newInstance(queryContext, connectionSession.getDatabaseConnectionManager(), false);
        if (sqlStatement instanceof XARecoveryStatement) {
            return new XARecoveryProxyBackendHandler(databaseProxyConnector);
        }
        if (sqlStatement instanceof XABeginStatement) {
            return new XABeginProxyBackendHandler(queryContext.getMetaData(), connectionSession, databaseProxyConnector);
        }
        if (sqlStatement instanceof XACommitStatement) {
            return new XACommitProxyBackendHandler(connectionSession, databaseProxyConnector);
        }
        if (sqlStatement instanceof XARollbackStatement) {
            return new XARollbackProxyBackendHandler(connectionSession, databaseProxyConnector);
        }
        return new XAOtherOperationProxyBackendHandler(databaseProxyConnector);
    }
}

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
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.local.LocalTCLProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.XATCLProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAStatement;

/**
 * TCL proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TCLProxyBackendHandlerFactory {
    
    /**
     * New instance of TCL proxy backend handler.
     *
     * @param sqlStatementContext SQL statement context
     * @param sql SQL
     * @param connectionSession connection session
     * @return created instance
     */
    public static ProxyBackendHandler newInstance(final SQLStatementContext sqlStatementContext, final String sql, final ConnectionSession connectionSession) {
        return sqlStatementContext.getSqlStatement() instanceof XAStatement
                ? XATCLProxyBackendHandlerFactory.newInstance(sqlStatementContext, sql, connectionSession)
                : LocalTCLProxyBackendHandlerFactory.newInstance(sqlStatementContext, sql, connectionSession);
    }
}

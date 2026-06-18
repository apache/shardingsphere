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

package org.apache.shardingsphere.proxy.backend.handler.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.RALStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

/**
 * DistSQL proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLProxyBackendHandlerFactory {
    
    /**
     * Create new instance of DistSQL proxy backend handler.
     *
     * @param sqlStatement DistSQL statement
     * @param queryContext query context
     * @param connectionSession connection session
     * @return proxy backend handler
     * @throws UnsupportedSQLOperationException unsupported SQL operation exception
     */
    public static ProxyBackendHandler newInstance(final DistSQLStatement sqlStatement, final QueryContext queryContext, final ConnectionSession connectionSession) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        if (sqlStatement instanceof RQLStatement || sqlStatement instanceof RULStatement) {
            return new DistSQLQueryProxyBackendHandler(sqlStatement, queryContext, connectionSession, contextManager);
        }
        if (sqlStatement instanceof RDLStatement) {
            return new DistSQLUpdateProxyBackendHandler(sqlStatement, queryContext, connectionSession, contextManager);
        }
        if (sqlStatement instanceof RALStatement) {
            return sqlStatement instanceof QueryableRALStatement
                    ? new DistSQLQueryProxyBackendHandler(sqlStatement, queryContext, connectionSession, contextManager)
                    : new DistSQLUpdateProxyBackendHandler(sqlStatement, queryContext, connectionSession, contextManager);
        }
        throw new UnsupportedSQLOperationException(sqlStatement.getClass().getName());
    }
}

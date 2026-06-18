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

package org.apache.shardingsphere.proxy.backend.handler.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.data.type.UnicastDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.DatabaseSelectRequiredSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Database proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseProxyBackendHandlerFactory {
    
    /**
     * New instance of database proxy backend handler.
     *
     * @param queryContext query context
     * @param connectionSession connection session
     * @param preferPreparedStatement use prepared statement as possible
     * @return created instance
     */
    public static DatabaseProxyBackendHandler newInstance(final QueryContext queryContext, final ConnectionSession connectionSession, final boolean preferPreparedStatement) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof SetStatement && null == connectionSession.getUsedDatabaseName()) {
            return () -> new UpdateResponseHeader(sqlStatement);
        }
        if (sqlStatement instanceof DoStatement || isNotDatabaseSelectRequiredDALStatement(sqlStatement) || isNotContainFromSelectStatement(sqlStatement)) {
            return new UnicastDatabaseProxyBackendHandler(queryContext, ProxyContext.getInstance().getContextManager(), connectionSession);
        }
        return DatabaseProxyConnectorFactory.newInstance(queryContext, connectionSession.getDatabaseConnectionManager(), preferPreparedStatement);
    }
    
    private static boolean isNotDatabaseSelectRequiredDALStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof DALStatement && !sqlStatement.getAttributes().findAttribute(DatabaseSelectRequiredSQLStatementAttribute.class).isPresent();
    }
    
    private static boolean isNotContainFromSelectStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && !((SelectStatement) sqlStatement).getFrom().isPresent();
    }
}

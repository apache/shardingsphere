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

import io.vertx.core.Future;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.data.impl.UnicastDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * Database backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseBackendHandlerFactory {
    
    /**
     * New instance of database backend handler.
     *
     * @param queryContext Logic SQL
     * @param connectionSession connection session
     * @param preferPreparedStatement use prepared statement as possible
     * @return created instance
     */
    public static DatabaseBackendHandler newInstance(final QueryContext queryContext, final ConnectionSession connectionSession, final boolean preferPreparedStatement) {
        SQLStatementContext<?> sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof DoStatement) {
            return new UnicastDatabaseBackendHandler(queryContext, connectionSession);
        }
        if (sqlStatement instanceof SetStatement && null == connectionSession.getDatabaseName()) {
            return new DatabaseBackendHandler() {
                
                @Override
                public Future<ResponseHeader> executeFuture() {
                    return Future.succeededFuture(new UpdateResponseHeader(sqlStatement));
                }
                
                @Override
                public ResponseHeader execute() {
                    return new UpdateResponseHeader(sqlStatement);
                }
            };
        }
        if (sqlStatement instanceof DALStatement || (sqlStatement instanceof SelectStatement && null == ((SelectStatement) sqlStatement).getFrom())) {
            return new UnicastDatabaseBackendHandler(queryContext, connectionSession);
        }
        return DatabaseCommunicationEngineFactory.getInstance().newDatabaseCommunicationEngine(queryContext, connectionSession.getBackendConnection(), preferPreparedStatement);
    }
}

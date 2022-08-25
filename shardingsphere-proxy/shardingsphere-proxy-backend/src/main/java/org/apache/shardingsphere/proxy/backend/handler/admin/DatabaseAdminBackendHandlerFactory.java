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

package org.apache.shardingsphere.proxy.backend.handler.admin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreatorFactory;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;

import java.util.Optional;

/**
 * Database admin backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAdminBackendHandlerFactory {
    
    /**
     * Create new instance of database admin backend handler,
     * and this handler requires a connection containing a schema to be used.
     *
     * @param databaseType database type
     * @param sqlStatementContext SQL statement context
     * @param connectionSession connection session
     * @return created instance
     */
    public static Optional<ProxyBackendHandler> newInstance(final DatabaseType databaseType, final SQLStatementContext<?> sqlStatementContext, final ConnectionSession connectionSession) {
        Optional<DatabaseAdminExecutorCreator> creator = DatabaseAdminExecutorCreatorFactory.findInstance(databaseType);
        if (!creator.isPresent()) {
            return Optional.empty();
        }
        Optional<DatabaseAdminExecutor> executor = creator.get().create(sqlStatementContext);
        return executor.map(optional -> createProxyBackendHandler(sqlStatementContext, connectionSession, optional));
    }
    
    /**
     * Create new instance of database admin backend handler.
     *
     * @param databaseType database type
     * @param sqlStatementContext SQL statement context
     * @param connectionSession connection session
     * @param sql SQL being executed
     * @return created instance
     */
    public static Optional<ProxyBackendHandler> newInstance(final DatabaseType databaseType, final SQLStatementContext<?> sqlStatementContext,
                                                            final ConnectionSession connectionSession, final String sql) {
        Optional<DatabaseAdminExecutorCreator> executorFactory = DatabaseAdminExecutorCreatorFactory.findInstance(databaseType);
        if (!executorFactory.isPresent()) {
            return Optional.empty();
        }
        Optional<DatabaseAdminExecutor> executor = executorFactory.get().create(sqlStatementContext, sql, connectionSession.getDatabaseName());
        return executor.map(optional -> createProxyBackendHandler(sqlStatementContext, connectionSession, optional));
    }
    
    private static ProxyBackendHandler createProxyBackendHandler(final SQLStatementContext<?> sqlStatementContext,
                                                                 final ConnectionSession connectionSession, final DatabaseAdminExecutor executor) {
        if (executor instanceof DatabaseAdminQueryExecutor) {
            return new DatabaseAdminQueryBackendHandler(connectionSession, (DatabaseAdminQueryExecutor) executor);
        }
        return new DatabaseAdminUpdateBackendHandler(connectionSession, sqlStatementContext.getSqlStatement(), executor);
    }
}

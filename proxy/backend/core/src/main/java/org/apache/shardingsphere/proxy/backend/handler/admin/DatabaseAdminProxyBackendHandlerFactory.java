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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.List;
import java.util.Optional;

/**
 * Database admin proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAdminProxyBackendHandlerFactory {
    
    /**
     * Create new instance of database admin backend handler.
     *
     * @param databaseType database type
     * @param sqlStatementContext SQL statement context
     * @param connectionSession connection session
     * @param sql SQL being executed
     * @param parameters parameters
     * @return created instance
     */
    public static Optional<ProxyBackendHandler> newInstance(final DatabaseType databaseType, final SQLStatementContext sqlStatementContext, final ConnectionSession connectionSession,
                                                            final String sql, final List<Object> parameters) {
        Optional<DatabaseAdminExecutorCreator> executorCreator = DatabaseTypedSPILoader.findService(DatabaseAdminExecutorCreator.class, databaseType);
        if (!executorCreator.isPresent()) {
            return Optional.empty();
        }
        Optional<DatabaseAdminExecutor> executor = executorCreator.get().create(sqlStatementContext, sql, connectionSession.getUsedDatabaseName(), parameters);
        return executor.map(optional -> createProxyBackendHandler(sqlStatementContext, connectionSession, optional));
    }
    
    private static ProxyBackendHandler createProxyBackendHandler(final SQLStatementContext sqlStatementContext, final ConnectionSession connectionSession, final DatabaseAdminExecutor executor) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        return executor instanceof DatabaseAdminQueryExecutor
                ? new DatabaseAdminQueryProxyBackendHandler(contextManager, connectionSession, (DatabaseAdminQueryExecutor) executor)
                : new DatabaseAdminUpdateProxyBackendHandler(contextManager, connectionSession, sqlStatementContext.getSqlStatement(), executor);
    }
}

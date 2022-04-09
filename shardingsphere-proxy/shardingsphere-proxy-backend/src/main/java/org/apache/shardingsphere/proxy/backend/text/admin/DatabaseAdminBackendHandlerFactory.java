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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutorFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseSetCharsetExecutor;
import org.apache.shardingsphere.proxy.backend.text.encoding.DatabaseSetCharsetBackendHandler;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.Optional;

/**
 * Database admin backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAdminBackendHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseAdminExecutorFactory.class);
    }
    
    /**
     * Create new instance of database admin backend handler,
     * and this handler requires a connection containing a schema to be used.
     *
     * @param databaseType database type
     * @param sqlStatementContext SQL statement context
     * @param connectionSession connection session
     * @return new instance of database admin backend handler
     */
    public static Optional<TextProtocolBackendHandler> newInstance(final DatabaseType databaseType, final SQLStatementContext<?> sqlStatementContext, final ConnectionSession connectionSession) {
        Optional<DatabaseAdminExecutorFactory> executorFactory = TypedSPIRegistry.findRegisteredService(DatabaseAdminExecutorFactory.class, databaseType.getName());
        if (!executorFactory.isPresent()) {
            return Optional.empty();
        }
        Optional<DatabaseAdminExecutor> executor = executorFactory.get().newInstance(sqlStatementContext);
        return executor.map(optional -> createTextProtocolBackendHandler(sqlStatementContext, connectionSession, optional));
    }
    
    /**
     * Create new instance of database admin backend handler.
     *
     * @param databaseType database type
     * @param sqlStatementContext SQL statement context
     * @param connectionSession connection session
     * @param sql SQL being executed
     * @return new instance of database admin backend handler
     */
    public static Optional<TextProtocolBackendHandler> newInstance(final DatabaseType databaseType, final SQLStatementContext<?> sqlStatementContext,
                                                                   final ConnectionSession connectionSession, final String sql) {
        Optional<DatabaseAdminExecutorFactory> executorFactory = TypedSPIRegistry.findRegisteredService(DatabaseAdminExecutorFactory.class, databaseType.getName());
        if (!executorFactory.isPresent()) {
            return Optional.empty();
        }
        Optional<DatabaseAdminExecutor> executor = executorFactory.get().newInstance(sqlStatementContext, sql, connectionSession.getSchemaName());
        return executor.map(optional -> createTextProtocolBackendHandler(sqlStatementContext, connectionSession, optional));
    }
    
    private static TextProtocolBackendHandler createTextProtocolBackendHandler(final SQLStatementContext<?> sqlStatementContext, 
                                                                               final ConnectionSession connectionSession, final DatabaseAdminExecutor executor) {
        if (executor instanceof DatabaseAdminQueryExecutor) {
            return new DatabaseAdminQueryBackendHandler(connectionSession, (DatabaseAdminQueryExecutor) executor);
        }
        if (executor instanceof DatabaseSetCharsetExecutor) {
            return new DatabaseSetCharsetBackendHandler(connectionSession, (DatabaseSetCharsetExecutor) executor);
        }
        return new DatabaseAdminUpdateBackendHandler(connectionSession, sqlStatementContext.getSqlStatement(), executor);
    }
}

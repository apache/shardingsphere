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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutorFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;
import java.util.Properties;

/**
 * Database admin backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAdminBackendHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseAdminExecutorFactory.class);
    }
    
    /**
     * Create new instance of database admin backend handler. 
     * 
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param backendConnection backend connection
     * @return new instance of database admin backend handler
     */
    public static Optional<TextProtocolBackendHandler> newInstance(final DatabaseType databaseType, final SQLStatement sqlStatement, final BackendConnection backendConnection) {
        Optional<DatabaseAdminExecutorFactory> executorFactory = TypedSPIRegistry.findRegisteredService(DatabaseAdminExecutorFactory.class, databaseType.getName(), new Properties());
        if (!executorFactory.isPresent()) {
            return Optional.empty();
        }
        Optional<DatabaseAdminExecutor> executor = executorFactory.get().newInstance(backendConnection.getSchemaName(), sqlStatement);
        return executor.map(optional -> createTextProtocolBackendHandler(sqlStatement, backendConnection, optional));
    }
    
    private static TextProtocolBackendHandler createTextProtocolBackendHandler(final SQLStatement sqlStatement, final BackendConnection backendConnection, final DatabaseAdminExecutor executor) {
        return executor instanceof DatabaseAdminQueryExecutor
                ? new DatabaseAdminQueryBackendHandler(backendConnection, (DatabaseAdminQueryExecutor) executor) : new DatabaseAdminUpdateBackendHandler(backendConnection, sqlStatement, executor);
    }
}

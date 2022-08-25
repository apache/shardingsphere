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

package org.apache.shardingsphere.proxy.backend.communication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

/**
 * Database communication engine factory.
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseCommunicationEngineFactory {
    
    private static final DatabaseCommunicationEngineFactory INSTANCE = new DatabaseCommunicationEngineFactory();
    
    /**
     * Get backend handler factory instance.
     *
     * @return backend handler factory
     */
    public static DatabaseCommunicationEngineFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of {@link DatabaseCommunicationEngine}.
     *
     * @param <T> type of DatabaseCommunicationEngine
     * @param queryContext query context
     * @param backendConnection backend connection
     * @param preferPreparedStatement use prepared statement as possible
     * @return created instance
     */
    public <T extends DatabaseCommunicationEngine> T newDatabaseCommunicationEngine(final QueryContext queryContext, final BackendConnection<?> backendConnection,
                                                                                    final boolean preferPreparedStatement) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
        T result;
        if (backendConnection instanceof JDBCBackendConnection) {
            JDBCBackendConnection jdbcBackendConnection = (JDBCBackendConnection) backendConnection;
            String driverType = preferPreparedStatement || !queryContext.getParameters().isEmpty() ? JDBCDriverType.PREPARED_STATEMENT : JDBCDriverType.STATEMENT;
            result = (T) new JDBCDatabaseCommunicationEngine(driverType, database, queryContext, jdbcBackendConnection);
            jdbcBackendConnection.add(result);
        } else {
            VertxBackendConnection vertxBackendConnection = (VertxBackendConnection) backendConnection;
            result = (T) new VertxDatabaseCommunicationEngine(database, queryContext, vertxBackendConnection);
        }
        return result;
    }
}

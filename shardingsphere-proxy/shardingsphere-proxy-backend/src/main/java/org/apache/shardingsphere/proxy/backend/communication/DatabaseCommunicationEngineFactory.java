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
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Collections;
import java.util.List;

/**
 * Database communication engine factory.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
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
     * Create new instance of text protocol backend handler.
     *
     * @param <T> type of DatabaseCommunicationEngine
     * @param sqlStatementContext SQL statement context
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @return created instance
     */
    public <T extends DatabaseCommunicationEngine> T newTextProtocolInstance(final SQLStatementContext<?> sqlStatementContext, final String sql, final BackendConnection<?> backendConnection) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
        T result;
        if (backendConnection instanceof JDBCBackendConnection) {
            JDBCBackendConnection jdbcBackendConnection = (JDBCBackendConnection) backendConnection;
            result = (T) new JDBCDatabaseCommunicationEngine(JDBCDriverType.STATEMENT, database, logicSQL, jdbcBackendConnection);
            jdbcBackendConnection.add((JDBCDatabaseCommunicationEngine) result);
        } else {
            VertxBackendConnection vertxBackendConnection = (VertxBackendConnection) backendConnection;
            result = (T) new VertxDatabaseCommunicationEngine(database, logicSQL, vertxBackendConnection);
        }
        return result;
    }
    
    /**
     * Create new instance of binary protocol backend handler.
     *
     * @param <T> type of DatabaseCommunicationEngine
     * @param sqlStatementContext SQL statement context
     * @param sql SQL to be executed
     * @param parameters SQL parameters
     * @param backendConnection backend connection
     * @return created instance
     */
    public <T extends DatabaseCommunicationEngine> T newBinaryProtocolInstance(final SQLStatementContext<?> sqlStatementContext,
                                                                               final String sql, final List<Object> parameters, final BackendConnection<?> backendConnection) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, sql, parameters);
        T result;
        if (backendConnection instanceof JDBCBackendConnection) {
            JDBCBackendConnection jdbcBackendConnection = (JDBCBackendConnection) backendConnection;
            result = (T) new JDBCDatabaseCommunicationEngine(JDBCDriverType.PREPARED_STATEMENT, database, logicSQL, jdbcBackendConnection);
            jdbcBackendConnection.add((JDBCDatabaseCommunicationEngine) result);
        } else {
            VertxBackendConnection vertxBackendConnection = (VertxBackendConnection) backendConnection;
            result = (T) new VertxDatabaseCommunicationEngine(database, logicSQL, vertxBackendConnection);
        }
        return result;
    }
}

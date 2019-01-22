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

package org.apache.shardingsphere.shardingproxy.backend.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.JDBCDatabaseAccessEngine;
import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.execute.JDBCExecuteEngine;
import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.wrapper.PreparedStatementExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.wrapper.StatementExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.engine.netty.NettyDatabaseAccessEngine;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;

import java.util.List;

/**
 * Database access engine factory.
 *
 * @author zhangliang
 * @author panjuan
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAccessEngineFactory {
    
    private static final DatabaseAccessEngineFactory INSTANCE = new DatabaseAccessEngineFactory();
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    /**
     * Get backend handler factory instance.
     *
     * @return backend handler factory
     */
    public static DatabaseAccessEngineFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param logicSchema logic schema
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public DatabaseAccessEngine newTextProtocolInstance(
            final LogicSchema logicSchema, final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        return GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)
                ? new NettyDatabaseAccessEngine(logicSchema, backendConnection.getConnectionId(), sequenceId, sql, databaseType)
                : new JDBCDatabaseAccessEngine(logicSchema, sql, new JDBCExecuteEngine(backendConnection, new StatementExecutorWrapper(logicSchema)));
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param logicSchema logic schema
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param parameters SQL parameters
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public DatabaseAccessEngine newBinaryProtocolInstance(
            final LogicSchema logicSchema, final int sequenceId, final String sql, final List<Object> parameters, final BackendConnection backendConnection, final DatabaseType databaseType) {
        return GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)
                ? new NettyDatabaseAccessEngine(logicSchema, backendConnection.getConnectionId(), sequenceId, sql, databaseType)
                : new JDBCDatabaseAccessEngine(logicSchema, sql, new JDBCExecuteEngine(backendConnection, new PreparedStatementExecutorWrapper(logicSchema, parameters)));
    }
}

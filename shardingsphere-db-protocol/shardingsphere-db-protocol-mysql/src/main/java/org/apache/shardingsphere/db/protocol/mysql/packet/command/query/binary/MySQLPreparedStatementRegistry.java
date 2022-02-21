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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MySQL prepared statement registry.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class MySQLPreparedStatementRegistry {
    
    private static final MySQLPreparedStatementRegistry INSTANCE = new MySQLPreparedStatementRegistry();
    
    private final ConcurrentMap<Integer, MySQLConnectionPreparedStatements> connectionRegistry = new ConcurrentHashMap<>(8192, 1);
    
    /**
     * Get prepared statement registry instance.
     *
     * @return prepared statement registry instance
     */
    public static MySQLPreparedStatementRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new MySQLConnectionPreparedStatements());
    }
    
    /**
     * Get connection prepared statements.
     * 
     * @param connectionId connection ID
     * @return MySQL connection prepared statements
     */
    public MySQLConnectionPreparedStatements getConnectionPreparedStatements(final int connectionId) {
        return connectionRegistry.get(connectionId);
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
    }
    
    public static class MySQLConnectionPreparedStatements {
        
        private final Map<Integer, MySQLPreparedStatement> preparedStatements = new ConcurrentHashMap<>(16384, 1);
        
        private final AtomicInteger sequence = new AtomicInteger();
        
        /**
         * Prepare statement.
         *
         * @param sql SQL
         * @param parameterCount parameter count
         * @return statement ID
         */
        public int prepareStatement(final String sql, final int parameterCount) {
            int result = sequence.incrementAndGet();
            preparedStatements.put(result, new MySQLPreparedStatement(sql, parameterCount));
            return result;
        }
        
        /**
         * Get prepared statement.
         *
         * @param statementId statement ID
         * @return prepared statement
         */
        public MySQLPreparedStatement get(final int statementId) {
            return preparedStatements.get(statementId);
        }
        
        /**
         * Close statement.
         *
         * @param statementId statement ID
         */
        public void closeStatement(final int statementId) {
            preparedStatements.remove(statementId);
        }
    }
}

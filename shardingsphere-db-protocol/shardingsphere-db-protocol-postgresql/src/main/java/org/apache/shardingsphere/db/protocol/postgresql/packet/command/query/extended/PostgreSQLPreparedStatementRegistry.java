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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PostgreSQL prepared statement registry.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class PostgreSQLPreparedStatementRegistry {
    
    private static final PostgreSQLPreparedStatementRegistry INSTANCE = new PostgreSQLPreparedStatementRegistry();
    
    private final ConcurrentMap<Integer, PostgreSQLConnectionPreparedStatementRegistry> connectionPreparedStatements = new ConcurrentHashMap<>(65535, 1);
    
    /**
     * Get prepared statement registry instance.
     *
     * @return prepared statement registry instance
     */
    public static PostgreSQLPreparedStatementRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register.
     *
     * @param connectionId connection ID
     */
    public void register(final int connectionId) {
        connectionPreparedStatements.put(connectionId, new PostgreSQLConnectionPreparedStatementRegistry());
    }
    
    /**
     * Register.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param sql SQL
     * @param sqlStatement sql statement
     * @param columnTypes column types
     */
    public void register(final int connectionId, final String statementId, final String sql, final SQLStatement sqlStatement, final List<PostgreSQLColumnType> columnTypes) {
        connectionPreparedStatements.get(connectionId).getPreparedStatements().put(statementId, new PostgreSQLPreparedStatement(sql, sqlStatement, columnTypes));
    }
    
    /**
     * Get prepared statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return prepared statement
     */
    public PostgreSQLPreparedStatement get(final int connectionId, final String statementId) {
        return connectionPreparedStatements.get(connectionId).preparedStatements.getOrDefault(statementId, new PostgreSQLPreparedStatement("", new EmptyStatement(), Collections.emptyList()));
    }
    
    /**
     * Unregister.
     *
     * @param connectionId connection ID
     */
    public void unregister(final int connectionId) {
        connectionPreparedStatements.remove(connectionId);
    }
    
    /**
     * Unregister.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregister(final int connectionId, final String statementId) {
        if (connectionPreparedStatements.containsKey(connectionId)) {
            connectionPreparedStatements.get(connectionId).getPreparedStatements().remove(statementId);
        }
    }
    
    @Getter
    private final class PostgreSQLConnectionPreparedStatementRegistry {
        
        private final ConcurrentMap<String, PostgreSQLPreparedStatement> preparedStatements = new ConcurrentHashMap<>(65535, 1);
    }
}

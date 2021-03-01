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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Connection scope binary statement registry.
 */
public final class ConnectionScopeBinaryStatementRegistry {
    
    private final ConcurrentMap<String, PostgreSQLBinaryStatement> binaryStatements = new ConcurrentHashMap<>(65535, 1);
    
    /**
     * Register SQL.
     *
     * @param statementId statement ID
     * @param sql SQL
     * @param parameterCount parameter count
     * @param binaryStatementParameterTypes binary statement parameter types
     */
    public void register(final String statementId, final String sql, final int parameterCount, final List<PostgreSQLBinaryStatementParameterType> binaryStatementParameterTypes) {
        binaryStatements.put(statementId, new PostgreSQLBinaryStatement(sql, parameterCount, binaryStatementParameterTypes));
    }
    
    /**
     * Get binary prepared statement.
     *
     * @param statementId statement ID
     * @return binary prepared statement
     */
    public PostgreSQLBinaryStatement getBinaryStatement(final String statementId) {
        return binaryStatements.get(statementId);
    }
}

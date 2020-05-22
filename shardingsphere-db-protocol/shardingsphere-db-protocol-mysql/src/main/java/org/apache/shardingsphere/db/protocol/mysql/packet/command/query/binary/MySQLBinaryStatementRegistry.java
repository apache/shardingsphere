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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MySQL binary prepared statement registry.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class MySQLBinaryStatementRegistry {
    
    private static final MySQLBinaryStatementRegistry INSTANCE = new MySQLBinaryStatementRegistry();
    
    private final ConcurrentMap<String, Integer> statementIdAssigner = new ConcurrentHashMap<>(65535, 1);
    
    private final ConcurrentMap<Integer, MySQLBinaryStatement> binaryStatements = new ConcurrentHashMap<>(65535, 1);
    
    private final AtomicInteger sequence = new AtomicInteger();
    
    /**
     * Get prepared statement registry instance.
     *
     * @return prepared statement registry instance
     */
    public static MySQLBinaryStatementRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register SQL.
     *
     * @param sql SQL
     * @param parametersCount parameters count
     * @return statement ID
     */
    public synchronized int register(final String sql, final int parametersCount) {
        Integer result = statementIdAssigner.get(sql);
        if (null != result) {
            return result;
        }
        result = sequence.incrementAndGet();
        statementIdAssigner.putIfAbsent(sql, result);
        binaryStatements.putIfAbsent(result, new MySQLBinaryStatement(sql, parametersCount));
        return result;
    }
    
    /**
     * Get binary prepared statement.
     *
     * @param statementId statement ID
     * @return binary prepared statement
     */
    public MySQLBinaryStatement getBinaryStatement(final int statementId) {
        return binaryStatements.get(statementId);
    }
    
    /**
     * Remove expired cache statement.
     *
     * @param statementId statement ID
     */
    public synchronized void remove(final int statementId) {
        MySQLBinaryStatement binaryStatement = getBinaryStatement(statementId);
        if (null != binaryStatement) {
            statementIdAssigner.remove(binaryStatement.getSql());
            binaryStatements.remove(statementId);
        }
    }
}

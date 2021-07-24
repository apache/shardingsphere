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
     * Register.
     *
     * @param sql SQL
     * @param parameterCount parameter count
     * @return statement ID
     */
    public synchronized int register(final String sql, final int parameterCount) {
        Integer result = statementIdAssigner.get(sql);
        if (null != result) {
            return result;
        }
        result = sequence.incrementAndGet();
        statementIdAssigner.putIfAbsent(sql, result);
        binaryStatements.putIfAbsent(result, new MySQLBinaryStatement(sql, parameterCount));
        return result;
    }
    
    /**
     * Get binary statement.
     *
     * @param statementId statement ID
     * @return binary prepared statement
     */
    public MySQLBinaryStatement get(final int statementId) {
        return binaryStatements.get(statementId);
    }
    
    /**
     * Unregister.
     *
     * @param statementId statement ID
     */
    public synchronized void unregister(final int statementId) {
        if (binaryStatements.containsKey(statementId)) {
            statementIdAssigner.remove(binaryStatements.get(statementId).getSql());
            binaryStatements.remove(statementId);
        }
    }
}

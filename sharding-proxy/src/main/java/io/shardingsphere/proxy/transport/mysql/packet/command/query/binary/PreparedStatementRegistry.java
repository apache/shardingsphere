/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary;

import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.execute.PreparedStatementParameterHeader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prepared statement registry.
 *
 * @author zhangliang
 * @author zhangyonglun
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class PreparedStatementRegistry {
    
    private static final PreparedStatementRegistry INSTANCE = new PreparedStatementRegistry();
    
    private final ConcurrentMap<String, Integer> sqlToStatementIdMap = new ConcurrentHashMap<>(65535, 1);
    
    private final ConcurrentMap<Integer, PreparedStatementUnit> statementIdToPreparedStatementUnitMap = new ConcurrentHashMap<>(65535, 1);
    
    private final AtomicInteger sequence = new AtomicInteger();
    
    /**
     * Get prepared statement registry instance.
     * 
     * @return prepared statement registry instance
     */
    public static PreparedStatementRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register SQL.
     * 
     * @param sql SQL
     * @return statement ID
     */
    public int register(final String sql) {
        int statementId = sequence.incrementAndGet();
        Integer previousStatementId = sqlToStatementIdMap.putIfAbsent(sql, statementId);
        if (null == previousStatementId) {
            statementIdToPreparedStatementUnitMap.putIfAbsent(statementId, new PreparedStatementUnit(sql));
        } else {
            return previousStatementId;
        }
        return statementId;
    }
    
    /**
     * Get SQL.
     *
     * @param statementId statement ID
     * @return SQL
     */
    public String getSQL(final int statementId) {
        return statementIdToPreparedStatementUnitMap.get(statementId).getSql();
    }
    
    /**
     * Set parameter headers.
     *
     * @param statementId statement ID
     * @param preparedStatementParameterHeaders prepared statement parameter headers
     */
    public void setParameterHeaders(final int statementId, final List<PreparedStatementParameterHeader> preparedStatementParameterHeaders) {
        statementIdToPreparedStatementUnitMap.get(statementId).setPreparedStatementParameterHeaders(preparedStatementParameterHeaders);
    }
    
    /**
     * Get parameter header.
     *
     * @param statementId statement ID
     * @return prepared statement parameters
     */
    public List<PreparedStatementParameterHeader> getParameterHeader(final int statementId) {
        return statementIdToPreparedStatementUnitMap.get(statementId).getPreparedStatementParameterHeaders();
    }
}

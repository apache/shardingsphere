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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatement;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Binary prepared statement for MySQL.
 * This class may be accessed serially in different threads due to MySQL Proxy using a shared unbound thread pool.
 */
@RequiredArgsConstructor
@Getter
public final class MySQLServerPreparedStatement implements ServerPreparedStatement {
    
    private static final int MAX_LONG_DATA_LENGTH = 64 * 1024 * 1024;
    
    private static final int ER_UNKNOWN_ERROR = 1105;
    
    private static final int INITIAL_LONG_DATA_CAPACITY = 16;
    
    private static final String LONG_DATA_TOO_LARGE_MESSAGE =
            "Parameter of prepared statement which is set through mysql_send_long_data() is longer than 'max_allowed_packet' bytes";
    
    private final String sql;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final HintValueContext hintValueContext;
    
    private final List<MySQLBinaryColumnType> parameterColumnTypes = new CopyOnWriteArrayList<>();
    
    private final List<MySQLPreparedStatementParameterType> parameterTypes = new CopyOnWriteArrayList<>();
    
    @Getter(AccessLevel.NONE)
    private final Map<Integer, LongData> longData = new HashMap<>(INITIAL_LONG_DATA_CAPACITY);
    
    /**
     * Volatile writes publish serial command changes when subsequent commands run on different executor threads.
     */
    @Getter(AccessLevel.NONE)
    private volatile boolean longDataTooLarge;
    
    void appendLongData(final int paramIndex, final byte[] data) {
        if (longDataTooLarge) {
            return;
        }
        LongData parameterLongData = longData.get(paramIndex);
        if (null == parameterLongData) {
            parameterLongData = new LongData();
            longData.put(paramIndex, parameterLongData);
        }
        if (parameterLongData.append(data)) {
            longDataTooLarge = false;
        } else {
            longData.clear();
            longDataTooLarge = true;
        }
    }
    
    /**
     * Get indexes of parameters set through COM_STMT_SEND_LONG_DATA.
     *
     * @return indexes of parameters set through COM_STMT_SEND_LONG_DATA; the returned set must not be modified
     * @throws SQLException SQL exception when accumulated long data is too large
     */
    public Set<Integer> getLongDataIndexes() throws SQLException {
        if (longDataTooLarge) {
            throw new SQLException(LONG_DATA_TOO_LARGE_MESSAGE, XOpenSQLState.GENERAL_ERROR.getValue(), ER_UNKNOWN_ERROR);
        }
        return longData.isEmpty() ? Collections.emptySet() : longData.keySet();
    }
    
    /**
     * Apply accumulated long data to parameters.
     *
     * @param params parameters
     * @param charset character set
     */
    public void applyLongData(final List<Object> params, final Charset charset) {
        for (Entry<Integer, LongData> entry : longData.entrySet()) {
            byte[] value = entry.getValue().getValue();
            params.set(entry.getKey(), isCharacterParameter(entry.getKey()) ? new String(value, charset) : value);
        }
    }
    
    private boolean isCharacterParameter(final int paramIndex) {
        if (paramIndex >= parameterColumnTypes.size()) {
            return false;
        }
        MySQLBinaryColumnType columnType = parameterColumnTypes.get(paramIndex);
        return MySQLBinaryColumnType.STRING == columnType || MySQLBinaryColumnType.VAR_STRING == columnType || MySQLBinaryColumnType.VARCHAR == columnType;
    }
    
    /**
     * Clear accumulated long data and its error state.
     */
    public void clearLongData() {
        longData.clear();
        longDataTooLarge = false;
    }
    
    private static final class LongData {
        
        private byte[] firstChunk;
        
        private Collection<byte[]> chunks;
        
        private int length;
        
        private boolean append(final byte[] data) {
            if (data.length > MAX_LONG_DATA_LENGTH - length) {
                return false;
            }
            if (0 == length) {
                firstChunk = data;
            } else if (data.length > 0) {
                if (null == chunks) {
                    chunks = new ArrayDeque<>(2);
                    chunks.add(firstChunk);
                }
                chunks.add(data);
            }
            length += data.length;
            return true;
        }
        
        private byte[] getValue() {
            if (null == chunks) {
                return firstChunk;
            }
            byte[] result = new byte[length];
            int offset = 0;
            for (byte[] each : chunks) {
                System.arraycopy(each, 0, result, offset, each.length);
                offset += each.length;
            }
            return result;
        }
    }
}

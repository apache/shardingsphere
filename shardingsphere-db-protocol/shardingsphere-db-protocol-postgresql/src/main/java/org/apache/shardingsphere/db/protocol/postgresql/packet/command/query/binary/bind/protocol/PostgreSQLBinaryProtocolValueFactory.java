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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnType;

import java.util.HashMap;
import java.util.Map;

/**
 * Binary protocol value factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLBinaryProtocolValueFactory {
    
    private static final Map<PostgreSQLColumnType, PostgreSQLBinaryProtocolValue> BINARY_PROTOCOL_VALUES = new HashMap<>();
    
    static {
        setStringLenencBinaryProtocolValue();
        setInt8BinaryProtocolValue();
        setInt4BinaryProtocolValue();
        setInt2BinaryProtocolValue();
        setDoubleBinaryProtocolValue();
        setFloatBinaryProtocolValue();
        setDateBinaryProtocolValue();
        setTimeBinaryProtocolValue();
    }
    
    private static void setStringLenencBinaryProtocolValue() {
        PostgreSQLStringBinaryProtocolValue binaryProtocolValue = new PostgreSQLStringBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_VARCHAR, binaryProtocolValue);
    }
    
    private static void setInt8BinaryProtocolValue() {
        PostgreSQLInt8BinaryProtocolValue binaryProtocolValue = new PostgreSQLInt8BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_INT8, binaryProtocolValue);
    }
    
    private static void setInt4BinaryProtocolValue() {
        PostgreSQLInt4BinaryProtocolValue binaryProtocolValue = new PostgreSQLInt4BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, binaryProtocolValue);
    }
    
    private static void setInt2BinaryProtocolValue() {
        PostgreSQLInt2BinaryProtocolValue binaryProtocolValue = new PostgreSQLInt2BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_INT2, binaryProtocolValue);
    }
    
    private static void setDoubleBinaryProtocolValue() {
        PostgreSQLDoubleBinaryProtocolValue binaryProtocolValue = new PostgreSQLDoubleBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_FLOAT8, binaryProtocolValue);
    }
    
    private static void setFloatBinaryProtocolValue() {
        PostgreSQLFloatBinaryProtocolValue binaryProtocolValue = new PostgreSQLFloatBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_FLOAT4, binaryProtocolValue);
    }
    
    private static void setDateBinaryProtocolValue() {
        PostgreSQLDateBinaryProtocolValue binaryProtocolValue = new PostgreSQLDateBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_DATE, binaryProtocolValue);
    }
    
    private static void setTimeBinaryProtocolValue() {
        PostgreSQLTimeBinaryProtocolValue binaryProtocolValue = new PostgreSQLTimeBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.POSTGRESQL_TYPE_TIMESTAMP, binaryProtocolValue);
    }
    
    /**
     * Get binary protocol value.
     *
     * @param columnType column type
     * @return binary protocol value
     */
    public static PostgreSQLBinaryProtocolValue getBinaryProtocolValue(final PostgreSQLColumnType columnType) {
        Preconditions.checkArgument(BINARY_PROTOCOL_VALUES.containsKey(columnType), "Cannot find PostgreSQL type '%s' in column type when process binary protocol value", columnType);
        return BINARY_PROTOCOL_VALUES.get(columnType);
    }
}

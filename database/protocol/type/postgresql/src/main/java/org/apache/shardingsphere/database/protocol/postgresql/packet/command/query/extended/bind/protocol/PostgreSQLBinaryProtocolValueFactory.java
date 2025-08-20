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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;

import java.util.HashMap;
import java.util.Map;

/**
 * Binary protocol value factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLBinaryProtocolValueFactory {
    
    private static final Map<BinaryColumnType, PostgreSQLBinaryProtocolValue> BINARY_PROTOCOL_VALUES = new HashMap<>();
    
    static {
        setUnspecifiedBinaryProtocolValue();
        setStringLenencBinaryProtocolValue();
        setInt8BinaryProtocolValue();
        setInt4BinaryProtocolValue();
        setInt2BinaryProtocolValue();
        setDoubleBinaryProtocolValue();
        setFloatBinaryProtocolValue();
        setNumericBinaryProtocolValue();
        setDateBinaryProtocolValue();
        setTimeBinaryProtocolValue();
        setInt2ArrayBinaryProtocolValue();
        setInt4ArrayBinaryProtocolValue();
        setInt8ArrayBinaryProtocolValue();
        setFloat4ArrayBinaryProtocolValue();
        setFloat8ArrayBinaryProtocolValue();
        setBoolArrayBinaryProtocolValue();
        setStringArrayBinaryProtocolValue();
        setByteaBinaryProtocolValue();
        setUUIDBinaryProtocolValue();
        setBoolBinaryProtocolValue();
    }
    
    private static void setUnspecifiedBinaryProtocolValue() {
        PostgreSQLUnspecifiedBinaryProtocolValue binaryProtocolValue = new PostgreSQLUnspecifiedBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.UNSPECIFIED, binaryProtocolValue);
    }
    
    private static void setStringLenencBinaryProtocolValue() {
        PostgreSQLStringBinaryProtocolValue binaryProtocolValue = new PostgreSQLStringBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.VARCHAR, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.CHAR, binaryProtocolValue);
    }
    
    private static void setInt8BinaryProtocolValue() {
        PostgreSQLInt8BinaryProtocolValue binaryProtocolValue = new PostgreSQLInt8BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.INT8, binaryProtocolValue);
    }
    
    private static void setInt4BinaryProtocolValue() {
        PostgreSQLInt4BinaryProtocolValue binaryProtocolValue = new PostgreSQLInt4BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.INT4, binaryProtocolValue);
    }
    
    private static void setInt2BinaryProtocolValue() {
        PostgreSQLInt2BinaryProtocolValue binaryProtocolValue = new PostgreSQLInt2BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.INT2, binaryProtocolValue);
    }
    
    private static void setDoubleBinaryProtocolValue() {
        PostgreSQLDoubleBinaryProtocolValue binaryProtocolValue = new PostgreSQLDoubleBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.FLOAT8, binaryProtocolValue);
    }
    
    private static void setFloatBinaryProtocolValue() {
        PostgreSQLFloatBinaryProtocolValue binaryProtocolValue = new PostgreSQLFloatBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.FLOAT4, binaryProtocolValue);
    }
    
    private static void setNumericBinaryProtocolValue() {
        PostgreSQLNumericBinaryProtocolValue binaryProtocolValue = new PostgreSQLNumericBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.NUMERIC, binaryProtocolValue);
    }
    
    private static void setDateBinaryProtocolValue() {
        PostgreSQLDateBinaryProtocolValue binaryProtocolValue = new PostgreSQLDateBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.DATE, binaryProtocolValue);
    }
    
    private static void setTimeBinaryProtocolValue() {
        PostgreSQLTimeBinaryProtocolValue binaryProtocolValue = new PostgreSQLTimeBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.TIMESTAMP, binaryProtocolValue);
    }
    
    private static void setInt2ArrayBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.INT2_ARRAY, new PostgreSQLInt2ArrayBinaryProtocolValue());
    }
    
    private static void setInt4ArrayBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.INT4_ARRAY, new PostgreSQLInt4ArrayBinaryProtocolValue());
    }
    
    private static void setInt8ArrayBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.INT8_ARRAY, new PostgreSQLInt8ArrayBinaryProtocolValue());
    }
    
    private static void setFloat4ArrayBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.FLOAT4_ARRAY, new PostgreSQLFloat4ArrayBinaryProtocolValue());
    }
    
    private static void setFloat8ArrayBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.FLOAT8_ARRAY, new PostgreSQLFloat8ArrayBinaryProtocolValue());
    }
    
    private static void setBoolArrayBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.BOOL_ARRAY, new PostgreSQLBoolArrayBinaryProtocolValue());
    }
    
    private static void setStringArrayBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.VARCHAR_ARRAY, new PostgreSQLStringArrayBinaryProtocolValue());
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.CHAR_ARRAY, new PostgreSQLStringArrayBinaryProtocolValue());
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.TEXT_ARRAY, new PostgreSQLTextArrayBinaryProtocolValue());
    }
    
    private static void setByteaBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.BYTEA, new PostgreSQLByteaBinaryProtocolValue());
    }
    
    private static void setUUIDBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.UUID, new PostgreSQLUUIDBinaryProtocolValue());
    }
    
    private static void setBoolBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(PostgreSQLColumnType.BOOL, new PostgreSQLBoolBinaryProtocolValue());
    }
    
    /**
     * Get binary protocol value.
     *
     * @param binaryColumnType binary column type
     * @return binary protocol value
     */
    public static PostgreSQLBinaryProtocolValue getBinaryProtocolValue(final BinaryColumnType binaryColumnType) {
        Preconditions.checkArgument(BINARY_PROTOCOL_VALUES.containsKey(binaryColumnType), "Cannot find PostgreSQL type '%s' in column type when process binary protocol value", binaryColumnType);
        return BINARY_PROTOCOL_VALUES.get(binaryColumnType);
    }
}

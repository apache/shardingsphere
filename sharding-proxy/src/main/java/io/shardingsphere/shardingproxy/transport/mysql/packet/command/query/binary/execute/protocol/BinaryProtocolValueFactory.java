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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol;

import com.google.common.base.Preconditions;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Binary protocol value factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BinaryProtocolValueFactory {
    
    private static final Map<ColumnType, BinaryProtocolValue> BINARY_PROTOCOL_VALUES = new HashMap<>();
    
    static {
        setStringLenencBinaryProtocolValue();
        setInt8BinaryProtocolValue();
        setInt4BinaryProtocolValue();
        setInt2BinaryProtocolValue();
        setInt1BinaryProtocolValue();
        setDoubleBinaryProtocolValue();
        setFloatBinaryProtocolValue();
        setDateBinaryProtocolValue();
        setTimeBinaryProtocolValue();
    }
    
    private static void setStringLenencBinaryProtocolValue() {
        StringLenencBinaryProtocolValue binaryProtocolValue = new StringLenencBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_STRING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_VARCHAR, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_VAR_STRING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_ENUM, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_SET, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_LONG_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_MEDIUM_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_TINY_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_GEOMETRY, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_BIT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_DECIMAL, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_NEWDECIMAL, binaryProtocolValue);
    }
    
    private static void setInt8BinaryProtocolValue() {
        Int8BinaryProtocolValue binaryProtocolValue = new Int8BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_LONGLONG, binaryProtocolValue);
    }
    
    private static void setInt4BinaryProtocolValue() {
        Int4BinaryProtocolValue binaryProtocolValue = new Int4BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_LONG, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_INT24, binaryProtocolValue);
    }
    
    private static void setInt2BinaryProtocolValue() {
        Int2BinaryProtocolValue binaryProtocolValue = new Int2BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_SHORT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_YEAR, binaryProtocolValue);
    }
    
    private static void setInt1BinaryProtocolValue() {
        Int1BinaryProtocolValue binaryProtocolValue = new Int1BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_TINY, binaryProtocolValue);
    }
    
    private static void setDoubleBinaryProtocolValue() {
        DoubleBinaryProtocolValue binaryProtocolValue = new DoubleBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_DOUBLE, binaryProtocolValue);
    }
    
    private static void setFloatBinaryProtocolValue() {
        FloatBinaryProtocolValue binaryProtocolValue = new FloatBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_FLOAT, binaryProtocolValue);
    }
    
    private static void setDateBinaryProtocolValue() {
        DateBinaryProtocolValue binaryProtocolValue = new DateBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_DATE, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_DATETIME, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_TIMESTAMP, binaryProtocolValue);
    }
    
    private static void setTimeBinaryProtocolValue() {
        TimeBinaryProtocolValue binaryProtocolValue = new TimeBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(ColumnType.MYSQL_TYPE_TIME, binaryProtocolValue);
    }
    
    /**
     * Get binary protocol value.
     * 
     * @param columnType column type
     * @return binary protocol value
     */
    public static BinaryProtocolValue getBinaryProtocolValue(final ColumnType columnType) {
        Preconditions.checkArgument(BINARY_PROTOCOL_VALUES.containsKey(columnType), "Cannot find MySQL type '%s' in column type when process binary protocol value", columnType);
        return BINARY_PROTOCOL_VALUES.get(columnType);
    }
}

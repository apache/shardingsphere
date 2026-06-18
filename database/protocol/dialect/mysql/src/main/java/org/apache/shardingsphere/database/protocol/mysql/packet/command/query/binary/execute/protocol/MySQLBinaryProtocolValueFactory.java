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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;

import java.util.HashMap;
import java.util.Map;

/**
 * Binary protocol value factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLBinaryProtocolValueFactory {
    
    private static final Map<BinaryColumnType, MySQLBinaryProtocolValue> BINARY_PROTOCOL_VALUES = new HashMap<>();
    
    static {
        setStringLenencBinaryProtocolValue();
        setByteLenencBinaryProtocolValue();
        setInt8BinaryProtocolValue();
        setInt4BinaryProtocolValue();
        setInt2BinaryProtocolValue();
        setInt1BinaryProtocolValue();
        setDoubleBinaryProtocolValue();
        setFloatBinaryProtocolValue();
        setDateBinaryProtocolValue();
        setTimeBinaryProtocolValue();
        setNullBinaryProtocolValue();
    }
    
    private static void setStringLenencBinaryProtocolValue() {
        MySQLStringLenencBinaryProtocolValue binaryProtocolValue = new MySQLStringLenencBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.VARCHAR, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.VAR_STRING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.ENUM, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.SET, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.GEOMETRY, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.BIT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DECIMAL, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.NEWDECIMAL, binaryProtocolValue);
    }
    
    private static void setByteLenencBinaryProtocolValue() {
        MySQLByteLenencBinaryProtocolValue binaryProtocolValue = new MySQLByteLenencBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.STRING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.LONG_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.MEDIUM_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TINY_BLOB, binaryProtocolValue);
    }
    
    private static void setInt8BinaryProtocolValue() {
        MySQLInt8BinaryProtocolValue binaryProtocolValue = new MySQLInt8BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.LONGLONG, binaryProtocolValue);
    }
    
    private static void setInt4BinaryProtocolValue() {
        MySQLInt4BinaryProtocolValue binaryProtocolValue = new MySQLInt4BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.LONG, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.INT24, binaryProtocolValue);
    }
    
    private static void setInt2BinaryProtocolValue() {
        MySQLInt2BinaryProtocolValue binaryProtocolValue = new MySQLInt2BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.SHORT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.YEAR, binaryProtocolValue);
    }
    
    private static void setInt1BinaryProtocolValue() {
        MySQLInt1BinaryProtocolValue binaryProtocolValue = new MySQLInt1BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TINY, binaryProtocolValue);
    }
    
    private static void setDoubleBinaryProtocolValue() {
        MySQLDoubleBinaryProtocolValue binaryProtocolValue = new MySQLDoubleBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DOUBLE, binaryProtocolValue);
    }
    
    private static void setFloatBinaryProtocolValue() {
        MySQLFloatBinaryProtocolValue binaryProtocolValue = new MySQLFloatBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.FLOAT, binaryProtocolValue);
    }
    
    private static void setDateBinaryProtocolValue() {
        MySQLDateBinaryProtocolValue binaryProtocolValue = new MySQLDateBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DATE, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DATETIME, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TIMESTAMP, binaryProtocolValue);
    }
    
    private static void setTimeBinaryProtocolValue() {
        MySQLTimeBinaryProtocolValue binaryProtocolValue = new MySQLTimeBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TIME, binaryProtocolValue);
    }
    
    private static void setNullBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(MySQLBinaryColumnType.NULL, null);
    }
    
    /**
     * Get binary protocol value.
     *
     * @param binaryColumnType binary column type
     * @return binary protocol value
     */
    public static MySQLBinaryProtocolValue getBinaryProtocolValue(final BinaryColumnType binaryColumnType) {
        Preconditions.checkArgument(BINARY_PROTOCOL_VALUES.containsKey(binaryColumnType), "Cannot find MySQL type '%s' in column type when process binary protocol value", binaryColumnType);
        return BINARY_PROTOCOL_VALUES.get(binaryColumnType);
    }
}

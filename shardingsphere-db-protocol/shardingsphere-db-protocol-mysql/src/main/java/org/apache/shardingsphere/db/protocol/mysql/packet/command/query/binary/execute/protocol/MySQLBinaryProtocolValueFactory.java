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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;

import java.util.HashMap;
import java.util.Map;

/**
 * Binary protocol value factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLBinaryProtocolValueFactory {
    
    private static final Map<MySQLColumnType, MySQLBinaryProtocolValue> BINARY_PROTOCOL_VALUES = new HashMap<>();
    
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
        setNullBinaryProtocolValue();
    }
    
    private static void setStringLenencBinaryProtocolValue() {
        MySQLStringLenencBinaryProtocolValue binaryProtocolValue = new MySQLStringLenencBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_STRING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_VARCHAR, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_VAR_STRING, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_ENUM, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_SET, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_LONG_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_MEDIUM_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_TINY_BLOB, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_GEOMETRY, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_BIT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_DECIMAL, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_NEWDECIMAL, binaryProtocolValue);
    }
    
    private static void setInt8BinaryProtocolValue() {
        MySQLInt8BinaryProtocolValue binaryProtocolValue = new MySQLInt8BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_LONGLONG, binaryProtocolValue);
    }
    
    private static void setInt4BinaryProtocolValue() {
        MySQLInt4BinaryProtocolValue binaryProtocolValue = new MySQLInt4BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_LONG, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_INT24, binaryProtocolValue);
    }
    
    private static void setInt2BinaryProtocolValue() {
        MySQLInt2BinaryProtocolValue binaryProtocolValue = new MySQLInt2BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_SHORT, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_YEAR, binaryProtocolValue);
    }
    
    private static void setInt1BinaryProtocolValue() {
        MySQLInt1BinaryProtocolValue binaryProtocolValue = new MySQLInt1BinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_TINY, binaryProtocolValue);
    }
    
    private static void setDoubleBinaryProtocolValue() {
        MySQLDoubleBinaryProtocolValue binaryProtocolValue = new MySQLDoubleBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_DOUBLE, binaryProtocolValue);
    }
    
    private static void setFloatBinaryProtocolValue() {
        MySQLFloatBinaryProtocolValue binaryProtocolValue = new MySQLFloatBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_FLOAT, binaryProtocolValue);
    }
    
    private static void setDateBinaryProtocolValue() {
        MySQLDateBinaryProtocolValue binaryProtocolValue = new MySQLDateBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_DATE, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_DATETIME, binaryProtocolValue);
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_TIMESTAMP, binaryProtocolValue);
    }
    
    private static void setTimeBinaryProtocolValue() {
        MySQLTimeBinaryProtocolValue binaryProtocolValue = new MySQLTimeBinaryProtocolValue();
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_TIME, binaryProtocolValue);
    }
    
    private static void setNullBinaryProtocolValue() {
        BINARY_PROTOCOL_VALUES.put(MySQLColumnType.MYSQL_TYPE_NULL, null);
    }
    
    /**
     * Get binary protocol value.
     * 
     * @param columnType column type
     * @return binary protocol value
     */
    public static MySQLBinaryProtocolValue getBinaryProtocolValue(final MySQLColumnType columnType) {
        Preconditions.checkArgument(BINARY_PROTOCOL_VALUES.containsKey(columnType), "Cannot find MySQL type '%s' in column type when process binary protocol value", columnType);
        return BINARY_PROTOCOL_VALUES.get(columnType);
    }
}

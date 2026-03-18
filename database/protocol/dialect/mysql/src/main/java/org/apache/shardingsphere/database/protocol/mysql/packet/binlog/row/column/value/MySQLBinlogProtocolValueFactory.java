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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.blob.MySQLBlobBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.decimal.MySQLDecimalBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.decimal.MySQLDoubleBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.decimal.MySQLFloatBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.integer.MySQLBitBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.integer.MySQLInt24BinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.integer.MySQLLongBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.integer.MySQLLongLongBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.integer.MySQLShortBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.integer.MySQLTinyBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLJsonBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLStringBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLVarcharBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLDateBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLDatetime2BinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLDatetimeBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLTime2BinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLTimeBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLTimestamp2BinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLTimestampBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time.MySQLYearBinlogProtocolValue;

import java.util.EnumMap;
import java.util.Map;

/**
 * Binlog protocol value factory of MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLBinlogProtocolValueFactory {
    
    private static final Map<MySQLBinaryColumnType, MySQLBinlogProtocolValue> BINLOG_PROTOCOL_VALUES = new EnumMap<>(MySQLBinaryColumnType.class);
    
    static {
        registerIntegerTypeValue();
        registerDecimalTypeValue();
        registerTimeTypeValue();
        registerStringTypeValue();
        registerBlobTypeValue();
    }
    
    private static void registerIntegerTypeValue() {
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.BIT, new MySQLBitBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TINY, new MySQLTinyBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.SHORT, new MySQLShortBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.INT24, new MySQLInt24BinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.LONG, new MySQLLongBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.LONGLONG, new MySQLLongLongBinlogProtocolValue());
    }
    
    private static void registerDecimalTypeValue() {
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.NEWDECIMAL, new MySQLDecimalBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DOUBLE, new MySQLDoubleBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.FLOAT, new MySQLFloatBinlogProtocolValue());
    }
    
    private static void registerTimeTypeValue() {
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.YEAR, new MySQLYearBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DATE, new MySQLDateBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TIME, new MySQLTimeBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TIME2, new MySQLTime2BinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TIMESTAMP, new MySQLTimestampBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.TIMESTAMP2, new MySQLTimestamp2BinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DATETIME, new MySQLDatetimeBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.DATETIME2, new MySQLDatetime2BinlogProtocolValue());
    }
    
    private static void registerStringTypeValue() {
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.STRING, new MySQLStringBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.VARCHAR, new MySQLVarcharBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.VAR_STRING, new MySQLVarcharBinlogProtocolValue());
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.JSON, new MySQLJsonBinlogProtocolValue());
    }
    
    private static void registerBlobTypeValue() {
        BINLOG_PROTOCOL_VALUES.put(MySQLBinaryColumnType.BLOB, new MySQLBlobBinlogProtocolValue());
    }
    
    /**
     * Get binlog protocol value.
     *
     * @param columnType column type
     * @return binlog protocol value
     */
    public static MySQLBinlogProtocolValue getBinlogProtocolValue(final MySQLBinaryColumnType columnType) {
        Preconditions.checkArgument(BINLOG_PROTOCOL_VALUES.containsKey(columnType), "Cannot find MySQL type '%s' in column type when process binlog protocol value", columnType);
        return BINLOG_PROTOCOL_VALUES.get(columnType);
    }
}

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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryColumnType;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Binary column type for MySQL.
 * 
 * @see <a href="https://github.com/apache/shardingsphere/issues/4355"></a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLBinaryColumnType implements BinaryColumnType {
    
    DECIMAL(0x00),
    
    TINY(0x01),
    
    SHORT(0x02),
    
    LONG(0x03),
    
    FLOAT(0x04),
    
    DOUBLE(0x05),
    
    NULL(0x06),
    
    TIMESTAMP(0x07),
    
    LONGLONG(0x08),
    
    INT24(0x09),
    
    DATE(0x0a),
    
    TIME(0x0b),
    
    DATETIME(0x0c),
    
    YEAR(0x0d),
    
    NEWDATE(0x0e),
    
    VARCHAR(0x0f),
    
    BIT(0x10),
    
    TIMESTAMP2(0x11),
    
    DATETIME2(0x12),
    
    TIME2(0x13),
    
    /**
     * Do not describe in document, but actual exist.
     *
     * @see <a href="https://github.com/apache/shardingsphere/issues/4795"></a>
     */
    JSON(0xf5),
    
    NEWDECIMAL(0xf6),
    
    ENUM(0xf7),
    
    SET(0xf8),
    
    TINY_BLOB(0xf9),
    
    MEDIUM_BLOB(0xfa),
    
    LONG_BLOB(0xfb),
    
    BLOB(0xfc),
    
    VAR_STRING(0xfd),
    
    STRING(0xfe),
    
    GEOMETRY(0xff);
    
    private static final Map<Integer, MySQLBinaryColumnType> JDBC_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private static final Map<Integer, MySQLBinaryColumnType> VALUE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private final int value;
    
    static {
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIT, BIT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TINYINT, TINY);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SMALLINT, SHORT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.INTEGER, LONG);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIGINT, LONGLONG);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.FLOAT, FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.REAL, FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DOUBLE, DOUBLE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NUMERIC, NEWDECIMAL);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DECIMAL, NEWDECIMAL);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.CHAR, STRING);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARCHAR, VAR_STRING);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.LONGVARCHAR, VAR_STRING);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DATE, DATE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIME, TIME);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIMESTAMP, TIMESTAMP);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BINARY, LONG_BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARBINARY, TINY_BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.LONGVARBINARY, BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NULL, NULL);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BLOB, BLOB);
        for (MySQLBinaryColumnType each : values()) {
            VALUE_AND_COLUMN_TYPE_MAP.put(each.value, each);
        }
    }
    
    /**
     * Value of JDBC type.
     *
     * @param jdbcType JDBC type
     * @return column type enum
     */
    public static MySQLBinaryColumnType valueOfJDBCType(final int jdbcType) {
        Preconditions.checkArgument(JDBC_TYPE_AND_COLUMN_TYPE_MAP.containsKey(jdbcType), "Can not find JDBC type `%s` in column type", jdbcType);
        return JDBC_TYPE_AND_COLUMN_TYPE_MAP.get(jdbcType);
    }
    
    /**
     * Value of.
     *
     * @param value value
     * @return column type
     */
    public static MySQLBinaryColumnType valueOf(final int value) {
        Preconditions.checkArgument(VALUE_AND_COLUMN_TYPE_MAP.containsKey(value), "Can not find value `%s` in column type", value);
        return VALUE_AND_COLUMN_TYPE_MAP.get(value);
    }
}

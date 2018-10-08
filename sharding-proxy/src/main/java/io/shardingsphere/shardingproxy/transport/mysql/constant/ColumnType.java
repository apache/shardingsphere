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

package io.shardingsphere.shardingproxy.transport.mysql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Column types.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html#column-type">Column Type</a>
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum ColumnType {
    
    MYSQL_TYPE_DECIMAL(0x00), 
    
    MYSQL_TYPE_TINY(0x01), 
    
    MYSQL_TYPE_SHORT(0x02), 
    
    MYSQL_TYPE_LONG(0x03), 
    
    MYSQL_TYPE_FLOAT(0x04), 
    
    MYSQL_TYPE_DOUBLE(0x05),
    
    MYSQL_TYPE_NULL(0x06),
    
    MYSQL_TYPE_TIMESTAMP(0x07),
    
    MYSQL_TYPE_LONGLONG(0x08),
    
    MYSQL_TYPE_INT24(0x09),
    
    MYSQL_TYPE_DATE(0x0a),
    
    MYSQL_TYPE_TIME(0x0b),
    
    MYSQL_TYPE_DATETIME(0x0c),
    
    MYSQL_TYPE_YEAR(0x0d),
    
    MYSQL_TYPE_NEWDATE(0x0e),
    
    MYSQL_TYPE_VARCHAR(0x0f),
    
    MYSQL_TYPE_BIT(0x10),
    
    MYSQL_TYPE_TIMESTAMP2(0x11),
    
    MYSQL_TYPE_DATETIME2(0x12),
    
    MYSQL_TYPE_TIME2(0x13),
    
    MYSQL_TYPE_NEWDECIMAL(0xf6),
    
    MYSQL_TYPE_ENUM(0xf7),
    
    MYSQL_TYPE_SET(0xf8),
    
    MYSQL_TYPE_TINY_BLOB(0xf9),
    
    MYSQL_TYPE_MEDIUM_BLOB(0xfa),
    
    MYSQL_TYPE_LONG_BLOB(0xfb),
    
    MYSQL_TYPE_BLOB(0xfc),
    
    MYSQL_TYPE_VAR_STRING(0xfd),
    
    MYSQL_TYPE_STRING(0xfe),
    
    MYSQL_TYPE_GEOMETRY(0xff);
    
    private static final Map<Integer, ColumnType> JDBC_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(ColumnType.values().length, 1);
    
    private final int value;
    
    static {
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIT, MYSQL_TYPE_BIT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TINYINT, MYSQL_TYPE_TINY);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SMALLINT, MYSQL_TYPE_SHORT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.INTEGER, MYSQL_TYPE_LONG);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIGINT, MYSQL_TYPE_LONGLONG);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.FLOAT, MYSQL_TYPE_FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.REAL, MYSQL_TYPE_FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DOUBLE, MYSQL_TYPE_DOUBLE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NUMERIC, MYSQL_TYPE_NEWDECIMAL);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DECIMAL, MYSQL_TYPE_NEWDECIMAL);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.CHAR, MYSQL_TYPE_VARCHAR);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARCHAR, MYSQL_TYPE_VARCHAR);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.LONGVARCHAR, MYSQL_TYPE_VARCHAR);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DATE, MYSQL_TYPE_DATE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIME, MYSQL_TYPE_TIME);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIMESTAMP, MYSQL_TYPE_TIMESTAMP);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BINARY, MYSQL_TYPE_BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARBINARY, MYSQL_TYPE_MEDIUM_BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.LONGVARBINARY, MYSQL_TYPE_LONG_BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NULL, MYSQL_TYPE_NULL);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BLOB, MYSQL_TYPE_BLOB);
    }
    
    /**
     * Value of JDBC type.
     *
     * @param jdbcType JDBC type
     * @return column type enum
     */
    public static ColumnType valueOfJDBCType(final int jdbcType) {
        if (JDBC_TYPE_AND_COLUMN_TYPE_MAP.containsKey(jdbcType)) {
            return JDBC_TYPE_AND_COLUMN_TYPE_MAP.get(jdbcType);
        }
        throw new IllegalArgumentException(String.format("Cannot find JDBC type '%s' in column type", jdbcType));
    }
    
    /**
     * Value of.
     * 
     * @param value value
     * @return column type
     */
    public static ColumnType valueOf(final int value) {
        for (ColumnType each : ColumnType.values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find value '%s' in column type", value));
    }
}

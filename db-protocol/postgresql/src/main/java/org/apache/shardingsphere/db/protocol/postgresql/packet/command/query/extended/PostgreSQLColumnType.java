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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.binary.BinaryColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.exception.PostgreSQLProtocolException;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Column type for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLColumnType implements BinaryColumnType {
    
    UNSPECIFIED(0),
    
    INT2(21),
    
    INT2_ARRAY(1005),
    
    INT4(23),
    
    INT4_ARRAY(1007),
    
    INT8(20),
    
    INT8_ARRAY(1016),
    
    TEXT(25),
    
    TEXT_ARRAY(1009),
    
    NUMERIC(1700),
    
    NUMERIC_ARRAY(1231),
    
    FLOAT4(700),
    
    FLOAT4_ARRAY(1021),
    
    FLOAT8(701),
    
    FLOAT8_ARRAY(1022),
    
    BOOL(16),
    
    BOOL_ARRAY(1000),
    
    DATE(1082),
    
    DATE_ARRAY(1182),
    
    TIME(1083),
    
    TIME_ARRAY(1183),
    
    TIMETZ(1266),
    
    TIMETZ_ARRAY(1270),
    
    TIMESTAMP(1114),
    
    TIMESTAMP_ARRAY(1115),
    
    TIMESTAMPTZ(1184),
    
    TIMESTAMPTZ_ARRAY(1185),
    
    BYTEA(17),
    
    BYTEA_ARRAY(1001),
    
    VARCHAR(1043),
    
    VARCHAR_ARRAY(1015),
    
    OID(26),
    
    OID_ARRAY(1028),
    
    BPCHAR(1042),
    
    BPCHAR_ARRAY(1014),
    
    MONEY(790),
    
    MONEY_ARRAY(791),
    
    NAME(19),
    
    NAME_ARRAY(1003),
    
    BIT(1560),
    
    BIT_ARRAY(1561),
    
    VOID(2278),
    
    INTERVAL(1186),
    
    INTERVAL_ARRAY(1187),
    
    CHAR(18),
    
    CHAR_ARRAY(1002),
    
    VARBIT(1562),
    
    VARBIT_ARRAY(1563),
    
    UUID(2950),
    
    UUID_ARRAY(2951),
    
    XML(142),
    
    XML_ARRAY(143),
    
    POINT(600),
    
    POINT_ARRAY(1017),
    
    BOX(603),
    
    JSONB_ARRAY(3807),
    
    JSON(114),
    
    JSON_ARRAY(199),
    
    REF_CURSOR(1790),
    
    REF_CURSOR_ARRAY(2201);
    
    private static final Map<Integer, PostgreSQLColumnType> JDBC_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private final int value;
    
    static {
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TINYINT, INT2);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SMALLINT, INT2);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.INTEGER, INT4);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIGINT, INT8);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NUMERIC, NUMERIC);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DECIMAL, NUMERIC);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.REAL, FLOAT4);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DOUBLE, FLOAT8);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.CHAR, CHAR);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARCHAR, VARCHAR);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BINARY, BYTEA);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIT, BIT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DATE, DATE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIME, TIME);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIMESTAMP, TIMESTAMP);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.OTHER, JSON);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SQLXML, XML);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BOOLEAN, BOOL);
        // TODO Temporary solution for https://github.com/apache/shardingsphere/issues/22522
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.STRUCT, VARCHAR);
    }
    
    /**
     * Value of JDBC type.
     *
     * @param jdbcType JDBC type
     * @return PostgreSQL column type enum
     */
    public static PostgreSQLColumnType valueOfJDBCType(final int jdbcType) {
        Preconditions.checkArgument(JDBC_TYPE_AND_COLUMN_TYPE_MAP.containsKey(jdbcType), "Can not find JDBC type `%s` in PostgreSQL column type", jdbcType);
        return JDBC_TYPE_AND_COLUMN_TYPE_MAP.get(jdbcType);
    }
    
    /**
     * Value of JDBC type.
     *
     * @param jdbcType JDBC type
     * @param columnTypeName column type name
     * @return PostgreSQL column type enum
     */
    public static PostgreSQLColumnType valueOfJDBCType(final int jdbcType, final String columnTypeName) {
        if (isPgBit(jdbcType, columnTypeName)) {
            return BIT;
        }
        if (isPgBool(jdbcType, columnTypeName)) {
            return BOOL;
        }
        return valueOfJDBCType(jdbcType);
    }
    
    /**
     * check if pg bit type.
     *
     * @param jdbcType JDBC type
     * @param columnTypeName column type name
     * @return whether is PostgreSQL bit
     */
    public static boolean isPgBit(final int jdbcType, final String columnTypeName) {
        return Types.BIT == jdbcType && "bit".equalsIgnoreCase(columnTypeName);
    }
    
    /**
     * check if pg bit type.
     *
     * @param jdbcType JDBC type
     * @param columnTypeName column type name
     * @return whether is PostgreSQL bit
     */
    public static boolean isPgBool(final int jdbcType, final String columnTypeName) {
        return Types.BIT == jdbcType && "bool".equalsIgnoreCase(columnTypeName);
    }
    
    /**
     * Value of.
     * 
     * @param value value
     * @return PostgreSQL column type
     * @throws PostgreSQLProtocolException PostgreSQL protocol exception
     */
    public static PostgreSQLColumnType valueOf(final int value) {
        for (PostgreSQLColumnType each : values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new PostgreSQLProtocolException("Can not find value `%s` in PostgreSQL column type.", value);
    }
}

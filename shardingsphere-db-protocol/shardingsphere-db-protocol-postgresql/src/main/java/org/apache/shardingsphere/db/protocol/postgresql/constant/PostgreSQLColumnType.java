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

package org.apache.shardingsphere.db.protocol.postgresql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.type.SQLColumnType;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Column type for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLColumnType implements SQLColumnType {
    
    POSTGRESQL_TYPE_UNSPECIFIED(0),
    
    POSTGRESQL_TYPE_INT2(21),
    
    POSTGRESQL_TYPE_INT2_ARRAY(1005),
    
    POSTGRESQL_TYPE_INT4(23),
    
    POSTGRESQL_TYPE_INT4_ARRAY(1007),
    
    POSTGRESQL_TYPE_INT8(20),
    
    POSTGRESQL_TYPE_INT8_ARRAY(1016),
    
    POSTGRESQL_TYPE_TEXT(25),
    
    POSTGRESQL_TYPE_TEXT_ARRAY(1009),
    
    POSTGRESQL_TYPE_NUMERIC(1700),
    
    POSTGRESQL_TYPE_NUMERIC_ARRAY(1231),
    
    POSTGRESQL_TYPE_FLOAT4(700),
    
    POSTGRESQL_TYPE_FLOAT4_ARRAY(1021),
    
    POSTGRESQL_TYPE_FLOAT8(701),
    
    POSTGRESQL_TYPE_FLOAT8_ARRAY(1022),
    
    POSTGRESQL_TYPE_BOOL(16),
    
    POSTGRESQL_TYPE_BOOL_ARRAY(1000),
    
    POSTGRESQL_TYPE_DATE(1082),
    
    POSTGRESQL_TYPE_DATE_ARRAY(1182),
    
    POSTGRESQL_TYPE_TIME(1083),
    
    POSTGRESQL_TYPE_TIME_ARRAY(1183),
    
    POSTGRESQL_TYPE_TIMETZ(1266),
    
    POSTGRESQL_TYPE_TIMETZ_ARRAY(1270),
    
    POSTGRESQL_TYPE_TIMESTAMP(1114),
    
    POSTGRESQL_TYPE_TIMESTAMP_ARRAY(1115),
    
    POSTGRESQL_TYPE_TIMESTAMPTZ(1184),
    
    POSTGRESQL_TYPE_TIMESTAMPTZ_ARRAY(1185),
    
    POSTGRESQL_TYPE_BYTEA(17),
    
    POSTGRESQL_TYPE_BYTEA_ARRAY(1001),
    
    POSTGRESQL_TYPE_VARCHAR(1043),
    
    POSTGRESQL_TYPE_VARCHAR_ARRAY(1015),
    
    POSTGRESQL_TYPE_OID(26),
    
    POSTGRESQL_TYPE_OID_ARRAY(1028),
    
    POSTGRESQL_TYPE_BPCHAR(1042),
    
    POSTGRESQL_TYPE_BPCHAR_ARRAY(1014),
    
    POSTGRESQL_TYPE_MONEY(790),
    
    POSTGRESQL_TYPE_MONEY_ARRAY(791),
    
    POSTGRESQL_TYPE_NAME(19),
    
    POSTGRESQL_TYPE_NAME_ARRAY(1003),
    
    POSTGRESQL_TYPE_BIT(1560),
    
    POSTGRESQL_TYPE_BIT_ARRAY(1561),
    
    POSTGRESQL_TYPE_VOID(2278),
    
    POSTGRESQL_TYPE_INTERVAL(1186),
    
    POSTGRESQL_TYPE_INTERVAL_ARRAY(1187),
    
    POSTGRESQL_TYPE_CHAR(18),
    
    POSTGRESQL_TYPE_CHAR_ARRAY(1002),
    
    POSTGRESQL_TYPE_VARBIT(1562),
    
    POSTGRESQL_TYPE_VARBIT_ARRAY(1563),
    
    POSTGRESQL_TYPE_UUID(2950),
    
    POSTGRESQL_TYPE_UUID_ARRAY(2951),
    
    POSTGRESQL_TYPE_XML(142),
    
    POSTGRESQL_TYPE_XML_ARRAY(143),
    
    POSTGRESQL_TYPE_POINT(600),
    
    POSTGRESQL_TYPE_POINT_ARRAY(1017),
    
    POSTGRESQL_TYPE_BOX(603),
    
    POSTGRESQL_TYPE_JSONB_ARRAY(3807),
    
    POSTGRESQL_TYPE_JSON(114),
    
    POSTGRESQL_TYPE_JSON_ARRAY(199),
    
    POSTGRESQL_TYPE_REF_CURSOR(1790),
    
    POSTGRESQL_TYPE_REF_CURSOR_ARRAY(2201);
    
    private static final Map<Integer, PostgreSQLColumnType> JDBC_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1);
    
    private final int value;
    
    static {
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SMALLINT, POSTGRESQL_TYPE_INT2);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.INTEGER, POSTGRESQL_TYPE_INT4);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIGINT, POSTGRESQL_TYPE_INT8);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NUMERIC, POSTGRESQL_TYPE_NUMERIC);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.REAL, POSTGRESQL_TYPE_FLOAT4);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DOUBLE, POSTGRESQL_TYPE_FLOAT8);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.CHAR, POSTGRESQL_TYPE_CHAR);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARCHAR, POSTGRESQL_TYPE_VARCHAR);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BINARY, POSTGRESQL_TYPE_BYTEA);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIT, POSTGRESQL_TYPE_BIT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DATE, POSTGRESQL_TYPE_DATE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIME, POSTGRESQL_TYPE_TIME);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIMESTAMP, POSTGRESQL_TYPE_TIMESTAMP);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.OTHER, POSTGRESQL_TYPE_JSON);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SQLXML, POSTGRESQL_TYPE_XML);
    }
    
    /**
     * Value of JDBC type.
     *
     * @param jdbcType JDBC type
     * @return PostgreSQL column type enum
     */
    public static PostgreSQLColumnType valueOfJDBCType(final int jdbcType) {
        if (JDBC_TYPE_AND_COLUMN_TYPE_MAP.containsKey(jdbcType)) {
            return JDBC_TYPE_AND_COLUMN_TYPE_MAP.get(jdbcType);
        }
        throw new IllegalArgumentException(String.format("Cannot find JDBC type '%s' in PostgreSQL column type", jdbcType));
    }
    
    /**
     * Value of.
     * 
     * @param value value
     * @return PostgreSQL column type
     */
    public static PostgreSQLColumnType valueOf(final int value) {
        for (PostgreSQLColumnType each : values()) {
            if (value == each.value) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find value '%s' in PostgreSQL column type", value));
    }
}

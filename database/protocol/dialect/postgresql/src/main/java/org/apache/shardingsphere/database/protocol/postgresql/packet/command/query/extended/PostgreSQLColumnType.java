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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.exception.PostgreSQLProtocolException;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.PostgreSQLTextValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLBitValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLBoolValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLDateValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLDoubleValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLFloatValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLIntValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLJsonValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLLongValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLNumericValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLTextArrayValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLTimeValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLTimestampValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLUnspecifiedValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLVarcharArrayValueParser;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl.PostgreSQLVarcharValueParser;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Column type for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLColumnType implements BinaryColumnType {
    
    UNSPECIFIED(0, new PostgreSQLUnspecifiedValueParser()),
    
    INT2(21, new PostgreSQLIntValueParser()),
    
    INT2_ARRAY(1005, new PostgreSQLVarcharValueParser()),
    
    INT4(23, new PostgreSQLIntValueParser()),
    
    INT4_ARRAY(1007, new PostgreSQLVarcharValueParser()),
    
    INT8(20, new PostgreSQLLongValueParser()),
    
    INT8_ARRAY(1016, new PostgreSQLVarcharValueParser()),
    
    TEXT(25, new PostgreSQLVarcharValueParser()),
    
    TEXT_ARRAY(1009, new PostgreSQLTextArrayValueParser()),
    
    NUMERIC(1700, new PostgreSQLNumericValueParser()),
    
    NUMERIC_ARRAY(1231, new PostgreSQLVarcharValueParser()),
    
    FLOAT4(700, new PostgreSQLFloatValueParser()),
    
    FLOAT4_ARRAY(1021, new PostgreSQLVarcharValueParser()),
    
    FLOAT8(701, new PostgreSQLDoubleValueParser()),
    
    FLOAT8_ARRAY(1022, new PostgreSQLVarcharValueParser()),
    
    BOOL(16, new PostgreSQLBoolValueParser()),
    
    BOOL_ARRAY(1000, new PostgreSQLVarcharValueParser()),
    
    DATE(1082, new PostgreSQLDateValueParser()),
    
    DATE_ARRAY(1182, new PostgreSQLVarcharValueParser()),
    
    TIME(1083, new PostgreSQLTimeValueParser()),
    
    TIME_ARRAY(1183, new PostgreSQLVarcharValueParser()),
    
    TIMETZ(1266, new PostgreSQLTimeValueParser()),
    
    TIMETZ_ARRAY(1270, new PostgreSQLVarcharValueParser()),
    
    TIMESTAMP(1114, new PostgreSQLTimestampValueParser()),
    
    TIMESTAMP_ARRAY(1115, new PostgreSQLVarcharValueParser()),
    
    TIMESTAMPTZ(1184, new PostgreSQLTimestampValueParser()),
    
    TIMESTAMPTZ_ARRAY(1185, new PostgreSQLVarcharValueParser()),
    
    BYTEA(17, new PostgreSQLVarcharValueParser()),
    
    BYTEA_ARRAY(1001, new PostgreSQLVarcharValueParser()),
    
    VARCHAR(1043, new PostgreSQLVarcharValueParser()),
    
    VARCHAR_ARRAY(1015, new PostgreSQLVarcharArrayValueParser()),
    
    OID(26, new PostgreSQLVarcharValueParser()),
    
    OID_ARRAY(1028, new PostgreSQLVarcharValueParser()),
    
    BPCHAR(1042, new PostgreSQLVarcharValueParser()),
    
    BPCHAR_ARRAY(1014, new PostgreSQLVarcharValueParser()),
    
    MONEY(790, new PostgreSQLVarcharValueParser()),
    
    MONEY_ARRAY(791, new PostgreSQLVarcharValueParser()),
    
    NAME(19, new PostgreSQLVarcharValueParser()),
    
    NAME_ARRAY(1003, new PostgreSQLVarcharValueParser()),
    
    BIT(1560, new PostgreSQLBitValueParser()),
    
    BIT_ARRAY(1561, new PostgreSQLVarcharValueParser()),
    
    VOID(2278, new PostgreSQLVarcharValueParser()),
    
    INTERVAL(1186, new PostgreSQLVarcharValueParser()),
    
    INTERVAL_ARRAY(1187, new PostgreSQLVarcharValueParser()),
    
    CHAR(18, new PostgreSQLVarcharValueParser()),
    
    CHAR_ARRAY(1002, new PostgreSQLVarcharValueParser()),
    
    VARBIT(1562, new PostgreSQLVarcharValueParser()),
    
    VARBIT_ARRAY(1563, new PostgreSQLVarcharValueParser()),
    
    UUID(2950, new PostgreSQLVarcharValueParser()),
    
    UUID_ARRAY(2951, new PostgreSQLVarcharValueParser()),
    
    XML(142, new PostgreSQLVarcharValueParser()),
    
    XML_ARRAY(143, new PostgreSQLVarcharValueParser()),
    
    POINT(600, new PostgreSQLVarcharValueParser()),
    
    POINT_ARRAY(1017, new PostgreSQLVarcharValueParser()),
    
    BOX(603, new PostgreSQLVarcharValueParser()),
    
    JSONB_ARRAY(3807, new PostgreSQLVarcharValueParser()),
    
    JSON(114, new PostgreSQLJsonValueParser()),
    
    JSON_ARRAY(199, new PostgreSQLVarcharValueParser()),
    
    REF_CURSOR(1790, new PostgreSQLVarcharValueParser()),
    
    REF_CURSOR_ARRAY(2201, new PostgreSQLVarcharValueParser());
    
    private static final Map<Integer, PostgreSQLColumnType> JDBC_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private final int value;
    
    private final PostgreSQLTextValueParser<?> textValueParser;
    
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
        if (isBit(jdbcType, columnTypeName)) {
            return BIT;
        }
        if (isBool(jdbcType, columnTypeName)) {
            return BOOL;
        }
        if (isUUID(jdbcType, columnTypeName)) {
            return UUID;
        }
        return valueOfJDBCType(jdbcType);
    }
    
    /**
     * Check if pg PostgreSQL type.
     *
     * @param jdbcType JDBC type
     * @param columnTypeName column type name
     * @return whether is PostgreSQL bit
     */
    public static boolean isBit(final int jdbcType, final String columnTypeName) {
        return Types.BIT == jdbcType && "bit".equalsIgnoreCase(columnTypeName);
    }
    
    /**
     * Check if PostgreSQL bit type.
     *
     * @param jdbcType JDBC type
     * @param columnTypeName column type name
     * @return whether is PostgreSQL bit
     */
    public static boolean isBool(final int jdbcType, final String columnTypeName) {
        return Types.BIT == jdbcType && "bool".equalsIgnoreCase(columnTypeName);
    }
    
    /**
     * Check if PostgreSQL is UUID type.
     *
     * @param jdbcType JDBC type
     * @param columnTypeName column type name
     * @return whether it is PostgreSQL UUID
     */
    public static boolean isUUID(final int jdbcType, final String columnTypeName) {
        return Types.OTHER == jdbcType && "uuid".equalsIgnoreCase(columnTypeName);
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

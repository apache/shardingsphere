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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.binary.BinaryColumnType;
import org.firebirdsql.gds.BlrConstants;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Column type for Firebird.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdBinaryColumnType implements BinaryColumnType {
    
    // TODO add support for retrieving length for ARRAY types.
    // Currently not possible due to existing issues with ARRAY handling in the database itself.
    TEXT(452, 255),
    VARYING(448, 255),
    LEGACY_TEXT(452, 255),
    LEGACY_VARYING(448, 255),
    SHORT(500, 2),
    LONG(496, 4),
    FLOAT(482, 4),
    DOUBLE(480, 8),
    D_FLOAT(530, 8),
    TIMESTAMP(510, 8),
    BLOB(520, 8),
    ARRAY(540, 255),
    QUAD(550, 4),
    TIME(560, 4),
    DATE(570, 4),
    INT64(580, 8),
    NUMERIC(580, 8, 1),
    DECIMAL(580, 8, 2),
    TIMESTAMP_TZ_EX(32748, 10),
    TIME_TZ_EX(32750, 6),
    INT128(32752, 16),
    TIMESTAMP_TZ(32754, 10),
    TIME_TZ(32756, 6),
    DEC16(32760, 2),
    DEC34(32762, 4),
    BOOLEAN(32764, 1),
    NULL(32766, 0);
    
    private static final Map<Integer, FirebirdBinaryColumnType> JDBC_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private static final Map<Integer, FirebirdBinaryColumnType> BLR_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private static final Map<Integer, FirebirdBinaryColumnType> VALUE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);
    
    private final int value;
    
    private final int length;
    
    private final int subtype;
    
    static {
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TINYINT, SHORT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SMALLINT, SHORT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.INTEGER, LONG);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIGINT, INT64);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.FLOAT, FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.REAL, FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DOUBLE, DOUBLE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NUMERIC, NUMERIC);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DECIMAL, DECIMAL);
        // replace VARYING with TEXT when add proper length
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.CHAR, VARYING);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARCHAR, VARYING);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.LONGVARCHAR, BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DATE, DATE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIME, TIME);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIMESTAMP, TIMESTAMP);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BINARY, TEXT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARBINARY, VARYING);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.LONGVARBINARY, BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NULL, NULL);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BLOB, BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BOOLEAN, BOOLEAN);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.ARRAY, ARRAY);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIME_WITH_TIMEZONE, TIME_TZ);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIMESTAMP_WITH_TIMEZONE, TIMESTAMP_TZ);
        
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_varying2, VARYING);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_text2, TEXT);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_text, LEGACY_TEXT);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_varying, LEGACY_VARYING);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_double, DOUBLE);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_float, FLOAT);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_d_float, D_FLOAT);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_sql_date, DATE);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_sql_time, TIME);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_timestamp, TIMESTAMP);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_quad, BLOB);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_long, LONG);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_short, SHORT);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_int64, INT64);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_bool, BOOLEAN);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_dec64, DEC16);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_dec128, DEC34);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_int128, INT128);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_timestamp_tz, TIMESTAMP_TZ);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_sql_time_tz, TIME_TZ);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_ex_timestamp_tz, TIMESTAMP_TZ_EX);
        BLR_TYPE_AND_COLUMN_TYPE_MAP.put(BlrConstants.blr_ex_time_tz, TIME_TZ_EX);
        
        for (FirebirdBinaryColumnType each : values()) {
            VALUE_AND_COLUMN_TYPE_MAP.put(each.value, each);
        }
    }
    
    FirebirdBinaryColumnType(final int value, final int length) {
        this.value = value;
        this.length = length;
        subtype = 0;
    }
    
    /**
     * Value of JDBC type.
     *
     * @param jdbcType JDBC type
     * @return column type enum
     */
    public static FirebirdBinaryColumnType valueOfJDBCType(final int jdbcType) {
        Preconditions.checkArgument(JDBC_TYPE_AND_COLUMN_TYPE_MAP.containsKey(jdbcType), "Can not find JDBC type `%d` in column type", jdbcType);
        return JDBC_TYPE_AND_COLUMN_TYPE_MAP.get(jdbcType);
    }
    
    /**
     * Value of BLR type.
     *
     * @param blrType BLR type
     * @return column type enum
     */
    public static FirebirdBinaryColumnType valueOfBLRType(final int blrType) {
        Preconditions.checkArgument(BLR_TYPE_AND_COLUMN_TYPE_MAP.containsKey(blrType), "Can not find BLR type `%d` in column type", blrType);
        return BLR_TYPE_AND_COLUMN_TYPE_MAP.get(blrType);
    }
    
    /**
     * Value of.
     *
     * @param value value
     * @return column type
     */
    public static FirebirdBinaryColumnType valueOf(final int value) {
        Preconditions.checkArgument(VALUE_AND_COLUMN_TYPE_MAP.containsKey(value), "Can not find value `%d` in column type", value);
        return VALUE_AND_COLUMN_TYPE_MAP.get(value);
    }
}

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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.binary.BinaryColumnType;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Column type for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdColumnType implements BinaryColumnType {

    TEXT(452),
    VARYING(448),
    SHORT(500),
    LONG(496),
    FLOAT(482),
    DOUBLE(480),
    D_FLOAT(530),
    TIMESTAMP(510),
    BLOB(520),
    ARRAY(540),
    QUAD(550),
    TYPE_TIME(560),
    TYPE_DATE(570),
    INT64(580),
    TIMESTAMP_TZ_EX(32748),
    TIME_TZ_EX(32750),
    INT128(32752),
    TIMESTAMP_TZ(32754),
    TIME_TZ(32756),
    DEC16(32760),
    DEC34(32762),
    BOOLEAN(32764),
    NULL(32766);

    private static final Map<Integer, FirebirdColumnType> JDBC_TYPE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);

    private static final Map<Integer, FirebirdColumnType> VALUE_AND_COLUMN_TYPE_MAP = new HashMap<>(values().length, 1F);

    private final int value;

    static {
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TINYINT, SHORT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.SMALLINT, SHORT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.INTEGER, LONG);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.BIGINT, INT64);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.FLOAT, FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.REAL, FLOAT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DOUBLE, DOUBLE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.NUMERIC, INT128);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DECIMAL, INT128);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.CHAR, TEXT);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.VARCHAR, VARYING);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.LONGVARCHAR, BLOB);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.DATE, TYPE_DATE);
        JDBC_TYPE_AND_COLUMN_TYPE_MAP.put(Types.TIME, TYPE_TIME);
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
        for (FirebirdColumnType each : values()) {
            VALUE_AND_COLUMN_TYPE_MAP.put(each.value, each);
        }
    }

    /**
     * Value of JDBC type.
     *
     * @param jdbcType JDBC type
     * @return column type enum
     */
    public static FirebirdColumnType valueOfJDBCType(final int jdbcType) {
        Preconditions.checkArgument(JDBC_TYPE_AND_COLUMN_TYPE_MAP.containsKey(jdbcType), "Can not find JDBC type `%s` in column type", jdbcType);
        return JDBC_TYPE_AND_COLUMN_TYPE_MAP.get(jdbcType);
    }

    /**
     * Value of.
     *
     * @param value value
     * @return column type
     */
    public static FirebirdColumnType valueOf(final int value) {
        Preconditions.checkArgument(VALUE_AND_COLUMN_TYPE_MAP.containsKey(value), "Can not find value `%s` in column type", value);
        return VALUE_AND_COLUMN_TYPE_MAP.get(value);
    }
}

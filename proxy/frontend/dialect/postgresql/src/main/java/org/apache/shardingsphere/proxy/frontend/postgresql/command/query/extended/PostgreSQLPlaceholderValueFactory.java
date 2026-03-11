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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Placeholder value factory for PostgreSQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLPlaceholderValueFactory {
    
    /**
     * Create placeholder value from column and parameter type.
     *
     * @param column column
     * @param parameterType parameter type
     * @return placeholder value
     */
    public static Object create(final ShardingSphereColumn column, final PostgreSQLBinaryColumnType parameterType) {
        return null == column ? create(parameterType) : create(column.getDataType());
    }
    
    private static Object create(final PostgreSQLBinaryColumnType parameterType) {
        switch (parameterType) {
            case INT2:
            case INT4:
            case OID:
            case INT8:
            case UNSPECIFIED:
                return 0;
            case NUMERIC:
                return BigDecimal.ZERO;
            case FLOAT4:
                return 0F;
            case FLOAT8:
                return 0D;
            case BOOL:
                return false;
            case DATE:
                return java.sql.Date.valueOf("1970-01-01");
            case TIME:
            case TIMETZ:
                return Time.valueOf("00:00:00");
            case TIMESTAMP:
            case TIMESTAMPTZ:
                return Timestamp.valueOf("1970-01-01 00:00:00");
            default:
                return "";
        }
    }
    
    private static Object create(final int jdbcType) {
        switch (jdbcType) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return 0;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return BigDecimal.ZERO;
            case Types.REAL:
            case Types.FLOAT:
                return 0F;
            case Types.DOUBLE:
                return 0D;
            case Types.BIT:
            case Types.BOOLEAN:
                return false;
            case Types.DATE:
                return java.sql.Date.valueOf("1970-01-01");
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return Time.valueOf("00:00:00");
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return Timestamp.valueOf("1970-01-01 00:00:00");
            default:
                return "";
        }
    }
}

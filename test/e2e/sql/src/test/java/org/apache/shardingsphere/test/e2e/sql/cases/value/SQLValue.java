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

package org.apache.shardingsphere.test.e2e.sql.cases.value;

import lombok.Getter;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * SQL value.
 */
@Getter
public final class SQLValue {
    
    private final Object value;
    
    private final int index;
    
    public SQLValue(final String value, final String type, final int index) {
        this.value = null == type ? value : getValue(value, type.toLowerCase());
        this.index = index;
    }
    
    private Object getValue(final String value, final String type) {
        if (type.startsWith("enum#") || type.startsWith("set#") || type.startsWith("cast#")) {
            return value;
        }
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }
        switch (type) {
            case "string":
            case "varchar":
            case "char":
            case "tinytext":
            case "text":
            case "longtext":
            case "mediumtext":
            case "json":
            case "clob":
            case "enum":
            case "set":
                return value;
            case "tinyint":
                return Byte.parseByte(value);
            case "tinyint unsigned":
            case "smallint":
                return Short.parseShort(value);
            case "smallint unsigned":
            case "mediumint":
            case "mediumint unsigned":
            case "year":
            case "int":
            case "integer":
                return Integer.parseInt(value);
            case "int unsigned":
            case "bigint":
            case "long":
                return Long.parseLong(value);
            case "float":
            case "real":
                return Float.parseFloat(value);
            case "float unsigned":
            case "double":
            case "double unsigned":
                return Double.parseDouble(value);
            case "numeric":
            case "decimal":
            case "numeric unsigned":
            case "decimal unsigned":
            case "bigint unsigned":
                return new BigDecimal(value);
            case "boolean":
                return Boolean.parseBoolean(value);
            case "date":
                return Date.valueOf(LocalDate.parse(value, DateTimeFormatterFactory.getDateFormatter()));
            case "datetime":
                if (26 == value.length()) {
                    return Date.valueOf(LocalDate.parse(value, DateTimeFormatterFactory.getFullMillisDatetimeFormatter()));
                }
                if (10 == value.length()) {
                    return Date.valueOf(LocalDate.parse(value, DateTimeFormatterFactory.getDateFormatter()));
                }
                return Date.valueOf(LocalDate.parse(value, DateTimeFormatterFactory.getDatetimeFormatter()));
            case "time":
                if (value.length() > 8) {
                    return Time.valueOf(LocalTime.parse(value, DateTimeFormatterFactory.getFullTimeFormatter()));
                }
                return Time.valueOf(LocalTime.parse(value, DateTimeFormatterFactory.getTimeFormatter()));
            case "timestamp":
                if (26 == value.length()) {
                    return Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatterFactory.getFullMillisDatetimeFormatter()));
                }
                if (19 == value.length()) {
                    return Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatterFactory.getDatetimeFormatter()));
                }
                if (21 == value.length()) {
                    return Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatterFactory.getShortMillisDatetimeFormatter()));
                }
                if (22 == value.length()) {
                    return Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatterFactory.getDoubleMillisDatetimeFormatter()));
                }
                if (23 == value.length()) {
                    return Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatterFactory.getLongMillisDatetimeFormatter()));
                }
                return Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatterFactory.getDatetimeFormatter()));
            case "tinyblob":
            case "blob":
            case "longblob":
            case "mediumblob":
            case "bit":
            case "binary":
            case "varbinary":
            case "bytes":
                return value.getBytes(StandardCharsets.UTF_8);
            case "uuid":
                return UUID.fromString(value);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support type: `%s`", type));
        }
    }
    
    @Override
    public String toString() {
        if (null == value) {
            return null;
        }
        if (value instanceof String) {
            return formatString((String) value);
        }
        if (value instanceof Date) {
            return formatString(DateTimeFormatterFactory.getDateFormatter().format(((Date) value).toLocalDate()));
        }
        if (value instanceof Time) {
            return formatString(DateTimeFormatterFactory.getTimeFormatter().format(((Time) value).toLocalTime()));
        }
        if (value instanceof Timestamp) {
            return formatString(DateTimeFormatterFactory.getLongMillisDatetimeFormatter().format(((Timestamp) value).toLocalDateTime()));
        }
        if (value instanceof byte[]) {
            return formatString(new String((byte[]) value, StandardCharsets.UTF_8));
        }
        if (value instanceof UUID) {
            return formatString(value.toString());
        }
        return value.toString();
    }
    
    private String formatString(final String value) {
        return "'" + value + "'";
    }
}

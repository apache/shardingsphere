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

package org.apache.shardingsphere.test.e2e.cases.value;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * SQL value.
 */
@Slf4j
public final class SQLValue {
    
    @Getter
    private final Object value;
    
    @Getter
    private final int index;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    
    public SQLValue(final String value, final String type, final int index) {
        this.value = null == type ? value : getValue(value, type);
        this.index = index;
    }
    
    private Object getValue(final String value, final String type) {
        if (type.startsWith("enum#") || type.startsWith("cast#")) {
            return value;
        }
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }
        switch (type) {
            case "String":
            case "varchar":
            case "char":
                return value;
            case "tinyint":
                return Byte.parseByte(value);
            case "smallint":
                return Short.parseShort(value);
            case "int":
                return Integer.parseInt(value);
            case "long":
                return Long.parseLong(value);
            case "float":
                return Float.parseFloat(value);
            case "double":
                return Double.parseDouble(value);
            case "numeric":
                return value.contains(".") ? Double.parseDouble(value) : Long.parseLong(value);
            case "decimal":
                return new BigDecimal(value);
            case "boolean":
                return Boolean.parseBoolean(value);
            case "Date":
            case "datetime":
                return Date.valueOf(LocalDate.parse(value, dateFormatter));
            case "time":
                return Time.valueOf(LocalTime.parse(value, timeFormatter));
            case "timestamp":
                return Timestamp.valueOf(LocalDateTime.parse(value, timestampFormatter));
            case "bytes":
                return value.getBytes(StandardCharsets.UTF_8);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support type: `%s`", type));
        }
    }
    
    @Override
    public String toString() {
        if (value instanceof String) {
            return formatString((String) value);
        }
        if (value instanceof Date) {
            return formatString(dateFormatter.format(((Date) value).toLocalDate()));
        }
        if (value instanceof Time) {
            return formatString(timeFormatter.format(((Time) value).toLocalTime()));
        }
        if (value instanceof Timestamp) {
            return formatString(timestampFormatter.format(((Timestamp) value).toLocalDateTime()));
        }
        if (value instanceof byte[]) {
            return formatString(new String((byte[]) value, StandardCharsets.UTF_8));
        }
        return value.toString();
    }
    
    private String formatString(final String value) {
        return "'" + value + "'";
    }
}

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

package org.apache.shardingsphere.test.integration.cases.value;

import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * SQL value.
 */
@Getter
public final class SQLValue {
    
    private final Object value;
    
    private final int index;
    
    public SQLValue(final String value, final String type, final int index) throws ParseException {
        this.value = null == type ? value : getValue(value, type);
        this.index = index;
    }
    
    private Object getValue(final String value, final String type) throws ParseException {
        if (type.startsWith("enum#")) {
            return value;
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
            case "double":
                return Double.parseDouble(value);
            case "numeric":
                return value.contains("//.") ? Double.parseDouble(value) : Long.parseLong(value);
            case "decimal":
                return new BigDecimal(value);
            case "boolean":
                return Boolean.parseBoolean(value);
            case "Date":
            case "datetime":
                return new Date(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value).getTime());
            case "time":
                return new Time(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(value).getTime());
            case "timestamp":
                return new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(value).getTime());
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
            return formatString(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(value));
        }
        if (value instanceof Time) {
            return formatString(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(value));
        }
        if (value instanceof Timestamp) {
            return formatString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(value));
        }
        return value.toString();
    }
    
    private String formatString(final String value) {
        return "'" + value + "'";
    }
}

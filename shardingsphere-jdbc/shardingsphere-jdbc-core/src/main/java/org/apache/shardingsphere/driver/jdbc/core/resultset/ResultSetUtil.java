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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * ResultSet utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultSetUtil {
    
    /**
     * Convert value via expected class type.
     *
     * @param value original value
     * @param convertType expected class type
     * @return converted value
     */
    public static Object convertValue(final Object value, final Class<?> convertType) {
        if (null == value) {
            return convertNullValue(convertType);
        }
        if (value.getClass() == convertType) {
            return value;
        }
        if (LocalDateTime.class.equals(convertType)) {
            return convertLocalDateTimeValue(value);
        }
        if (LocalDate.class.equals(convertType)) {
            return convertLocalDateValue(value);
        }
        if (LocalTime.class.equals(convertType)) {
            return convertLocalTimeValue(value);
        }
        if (URL.class.equals(convertType)) {
            return convertURL(value);
        }
        if (value instanceof Number) {
            return convertNumberValue(value, convertType);
        }
        if (value instanceof Date) {
            return convertDateValue(value, convertType);
        }
        if (value instanceof byte[]) {
            return convertByteArrayValue(value, convertType);
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        } else {
            return value;
        }
    }
    
    private static Object convertURL(final Object value) {
        String val = value.toString();
        try {
            return new URL(val);
        } catch (MalformedURLException ex) {
            throw new ShardingSphereException("Unsupported Date type: URL value %s", value);
        }
    }
    
    /**
     * Convert object to BigDecimal.
     *
     * @param value current db object
     * @param needScale need scale
     * @param scale scale size
     * @return big decimal
     */
    public static Object convertBigDecimalValue(final Object value, final boolean needScale, final int scale) {
        if (null == value) {
            return convertNullValue(BigDecimal.class);
        }
        if (value.getClass() == BigDecimal.class) {
            return adjustBigDecimalResult((BigDecimal) value, needScale, scale);
        }
        if (value instanceof Number || value instanceof String) {
            BigDecimal bigDecimal = new BigDecimal(value.toString());
            return adjustBigDecimalResult(bigDecimal, needScale, scale);
        }
        throw new ShardingSphereException("Unsupported Date type: BigDecimal value %s", value);
    }
  
    private static BigDecimal adjustBigDecimalResult(final BigDecimal value, final boolean needScale, final int scale) {
        if (needScale) {
            try {
                return value.setScale(scale);
            } catch (final ArithmeticException ex) {
                return value.setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
        }
        return value;
    }

    private static Object convertLocalDateTimeValue(final Object value) {
        Timestamp timestamp = (Timestamp) value;
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static Object convertLocalDateValue(final Object value) {
        Timestamp timestamp = (Timestamp) value;
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Object convertLocalTimeValue(final Object value) {
        Timestamp timestamp = (Timestamp) value;
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private static Object convertNullValue(final Class<?> convertType) {
        switch (convertType.getName()) {
            case "boolean":
                return false;
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "float":
                return 0.0F;
            case "double":
                return 0.0D;
            default:
                return null;
        }
    }
    
    private static Object convertNumberValue(final Object value, final Class<?> convertType) {
        Number number = (Number) value;
        switch (convertType.getName()) {
            case "boolean":
                return 0 != number.longValue();
            case "byte":
                return number.byteValue();
            case "short":
                return number.shortValue();
            case "int":
                return number.intValue();
            case "long":
                return number.longValue();
            case "double":
                return number.doubleValue();
            case "float":
                return number.floatValue();
            case "java.math.BigDecimal":
                return new BigDecimal(number.toString());
            case "java.lang.Object":
                return value;
            case "java.lang.String":
                return value.toString();
            default:
                throw new ShardingSphereException("Unsupported data type: %s", convertType);
        }
    }
    
    private static Object convertDateValue(final Object value, final Class<?> convertType) {
        Date date = (Date) value;
        switch (convertType.getName()) {
            case "java.sql.Date":
                return new java.sql.Date(date.getTime());
            case "java.sql.Time":
                return new Time(date.getTime());
            case "java.sql.Timestamp":
                return new Timestamp(date.getTime());
            case "java.lang.String":
                return date.toString();
            default:
                throw new ShardingSphereException("Unsupported Date type: %s", convertType);
        }
    }
    
    private static Object convertByteArrayValue(final Object value, final Class<?> convertType) {
        byte[] bytesValue = (byte[]) value;
        switch (bytesValue.length) {
            case 1:
                return convertNumberValue(bytesValue[0], convertType);
            case Shorts.BYTES:
                return convertNumberValue(Shorts.fromByteArray(bytesValue), convertType);
            case Ints.BYTES:
                return convertNumberValue(Ints.fromByteArray(bytesValue), convertType);
            case Longs.BYTES:
                return convertNumberValue(Longs.fromByteArray(bytesValue), convertType);
            default:
                return value;
        }
    }
}

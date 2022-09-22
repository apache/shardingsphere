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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.exception.UnsupportedDataTypeConversionException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
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
     * @throws SQLException SQL exception
     */
    public static Object convertValue(final Object value, final Class<?> convertType) throws SQLException {
        ShardingSpherePreconditions.checkState(null != convertType, () -> new SQLFeatureNotSupportedException("Type can not be null"));
        if (null == value) {
            return convertNullValue(convertType);
        }
        if (value.getClass() == convertType) {
            return value;
        }
        if (value instanceof LocalDateTime) {
            return convertLocalDateTimeValue(value, convertType);
        }
        if (value instanceof Timestamp) {
            return convertTimestampValue(value, convertType);
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
        if (boolean.class.equals(convertType)) {
            return convertBooleanValue(value);
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        }
        throw new SQLFeatureNotSupportedException("getObject with type");
    }
    
    private static Object convertURL(final Object value) {
        try {
            return new URL(value.toString());
        } catch (final MalformedURLException ex) {
            throw new UnsupportedDataTypeConversionException(URL.class, value);
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
        if (BigDecimal.class == value.getClass()) {
            return adjustBigDecimalResult((BigDecimal) value, needScale, scale);
        }
        if (value instanceof Number || value instanceof String) {
            BigDecimal bigDecimal = new BigDecimal(value.toString());
            return adjustBigDecimalResult(bigDecimal, needScale, scale);
        }
        throw new UnsupportedDataTypeConversionException(BigDecimal.class, value);
    }
    
    private static BigDecimal adjustBigDecimalResult(final BigDecimal value, final boolean needScale, final int scale) {
        if (needScale) {
            try {
                return value.setScale(scale);
            } catch (final ArithmeticException ex) {
                return value.setScale(scale, RoundingMode.HALF_UP);
            }
        }
        return value;
    }
    
    private static Object convertLocalDateTimeValue(final Object value, final Class<?> convertType) {
        LocalDateTime localDateTime = (LocalDateTime) value;
        if (Timestamp.class.equals(convertType)) {
            return Timestamp.valueOf(localDateTime);
        }
        return value;
    }
    
    private static Object convertTimestampValue(final Object value, final Class<?> convertType) {
        Timestamp timestamp = (Timestamp) value;
        if (LocalDateTime.class.equals(convertType)) {
            return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (LocalDate.class.equals(convertType)) {
            return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (LocalTime.class.equals(convertType)) {
            return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }
        if (OffsetDateTime.class.equals(convertType)) {
            return timestamp.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        return value;
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
                return longToBoolean(number.longValue());
            case "byte":
            case "java.lang.Byte":
                return number.byteValue();
            case "short":
            case "java.lang.Short":
                return number.shortValue();
            case "int":
            case "java.lang.Integer":
                return number.intValue();
            case "long":
            case "java.lang.Long":
                return number.longValue();
            case "double":
            case "java.lang.Double":
                return number.doubleValue();
            case "float":
            case "java.lang.Float":
                return number.floatValue();
            case "java.math.BigDecimal":
                return new BigDecimal(number.toString());
            case "java.lang.Object":
                return value;
            case "java.lang.String":
                return value.toString();
            default:
                throw new UnsupportedDataTypeConversionException(convertType, value);
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
                throw new UnsupportedDataTypeConversionException(convertType, value);
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
    
    private static Object convertBooleanValue(final Object value) {
        if (value instanceof Boolean) {
            return value;
        }
        String stringVal = value.toString();
        if (stringVal.length() > 0) {
            int firstChar = Character.toLowerCase(stringVal.charAt(0));
            return 't' == firstChar || 'y' == firstChar || '1' == firstChar || "-1".equals(stringVal);
        } else {
            return false;
        }
    }
    
    private static Boolean longToBoolean(final long longVal) {
        return -1 == longVal || longVal > 0;
    }
    
}

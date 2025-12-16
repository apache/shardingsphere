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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.data.UnsupportedDataTypeConversionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
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
 * Result set utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultSetUtils {
    
    /**
     * Convert value via expected class type.
     *
     * @param value original value
     * @param convertType expected class type
     * @return converted value
     * @throws SQLFeatureNotSupportedException SQL feature not supported exception
     */
    public static Object convertValue(final Object value, final Class<?> convertType) throws SQLFeatureNotSupportedException {
        ShardingSpherePreconditions.checkNotNull(convertType, () -> new SQLFeatureNotSupportedException("Type can not be null"));
        if (null == value) {
            return convertNullValue(convertType);
        }
        if (value.getClass() == convertType) {
            return value;
        }
        if (value instanceof LocalDateTime) {
            return convertLocalDateTimeValue((LocalDateTime) value, convertType);
        }
        if (value instanceof LocalDate) {
            return convertLocalDateValue((LocalDate) value, convertType);
        }
        if (value instanceof Timestamp) {
            return convertTimestampValue((Timestamp) value, convertType);
        }
        if (URL.class.equals(convertType)) {
            return convertURL(value);
        }
        if (value instanceof Number) {
            return convertNumberValue(value, convertType);
        }
        if (value instanceof Date) {
            return convertDateValue((Date) value, convertType);
        }
        if (value instanceof byte[]) {
            return convertByteArrayValue((byte[]) value, convertType);
        }
        if (boolean.class.equals(convertType)) {
            return convertBooleanValue(value);
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        }
        try {
            return convertType.cast(value);
        } catch (final ClassCastException ignored) {
            throw new SQLFeatureNotSupportedException("getObject with type");
        }
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
    
    private static Object convertLocalDateTimeValue(final LocalDateTime value, final Class<?> convertType) {
        if (Timestamp.class.equals(convertType)) {
            return Timestamp.valueOf(value);
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        }
        return value;
    }
    
    private static Object convertLocalDateValue(final LocalDate value, final Class<?> convertType) {
        if (java.sql.Date.class.equals(convertType)) {
            return java.sql.Date.valueOf(value);
        }
        if (Timestamp.class.equals(convertType)) {
            return Timestamp.valueOf(value.atStartOfDay());
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        }
        return value;
    }
    
    private static Object convertTimestampValue(final Timestamp value, final Class<?> convertType) {
        if (LocalDateTime.class.equals(convertType)) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (LocalDate.class.equals(convertType)) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (LocalTime.class.equals(convertType)) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }
        if (OffsetDateTime.class.equals(convertType)) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        }
        return value;
    }
    
    private static Object convertURL(final Object value) {
        try {
            return new URL(value.toString());
        } catch (final MalformedURLException ignored) {
            throw new UnsupportedDataTypeConversionException(URL.class, value);
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
    
    private static Boolean longToBoolean(final long longVal) {
        return -1L == longVal || longVal > 0L;
    }
    
    private static Object convertDateValue(final Date value, final Class<?> convertType) {
        switch (convertType.getName()) {
            case "java.sql.Date":
                return new java.sql.Date(value.getTime());
            case "java.sql.Time":
                return new Time(value.getTime());
            case "java.sql.Timestamp":
                return new Timestamp(value.getTime());
            case "java.time.LocalDate":
                return new java.sql.Date(value.getTime()).toLocalDate();
            case "java.lang.String":
                return value.toString();
            default:
                throw new UnsupportedDataTypeConversionException(convertType, value);
        }
    }
    
    private static Object convertByteArrayValue(final byte[] value, final Class<?> convertType) {
        switch (value.length) {
            case 1:
                return convertNumberValue(value[0], convertType);
            case Shorts.BYTES:
                return convertNumberValue(Shorts.fromByteArray(value), convertType);
            case Ints.BYTES:
                return convertNumberValue(Ints.fromByteArray(value), convertType);
            case Longs.BYTES:
                return convertNumberValue(Longs.fromByteArray(value), convertType);
            default:
                return value;
        }
    }
    
    private static Object convertBooleanValue(final Object value) {
        if (value instanceof Boolean) {
            return value;
        }
        String stringVal = value.toString();
        if (stringVal.isEmpty()) {
            return false;
        }
        int firstChar = Character.toLowerCase(stringVal.charAt(0));
        return 't' == firstChar || 'y' == firstChar || '1' == firstChar || "-1".equals(stringVal);
    }
    
    /**
     * Convert object to BigDecimal.
     *
     * @param value current db object
     * @param needScale need scale
     * @param scale scale size
     * @return big decimal
     * @throws UnsupportedDataTypeConversionException unsupported data type conversion exception
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
                return value.setScale(scale, RoundingMode.UNNECESSARY);
            } catch (final ArithmeticException ex) {
                return value.setScale(scale, RoundingMode.HALF_UP);
            }
        }
        return value;
    }
}

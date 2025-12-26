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

package org.apache.shardingsphere.sharding.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

/**
 * Data type convert utility for sharding value.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingValueTypeConvertUtils {
    
    /**
     * Convert collection to target type.
     *
     * @param sourceCollection source collection
     * @param targetType target type
     * @return converted collection
     */
    public static Collection<Comparable<?>> convertCollectionType(final Collection<Comparable<?>> sourceCollection, final Class<?> targetType) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> value : sourceCollection) {
            result.add(convertToTargetType(value, targetType));
        }
        return result;
    }
    
    /**
     * Convert value to target type.
     *
     * @param <T> type of exception
     * @param value source value
     * @param targetType target type class
     * @return converted value
     * @throws ClassCastException if conversion is not supported
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertToTargetType(final Comparable<?> value, final Class<?> targetType) {
        if (null == value) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return (T) value;
        }
        if (Integer.class == targetType) {
            return (T) convertToInteger(value);
        } else if (Long.class == targetType) {
            return (T) convertToLong(value);
        } else if (Short.class == targetType) {
            return (T) convertToShort(value);
        } else if (Byte.class == targetType) {
            return (T) convertToByte(value);
        } else if (Double.class == targetType) {
            return (T) convertToDouble(value);
        } else if (Float.class == targetType) {
            return (T) convertToFloat(value);
        } else if (BigDecimal.class == targetType) {
            return (T) convertToBigDecimal(value);
        } else if (BigInteger.class == targetType) {
            return (T) convertToBigInteger(value);
        } else if (Boolean.class == targetType) {
            return (T) convertToBoolean(value);
        } else if (Character.class == targetType) {
            return (T) convertToCharacter(value);
        } else if (String.class == targetType) {
            return (T) value.toString();
        } else if (Date.class == targetType) {
            return (T) convertToDate(value);
        } else if (java.sql.Date.class == targetType) {
            return (T) convertToSqlDate(value);
        } else if (java.sql.Time.class == targetType) {
            return (T) convertToSqlTime(value);
        } else if (java.sql.Timestamp.class == targetType) {
            return (T) convertToTimestamp(value);
        } else if (LocalDate.class == targetType) {
            return (T) convertToLocalDate(value);
        } else if (LocalTime.class == targetType) {
            return (T) convertToLocalTime(value);
        } else if (LocalDateTime.class == targetType) {
            return (T) convertToLocalDateTime(value);
        } else if (Instant.class == targetType) {
            return (T) convertToInstant(value);
        } else if (Year.class == targetType) {
            return (T) convertToYear(value);
        } else if (YearMonth.class == targetType) {
            return (T) convertToYearMonth(value);
        } else if (MonthDay.class == targetType) {
            return (T) convertToMonthDay(value);
        } else if (Duration.class == targetType) {
            return (T) convertToDuration(value);
        }
        throw new ClassCastException("Unsupported type conversion from " + value.getClass().getName() + " to " + targetType.getName());
    }
    
    private static Integer convertToInteger(final Comparable<?> value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
    
    private static Long convertToLong(final Comparable<?> value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
    
    private static Short convertToShort(final Comparable<?> value) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return Short.parseShort(value.toString());
    }
    
    private static Byte convertToByte(final Comparable<?> value) {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        return Byte.parseByte(value.toString());
    }
    
    private static Double convertToDouble(final Comparable<?> value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
    
    private static Float convertToFloat(final Comparable<?> value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return Float.parseFloat(value.toString());
    }
    
    private static BigDecimal convertToBigDecimal(final Comparable<?> value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }
    
    private static BigInteger convertToBigInteger(final Comparable<?> value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        }
        if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        return new BigInteger(value.toString());
    }
    
    private static Boolean convertToBoolean(final Comparable<?> value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        String strValue = value.toString().trim();
        if ("true".equalsIgnoreCase(strValue) || "1".equals(strValue)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(strValue) || "0".equals(strValue)) {
            return Boolean.FALSE;
        }
        return Boolean.parseBoolean(strValue);
    }
    
    private static Character convertToCharacter(final Comparable<?> value) {
        if (value instanceof Character) {
            return (Character) value;
        }
        if (value instanceof Number) {
            return (char) ((Number) value).intValue();
        }
        String strValue = value.toString();
        return strValue.isEmpty() ? '\0' : strValue.charAt(0);
    }
    
    private static Date convertToDate(final Comparable<?> value) {
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof LocalDateTime) {
            return Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof LocalDate) {
            return Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof Instant) {
            return Date.from((Instant) value);
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        return Date.from(parseInstant(value.toString()));
    }
    
    private static java.sql.Date convertToSqlDate(final Comparable<?> value) {
        if (value instanceof java.sql.Date) {
            return (java.sql.Date) value;
        }
        if (value instanceof Date) {
            return new java.sql.Date(((Date) value).getTime());
        }
        if (value instanceof LocalDate) {
            return java.sql.Date.valueOf((LocalDate) value);
        }
        if (value instanceof LocalDateTime) {
            return java.sql.Date.valueOf(((LocalDateTime) value).toLocalDate());
        }
        if (value instanceof Number) {
            return new java.sql.Date(((Number) value).longValue());
        }
        return new java.sql.Date(parseInstant(value.toString()).toEpochMilli());
    }
    
    private static java.sql.Time convertToSqlTime(final Comparable<?> value) {
        if (value instanceof java.sql.Time) {
            return (java.sql.Time) value;
        }
        if (value instanceof Date) {
            return new java.sql.Time(((Date) value).getTime());
        }
        if (value instanceof LocalTime) {
            return java.sql.Time.valueOf((LocalTime) value);
        }
        if (value instanceof LocalDateTime) {
            return java.sql.Time.valueOf(((LocalDateTime) value).toLocalTime());
        }
        if (value instanceof Number) {
            return new java.sql.Time(((Number) value).longValue());
        }
        return java.sql.Time.valueOf(parseLocalTime(value.toString()));
    }
    
    private static java.sql.Timestamp convertToTimestamp(final Comparable<?> value) {
        if (value instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp) value;
        }
        if (value instanceof Date) {
            return new java.sql.Timestamp(((Date) value).getTime());
        }
        if (value instanceof LocalDateTime) {
            return java.sql.Timestamp.valueOf((LocalDateTime) value);
        }
        if (value instanceof Instant) {
            return java.sql.Timestamp.from((Instant) value);
        }
        if (value instanceof Number) {
            return new java.sql.Timestamp(((Number) value).longValue());
        }
        return java.sql.Timestamp.from(parseInstant(value.toString()));
    }
    
    private static LocalDate convertToLocalDate(final Comparable<?> value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof Instant) {
            return ((Instant) value).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return parseLocalDate(value.toString());
    }
    
    private static LocalTime convertToLocalTime(final Comparable<?> value) {
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalTime();
        }
        if (value instanceof java.sql.Time) {
            return ((java.sql.Time) value).toLocalTime();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue()).atZone(ZoneId.systemDefault()).toLocalTime();
        }
        return LocalTime.parse(value.toString());
    }
    
    private static LocalDateTime convertToLocalDateTime(final Comparable<?> value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneId.systemDefault());
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return parseLocalDateTime(value.toString());
    }
    
    private static Instant convertToInstant(final Comparable<?> value) {
        if (value instanceof Instant) {
            return (Instant) value;
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant();
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }
        return parseInstant(value.toString());
    }
    
    private static Year convertToYear(final Comparable<?> value) {
        if (value instanceof Year) {
            return (Year) value;
        }
        if (value instanceof Number) {
            return Year.of(((Number) value).intValue());
        }
        if (value instanceof LocalDate) {
            return Year.of(((LocalDate) value).getYear());
        }
        if (value instanceof LocalDateTime) {
            return Year.of(((LocalDateTime) value).getYear());
        }
        return Year.parse(value.toString());
    }
    
    private static YearMonth convertToYearMonth(final Comparable<?> value) {
        if (value instanceof YearMonth) {
            return (YearMonth) value;
        }
        if (value instanceof LocalDate) {
            return YearMonth.of(((LocalDate) value).getYear(), ((LocalDate) value).getMonth());
        }
        if (value instanceof LocalDateTime) {
            return YearMonth.of(((LocalDateTime) value).getYear(), ((LocalDateTime) value).getMonth());
        }
        return YearMonth.parse(value.toString());
    }
    
    private static MonthDay convertToMonthDay(final Comparable<?> value) {
        if (value instanceof MonthDay) {
            return (MonthDay) value;
        }
        if (value instanceof LocalDate) {
            return MonthDay.of(((LocalDate) value).getMonth(), ((LocalDate) value).getDayOfMonth());
        }
        if (value instanceof LocalDateTime) {
            return MonthDay.of(((LocalDateTime) value).getMonth(), ((LocalDateTime) value).getDayOfMonth());
        }
        return MonthDay.parse(value.toString());
    }
    
    private static Instant parseInstant(final String value) {
        try {
            return Instant.parse(value);
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
            return parseInstantByLocalDateTime(value);
        }
    }
    
    private static Instant parseInstantByLocalDateTime(final String value) {
        try {
            return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
            return LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
    }
    
    private static LocalDate parseLocalDate(final String value) {
        try {
            return LocalDate.parse(value);
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
            return LocalDateTime.parse(value).toLocalDate();
        }
    }
    
    private static LocalDateTime parseLocalDateTime(final String value) {
        try {
            return LocalDateTime.parse(value);
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
            return Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }
    
    private static LocalTime parseLocalTime(final String value) {
        try {
            return LocalTime.parse(value);
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
            return LocalDateTime.parse(value).toLocalTime();
        }
    }
    
    private static Duration convertToDuration(final Comparable<?> value) {
        if (value instanceof Duration) {
            return (Duration) value;
        }
        if (value instanceof Number) {
            return Duration.ofMillis(((Number) value).longValue());
        }
        return Duration.parse(value.toString());
    }
}

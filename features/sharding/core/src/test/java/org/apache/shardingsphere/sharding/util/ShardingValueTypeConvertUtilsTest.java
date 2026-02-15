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

import org.junit.jupiter.api.Test;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingValueTypeConvertUtilsTest {
    
    @Test
    void assertConvertToTargetTypeWhenValueIsNull() {
        assertNull(ShardingValueTypeConvertUtils.convertToTargetType(null, Integer.class));
    }
    
    @Test
    void assertConvertToTargetTypeWhenTypesMatch() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(100, Integer.class), is(100));
    }
    
    @Test
    void assertConvertToIntegerFromLong() {
        Integer result = ShardingValueTypeConvertUtils.convertToTargetType(123L, Integer.class);
        assertThat(result, is(123));
    }
    
    @Test
    void assertConvertToIntegerFromShort() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(45, Integer.class), is(45));
    }
    
    @Test
    void assertConvertToIntegerFromByte() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(10, Integer.class), is(10));
    }
    
    @Test
    void assertConvertToIntegerFromDouble() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(123.45, Integer.class), is(123));
    }
    
    @Test
    void assertConvertToIntegerFromFloat() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(67.89f, Integer.class), is(67));
    }
    
    @Test
    void assertConvertToIntegerFromBigDecimal() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(new BigDecimal("999"), Integer.class), is(999));
    }
    
    @Test
    void assertConvertToIntegerFromBigInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(BigInteger.valueOf(777), Integer.class), is(777));
    }
    
    @Test
    void assertConvertToIntegerFromString() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType("555", Integer.class), is(555));
    }
    
    @Test
    void assertConvertToLongFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(123456, Long.class), is(123456L));
    }
    
    @Test
    void assertConvertToLongFromShort() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(789, Long.class), is(789L));
    }
    
    @Test
    void assertConvertToLongFromDouble() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(12345.67, Long.class), is(12345L));
    }
    
    @Test
    void assertConvertToLongFromBigDecimal() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(new BigDecimal("9876543210"), Long.class), is(9876543210L));
    }
    
    @Test
    void assertConvertToShortFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(123, Short.class), is((short) 123));
    }
    
    @Test
    void assertConvertToShortFromLong() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(456L, Short.class), is((short) 456));
    }
    
    @Test
    void assertConvertToByteFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(100, Byte.class), is((byte) 100));
    }
    
    @Test
    void assertConvertToByteFromShort() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(50, Byte.class), is((byte) 50));
    }
    
    @Test
    void assertConvertToDoubleFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(123, Double.class), is(123.0));
    }
    
    @Test
    void assertConvertToDoubleFromLong() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(456L, Double.class), is(456.0));
    }
    
    @Test
    void assertConvertToDoubleFromFloat() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(78.9f, Double.class), is(78.9000015258789));
    }
    
    @Test
    void assertConvertToDoubleFromBigDecimal() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(new BigDecimal("123.456"), Double.class), is(123.456));
    }
    
    @Test
    void assertConvertToFloatFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(234, Float.class), is(234.0f));
    }
    
    @Test
    void assertConvertToFloatFromDouble() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(56.78, Float.class), is(56.78f));
    }
    
    @Test
    void assertConvertToBigDecimalFromInteger() {
        assertThat(((BigDecimal) ShardingValueTypeConvertUtils.convertToTargetType(999, BigDecimal.class)).compareTo(new BigDecimal("999")), is(0));
    }
    
    @Test
    void assertConvertToBigDecimalFromLong() {
        assertThat(((BigDecimal) ShardingValueTypeConvertUtils.convertToTargetType(888L, BigDecimal.class)).compareTo(new BigDecimal("888")), is(0));
    }
    
    @Test
    void assertConvertToBigDecimalFromDouble() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(123.456, BigDecimal.class), is(BigDecimal.valueOf(123.456)));
    }
    
    @Test
    void assertConvertToBigDecimalFromBigInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(BigInteger.valueOf(12345), BigDecimal.class), is(new BigDecimal("12345")));
    }
    
    @Test
    void assertConvertToBigDecimalFromString() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType("999.888", BigDecimal.class), is(new BigDecimal("999.888")));
    }
    
    @Test
    void assertConvertToBigIntegerFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(777, BigInteger.class), is(BigInteger.valueOf(777)));
    }
    
    @Test
    void assertConvertToBigIntegerFromLong() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(666L, BigInteger.class), is(BigInteger.valueOf(666)));
    }
    
    @Test
    void assertConvertToBigIntegerFromBigDecimal() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(new BigDecimal("555"), BigInteger.class), is(BigInteger.valueOf(555)));
    }
    
    @Test
    void assertConvertToStringFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(123, String.class), is("123"));
    }
    
    @Test
    void assertConvertToStringFromLong() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(456L, String.class), is("456"));
    }
    
    @Test
    void assertConvertToStringFromDouble() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(78.9, String.class), is("78.9"));
    }
    
    @Test
    void assertConvertToStringFromBigDecimal() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(new BigDecimal("123.456"), String.class), is("123.456"));
    }
    
    @Test
    void assertConvertCollectionTypeToIntegers() {
        Collection<Comparable<?>> source = Arrays.asList(1L, 2L, 3L, 4L);
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, Integer.class);
        assertThat(actual.size(), is(4));
        for (Comparable<?> value : actual) {
            assertThat(value, isA(Integer.class));
        }
    }
    
    @Test
    void assertConvertCollectionTypeToLongs() {
        Collection<Comparable<?>> source = Arrays.asList(100, 200, 300);
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, Long.class);
        assertThat(actual.size(), is(3));
        for (Comparable<?> value : actual) {
            assertThat(value, isA(Long.class));
        }
    }
    
    @Test
    void assertConvertCollectionTypeToDoubles() {
        Collection<Comparable<?>> source = Arrays.asList(10, 20, 30);
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, Double.class);
        assertThat(actual.size(), is(3));
        for (Comparable<?> value : actual) {
            assertThat(value, isA(Double.class));
        }
    }
    
    @Test
    void assertConvertCollectionTypeToStrings() {
        Collection<Comparable<?>> source = Arrays.asList(123, 456, 789);
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, String.class);
        assertThat(actual.size(), is(3));
        assertThat(new LinkedList<>(actual).get(0), is("123"));
        assertThat(new LinkedList<>(actual).get(1), is("456"));
        assertThat(new LinkedList<>(actual).get(2), is("789"));
    }
    
    @Test
    void assertConvertCollectionTypeWithMixedNumericTypes() {
        Collection<Comparable<?>> source = Arrays.asList(1, 2L, 3.0, 4.0f);
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, Integer.class);
        assertThat(actual.size(), is(4));
        for (Comparable<?> value : actual) {
            assertThat(value, isA(Integer.class));
        }
    }
    
    @Test
    void assertConvertCollectionTypeEmptyCollection() {
        Collection<Comparable<?>> source = new HashSet<>();
        assertTrue(ShardingValueTypeConvertUtils.convertCollectionType(source, Integer.class).isEmpty());
    }
    
    @Test
    void assertConvertCollectionTypeToBigDecimal() {
        Collection<Comparable<?>> source = Arrays.asList(100, 200L, 300.5);
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, BigDecimal.class);
        assertThat(actual.size(), is(3));
        for (Comparable<?> value : actual) {
            assertThat(value, isA(BigDecimal.class));
        }
    }
    
    @Test
    void assertConvertCollectionTypeToBigInteger() {
        Collection<Comparable<?>> source = Arrays.asList(100, 200L, 300);
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, BigInteger.class);
        assertThat(actual.size(), is(3));
        for (Comparable<?> value : actual) {
            assertThat(value, isA(BigInteger.class));
        }
    }
    
    @Test
    void assertConvertToBooleanFromInteger() {
        assertTrue(ShardingValueTypeConvertUtils.convertToTargetType(1, Boolean.class));
    }
    
    @Test
    void assertConvertToBooleanFromIntegerZero() {
        assertFalse(ShardingValueTypeConvertUtils.convertToTargetType(0, Boolean.class));
    }
    
    @Test
    void assertConvertToBooleanFromStringTrue() {
        assertTrue(ShardingValueTypeConvertUtils.convertToTargetType("true", Boolean.class));
    }
    
    @Test
    void assertConvertToBooleanFromStringFalse() {
        assertFalse(ShardingValueTypeConvertUtils.convertToTargetType("false", Boolean.class));
    }
    
    @Test
    void assertConvertToBooleanFromStringOne() {
        assertTrue(ShardingValueTypeConvertUtils.convertToTargetType("1", Boolean.class));
    }
    
    @Test
    void assertConvertToBooleanFromStringZero() {
        assertFalse(ShardingValueTypeConvertUtils.convertToTargetType("0", Boolean.class));
    }
    
    @Test
    void assertConvertToCharacterFromInteger() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(65, Character.class), is('A'));
    }
    
    @Test
    void assertConvertToCharacterFromString() {
        String value = "X";
        Character result = ShardingValueTypeConvertUtils.convertToTargetType(value, Character.class);
        assertThat(result, is(equalTo('X')));
        assertThat(result, instanceOf(Character.class));
    }
    
    @Test
    void assertConvertToCharacterFromEmptyString() {
        String value = "";
        Character result = ShardingValueTypeConvertUtils.convertToTargetType(value, Character.class);
        assertThat(result, is(equalTo('\0')));
        assertThat(result, instanceOf(Character.class));
    }
    
    @Test
    void assertConvertToDateFromLong() {
        Long value = 1704067200000L;
        Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(result, instanceOf(Date.class));
        assertThat(result.getTime(), is(equalTo(value)));
    }
    
    @Test
    void assertConvertToDateFromLocalDateTime() {
        LocalDateTime value = LocalDateTime.of(2024, 1, 1, 0, 0);
        Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(result, instanceOf(Date.class));
    }
    
    @Test
    void assertConvertToDateFromLocalDate() {
        LocalDate value = LocalDate.of(2024, 1, 1);
        Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(result, instanceOf(Date.class));
    }
    
    @Test
    void assertConvertToDateFromInstant() {
        Instant value = Instant.parse("2024-01-01T00:00:00Z");
        Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(result, instanceOf(Date.class));
        assertThat(result.toInstant(), is(equalTo(value)));
    }
    
    @Test
    void assertConvertToSqlDateFromLocalDate() {
        LocalDate value = LocalDate.of(2024, 1, 1);
        java.sql.Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Date.class);
        assertThat(result, instanceOf(java.sql.Date.class));
        assertThat(result.toLocalDate(), is(equalTo(value)));
    }
    
    @Test
    void assertConvertToSqlDateFromDate() {
        Date value = new Date();
        java.sql.Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Date.class);
        assertThat(result, instanceOf(java.sql.Date.class));
    }
    
    @Test
    void assertConvertToSqlTimeFromLocalTime() {
        LocalTime value = LocalTime.of(12, 30, 45);
        java.sql.Time result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Time.class);
        assertThat(result, instanceOf(java.sql.Time.class));
        assertThat(result.toLocalTime(), is(equalTo(value)));
    }
    
    @Test
    void assertConvertToTimestampFromLocalDateTime() {
        LocalDateTime value = LocalDateTime.of(2024, 1, 1, 12, 30, 45);
        java.sql.Timestamp result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Timestamp.class);
        assertThat(result, instanceOf(java.sql.Timestamp.class));
        assertThat(result.toLocalDateTime(), is(equalTo(value)));
    }
    
    @Test
    void assertConvertToTimestampFromInstant() {
        Instant value = Instant.parse("2024-01-01T12:30:45Z");
        java.sql.Timestamp result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Timestamp.class);
        assertThat(result, instanceOf(java.sql.Timestamp.class));
    }
    
    @Test
    void assertConvertToLocalDateFromDate() {
        Date value = new Date();
        LocalDate result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDate.class);
        assertThat(result, instanceOf(LocalDate.class));
    }
    
    @Test
    void assertConvertToLocalDateFromLocalDateTime() {
        LocalDateTime value = LocalDateTime.of(2024, 1, 1, 12, 30, 45);
        LocalDate result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDate.class);
        assertThat(result, is(equalTo(LocalDate.of(2024, 1, 1))));
        assertThat(result, instanceOf(LocalDate.class));
    }
    
    @Test
    void assertConvertToLocalDateFromSqlDate() {
        java.sql.Date value = java.sql.Date.valueOf("2024-01-01");
        LocalDate result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDate.class);
        assertThat(result, is(equalTo(LocalDate.of(2024, 1, 1))));
        assertThat(result, instanceOf(LocalDate.class));
    }
    
    @Test
    void assertConvertToLocalTimeFromSqlTime() {
        java.sql.Time value = java.sql.Time.valueOf("12:30:45");
        LocalTime result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalTime.class);
        assertThat(result, is(equalTo(LocalTime.of(12, 30, 45))));
        assertThat(result, instanceOf(LocalTime.class));
    }
    
    @Test
    void assertConvertToLocalTimeFromLocalDateTime() {
        LocalDateTime value = LocalDateTime.of(2024, 1, 1, 12, 30, 45);
        LocalTime result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalTime.class);
        assertThat(result, is(equalTo(LocalTime.of(12, 30, 45))));
        assertThat(result, instanceOf(LocalTime.class));
    }
    
    @Test
    void assertConvertToLocalDateTimeFromDate() {
        Date value = new Date();
        LocalDateTime result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDateTime.class);
        assertThat(result, instanceOf(LocalDateTime.class));
    }
    
    @Test
    void assertConvertToLocalDateTimeFromLocalDate() {
        LocalDate value = LocalDate.of(2024, 1, 1);
        LocalDateTime result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDateTime.class);
        assertThat(result, is(equalTo(LocalDateTime.of(2024, 1, 1, 0, 0))));
        assertThat(result, instanceOf(LocalDateTime.class));
    }
    
    @Test
    void assertConvertToLocalDateTimeFromTimestamp() {
        java.sql.Timestamp value = java.sql.Timestamp.valueOf("2024-01-01 12:30:45");
        LocalDateTime result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDateTime.class);
        assertThat(result, is(equalTo(LocalDateTime.of(2024, 1, 1, 12, 30, 45))));
        assertThat(result, instanceOf(LocalDateTime.class));
    }
    
    @Test
    void assertConvertToInstantFromDate() {
        Date value = new Date();
        Instant result = ShardingValueTypeConvertUtils.convertToTargetType(value, Instant.class);
        assertThat(result, is(equalTo(value.toInstant())));
        assertThat(result, instanceOf(Instant.class));
    }
    
    @Test
    void assertConvertToInstantFromLong() {
        Long value = 1704067200000L;
        Instant result = ShardingValueTypeConvertUtils.convertToTargetType(value, Instant.class);
        assertThat(result, is(equalTo(Instant.ofEpochMilli(value))));
        assertThat(result, instanceOf(Instant.class));
    }
    
    @Test
    void assertConvertToYearFromInteger() {
        Integer value = 2024;
        Year result = ShardingValueTypeConvertUtils.convertToTargetType(value, Year.class);
        assertThat(result, is(equalTo(Year.of(2024))));
        assertThat(result, instanceOf(Year.class));
    }
    
    @Test
    void assertConvertToYearFromLocalDate() {
        LocalDate value = LocalDate.of(2024, 6, 15);
        Year result = ShardingValueTypeConvertUtils.convertToTargetType(value, Year.class);
        assertThat(result, is(equalTo(Year.of(2024))));
        assertThat(result, instanceOf(Year.class));
    }
    
    @Test
    void assertConvertToYearMonthFromLocalDate() {
        LocalDate value = LocalDate.of(2024, 6, 15);
        YearMonth result = ShardingValueTypeConvertUtils.convertToTargetType(value, YearMonth.class);
        assertThat(result, is(equalTo(YearMonth.of(2024, 6))));
        assertThat(result, instanceOf(YearMonth.class));
    }
    
    @Test
    void assertConvertToMonthDayFromLocalDate() {
        LocalDate value = LocalDate.of(2024, 6, 15);
        MonthDay result = ShardingValueTypeConvertUtils.convertToTargetType(value, MonthDay.class);
        assertThat(result, is(equalTo(MonthDay.of(6, 15))));
        assertThat(result, instanceOf(MonthDay.class));
    }
    
    @Test
    void assertConvertToDurationFromLong() {
        Long value = 5000L;
        Duration result = ShardingValueTypeConvertUtils.convertToTargetType(value, Duration.class);
        assertThat(result, is(equalTo(Duration.ofMillis(5000L))));
        assertThat(result, instanceOf(Duration.class));
    }
    
    @Test
    void assertConvertToDurationFromString() {
        String value = "PT60S";
        Duration result = ShardingValueTypeConvertUtils.convertToTargetType(value, Duration.class);
        assertThat(result, is(equalTo(Duration.ofSeconds(60))));
        assertThat(result, instanceOf(Duration.class));
    }
    
    @Test
    void assertConvertCollectionTypeToLocalDates() {
        Collection<Comparable<?>> source = Arrays.asList(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 3, 1));
        Collection<Comparable<?>> result = ShardingValueTypeConvertUtils.convertCollectionType(source, Year.class);
        assertThat(result.size(), is(3));
        for (Comparable<?> value : result) {
            assertThat(value, instanceOf(Year.class));
        }
    }
    
    @Test
    void assertConvertCollectionTypeToBooleans() {
        Collection<Comparable<?>> source = Arrays.asList(1, 0, 2, "true");
        Collection<Comparable<?>> result = ShardingValueTypeConvertUtils.convertCollectionType(source, Boolean.class);
        assertThat(result.size(), is(4));
        for (Comparable<?> value : result) {
            assertThat(value, instanceOf(Boolean.class));
        }
    }
    
    @Test
    void assertConvertCollectionTypeToDurations() {
        Collection<Comparable<?>> source = Arrays.asList(1000L, 2000L, 3000L);
        Collection<Comparable<?>> result = ShardingValueTypeConvertUtils.convertCollectionType(source, Duration.class);
        assertThat(result.size(), is(3));
        for (Comparable<?> value : result) {
            assertThat(value, instanceOf(Duration.class));
        }
    }
    
    // ========== String to Type Conversion Tests (non-duplicate) ==========
    
    @Test
    void assertConvertToLongFromString() {
        String value = "123456789";
        Long result = ShardingValueTypeConvertUtils.convertToTargetType(value, Long.class);
        assertThat(result, is(equalTo(123456789L)));
        assertThat(result, instanceOf(Long.class));
    }
    
    @Test
    void assertConvertToShortFromString() {
        String value = "123";
        Short result = ShardingValueTypeConvertUtils.convertToTargetType(value, Short.class);
        assertThat(result, is(equalTo((short) 123)));
        assertThat(result, instanceOf(Short.class));
    }
    
    @Test
    void assertConvertToByteFromString() {
        String value = "100";
        Byte result = ShardingValueTypeConvertUtils.convertToTargetType(value, Byte.class);
        assertThat(result, is(equalTo((byte) 100)));
        assertThat(result, instanceOf(Byte.class));
    }
    
    @Test
    void assertConvertToDoubleFromString() {
        String value = "123.456";
        Double result = ShardingValueTypeConvertUtils.convertToTargetType(value, Double.class);
        assertThat(result, is(equalTo(123.456)));
        assertThat(result, instanceOf(Double.class));
    }
    
    @Test
    void assertConvertToFloatFromString() {
        String value = "78.9";
        Float result = ShardingValueTypeConvertUtils.convertToTargetType(value, Float.class);
        assertThat(result, is(equalTo(78.9f)));
        assertThat(result, instanceOf(Float.class));
    }
    
    @Test
    void assertConvertToBigIntegerFromString() {
        String value = "123456789";
        BigInteger result = ShardingValueTypeConvertUtils.convertToTargetType(value, BigInteger.class);
        assertThat(result, is(equalTo(BigInteger.valueOf(123456789))));
        assertThat(result, instanceOf(BigInteger.class));
    }
    
    @Test
    void assertConvertToBooleanFromStringUpperCase() {
        String value = "TRUE";
        Boolean result = ShardingValueTypeConvertUtils.convertToTargetType(value, Boolean.class);
        assertThat(result, is(equalTo(true)));
        assertThat(result, instanceOf(Boolean.class));
    }
    
    @Test
    void assertConvertToDateFromStringLocalDate() {
        String value = "2024-08-05";
        Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(result, instanceOf(Date.class));
    }
    
    @Test
    void assertConvertToDateFromStringLocalDateTime() {
        String value = "2024-08-05T12:30:45";
        Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(result, instanceOf(Date.class));
    }
    
    @Test
    void assertConvertToDateFromStringInstant() {
        String value = "2024-08-05T12:30:45Z";
        Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(result, instanceOf(Date.class));
    }
    
    @Test
    void assertConvertToSqlDateFromStringLocalDate() {
        String value = "2024-08-05";
        java.sql.Date result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Date.class);
        assertThat(result, instanceOf(java.sql.Date.class));
        assertThat(result.toLocalDate(), is(equalTo(LocalDate.of(2024, 8, 5))));
    }
    
    @Test
    void assertConvertToSqlTimeFromString() {
        String value = "12:30:45";
        java.sql.Time result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Time.class);
        assertThat(result, instanceOf(java.sql.Time.class));
        assertThat(result.toLocalTime(), is(equalTo(LocalTime.of(12, 30, 45))));
    }
    
    @Test
    void assertConvertToTimestampFromString() {
        String value = "2024-08-05T12:30:45";
        java.sql.Timestamp result = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Timestamp.class);
        assertThat(result, instanceOf(java.sql.Timestamp.class));
    }
    
    @Test
    void assertConvertToLocalDateFromString() {
        String value = "2024-08-05";
        LocalDate result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDate.class);
        assertThat(result, is(equalTo(LocalDate.of(2024, 8, 5))));
        assertThat(result, instanceOf(LocalDate.class));
    }
    
    @Test
    void assertConvertToLocalDateFromStringDateTime() {
        String value = "2024-08-05T12:30:45";
        LocalDate result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDate.class);
        assertThat(result, is(equalTo(LocalDate.of(2024, 8, 5))));
        assertThat(result, instanceOf(LocalDate.class));
    }
    
    @Test
    void assertConvertToLocalTimeFromString() {
        String value = "12:30:45";
        LocalTime result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalTime.class);
        assertThat(result, is(equalTo(LocalTime.of(12, 30, 45))));
        assertThat(result, instanceOf(LocalTime.class));
    }
    
    @Test
    void assertConvertToLocalDateTimeFromString() {
        String value = "2024-08-05T12:30:45";
        LocalDateTime result = ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDateTime.class);
        assertThat(result, is(equalTo(LocalDateTime.of(2024, 8, 5, 12, 30, 45))));
        assertThat(result, instanceOf(LocalDateTime.class));
    }
    
    @Test
    void assertConvertToInstantFromString() {
        String value = "2024-08-05T12:30:45Z";
        Instant result = ShardingValueTypeConvertUtils.convertToTargetType(value, Instant.class);
        assertThat(result, instanceOf(Instant.class));
    }
    
    @Test
    void assertConvertToInstantFromStringLocalDate() {
        String value = "2024-08-05";
        Instant result = ShardingValueTypeConvertUtils.convertToTargetType(value, Instant.class);
        assertThat(result, instanceOf(Instant.class));
        assertThat(result.atZone(ZoneId.systemDefault()).toLocalDate(), is(equalTo(LocalDate.of(2024, 8, 5))));
    }
    
    @Test
    void assertConvertToYearFromString() {
        String value = "2024";
        Year result = ShardingValueTypeConvertUtils.convertToTargetType(value, Year.class);
        assertThat(result, is(equalTo(Year.of(2024))));
        assertThat(result, instanceOf(Year.class));
    }
    
    @Test
    void assertConvertToYearMonthFromString() {
        String value = "2024-08";
        YearMonth result = ShardingValueTypeConvertUtils.convertToTargetType(value, YearMonth.class);
        assertThat(result, is(equalTo(YearMonth.of(2024, 8))));
        assertThat(result, instanceOf(YearMonth.class));
    }
    
    @Test
    void assertConvertToMonthDayFromString() {
        String value = "--08-05";
        MonthDay result = ShardingValueTypeConvertUtils.convertToTargetType(value, MonthDay.class);
        assertThat(result, is(equalTo(MonthDay.of(8, 5))));
        assertThat(result, instanceOf(MonthDay.class));
    }
    
    @Test
    void assertConvertToDurationFromStringSeconds() {
        String value = "PT60S";
        Duration result = ShardingValueTypeConvertUtils.convertToTargetType(value, Duration.class);
        assertThat(result, is(equalTo(Duration.ofSeconds(60))));
        assertThat(result, instanceOf(Duration.class));
    }
}

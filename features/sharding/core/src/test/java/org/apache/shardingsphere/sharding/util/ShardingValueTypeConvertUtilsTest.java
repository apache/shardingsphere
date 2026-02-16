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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.util.stream.Stream;

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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToIntegerArguments")
    void assertConvertToInteger(final String caseName, final Comparable<?> value, final Integer expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Integer.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToLongArguments")
    void assertConvertToLong(final String caseName, final Comparable<?> value, final Long expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Long.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToShortArguments")
    void assertConvertToShort(final String caseName, final Comparable<?> value, final Short expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Short.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToByteArguments")
    void assertConvertToByte(final String caseName, final Comparable<?> value, final Byte expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Byte.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToDoubleArguments")
    void assertConvertToDouble(final String caseName, final Comparable<?> value, final Double expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Double.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToFloatArguments")
    void assertConvertToFloat(final String caseName, final Comparable<?> value, final Float expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Float.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToBigDecimalArguments")
    void assertConvertToBigDecimal(final String caseName, final Comparable<?> value, final BigDecimal expected) {
        BigDecimal actual = ShardingValueTypeConvertUtils.convertToTargetType(value, BigDecimal.class);
        assertThat(actual.compareTo(expected), is(0));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToBigIntegerArguments")
    void assertConvertToBigInteger(final String caseName, final Comparable<?> value, final BigInteger expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, BigInteger.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToStringArguments")
    void assertConvertToString(final String caseName, final Comparable<?> value, final String expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, String.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToBooleanArguments")
    void assertConvertToBoolean(final String caseName, final Comparable<?> value, final boolean expected) {
        Boolean actual = ShardingValueTypeConvertUtils.convertToTargetType(value, Boolean.class);
        if (expected) {
            assertTrue(actual);
            return;
        }
        assertFalse(actual);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToCharacterArguments")
    void assertConvertToCharacter(final String caseName, final Comparable<?> value, final Character expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Character.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertCollectionTypeArguments")
    void assertConvertCollectionType(
                                     final String caseName, final Collection<Comparable<?>> source, final Class<?> targetType, final int expectedSize, final Class<?> expectedElementType) {
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, targetType);
        assertThat(actual.size(), is(expectedSize));
        for (Comparable<?> each : actual) {
            assertThat(each, isA(expectedElementType));
        }
    }
    
    @Test
    void assertConvertCollectionTypeToStrings() {
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(createComparableCollection(123, 456, 789), String.class);
        assertThat(actual.size(), is(3));
        assertThat(new LinkedList<>(actual).get(0), is("123"));
        assertThat(new LinkedList<>(actual).get(1), is("456"));
        assertThat(new LinkedList<>(actual).get(2), is("789"));
    }
    
    @Test
    void assertConvertCollectionTypeEmptyCollection() {
        assertTrue(ShardingValueTypeConvertUtils.convertCollectionType(new HashSet<>(), Integer.class).isEmpty());
    }
    
    @Test
    void assertConvertToDateFromLong() {
        Long value = 1704067200000L;
        Date actual = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(actual, isA(Date.class));
        assertThat(actual.getTime(), is(value));
    }
    
    @Test
    void assertConvertToDateFromInstant() {
        Instant value = Instant.parse("2024-01-01T00:00:00Z");
        Date actual = ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class);
        assertThat(actual, isA(Date.class));
        assertThat(actual.toInstant(), is(value));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToDateWithInstanceResultArguments")
    void assertConvertToDateWithInstanceResult(final String caseName, final Comparable<?> value) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Date.class), isA(Date.class));
    }
    
    @Test
    void assertConvertToSqlDateFromLocalDate() {
        LocalDate value = LocalDate.of(2024, 1, 1);
        java.sql.Date actual = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Date.class);
        assertThat(actual, isA(java.sql.Date.class));
        assertThat(actual.toLocalDate(), is(value));
    }
    
    @Test
    void assertConvertToSqlDateFromDate() {
        Date value = new Date();
        java.sql.Date actual = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Date.class);
        assertThat(actual, isA(java.sql.Date.class));
    }
    
    @Test
    void assertConvertToSqlDateFromStringLocalDate() {
        java.sql.Date actual = ShardingValueTypeConvertUtils.convertToTargetType("2024-08-05", java.sql.Date.class);
        assertThat(actual, isA(java.sql.Date.class));
        assertThat(actual.toLocalDate(), is(LocalDate.of(2024, 8, 5)));
    }
    
    @Test
    void assertConvertToSqlTimeFromLocalTime() {
        LocalTime value = LocalTime.of(12, 30, 45);
        java.sql.Time actual = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Time.class);
        assertThat(actual, isA(java.sql.Time.class));
        assertThat(actual.toLocalTime(), is(value));
    }
    
    @Test
    void assertConvertToSqlTimeFromString() {
        java.sql.Time actual = ShardingValueTypeConvertUtils.convertToTargetType("12:30:45", java.sql.Time.class);
        assertThat(actual, isA(java.sql.Time.class));
        assertThat(actual.toLocalTime(), is(LocalTime.of(12, 30, 45)));
    }
    
    @Test
    void assertConvertToTimestampFromLocalDateTime() {
        LocalDateTime value = LocalDateTime.of(2024, 1, 1, 12, 30, 45);
        java.sql.Timestamp actual = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Timestamp.class);
        assertThat(actual, isA(java.sql.Timestamp.class));
        assertThat(actual.toLocalDateTime(), is(value));
    }
    
    @Test
    void assertConvertToTimestampFromInstant() {
        Instant value = Instant.parse("2024-01-01T12:30:45Z");
        java.sql.Timestamp actual = ShardingValueTypeConvertUtils.convertToTargetType(value, java.sql.Timestamp.class);
        assertThat(actual, isA(java.sql.Timestamp.class));
        assertThat(actual.toInstant(), is(value));
    }
    
    @Test
    void assertConvertToTimestampFromString() {
        java.sql.Timestamp actual = ShardingValueTypeConvertUtils.convertToTargetType("2024-08-05T12:30:45", java.sql.Timestamp.class);
        assertThat(actual, isA(java.sql.Timestamp.class));
    }
    
    @Test
    void assertConvertToLocalDateFromDate() {
        LocalDate actual = ShardingValueTypeConvertUtils.convertToTargetType(new Date(), LocalDate.class);
        assertThat(actual, isA(LocalDate.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToLocalDateWithExpectedValueArguments")
    void assertConvertToLocalDateWithExpectedValue(final String caseName, final Comparable<?> value, final LocalDate expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDate.class), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToLocalTimeWithExpectedValueArguments")
    void assertConvertToLocalTimeWithExpectedValue(final String caseName, final Comparable<?> value, final LocalTime expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, LocalTime.class), is(expected));
    }
    
    @Test
    void assertConvertToLocalDateTimeFromDate() {
        LocalDateTime actual = ShardingValueTypeConvertUtils.convertToTargetType(new Date(), LocalDateTime.class);
        assertThat(actual, isA(LocalDateTime.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToLocalDateTimeWithExpectedValueArguments")
    void assertConvertToLocalDateTimeWithExpectedValue(final String caseName, final Comparable<?> value, final LocalDateTime expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, LocalDateTime.class), is(expected));
    }
    
    @Test
    void assertConvertToInstantFromDate() {
        Date value = new Date();
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Instant.class), is(value.toInstant()));
    }
    
    @Test
    void assertConvertToInstantFromLong() {
        Long value = 1704067200000L;
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Instant.class), is(Instant.ofEpochMilli(value)));
    }
    
    @Test
    void assertConvertToInstantFromString() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType("2024-08-05T12:30:45Z", Instant.class), isA(Instant.class));
    }
    
    @Test
    void assertConvertToInstantFromStringLocalDate() {
        Instant actual = ShardingValueTypeConvertUtils.convertToTargetType("2024-08-05", Instant.class);
        assertThat(actual, isA(Instant.class));
        assertThat(actual.atZone(ZoneId.systemDefault()).toLocalDate(), is(LocalDate.of(2024, 8, 5)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToYearArguments")
    void assertConvertToYear(final String caseName, final Comparable<?> value, final Year expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, Year.class), is(expected));
    }
    
    @Test
    void assertConvertToYearMonthFromLocalDate() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(LocalDate.of(2024, 6, 15), YearMonth.class), is(YearMonth.of(2024, 6)));
    }
    
    @Test
    void assertConvertToYearMonthFromString() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType("2024-08", YearMonth.class), is(YearMonth.of(2024, 8)));
    }
    
    @Test
    void assertConvertToMonthDayFromLocalDate() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(LocalDate.of(2024, 6, 15), MonthDay.class), is(MonthDay.of(6, 15)));
    }
    
    @Test
    void assertConvertToMonthDayFromString() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType("--08-05", MonthDay.class), is(MonthDay.of(8, 5)));
    }
    
    @Test
    void assertConvertToDurationFromLong() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(5000L, Duration.class), is(Duration.ofMillis(5000L)));
    }
    
    @Test
    void assertConvertToDurationFromString() {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType("PT60S", Duration.class), is(Duration.ofSeconds(60)));
    }
    
    private static Stream<Arguments> convertToIntegerArguments() {
        return Stream.of(
                Arguments.of("Integer <- Long", 123L, 123),
                Arguments.of("Integer <- Short", (short) 45, 45),
                Arguments.of("Integer <- Byte", (byte) 10, 10),
                Arguments.of("Integer <- Double", 123.45D, 123),
                Arguments.of("Integer <- Float", 67.89F, 67),
                Arguments.of("Integer <- BigDecimal", new BigDecimal("999"), 999),
                Arguments.of("Integer <- BigInteger", BigInteger.valueOf(777), 777),
                Arguments.of("Integer <- String", "555", 555));
    }
    
    private static Stream<Arguments> convertToLongArguments() {
        return Stream.of(
                Arguments.of("Long <- Integer", 123456, 123456L),
                Arguments.of("Long <- Short", (short) 789, 789L),
                Arguments.of("Long <- Double", 12345.67D, 12345L),
                Arguments.of("Long <- BigDecimal", new BigDecimal("9876543210"), 9876543210L),
                Arguments.of("Long <- String", "123456789", 123456789L));
    }
    
    private static Stream<Arguments> convertToShortArguments() {
        return Stream.of(
                Arguments.of("Short <- Integer", 123, (short) 123),
                Arguments.of("Short <- Long", 456L, (short) 456),
                Arguments.of("Short <- String", "123", (short) 123));
    }
    
    private static Stream<Arguments> convertToByteArguments() {
        return Stream.of(
                Arguments.of("Byte <- Integer", 100, (byte) 100),
                Arguments.of("Byte <- Short", (short) 50, (byte) 50),
                Arguments.of("Byte <- String", "100", (byte) 100));
    }
    
    private static Stream<Arguments> convertToDoubleArguments() {
        return Stream.of(
                Arguments.of("Double <- Integer", 123, 123.0D),
                Arguments.of("Double <- Long", 456L, 456.0D),
                Arguments.of("Double <- Float", 78.9F, 78.9000015258789D),
                Arguments.of("Double <- BigDecimal", new BigDecimal("123.456"), 123.456D),
                Arguments.of("Double <- String", "123.456", 123.456D));
    }
    
    private static Stream<Arguments> convertToFloatArguments() {
        return Stream.of(
                Arguments.of("Float <- Integer", 234, 234.0F),
                Arguments.of("Float <- Double", 56.78D, 56.78F),
                Arguments.of("Float <- String", "78.9", 78.9F));
    }
    
    private static Stream<Arguments> convertToBigDecimalArguments() {
        return Stream.of(
                Arguments.of("BigDecimal <- Integer", 999, new BigDecimal("999")),
                Arguments.of("BigDecimal <- Long", 888L, new BigDecimal("888")),
                Arguments.of("BigDecimal <- Double", 123.456D, BigDecimal.valueOf(123.456D)),
                Arguments.of("BigDecimal <- BigInteger", BigInteger.valueOf(12345), new BigDecimal("12345")),
                Arguments.of("BigDecimal <- String", "999.888", new BigDecimal("999.888")));
    }
    
    private static Stream<Arguments> convertToBigIntegerArguments() {
        return Stream.of(
                Arguments.of("BigInteger <- Integer", 777, BigInteger.valueOf(777)),
                Arguments.of("BigInteger <- Long", 666L, BigInteger.valueOf(666)),
                Arguments.of("BigInteger <- BigDecimal", new BigDecimal("555"), BigInteger.valueOf(555)),
                Arguments.of("BigInteger <- String", "123456789", BigInteger.valueOf(123456789L)));
    }
    
    private static Stream<Arguments> convertToStringArguments() {
        return Stream.of(
                Arguments.of("String <- Integer", 123, "123"),
                Arguments.of("String <- Long", 456L, "456"),
                Arguments.of("String <- Double", 78.9D, "78.9"),
                Arguments.of("String <- BigDecimal", new BigDecimal("123.456"), "123.456"));
    }
    
    private static Stream<Arguments> convertToBooleanArguments() {
        return Stream.of(
                Arguments.of("Boolean <- Integer(1)", 1, true),
                Arguments.of("Boolean <- Integer(0)", 0, false),
                Arguments.of("Boolean <- String(true)", "true", true),
                Arguments.of("Boolean <- String(false)", "false", false),
                Arguments.of("Boolean <- String(1)", "1", true),
                Arguments.of("Boolean <- String(0)", "0", false),
                Arguments.of("Boolean <- String(TRUE)", "TRUE", true));
    }
    
    private static Stream<Arguments> convertToCharacterArguments() {
        return Stream.of(
                Arguments.of("Character <- Integer", 65, 'A'),
                Arguments.of("Character <- String", "X", 'X'),
                Arguments.of("Character <- EmptyString", "", '\0'));
    }
    
    private static Stream<Arguments> convertCollectionTypeArguments() {
        return Stream.of(
                Arguments.of("Collection -> Integer", createComparableCollection(1L, 2L, 3L, 4L), Integer.class, 4, Integer.class),
                Arguments.of("Collection -> Long", createComparableCollection(100, 200, 300), Long.class, 3, Long.class),
                Arguments.of("Collection -> Double", createComparableCollection(10, 20, 30), Double.class, 3, Double.class),
                Arguments.of("Collection Mixed -> Integer", createComparableCollection(1, 2L, 3.0D, 4.0F), Integer.class, 4, Integer.class),
                Arguments.of("Collection -> BigDecimal", createComparableCollection(100, 200L, 300.5D), BigDecimal.class, 3, BigDecimal.class),
                Arguments.of("Collection -> BigInteger", createComparableCollection(100, 200L, 300), BigInteger.class, 3, BigInteger.class),
                Arguments.of("Collection LocalDate -> Year", createComparableCollection(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)), Year.class, 3, Year.class),
                Arguments.of("Collection -> Boolean", createComparableCollection(1, 0, 2, "true"), Boolean.class, 4, Boolean.class),
                Arguments.of("Collection -> Duration", createComparableCollection(1000L, 2000L, 3000L), Duration.class, 3, Duration.class));
    }
    
    private static Stream<Arguments> convertToDateWithInstanceResultArguments() {
        return Stream.of(
                Arguments.of("Date <- LocalDateTime", LocalDateTime.of(2024, 1, 1, 0, 0)),
                Arguments.of("Date <- LocalDate", LocalDate.of(2024, 1, 1)),
                Arguments.of("Date <- String(LocalDate)", "2024-08-05"),
                Arguments.of("Date <- String(LocalDateTime)", "2024-08-05T12:30:45"),
                Arguments.of("Date <- String(Instant)", "2024-08-05T12:30:45Z"));
    }
    
    private static Stream<Arguments> convertToLocalDateWithExpectedValueArguments() {
        return Stream.of(
                Arguments.of("LocalDate <- LocalDateTime", LocalDateTime.of(2024, 1, 1, 12, 30, 45), LocalDate.of(2024, 1, 1)),
                Arguments.of("LocalDate <- SqlDate", java.sql.Date.valueOf("2024-01-01"), LocalDate.of(2024, 1, 1)),
                Arguments.of("LocalDate <- String", "2024-08-05", LocalDate.of(2024, 8, 5)),
                Arguments.of("LocalDate <- String(LocalDateTime)", "2024-08-05T12:30:45", LocalDate.of(2024, 8, 5)));
    }
    
    private static Stream<Arguments> convertToLocalTimeWithExpectedValueArguments() {
        return Stream.of(
                Arguments.of("LocalTime <- SqlTime", java.sql.Time.valueOf("12:30:45"), LocalTime.of(12, 30, 45)),
                Arguments.of("LocalTime <- LocalDateTime", LocalDateTime.of(2024, 1, 1, 12, 30, 45), LocalTime.of(12, 30, 45)),
                Arguments.of("LocalTime <- String", "12:30:45", LocalTime.of(12, 30, 45)));
    }
    
    private static Stream<Arguments> convertToLocalDateTimeWithExpectedValueArguments() {
        return Stream.of(
                Arguments.of("LocalDateTime <- LocalDate", LocalDate.of(2024, 1, 1), LocalDateTime.of(2024, 1, 1, 0, 0)),
                Arguments.of("LocalDateTime <- Timestamp", java.sql.Timestamp.valueOf("2024-01-01 12:30:45"), LocalDateTime.of(2024, 1, 1, 12, 30, 45)),
                Arguments.of("LocalDateTime <- String", "2024-08-05T12:30:45", LocalDateTime.of(2024, 8, 5, 12, 30, 45)));
    }
    
    private static Stream<Arguments> convertToYearArguments() {
        return Stream.of(
                Arguments.of("Year <- Integer", 2024, Year.of(2024)),
                Arguments.of("Year <- LocalDate", LocalDate.of(2024, 6, 15), Year.of(2024)),
                Arguments.of("Year <- String", "2024", Year.of(2024)));
    }
    
    private static Collection<Comparable<?>> createComparableCollection(final Comparable<?>... values) {
        return new LinkedList<>(Arrays.asList(values));
    }
}

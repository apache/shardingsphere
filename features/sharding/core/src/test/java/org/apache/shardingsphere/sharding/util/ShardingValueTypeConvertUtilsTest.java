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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
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
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class ShardingValueTypeConvertUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertToTargetTypeArguments")
    void assertConvertToTargetType(final String name, final Comparable<?> value, final Class<?> targetType, final Object expected) {
        assertThat(ShardingValueTypeConvertUtils.convertToTargetType(value, targetType), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertCollectionTypeArguments")
    void assertConvertCollectionType(final String name, final Collection<Comparable<?>> source, final Class<?> targetType, final int expectedSize, final Class<?> expectedElementType) {
        Collection<Comparable<?>> actual = ShardingValueTypeConvertUtils.convertCollectionType(source, targetType);
        assertThat(actual.size(), is(expectedSize));
        for (Comparable<?> each : actual) {
            assertThat(each, isA(expectedElementType));
        }
    }
    
    private static Stream<Arguments> convertToTargetTypeArguments() {
        return Stream.of(
                Arguments.of("TargetType <- null", null, Integer.class, null),
                Arguments.of("TargetType <- types match", 100, Integer.class, 100),
                Arguments.of("Integer <- Long", 123L, Integer.class, 123),
                Arguments.of("Integer <- Short", (short) 45, Integer.class, 45),
                Arguments.of("Integer <- Byte", (byte) 10, Integer.class, 10),
                Arguments.of("Integer <- Double", 123.45D, Integer.class, 123),
                Arguments.of("Integer <- Float", 67.89F, Integer.class, 67),
                Arguments.of("Integer <- BigDecimal", new BigDecimal("999"), Integer.class, 999),
                Arguments.of("Integer <- BigInteger", BigInteger.valueOf(777), Integer.class, 777),
                Arguments.of("Integer <- String", "555", Integer.class, 555),
                Arguments.of("Long <- Integer", 123456, Long.class, 123456L),
                Arguments.of("Long <- Short", (short) 789, Long.class, 789L),
                Arguments.of("Long <- Double", 12345.67D, Long.class, 12345L),
                Arguments.of("Long <- BigDecimal", new BigDecimal("9876543210"), Long.class, 9876543210L),
                Arguments.of("Long <- String", "123456789", Long.class, 123456789L),
                Arguments.of("Short <- Integer", 123, Short.class, (short) 123),
                Arguments.of("Short <- Long", 456L, Short.class, (short) 456),
                Arguments.of("Short <- String", "123", Short.class, (short) 123),
                Arguments.of("Byte <- Integer", 100, Byte.class, (byte) 100),
                Arguments.of("Byte <- Short", (short) 50, Byte.class, (byte) 50),
                Arguments.of("Byte <- String", "100", Byte.class, (byte) 100),
                Arguments.of("Double <- Integer", 123, Double.class, 123.0D),
                Arguments.of("Double <- Long", 456L, Double.class, 456.0D),
                Arguments.of("Double <- Float", 78.9F, Double.class, 78.9000015258789D),
                Arguments.of("Double <- BigDecimal", new BigDecimal("123.456"), Double.class, 123.456D),
                Arguments.of("Double <- String", "123.456", Double.class, 123.456D),
                Arguments.of("Float <- Integer", 234, Float.class, 234.0F),
                Arguments.of("Float <- Double", 56.78D, Float.class, 56.78F),
                Arguments.of("Float <- String", "78.9", Float.class, 78.9F),
                Arguments.of("BigDecimal <- Integer", 999, BigDecimal.class, BigDecimal.valueOf(999D)),
                Arguments.of("BigDecimal <- Long", 888L, BigDecimal.class, BigDecimal.valueOf(888D)),
                Arguments.of("BigDecimal <- Double", 123.456D, BigDecimal.class, BigDecimal.valueOf(123.456D)),
                Arguments.of("BigDecimal <- BigInteger", BigInteger.valueOf(12345), BigDecimal.class, new BigDecimal("12345")),
                Arguments.of("BigDecimal <- String", "999.888", BigDecimal.class, new BigDecimal("999.888")),
                Arguments.of("BigInteger <- Integer", 777, BigInteger.class, BigInteger.valueOf(777)),
                Arguments.of("BigInteger <- Long", 666L, BigInteger.class, BigInteger.valueOf(666)),
                Arguments.of("BigInteger <- BigDecimal", new BigDecimal("555"), BigInteger.class, BigInteger.valueOf(555)),
                Arguments.of("BigInteger <- String", "123456789", BigInteger.class, BigInteger.valueOf(123456789L)),
                Arguments.of("String <- Integer", 123, String.class, "123"),
                Arguments.of("String <- Long", 456L, String.class, "456"),
                Arguments.of("String <- Double", 78.9D, String.class, "78.9"),
                Arguments.of("String <- BigDecimal", new BigDecimal("123.456"), String.class, "123.456"),
                Arguments.of("Boolean <- Integer(1)", 1, Boolean.class, true),
                Arguments.of("Boolean <- Integer(0)", 0, Boolean.class, false),
                Arguments.of("Boolean <- String(true)", "true", Boolean.class, true),
                Arguments.of("Boolean <- String(false)", "false", Boolean.class, false),
                Arguments.of("Boolean <- String(1)", "1", Boolean.class, true),
                Arguments.of("Boolean <- String(0)", "0", Boolean.class, false),
                Arguments.of("Boolean <- String(TRUE)", "TRUE", Boolean.class, true),
                Arguments.of("Character <- Integer", 65, Character.class, 'A'),
                Arguments.of("Character <- String", "X", Character.class, 'X'),
                Arguments.of("Character <- EmptyString", "", Character.class, '\0'),
                Arguments.of("Date <- Long", 1704067200000L, Date.class, new Date(1704067200000L)),
                Arguments.of("Date <- LocalDateTime", LocalDateTime.of(2024, 1, 1, 0, 0), Date.class, Date.from(LocalDateTime.of(2024, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant())),
                Arguments.of("Date <- LocalDate", LocalDate.of(2024, 1, 1), Date.class, Date.from(LocalDate.of(2024, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant())),
                Arguments.of("Date <- Instant", Instant.parse("2024-01-01T00:00:00Z"), Date.class, Date.from(Instant.parse("2024-01-01T00:00:00Z"))),
                Arguments.of("Date <- String(LocalDate)", "2024-08-05", Date.class, Date.from(LocalDate.of(2024, 8, 5).atStartOfDay(ZoneId.systemDefault()).toInstant())),
                Arguments.of("Date <- String(LocalDateTime)", "2024-08-05T12:30:45", Date.class, Date.from(LocalDateTime.of(2024, 8, 5, 12, 30, 45).atZone(ZoneId.systemDefault()).toInstant())),
                Arguments.of("Date <- String(Instant)", "2024-08-05T12:30:45Z", Date.class, Date.from(Instant.parse("2024-08-05T12:30:45Z"))),
                Arguments.of("SqlDate <- LocalDate", LocalDate.of(2024, 1, 1), java.sql.Date.class, java.sql.Date.valueOf(LocalDate.of(2024, 1, 1))),
                Arguments.of("SqlDate <- Date", new Date(1704067200000L), java.sql.Date.class, new java.sql.Date(new Date(1704067200000L).getTime())),
                Arguments.of("SqlDate <- String(LocalDate)", "2024-08-05", java.sql.Date.class, java.sql.Date.valueOf(LocalDate.of(2024, 8, 5))),
                Arguments.of("SqlTime <- LocalTime", LocalTime.of(12, 30, 45), Time.class, Time.valueOf(LocalTime.of(12, 30, 45))),
                Arguments.of("SqlTime <- String", "12:30:45", Time.class, Time.valueOf(LocalTime.of(12, 30, 45))),
                Arguments.of("Timestamp <- LocalDateTime", LocalDateTime.of(2024, 1, 1, 12, 30, 45), Timestamp.class, Timestamp.valueOf(LocalDateTime.of(2024, 1, 1, 12, 30, 45))),
                Arguments.of("Timestamp <- Instant", Instant.parse("2024-01-01T12:30:45Z"), Timestamp.class, Timestamp.from(Instant.parse("2024-01-01T12:30:45Z"))),
                Arguments.of("Timestamp <- String", "2024-08-05T12:30:45", Timestamp.class, Timestamp.from(LocalDateTime.of(2024, 8, 5, 12, 30, 45).atZone(ZoneId.systemDefault()).toInstant())),
                Arguments.of("Instant <- Date", new Date(1704067200000L), Instant.class, new Date(1704067200000L).toInstant()),
                Arguments.of("LocalDate <- Date", new Date(1704067200000L), LocalDate.class, new Date(1704067200000L).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                Arguments.of("LocalDate <- LocalDateTime", LocalDateTime.of(2024, 1, 1, 12, 30, 45), LocalDate.class, LocalDate.of(2024, 1, 1)),
                Arguments.of("LocalDate <- SqlDate", java.sql.Date.valueOf(LocalDate.of(2024, 1, 1)), LocalDate.class, LocalDate.of(2024, 1, 1)),
                Arguments.of("LocalDate <- String", "2024-08-05", LocalDate.class, LocalDate.of(2024, 8, 5)),
                Arguments.of("LocalDate <- String(LocalDateTime)", "2024-08-05T12:30:45", LocalDate.class, LocalDate.of(2024, 8, 5)),
                Arguments.of("LocalTime <- SqlTime", Time.valueOf("12:30:45"), LocalTime.class, LocalTime.of(12, 30, 45)),
                Arguments.of("LocalTime <- LocalDateTime", LocalDateTime.of(2024, 1, 1, 12, 30, 45), LocalTime.class, LocalTime.of(12, 30, 45)),
                Arguments.of("LocalTime <- String", "12:30:45", LocalTime.class, LocalTime.of(12, 30, 45)),
                Arguments.of("LocalDateTime <- Date", new Date(1704067200000L), LocalDateTime.class, new Date(1704067200000L).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()),
                Arguments.of("LocalDateTime <- LocalDate", LocalDate.of(2024, 1, 1), LocalDateTime.class, LocalDateTime.of(2024, 1, 1, 0, 0)),
                Arguments.of("LocalDateTime <- Timestamp", Timestamp.valueOf("2024-01-01 12:30:45"), LocalDateTime.class, LocalDateTime.of(2024, 1, 1, 12, 30, 45)),
                Arguments.of("LocalDateTime <- String", "2024-08-05T12:30:45", LocalDateTime.class, LocalDateTime.of(2024, 8, 5, 12, 30, 45)),
                Arguments.of("Instant <- Long", 1704067200000L, Instant.class, Instant.ofEpochMilli(1704067200000L)),
                Arguments.of("Instant <- String", "2024-08-05T12:30:45Z", Instant.class, Instant.parse("2024-08-05T12:30:45Z")),
                Arguments.of("Instant <- String(LocalDate)", "2024-08-05", Instant.class, LocalDate.of(2024, 8, 5).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Arguments.of("Year <- Integer", 2024, Year.class, Year.of(2024)),
                Arguments.of("Year <- LocalDate", LocalDate.of(2024, 6, 15), Year.class, Year.of(2024)),
                Arguments.of("Year <- String", "2024", Year.class, Year.of(2024)),
                Arguments.of("YearMonth <- LocalDate", LocalDate.of(2024, 6, 15), YearMonth.class, YearMonth.of(2024, 6)),
                Arguments.of("YearMonth <- String", "2024-08", YearMonth.class, YearMonth.of(2024, 8)),
                Arguments.of("MonthDay <- LocalDate", LocalDate.of(2024, 6, 15), MonthDay.class, MonthDay.of(6, 15)),
                Arguments.of("MonthDay <- String", "--08-05", MonthDay.class, MonthDay.of(8, 5)),
                Arguments.of("Duration <- Long", 5000L, Duration.class, Duration.ofMillis(5000L)),
                Arguments.of("Duration <- String", "PT60S", Duration.class, Duration.ofSeconds(60)));
    }
    
    private static Stream<Arguments> convertCollectionTypeArguments() {
        return Stream.of(
                Arguments.of("Integer", Arrays.asList((Comparable<?>[]) new Comparable[]{1L, 2L, 3L, 4L}), Integer.class, 4, Integer.class),
                Arguments.of("Long", Arrays.asList((Comparable<?>[]) new Comparable[]{100, 200, 300}), Long.class, 3, Long.class),
                Arguments.of("Double", Arrays.asList((Comparable<?>[]) new Comparable[]{10, 20, 30}), Double.class, 3, Double.class),
                Arguments.of("Collection Mixed -> Integer", Arrays.asList((Comparable<?>[]) new Comparable[]{1, 2L, 3.0D, 4.0F}), Integer.class, 4, Integer.class),
                Arguments.of("BigDecimal", Arrays.asList((Comparable<?>[]) new Comparable[]{100, 200L, 300.5D}), BigDecimal.class, 3, BigDecimal.class),
                Arguments.of("BigInteger", Arrays.asList((Comparable<?>[]) new Comparable[]{100, 200L, 300}), BigInteger.class, 3, BigInteger.class),
                Arguments.of("Year", Arrays.asList((Comparable<?>[]) new Comparable[]{LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)}), Year.class, 3, Year.class),
                Arguments.of("Boolean", Arrays.asList((Comparable<?>[]) new Comparable[]{1, 0, 2, "true"}), Boolean.class, 4, Boolean.class),
                Arguments.of("Duration", Arrays.asList((Comparable<?>[]) new Comparable[]{1000L, 2000L, 3000L}), Duration.class, 3, Duration.class));
    }
}

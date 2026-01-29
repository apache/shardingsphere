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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import org.apache.calcite.runtime.CalciteException;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNumericLiteral;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.TimeString;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiteralExpressionConverterTest {
    
    private enum SampleEnum {
        VALUE
    }
    
    @Test
    void assertConvertNullLiteral() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, null), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.NULL));
    }
    
    @Test
    void assertConvertTrimFlags() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, "both"), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getValueAs(String.class), is("both"));
        SqlLiteral leading = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, "LEADING"), null).orElse(null);
        assertNotNull(leading);
        assertThat(leading.getValueAs(String.class), is("LEADING"));
        SqlLiteral trailing = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, "trailing"), null).orElse(null);
        assertNotNull(trailing);
        assertThat(trailing.getValueAs(String.class), is("trailing"));
    }
    
    @Test
    void assertConvertTimeUnitName() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, "year"), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getValueAs(String.class), is("year"));
    }
    
    @Test
    void assertConvertApproximateNumber() {
        SqlNumericLiteral actual = (SqlNumericLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, new Float("1.5")), null).orElse(null);
        assertNotNull(actual);
        assertFalse(actual.isExact());
    }
    
    @Test
    void assertConvertExactNumber() {
        SqlNumericLiteral actual = (SqlNumericLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, 10), null).orElse(null);
        assertNotNull(actual);
        assertTrue(actual.isExact());
    }
    
    @Test
    void assertConvertStringLiteral() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, "text"), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getValueAs(String.class), is("text"));
    }
    
    @Test
    void assertConvertBooleanLiteral() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, true), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getValueAs(Boolean.class), is(true));
    }
    
    @Test
    void assertConvertCalendarWithoutTimePart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, calendar), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.DATE));
    }
    
    @Test
    void assertConvertCalendarWithTimePart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JANUARY, 1, 1, 1, 1);
        calendar.set(Calendar.MILLISECOND, 1);
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, calendar), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.TIMESTAMP));
    }
    
    @Test
    void assertConvertTimestampDate() {
        Timestamp timestamp = Timestamp.valueOf("2023-01-02 03:04:05");
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, timestamp), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.TIMESTAMP_TZ));
    }
    
    @Test
    void assertConvertTimeDate() {
        Time time = new Time(new TimeString("01:02:03").getMillisOfDay());
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, time), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.TIME));
    }
    
    @Test
    void assertConvertUtilDate() {
        java.sql.Date date = java.sql.Date.valueOf("2020-01-01");
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, date), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.DATE));
    }
    
    @Test
    void assertConvertLocalDate() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, LocalDate.of(2020, 1, 1)), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.DATE));
    }
    
    @Test
    void assertConvertLocalTime() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, LocalTime.of(1, 2, 3)), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.TIME));
    }
    
    @Test
    void assertConvertLocalDateTime() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, LocalDateTime.of(2020, 1, 1, 1, 1, 1)), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.TIMESTAMP));
    }
    
    @Test
    void assertConvertZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 1, 1, 1), ZoneId.of("UTC"));
        assertThrows(CalciteException.class, () -> LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, zonedDateTime), null));
    }
    
    @Test
    void assertConvertBinary() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, new byte[]{1, 2}), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getTypeName(), is(SqlTypeName.BINARY));
    }
    
    @Test
    void assertConvertEnumLiteral() {
        SqlLiteral actual = (SqlLiteral) LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, SampleEnum.VALUE), null).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getValueAs(String.class), is("VALUE"));
    }
    
    @Test
    void assertConvertReturnsEmptyForUnsupportedType() {
        Optional<?> actual = LiteralExpressionConverter.convert(new LiteralExpressionSegment(0, 0, new Object()), null);
        assertFalse(actual.isPresent());
    }
}

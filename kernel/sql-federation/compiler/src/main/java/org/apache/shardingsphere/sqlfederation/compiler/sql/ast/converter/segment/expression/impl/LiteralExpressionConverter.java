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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.DateString;
import org.apache.calcite.util.NlsString;
import org.apache.calcite.util.TimeString;
import org.apache.calcite.util.TimestampString;
import org.apache.calcite.util.TimestampWithTimeZoneString;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Literal expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteralExpressionConverter {
    
    /**
     * Convert literal expression segment to SQL node.
     *
     * @param segment literal expression segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final LiteralExpressionSegment segment) {
        if (null == segment.getLiterals()) {
            return Optional.of(SqlLiteral.createNull(SqlParserPos.ZERO));
        }
        String literalValue = String.valueOf(segment.getLiterals());
        if (segment.getLiterals() instanceof Number) {
            return Optional.of(convertNumber(segment, literalValue));
        }
        if (segment.getLiterals() instanceof String) {
            return Optional.of(SqlLiteral.createCharString(literalValue, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof NlsString) {
            return Optional.of(SqlLiteral.createCharString(((NlsString) segment.getLiterals()).getValue(), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof Boolean) {
            return Optional.of(SqlLiteral.createBoolean(Boolean.parseBoolean(literalValue), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof Calendar) {
            return Optional.of(convertCalendar(segment));
        }
        if (segment.getLiterals() instanceof Date) {
            return Optional.of(convertDate(segment, literalValue));
        }
        if (segment.getLiterals() instanceof TimestampString) {
            return Optional.of(SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, (TimestampString) segment.getLiterals(), 1, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof LocalDate) {
            return Optional.of(SqlLiteral.createDate(DateString.fromDaysSinceEpoch((int) ((LocalDate) segment.getLiterals()).toEpochDay()), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof LocalTime) {
            String formatedValue = DateTimeFormatterFactory.getFullTimeFormatter().format((LocalTime) segment.getLiterals());
            return Optional.of(SqlLiteral.createTime(new TimeString(formatedValue), 1, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof LocalDateTime) {
            String formatedValue = ((LocalDateTime) segment.getLiterals()).format(DateTimeFormatterFactory.getDatetimeFormatter());
            return Optional.of(SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, new TimestampString(formatedValue), 1, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof ZonedDateTime) {
            return Optional.of(SqlLiteral.createTimestamp(new TimestampWithTimeZoneString(literalValue), 1, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof byte[]) {
            return Optional.of(SqlLiteral.createBinaryString((byte[]) segment.getLiterals(), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof Enum) {
            return Optional.of(SqlLiteral.createCharString(((Enum<?>) segment.getLiterals()).name(), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
    
    private static SqlNode convertNumber(final LiteralExpressionSegment segment, final String literalValue) {
        if (segment.getLiterals() instanceof Float || segment.getLiterals() instanceof Double) {
            return SqlLiteral.createApproxNumeric(literalValue, SqlParserPos.ZERO);
        }
        return SqlLiteral.createExactNumeric(literalValue, SqlParserPos.ZERO);
    }
    
    private static SqlNode convertCalendar(final LiteralExpressionSegment segment) {
        Calendar calendar = (Calendar) segment.getLiterals();
        if (hasTimePart(calendar)) {
            return SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, TimestampString.fromCalendarFields(calendar), 1, SqlParserPos.ZERO);
        }
        return SqlLiteral.createDate(DateString.fromCalendarFields(calendar), SqlParserPos.ZERO);
    }
    
    private static boolean hasTimePart(final Calendar calendar) {
        return 0 != calendar.get(Calendar.HOUR_OF_DAY) || 0 != calendar.get(Calendar.MINUTE) || 0 != calendar.get(Calendar.SECOND) || 0 != calendar.get(Calendar.MILLISECOND);
    }
    
    private static SqlNode convertDate(final LiteralExpressionSegment segment, final String literalValue) {
        if (segment.getLiterals() instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) segment.getLiterals();
            return SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, TimestampString.fromMillisSinceEpoch(timestamp.getTime()), 1, SqlParserPos.ZERO);
        }
        if (segment.getLiterals() instanceof Time) {
            return SqlLiteral.createTime(new TimeString(literalValue), 1, SqlParserPos.ZERO);
        }
        return SqlLiteral.createDate(new DateString(literalValue), SqlParserPos.ZERO);
    }
}

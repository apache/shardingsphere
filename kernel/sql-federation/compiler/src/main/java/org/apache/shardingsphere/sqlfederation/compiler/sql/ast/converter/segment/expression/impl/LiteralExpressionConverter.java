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
import org.apache.calcite.rel.type.RelDataType;
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
import java.util.TimeZone;

/**
 * Literal expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteralExpressionConverter {
    
    /**
     * Convert literal expression segment to sql node.
     *
     * @param segment literal expression segment
     * @param dataType data type
     * @return sql node
     */
    public static Optional<SqlNode> convert(final LiteralExpressionSegment segment, final RelDataType dataType) {
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
            return Optional.of(convertCalendar(segment, literalValue, dataType));
        }
        if (segment.getLiterals() instanceof Date) {
            return Optional.of(convertDate(segment, literalValue, dataType));
        }
        if (segment.getLiterals() instanceof TimestampString) {
            return Optional.of(SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, (TimestampString) segment.getLiterals(), getTimeScale(literalValue, dataType), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof LocalDate) {
            return Optional.of(SqlLiteral.createDate(DateString.fromDaysSinceEpoch((int) ((LocalDate) segment.getLiterals()).toEpochDay()), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof LocalTime) {
            return Optional.of(convertLocalTime(segment, literalValue, dataType));
        }
        if (segment.getLiterals() instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) segment.getLiterals();
            String fraction = literalValue.contains(".") ? literalValue.substring(literalValue.lastIndexOf('.') + 1) : "";
            TimestampString timestampString = TimestampString.fromCalendarFields(toCalendar(dateTime.getYear(), dateTime.getMonthValue(),
                    dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond())).withFraction(fraction);
            return Optional.of(SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, timestampString, getTimeScale(literalValue, dataType), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof ZonedDateTime) {
            return Optional.of(SqlLiteral.createTimestamp(new TimestampWithTimeZoneString(literalValue), getTimeScale(literalValue, dataType), SqlParserPos.ZERO));
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
    
    private static SqlNode convertCalendar(final LiteralExpressionSegment segment, final String literalValue, final RelDataType dataType) {
        Calendar calendar = (Calendar) segment.getLiterals();
        if (hasTimePart(calendar)) {
            return SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, TimestampString.fromCalendarFields(calendar), getTimeScale(literalValue, dataType), SqlParserPos.ZERO);
        }
        return SqlLiteral.createDate(DateString.fromCalendarFields(calendar), SqlParserPos.ZERO);
    }
    
    private static boolean hasTimePart(final Calendar calendar) {
        return 0 != calendar.get(Calendar.HOUR_OF_DAY) || 0 != calendar.get(Calendar.MINUTE) || 0 != calendar.get(Calendar.SECOND) || 0 != calendar.get(Calendar.MILLISECOND);
    }
    
    private static SqlNode convertDate(final LiteralExpressionSegment segment, final String literalValue, final RelDataType dataType) {
        if (segment.getLiterals() instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) segment.getLiterals();
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            String fraction = literalValue.contains(".") ? literalValue.substring(literalValue.lastIndexOf('.') + 1) : "";
            TimestampWithTimeZoneString timestampWithTimeZone = TimestampWithTimeZoneString.fromCalendarFields(toCalendar(dateTime.getYear(), dateTime.getMonthValue(),
                    dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond())).withFraction(fraction).withTimeZone(TimeZone.getDefault());
            return SqlLiteral.createTimestamp(timestampWithTimeZone, getTimeScale(literalValue, dataType), SqlParserPos.ZERO);
        }
        if (segment.getLiterals() instanceof Time) {
            return SqlLiteral.createTime(new TimeString(literalValue), getTimeScale(literalValue, dataType), SqlParserPos.ZERO);
        }
        return SqlLiteral.createDate(new DateString(literalValue), SqlParserPos.ZERO);
    }
    
    private static SqlNode convertLocalTime(final LiteralExpressionSegment segment, final String literalValue, final RelDataType dataType) {
        LocalTime localTime = (LocalTime) segment.getLiterals();
        int nanos = localTime.getNano();
        String formattedValue = 0 == nanos ? DateTimeFormatterFactory.getTimeFormatter().format(localTime) : DateTimeFormatterFactory.getFullTimeFormatter().format(localTime);
        return SqlLiteral.createTime(new TimeString(formattedValue), getTimeScale(literalValue, dataType), SqlParserPos.ZERO);
    }
    
    private static int getTimeScale(final String literalValue, final RelDataType dataType) {
        if (null != dataType && dataType.getScale() > 0) {
            return dataType.getScale();
        }
        return literalValue.contains(".") ? literalValue.substring(literalValue.lastIndexOf(".")).length() : 0;
    }
    
    @SuppressWarnings("MagicConstant")
    private static Calendar toCalendar(final int year, final int month, final int day, final int hour, final int minute, final int second) {
        Calendar result = Calendar.getInstance();
        result.clear();
        result.set(year, month - 1, day, hour, minute, second);
        return result;
    }
}

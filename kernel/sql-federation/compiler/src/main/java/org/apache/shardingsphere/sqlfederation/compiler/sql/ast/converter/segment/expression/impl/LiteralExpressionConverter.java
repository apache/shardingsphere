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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.util.TimeUnit;
import org.apache.calcite.sql.SqlIntervalQualifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlTrimFunction.Flag;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.DateString;
import org.apache.calcite.util.TimeString;
import org.apache.calcite.util.TimestampString;
import org.apache.calcite.util.TimestampWithTimeZoneString;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * Literal expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteralExpressionConverter {
    
    private static final Collection<String> TRIM_FUNCTION_FLAGS = new CaseInsensitiveSet<>(3, 1F);
    
    private static final Collection<String> TIME_UNIT_NAMES = new CaseInsensitiveSet<>(7, 1F);
    
    static {
        TRIM_FUNCTION_FLAGS.add("BOTH");
        TRIM_FUNCTION_FLAGS.add("LEADING");
        TRIM_FUNCTION_FLAGS.add("TRAILING");
        TIME_UNIT_NAMES.add("YEAR");
        TIME_UNIT_NAMES.add("MONTH");
        TIME_UNIT_NAMES.add("WEEK");
        TIME_UNIT_NAMES.add("DAY");
        TIME_UNIT_NAMES.add("HOUR");
        TIME_UNIT_NAMES.add("MINUTE");
        TIME_UNIT_NAMES.add("SECOND");
    }
    
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
        if (TRIM_FUNCTION_FLAGS.contains(literalValue)) {
            return Optional.of(SqlLiteral.createSymbol(Flag.valueOf(literalValue.toUpperCase()), SqlParserPos.ZERO));
        }
        if (TIME_UNIT_NAMES.contains(literalValue)) {
            return Optional.of(new SqlIntervalQualifier(TimeUnit.valueOf(literalValue.toUpperCase()), null, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof Number) {
            return Optional.of(convertNumber(segment, literalValue));
        }
        if (segment.getLiterals() instanceof String) {
            return Optional.of(SqlLiteral.createCharString(literalValue, SqlParserPos.ZERO));
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
        if (segment.getLiterals() instanceof LocalDate) {
            return Optional.of(SqlLiteral.createDate(DateString.fromDaysSinceEpoch((int) ((LocalDate) segment.getLiterals()).toEpochDay()), SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof LocalTime) {
            return Optional.of(SqlLiteral.createTime(new TimeString(literalValue), 1, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof LocalDateTime) {
            return Optional.of(SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, new TimestampString(literalValue), 1, SqlParserPos.ZERO));
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
        return segment.getLiterals() instanceof BigDecimal || segment.getLiterals() instanceof BigInteger
                ? SqlLiteral.createApproxNumeric(literalValue, SqlParserPos.ZERO)
                : SqlLiteral.createExactNumeric(literalValue, SqlParserPos.ZERO);
    }
    
    private static SqlNode convertCalendar(final LiteralExpressionSegment segment) {
        Calendar calendar = (Calendar) segment.getLiterals();
        return hasTimePart(calendar)
                ? SqlLiteral.createTimestamp(SqlTypeName.TIMESTAMP, TimestampString.fromCalendarFields(calendar), 1, SqlParserPos.ZERO)
                : SqlLiteral.createDate(DateString.fromCalendarFields(calendar), SqlParserPos.ZERO);
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

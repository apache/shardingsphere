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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime;

import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.shardingsphere.infra.util.exception.external.sql.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.InvalidDatetimeFormatException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interval sharding algorithm.
 */
public final class IntervalShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {
    
    private static final String DATE_TIME_PATTERN_KEY = "datetime-pattern";
    
    private static final String DATE_TIME_LOWER_KEY = "datetime-lower";
    
    private static final String DATE_TIME_UPPER_KEY = "datetime-upper";
    
    private static final String SHARDING_SUFFIX_FORMAT_KEY = "sharding-suffix-pattern";
    
    private static final String INTERVAL_AMOUNT_KEY = "datetime-interval-amount";
    
    private static final String INTERVAL_UNIT_KEY = "datetime-interval-unit";
    
    @Getter
    private Properties props;
    
    private DateTimeFormatter dateTimeFormatter;
    
    private int dateTimePatternLength;
    
    private TemporalAccessor dateTimeLower;
    
    private TemporalAccessor dateTimeUpper;
    
    private DateTimeFormatter tableSuffixPattern;
    
    private int stepAmount;
    
    private ChronoUnit stepUnit;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        String dateTimePattern = getDateTimePattern(props);
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        dateTimePatternLength = dateTimePattern.length();
        dateTimeLower = getDateTimeLower(props, dateTimePattern);
        dateTimeUpper = getDateTimeUpper(props, dateTimePattern);
        tableSuffixPattern = getTableSuffixPattern(props);
        stepAmount = Integer.parseInt(props.getOrDefault(INTERVAL_AMOUNT_KEY, 1).toString());
        stepUnit = props.containsKey(INTERVAL_UNIT_KEY) ? getStepUnit(props.getProperty(INTERVAL_UNIT_KEY)) : ChronoUnit.DAYS;
    }
    
    private String getDateTimePattern(final Properties props) {
        Preconditions.checkArgument(props.containsKey(DATE_TIME_PATTERN_KEY), "%s can not be null.", DATE_TIME_PATTERN_KEY);
        return props.getProperty(DATE_TIME_PATTERN_KEY);
    }
    
    private TemporalAccessor getDateTimeLower(final Properties props, final String dateTimePattern) {
        Preconditions.checkArgument(props.containsKey(DATE_TIME_LOWER_KEY), "%s can not be null.", DATE_TIME_LOWER_KEY);
        return getDateTime(DATE_TIME_LOWER_KEY, props.getProperty(DATE_TIME_LOWER_KEY), dateTimePattern);
    }
    
    private TemporalAccessor getDateTimeUpper(final Properties props, final String dateTimePattern) {
        return props.containsKey(DATE_TIME_UPPER_KEY) ? getDateTime(DATE_TIME_UPPER_KEY, props.getProperty(DATE_TIME_UPPER_KEY), dateTimePattern) : LocalDateTime.now();
    }
    
    private TemporalAccessor getDateTime(final String dateTimeKey, final String dateTimeValue, final String dateTimePattern) {
        try {
            return dateTimeFormatter.parse(dateTimeValue);
        } catch (final DateTimeParseException ex) {
            throw new InvalidDatetimeFormatException(dateTimeKey, dateTimeValue, dateTimePattern);
        }
    }
    
    private DateTimeFormatter getTableSuffixPattern(final Properties props) {
        Preconditions.checkArgument(props.containsKey(SHARDING_SUFFIX_FORMAT_KEY), "%s can not be null.", SHARDING_SUFFIX_FORMAT_KEY);
        return DateTimeFormatter.ofPattern(props.getProperty(SHARDING_SUFFIX_FORMAT_KEY));
    }
    
    private ChronoUnit getStepUnit(final String stepUnit) {
        for (ChronoUnit each : ChronoUnit.values()) {
            if (each.toString().equalsIgnoreCase(stepUnit)) {
                return each;
            }
        }
        throw new UnsupportedSQLOperationException(String.format("Cannot find step unit for specified %s property: `%s`", INTERVAL_UNIT_KEY, stepUnit));
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        return doSharding(availableTargetNames, Range.singleton(shardingValue.getValue())).stream().findFirst().orElse(null);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return doSharding(availableTargetNames, shardingValue.getValueRange());
    }
    
    private Collection<String> doSharding(final Collection<String> availableTargetNames, final Range<Comparable<?>> range) {
        TemporalAccessor calculateTime = dateTimeLower;
        if (!calculateTime.isSupported(ChronoField.NANO_OF_DAY)) {
            if (calculateTime.isSupported(ChronoField.EPOCH_DAY)) {
                return doShardingInLocalDate(availableTargetNames, range, calculateTime);
            }
            if (calculateTime.isSupported(ChronoField.YEAR) && calculateTime.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return doShardingInYearMonth(availableTargetNames, range, calculateTime);
            }
            if (calculateTime.isSupported(ChronoField.YEAR)) {
                return doShardingInYear(availableTargetNames, range, calculateTime);
            }
            if (calculateTime.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return doShardingInMonth(availableTargetNames, range, calculateTime);
            }
        }
        if (!calculateTime.isSupported(ChronoField.EPOCH_DAY)) {
            return doShardingInLocalTime(availableTargetNames, range, calculateTime);
        }
        return doShardingInLocalDateTime(availableTargetNames, range, calculateTime);
    }
    
    private Collection<String> doShardingInLocalDateTime(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Set<String> result = new HashSet<>();
        LocalDateTime calculateTimeAsView = LocalDateTime.from(calculateTime);
        LocalDateTime dateTimeUpperAsLocalDateTime = LocalDateTime.from(dateTimeUpper);
        LocalDateTime dateTimeLowerAsLocalDateTime = LocalDateTime.from(dateTimeLower);
        while (!calculateTimeAsView.isAfter(dateTimeUpperAsLocalDateTime)) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, calculateTimeAsView.plus(stepAmount, stepUnit)), range, dateTimeLowerAsLocalDateTime, dateTimeUpperAsLocalDateTime)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = calculateTimeAsView.plus(stepAmount, stepUnit);
        }
        return result;
    }
    
    private Collection<String> doShardingInLocalTime(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Set<String> result = new HashSet<>();
        LocalTime dateTimeUpperAsLocalTime = dateTimeUpper.query(TemporalQueries.localTime());
        LocalTime dateTimeLowerAsLocalTime = dateTimeLower.query(TemporalQueries.localTime());
        LocalTime calculateTimeAsView = calculateTime.query(TemporalQueries.localTime());
        while (!calculateTimeAsView.isAfter(dateTimeUpperAsLocalTime)) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, calculateTimeAsView.plus(stepAmount, stepUnit)), range, dateTimeLowerAsLocalTime, dateTimeUpperAsLocalTime)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = calculateTimeAsView.plus(stepAmount, stepUnit);
        }
        return result;
    }
    
    private Collection<String> doShardingInLocalDate(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Set<String> result = new HashSet<>();
        LocalDate dateTimeUpperAsLocalDate = dateTimeUpper.query(TemporalQueries.localDate());
        LocalDate dateTimeLowerAsLocalDate = dateTimeLower.query(TemporalQueries.localDate());
        LocalDate calculateTimeAsView = calculateTime.query(TemporalQueries.localDate());
        while (!calculateTimeAsView.isAfter(dateTimeUpperAsLocalDate)) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, calculateTimeAsView.plus(stepAmount, stepUnit)), range, dateTimeLowerAsLocalDate, dateTimeUpperAsLocalDate)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = calculateTimeAsView.plus(stepAmount, stepUnit);
        }
        return result;
    }
    
    private Collection<String> doShardingInYear(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Set<String> result = new HashSet<>();
        Year dateTimeUpperAsYear = dateTimeUpper.query(Year::from);
        Year dateTimeLowerAsYear = dateTimeLower.query(Year::from);
        Year calculateTimeAsView = calculateTime.query(Year::from);
        while (!calculateTimeAsView.isAfter(dateTimeUpperAsYear)) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, calculateTimeAsView.plus(stepAmount, stepUnit)), range, dateTimeLowerAsYear, dateTimeUpperAsYear)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = calculateTimeAsView.plus(stepAmount, stepUnit);
        }
        return result;
    }
    
    private Collection<String> doShardingInMonth(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Set<String> result = new HashSet<>();
        Month dateTimeUpperAsMonth = dateTimeUpper.query(Month::from);
        Month dateTimeLowerAsMonth = dateTimeLower.query(Month::from);
        Month calculateTimeAsView = calculateTime.query(Month::from);
        while (!(calculateTimeAsView.getValue() > dateTimeUpperAsMonth.getValue()) && (calculateTimeAsView.getValue() + stepAmount) <= Month.DECEMBER.getValue()) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, calculateTimeAsView.plus(stepAmount)), range, dateTimeLowerAsMonth, dateTimeUpperAsMonth)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = calculateTimeAsView.plus(stepAmount);
        }
        return result;
    }
    
    private Collection<String> doShardingInYearMonth(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Set<String> result = new HashSet<>();
        YearMonth dateTimeUpperAsYearMonth = dateTimeUpper.query(YearMonth::from);
        YearMonth dateTimeLowerAsYearMonth = dateTimeLower.query(YearMonth::from);
        YearMonth calculateTimeAsView = calculateTime.query(YearMonth::from);
        while (!calculateTimeAsView.isAfter(dateTimeUpperAsYearMonth)) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, calculateTimeAsView.plus(stepAmount, stepUnit)), range, dateTimeLowerAsYearMonth, dateTimeUpperAsYearMonth)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = calculateTimeAsView.plus(stepAmount, stepUnit);
        }
        return result;
    }
    
    private boolean hasIntersection(final Range<LocalDateTime> calculateRange, final Range<Comparable<?>> range, final LocalDateTime dateTimeLower, final LocalDateTime dateTimeUpper) {
        LocalDateTime lower = range.hasLowerBound() ? parseLocalDateTime(range.lowerEndpoint()) : dateTimeLower;
        LocalDateTime upper = range.hasUpperBound() ? parseLocalDateTime(range.upperEndpoint()) : dateTimeUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<LocalDateTime> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
        return calculateRange.isConnected(dateTimeRange) && !calculateRange.intersection(dateTimeRange).isEmpty();
    }
    
    private boolean hasIntersection(final Range<LocalDate> calculateRange, final Range<Comparable<?>> range, final LocalDate dateTimeLower, final LocalDate dateTimeUpper) {
        LocalDate lower = range.hasLowerBound() ? parseLocalDate(range.lowerEndpoint()) : dateTimeLower;
        LocalDate upper = range.hasUpperBound() ? parseLocalDate(range.upperEndpoint()) : dateTimeUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<LocalDate> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
        return calculateRange.isConnected(dateTimeRange) && !calculateRange.intersection(dateTimeRange).isEmpty();
    }
    
    private boolean hasIntersection(final Range<LocalTime> calculateRange, final Range<Comparable<?>> range, final LocalTime dateTimeLower, final LocalTime dateTimeUpper) {
        LocalTime lower = range.hasLowerBound() ? parseLocalTime(range.lowerEndpoint()) : dateTimeLower;
        LocalTime upper = range.hasUpperBound() ? parseLocalTime(range.upperEndpoint()) : dateTimeUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<LocalTime> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
        return calculateRange.isConnected(dateTimeRange) && !calculateRange.intersection(dateTimeRange).isEmpty();
    }
    
    private boolean hasIntersection(final Range<Year> calculateRange, final Range<Comparable<?>> range, final Year dateTimeLower, final Year dateTimeUpper) {
        Year lower = range.hasLowerBound() ? parseYear(range.lowerEndpoint()) : dateTimeLower;
        Year upper = range.hasUpperBound() ? parseYear(range.upperEndpoint()) : dateTimeUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<Year> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
        return calculateRange.isConnected(dateTimeRange) && !calculateRange.intersection(dateTimeRange).isEmpty();
    }
    
    private boolean hasIntersection(final Range<Month> calculateRange, final Range<Comparable<?>> range, final Month dateTimeLower, final Month dateTimeUpper) {
        Month lower = range.hasLowerBound() ? parseMonth(range.lowerEndpoint()) : dateTimeLower;
        Month upper = range.hasUpperBound() ? parseMonth(range.upperEndpoint()) : dateTimeUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<Month> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
        return calculateRange.isConnected(dateTimeRange) && !calculateRange.intersection(dateTimeRange).isEmpty();
    }
    
    private boolean hasIntersection(final Range<YearMonth> calculateRange, final Range<Comparable<?>> range, final YearMonth dateTimeLower, final YearMonth dateTimeUpper) {
        YearMonth lower = range.hasLowerBound() ? parseYearMonth(range.lowerEndpoint()) : dateTimeLower;
        YearMonth upper = range.hasUpperBound() ? parseYearMonth(range.upperEndpoint()) : dateTimeUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<YearMonth> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
        return calculateRange.isConnected(dateTimeRange) && !calculateRange.intersection(dateTimeRange).isEmpty();
    }
    
    private LocalDateTime parseLocalDateTime(final Comparable<?> endpoint) {
        return LocalDateTime.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private LocalDate parseLocalDate(final Comparable<?> endpoint) {
        return LocalDate.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private LocalTime parseLocalTime(final Comparable<?> endpoint) {
        return LocalTime.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private Year parseYear(final Comparable<?> endpoint) {
        return Year.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private Month parseMonth(final Comparable<?> endpoint) {
        return Month.of(Integer.parseInt(getDateTimeText(endpoint).substring(0, dateTimePatternLength)));
    }
    
    private YearMonth parseYearMonth(final Comparable<?> endpoint) {
        return YearMonth.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private String getDateTimeText(final Comparable<?> endpoint) {
        if (endpoint instanceof Instant) {
            return dateTimeFormatter.withZone(ZoneId.systemDefault()).format((Instant) endpoint);
        }
        if (endpoint instanceof TemporalAccessor) {
            return dateTimeFormatter.format((TemporalAccessor) endpoint);
        }
        if (endpoint instanceof Date) {
            return dateTimeFormatter.format(((Date) endpoint).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        return endpoint.toString();
    }
    
    private Collection<String> getMatchedTables(final TemporalAccessor dateTime, final Collection<String> availableTargetNames) {
        String tableSuffix;
        if (!dateTime.isSupported(ChronoField.NANO_OF_DAY)) {
            if (dateTime.isSupported(ChronoField.EPOCH_DAY)) {
                tableSuffix = tableSuffixPattern.format(dateTime.query(TemporalQueries.localDate()));
                return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
            }
            if (dateTime.isSupported(ChronoField.YEAR) && dateTime.isSupported(ChronoField.MONTH_OF_YEAR)) {
                tableSuffix = tableSuffixPattern.format(dateTime.query(YearMonth::from));
                return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
            }
            if (dateTime.isSupported(ChronoField.YEAR)) {
                tableSuffix = tableSuffixPattern.format(dateTime.query(Year::from));
                return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
            }
            if (dateTime.isSupported(ChronoField.MONTH_OF_YEAR)) {
                tableSuffix = tableSuffixPattern.format(dateTime.query(Month::from));
                return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
            }
        }
        if (!dateTime.isSupported(ChronoField.EPOCH_DAY)) {
            tableSuffix = dateTime.query(TemporalQueries.localTime()).format(tableSuffixPattern);
            return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
        }
        tableSuffix = LocalDateTime.from(dateTime).format(tableSuffixPattern);
        return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
    }
    
    @Override
    public String getType() {
        return "INTERVAL";
    }
}

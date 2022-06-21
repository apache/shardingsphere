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
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.ZoneId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Date;
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
            throw new ShardingSphereConfigurationException("Invalid %s, datetime pattern should be `%s`, value is `%s`", dateTimeKey, dateTimePattern, dateTimeValue);
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
        throw new UnsupportedOperationException(String.format("Cannot find step unit for specified %s property: `%s`", INTERVAL_UNIT_KEY, stepUnit));
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
        LocalDate queryToLocalDate = calculateTime.query(TemporalQueries.localDate());
        LocalTime queryToLocalTime = calculateTime.query(TemporalQueries.localTime());
        if (null == queryToLocalTime) {
            return doShardingInLocalDate(availableTargetNames, range, calculateTime);
        }
        if (null == queryToLocalDate) {
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
    
    private LocalDateTime parseLocalDateTime(final Comparable<?> endpoint) {
        return LocalDateTime.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private LocalDate parseLocalDate(final Comparable<?> endpoint) {
        return LocalDate.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
    }
    
    private LocalTime parseLocalTime(final Comparable<?> endpoint) {
        return LocalTime.parse(getDateTimeText(endpoint).substring(0, dateTimePatternLength), dateTimeFormatter);
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
        LocalDate localDate = dateTime.query(TemporalQueries.localDate());
        LocalTime localTime = dateTime.query(TemporalQueries.localTime());
        String tableSuffix;
        if (null == localTime) {
            tableSuffix = localDate.format(tableSuffixPattern);
            return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
        }
        if (null == localDate) {
            tableSuffix = localTime.format(tableSuffixPattern);
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

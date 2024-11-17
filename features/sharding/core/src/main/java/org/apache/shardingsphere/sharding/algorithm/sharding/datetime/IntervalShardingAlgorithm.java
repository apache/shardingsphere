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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.TemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTimeTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalTimeTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.MonthTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearMonthTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearTemporalHandler;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.data.InvalidDatetimeFormatException;
import org.apache.shardingsphere.sharding.exception.data.NullShardingValueException;

import java.time.Instant;
import java.time.LocalDateTime;
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
import java.util.HashSet;
import java.util.Properties;
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
    
    private String dateTimePatternString;
    
    private DateTimeFormatter dateTimeFormatter;
    
    private int dateTimePatternLength;
    
    private TemporalAccessor dateTimeLower;
    
    private TemporalAccessor dateTimeUpper;
    
    private DateTimeFormatter tableSuffixPattern;
    
    private int stepAmount;
    
    private ChronoUnit stepUnit;
    
    @Override
    public void init(final Properties props) {
        dateTimePatternString = getDateTimePattern(props);
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePatternString);
        dateTimePatternLength = dateTimePatternString.length();
        dateTimeLower = getDateTimeLower(props, dateTimePatternString);
        dateTimeUpper = getDateTimeUpper(props, dateTimePatternString);
        tableSuffixPattern = getTableSuffixPattern(props);
        stepAmount = Integer.parseInt(props.getOrDefault(INTERVAL_AMOUNT_KEY, 1).toString());
        stepUnit = props.containsKey(INTERVAL_UNIT_KEY) ? getStepUnit(props.getProperty(INTERVAL_UNIT_KEY)) : ChronoUnit.DAYS;
    }
    
    private String getDateTimePattern(final Properties props) {
        ShardingSpherePreconditions.checkContainsKey(props, DATE_TIME_PATTERN_KEY, () -> new AlgorithmInitializationException(this, String.format("%s can not be null", DATE_TIME_PATTERN_KEY)));
        return props.getProperty(DATE_TIME_PATTERN_KEY);
    }
    
    private TemporalAccessor getDateTimeLower(final Properties props, final String dateTimePattern) {
        ShardingSpherePreconditions.checkContainsKey(props, DATE_TIME_LOWER_KEY, () -> new AlgorithmInitializationException(this, String.format("%s can not be null.", DATE_TIME_LOWER_KEY)));
        return getDateTime(DATE_TIME_LOWER_KEY, props.getProperty(DATE_TIME_LOWER_KEY), dateTimePattern);
    }
    
    private TemporalAccessor getDateTimeUpper(final Properties props, final String dateTimePattern) {
        return props.containsKey(DATE_TIME_UPPER_KEY) ? getDateTime(DATE_TIME_UPPER_KEY, props.getProperty(DATE_TIME_UPPER_KEY), dateTimePattern) : LocalDateTime.now();
    }
    
    private TemporalAccessor getDateTime(final String dateTimeKey, final String dateTimeValue, final String dateTimePattern) {
        try {
            return dateTimeFormatter.parse(dateTimeValue);
        } catch (final DateTimeParseException ignored) {
            throw new InvalidDatetimeFormatException(dateTimeKey, dateTimeValue, dateTimePattern);
        }
    }
    
    private DateTimeFormatter getTableSuffixPattern(final Properties props) {
        String suffix = props.getProperty(SHARDING_SUFFIX_FORMAT_KEY);
        ShardingSpherePreconditions.checkNotEmpty(suffix, () -> new AlgorithmInitializationException(this, String.format("%s can not be null or empty.", SHARDING_SUFFIX_FORMAT_KEY)));
        return DateTimeFormatter.ofPattern(suffix);
    }
    
    private ChronoUnit getStepUnit(final String stepUnit) {
        for (ChronoUnit each : ChronoUnit.values()) {
            if (each.toString().equalsIgnoreCase(stepUnit)) {
                return each;
            }
        }
        throw new UnsupportedSQLOperationException(String.format("Cannot find step unit for specified %s property: `%s`", INTERVAL_UNIT_KEY, stepUnit));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        ShardingSpherePreconditions.checkNotNull(shardingValue.getValue(), NullShardingValueException::new);
        Range<Comparable<?>> range = Range.singleton(shardingValue.getValue());
        return ((Collection<String>) getMatchedTables(availableTargetNames, range, createTemporalParser())).stream().findFirst().orElse(null);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return getMatchedTables(availableTargetNames, shardingValue.getValueRange(), createTemporalParser());
    }
    
    @SuppressWarnings("rawtypes")
    private TemporalHandler createTemporalParser() {
        if (!dateTimeLower.isSupported(ChronoField.NANO_OF_DAY)) {
            if (dateTimeLower.isSupported(ChronoField.EPOCH_DAY)) {
                return new LocalDateTemporalHandler();
            }
            if (dateTimeLower.isSupported(ChronoField.YEAR) && dateTimeLower.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return new YearMonthTemporalHandler();
            }
            if (dateTimeLower.isSupported(ChronoField.YEAR)) {
                return new YearTemporalHandler();
            }
            if (dateTimeLower.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return new MonthTemporalHandler();
            }
        }
        return dateTimeLower.isSupported(ChronoField.EPOCH_DAY) ? new LocalDateTimeTemporalHandler() : new LocalTimeTemporalHandler();
    }
    
    private <T extends TemporalAccessor & Comparable<?>> Collection<String> getMatchedTables(final Collection<String> availableTargetNames,
                                                                                             final Range<Comparable<?>> range, final TemporalHandler<T> temporalHandler) {
        Collection<String> result = new HashSet<>();
        T dateTimeUpper = temporalHandler.convertTo(this.dateTimeUpper);
        T dateTimeLower = temporalHandler.convertTo(this.dateTimeLower);
        T calculateTimeAsView = temporalHandler.convertTo(this.dateTimeLower);
        while (!temporalHandler.isAfter(calculateTimeAsView, dateTimeUpper, stepAmount)) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView,
                    temporalHandler.add(calculateTimeAsView, stepAmount, stepUnit)), range, dateTimeLower, dateTimeUpper, temporalHandler)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = temporalHandler.add(calculateTimeAsView, stepAmount, stepUnit);
        }
        return result;
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
    
    private <T extends TemporalAccessor & Comparable<?>> boolean hasIntersection(final Range<T> calculateRange, final Range<Comparable<?>> range, final T temporalLower, final T temporalUpper,
                                                                                 final TemporalHandler<T> temporalHandler) {
        T lower = range.hasLowerBound() ? parseTemporal(range.lowerEndpoint(), temporalHandler) : temporalLower;
        T upper = range.hasUpperBound() ? parseTemporal(range.upperEndpoint(), temporalHandler) : temporalUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<T> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
        return calculateRange.isConnected(dateTimeRange) && !calculateRange.intersection(dateTimeRange).isEmpty();
    }
    
    private <T extends TemporalAccessor> T parseTemporal(final Comparable<?> endpoint, final TemporalHandler<T> temporalHandler) {
        String dateTimeText = getDateTimeText(endpoint);
        return dateTimeText.length() >= dateTimePatternLength
                ? temporalHandler.parse(dateTimeText.substring(0, dateTimePatternLength), dateTimeFormatter)
                : temporalHandler.parse(dateTimeText, createRelaxedDateTimeFormatter(dateTimeText));
    }
    
    /*
     * When the sharding key is a {@link String} and the length of this {@link String} is less than the `datetime-pattern` set by the algorithm, ShardingSphere will try to use a substring of
     * `datetime-pattern` to parse the sharding key. This is to be compatible with the behavior of ORM libraries such as <a href="https://github.com/go-gorm/gorm">go-gorm/gorm</a>.
     *
     * @param dateTimeText Sharding key with class name {@link String}
     *
     * @return Child `datetime-pattern`, the pattern length is consistent with the shard key.
     */
    private DateTimeFormatter createRelaxedDateTimeFormatter(final String dateTimeText) {
        return DateTimeFormatter.ofPattern(dateTimePatternString.substring(0, dateTimeText.length()));
    }
    
    private String getDateTimeText(final Comparable<?> endpoint) {
        if (endpoint instanceof Instant) {
            return dateTimeFormatter.format(((Instant) endpoint).atZone(ZoneId.systemDefault()));
        }
        if (endpoint instanceof TemporalAccessor) {
            return dateTimeFormatter.format((TemporalAccessor) endpoint);
        }
        if (endpoint instanceof java.sql.Date) {
            return dateTimeFormatter.format(((java.sql.Date) endpoint).toLocalDate());
        }
        if (endpoint instanceof java.util.Date) {
            return dateTimeFormatter.format(((java.util.Date) endpoint).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        return endpoint.toString();
    }
    
    @Override
    public String getType() {
        return "INTERVAL";
    }
}

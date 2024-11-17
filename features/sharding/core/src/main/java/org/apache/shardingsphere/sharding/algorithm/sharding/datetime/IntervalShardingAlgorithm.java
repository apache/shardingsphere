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
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTemporalParser;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTimeTemporalParser;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalTimeTemporalParser;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.TemporalParser;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearMonthTemporalParser;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearTemporalParser;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.data.InvalidDatetimeFormatException;
import org.apache.shardingsphere.sharding.exception.data.NullShardingValueException;

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
import java.time.temporal.Temporal;
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
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        ShardingSpherePreconditions.checkNotNull(shardingValue.getValue(), NullShardingValueException::new);
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
        return calculateTime.isSupported(ChronoField.EPOCH_DAY)
                ? doShardingInLocalDateTime(availableTargetNames, range, calculateTime)
                : doShardingInLocalTime(availableTargetNames, range, calculateTime);
    }
    
    private Collection<String> doShardingInLocalDate(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        LocalDate dateTimeUpper = this.dateTimeUpper.query(TemporalQueries.localDate());
        LocalDate dateTimeLower = this.dateTimeLower.query(TemporalQueries.localDate());
        LocalDate calculateTimeAsView = calculateTime.query(TemporalQueries.localDate());
        LocalDateTemporalParser temporalParser = new LocalDateTemporalParser();
        return getMatchedTables(availableTargetNames, range, dateTimeUpper, dateTimeLower, calculateTimeAsView, temporalParser);
    }
    
    private Collection<String> doShardingInYearMonth(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        YearMonth dateTimeUpper = this.dateTimeUpper.query(YearMonth::from);
        YearMonth dateTimeLower = this.dateTimeLower.query(YearMonth::from);
        YearMonth calculateTimeAsView = calculateTime.query(YearMonth::from);
        YearMonthTemporalParser temporalParser = new YearMonthTemporalParser();
        return getMatchedTables(availableTargetNames, range, dateTimeUpper, dateTimeLower, calculateTimeAsView, temporalParser);
    }
    
    private Collection<String> doShardingInYear(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Year dateTimeUpper = this.dateTimeUpper.query(Year::from);
        Year dateTimeLower = this.dateTimeLower.query(Year::from);
        Year calculateTimeAsView = calculateTime.query(Year::from);
        YearTemporalParser temporalParser = new YearTemporalParser();
        return getMatchedTables(availableTargetNames, range, dateTimeUpper, dateTimeLower, calculateTimeAsView, temporalParser);
    }
    
    private Collection<String> doShardingInMonth(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        Collection<String> result = new HashSet<>();
        Month dateTimeUpper = this.dateTimeUpper.query(Month::from);
        Month dateTimeLower = this.dateTimeLower.query(Month::from);
        Month calculateTimeAsView = calculateTime.query(Month::from);
        while (calculateTimeAsView.getValue() <= dateTimeUpper.getValue() && (calculateTimeAsView.getValue() + stepAmount) <= Month.DECEMBER.getValue()) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, calculateTimeAsView.plus(stepAmount)), range, dateTimeLower, dateTimeUpper)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames));
            }
            calculateTimeAsView = calculateTimeAsView.plus(stepAmount);
        }
        return result;
    }
    
    private Collection<String> doShardingInLocalDateTime(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        LocalDateTime calculateTimeAsView = LocalDateTime.from(calculateTime);
        LocalDateTime dateTimeUpper = LocalDateTime.from(this.dateTimeUpper);
        LocalDateTime dateTimeLower = LocalDateTime.from(this.dateTimeLower);
        LocalDateTimeTemporalParser temporalParser = new LocalDateTimeTemporalParser();
        return getMatchedTables(availableTargetNames, range, dateTimeUpper, dateTimeLower, calculateTimeAsView, temporalParser);
    }
    
    private Collection<String> doShardingInLocalTime(final Collection<String> availableTargetNames, final Range<Comparable<?>> range, final TemporalAccessor calculateTime) {
        LocalTime dateTimeUpper = this.dateTimeUpper.query(TemporalQueries.localTime());
        LocalTime dateTimeLower = this.dateTimeLower.query(TemporalQueries.localTime());
        LocalTime calculateTimeAsView = calculateTime.query(TemporalQueries.localTime());
        LocalTimeTemporalParser temporalParser = new LocalTimeTemporalParser();
        return getMatchedTables(availableTargetNames, range, dateTimeUpper, dateTimeLower, calculateTimeAsView, temporalParser);
    }
    
    private <T extends Temporal & Comparable<?>> boolean hasIntersection(final Range<T> calculateRange, final Range<Comparable<?>> range, final T temporalLower, final T temporalUpper,
                                                                         final TemporalParser<T> temporalParser) {
        T lower = range.hasLowerBound() ? parseTemporal(range.lowerEndpoint(), temporalParser) : temporalLower;
        T upper = range.hasUpperBound() ? parseTemporal(range.upperEndpoint(), temporalParser) : temporalUpper;
        BoundType lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.CLOSED;
        BoundType upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.CLOSED;
        Range<T> dateTimeRange = Range.range(lower, lowerBoundType, upper, upperBoundType);
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
    
    private <T extends Temporal> T parseTemporal(final Comparable<?> endpoint, final TemporalParser<T> temporalParser) {
        String dateTimeText = getDateTimeText(endpoint);
        return dateTimeText.length() >= dateTimePatternLength
                ? temporalParser.parse(dateTimeText.substring(0, dateTimePatternLength), dateTimeFormatter)
                : temporalParser.parse(dateTimeText, createRelaxedDateTimeFormatter(dateTimeText));
    }
    
    /*
     * After the sharding key is formatted as a {@link String},
     * if the length of the {@link String} is less than `datetime-pattern`,
     * it usually means there is a problem with the sharding key.
     * @param endpoint A class carrying time information with an unknown class name.
     * @return {@link java.time.Month}
     */
    private Month parseMonth(final Comparable<?> endpoint) {
        return Month.of(Integer.parseInt(getDateTimeText(endpoint).substring(0, dateTimePatternLength)));
    }
    
    /*
     * When the sharding key is a {@link String} and the length of this {@link String} is less than the `datetime-pattern` set by the algorithm,
     * ShardingSphere will try to use a substring of `datetime-pattern` to parse the sharding key.
     * This is to be compatible with the behavior of ORM libraries such as <a href="https://github.com/go-gorm/gorm">go-gorm/gorm</a>.
     * @param dateTimeText Sharding key with class name {@link String}
     * @return Child `datetime-pattern`, the pattern length is consistent with the shard key.
     */
    private DateTimeFormatter createRelaxedDateTimeFormatter(final String dateTimeText) {
        String dateTimeFormatterString = dateTimePatternString.substring(0, dateTimeText.length());
        return DateTimeFormatter.ofPattern(dateTimeFormatterString);
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
    
    private <T extends Temporal & Comparable<?>> Collection<String> getMatchedTables(final Collection<String> availableTargetNames, final Range<Comparable<?>> range,
                                                                                     final T dateTimeUpperAsLocalTime, final T dateTimeLowerAsLocalTime,
                                                                                     final T calculateTimeAsView, final TemporalParser<T> temporalParser) {
        Collection<String> result = new HashSet<>();
        T newCalculateTimeAsView = calculateTimeAsView;
        while (!temporalParser.isAfter(newCalculateTimeAsView, dateTimeUpperAsLocalTime)) {
            if (hasIntersection(Range.closedOpen(newCalculateTimeAsView,
                    temporalParser.plus(newCalculateTimeAsView, stepAmount, stepUnit)), range, dateTimeLowerAsLocalTime, dateTimeUpperAsLocalTime, temporalParser)) {
                result.addAll(getMatchedTables(newCalculateTimeAsView, availableTargetNames));
            }
            newCalculateTimeAsView = temporalParser.plus(newCalculateTimeAsView, stepAmount, stepUnit);
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
    
    @Override
    public String getType() {
        return "INTERVAL";
    }
}

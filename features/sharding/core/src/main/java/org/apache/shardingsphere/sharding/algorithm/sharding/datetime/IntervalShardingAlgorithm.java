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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.TemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.TemporalHandlerFactory;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.data.InvalidDatetimeFormatException;
import org.apache.shardingsphere.sharding.exception.data.NullShardingValueException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
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
        Range<Comparable<?>> range = Range.singleton(shardingValue.getValue());
        return getMatchedTables(availableTargetNames, range).stream().findFirst().orElse(null);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return getMatchedTables(availableTargetNames, shardingValue.getValueRange());
    }
    
    @SuppressWarnings("unchecked")
    private <T extends TemporalAccessor & Comparable<?>> Collection<String> getMatchedTables(final Collection<String> availableTargetNames, final Range<Comparable<?>> range) {
        Collection<String> result = new HashSet<>();
        TemporalHandler<T> temporalHandler = TemporalHandlerFactory.newInstance(dateTimeLower);
        T dateTimeUpper = temporalHandler.convertTo(this.dateTimeUpper);
        T dateTimeLower = temporalHandler.convertTo(this.dateTimeLower);
        T calculateTimeAsView = temporalHandler.convertTo(this.dateTimeLower);
        while (!temporalHandler.isAfter(calculateTimeAsView, dateTimeUpper, stepAmount)) {
            if (hasIntersection(Range.closedOpen(calculateTimeAsView, temporalHandler.add(calculateTimeAsView, stepAmount, stepUnit)), range, dateTimeLower, dateTimeUpper, temporalHandler)) {
                result.addAll(getMatchedTables(calculateTimeAsView, availableTargetNames, temporalHandler));
            }
            calculateTimeAsView = temporalHandler.add(calculateTimeAsView, stepAmount, stepUnit);
        }
        return result;
    }
    
    private <T extends TemporalAccessor & Comparable<?>> Collection<String> getMatchedTables(final TemporalAccessor calculateTimeAsView,
                                                                                             final Collection<String> availableTargetNames, final TemporalHandler<T> temporalHandler) {
        return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffixPattern.format(temporalHandler.convertTo(calculateTimeAsView)))).collect(Collectors.toSet());
    }
    
    private <T extends TemporalAccessor & Comparable<?>> boolean hasIntersection(final Range<T> calculateRange, final Range<Comparable<?>> range,
                                                                                 final T temporalLower, final T temporalUpper, final TemporalHandler<T> temporalHandler) {
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
    
    @Override
    public String getType() {
        return "INTERVAL";
    }
}

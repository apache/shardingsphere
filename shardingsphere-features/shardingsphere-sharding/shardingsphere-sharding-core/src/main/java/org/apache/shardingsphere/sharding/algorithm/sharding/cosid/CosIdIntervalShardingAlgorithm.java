/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.algorithm.sharding.cosid;

import com.google.common.base.Strings;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import lombok.Getter;
import lombok.Setter;
import me.ahoo.cosid.sharding.IntervalStep;
import me.ahoo.cosid.util.LocalDateTimeConvert;
import me.ahoo.cosid.sharding.IntervalTimeline;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

/**
 * Interval-based time range sharding algorithm.
 */
public final class CosIdIntervalShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    public static final String TYPE = CosIdAlgorithm.TYPE_PREFIX + "INTERVAL";

    public static final String DATE_TIME_PATTERN_KEY = "datetime-pattern";

    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);

    public static final String DATE_TIME_LOWER_KEY = "datetime-lower";

    public static final String DATE_TIME_UPPER_KEY = "datetime-upper";

    public static final String SHARDING_SUFFIX_FORMAT_KEY = "sharding-suffix-pattern";

    public static final String INTERVAL_UNIT_KEY = "datetime-interval-unit";

    public static final String INTERVAL_AMOUNT_KEY = "datetime-interval-amount";

    public static final String TIMESTAMP_SECOND_TYPE = "SECOND";

    public static final String ZONE_ID_KEY = "zone-id";

    public static final String TIMESTAMP_TYPE_KEY = "ts-type";

    @Getter
    @Setter
    private Properties props = new Properties();

    private volatile AlgorithmConfig algorithmConfig;

    @Override
    public void init() {
        String logicNamePrefix = PropertiesUtil.getRequiredValue(getProps(), CosIdAlgorithm.LOGIC_NAME_PREFIX_KEY);
        LocalDateTime effectiveLower = LocalDateTime.parse(PropertiesUtil.getRequiredValue(getProps(), DATE_TIME_LOWER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        LocalDateTime effectiveUpper = LocalDateTime.parse(PropertiesUtil.getRequiredValue(getProps(), DATE_TIME_UPPER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        DateTimeFormatter suffixFormatter = DateTimeFormatter.ofPattern(PropertiesUtil.getRequiredValue(getProps(), SHARDING_SUFFIX_FORMAT_KEY));
        ChronoUnit stepUnit = ChronoUnit.valueOf(PropertiesUtil.getRequiredValue(getProps(), INTERVAL_UNIT_KEY));
        int stepAmount = Integer.parseInt(getProps().getProperty(INTERVAL_AMOUNT_KEY, "1"));
        IntervalTimeline intervalTimeline = new IntervalTimeline(logicNamePrefix, Range.closed(effectiveLower, effectiveUpper), IntervalStep.of(stepUnit, stepAmount), suffixFormatter);
        boolean isSecondTs = getProps().containsKey(TIMESTAMP_TYPE_KEY)
                && TIMESTAMP_SECOND_TYPE.equalsIgnoreCase(getProps().getProperty(TIMESTAMP_TYPE_KEY));
        String dateTimePattern = getProps().getProperty(DATE_TIME_PATTERN_KEY, DEFAULT_DATE_TIME_PATTERN);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        ZoneId zoneId = ZoneId.systemDefault();
        if (getProps().containsKey(ZONE_ID_KEY)) {
            zoneId = ZoneId.of(PropertiesUtil.getRequiredValue(getProps(), ZONE_ID_KEY));
        }
        algorithmConfig = new AlgorithmConfig(isSecondTs, dateTimeFormatter, zoneId, intervalTimeline);
    }

    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        LocalDateTime shardingTime = convertShardingValue(shardingValue.getValue());
        return algorithmConfig.getIntervalTimeline().sharding(shardingTime);
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Range<LocalDateTime> shardingRangeTime = convertRangeShardingValue(shardingValue.getValueRange());
        return algorithmConfig.getIntervalTimeline().sharding(shardingRangeTime);
    }

    private LocalDateTime convertShardingValue(final Comparable<?> shardingValue) {
        if (shardingValue instanceof LocalDateTime) {
            return (LocalDateTime) shardingValue;
        }
        if (shardingValue instanceof Date) {
            return LocalDateTimeConvert.fromDate((Date) shardingValue, algorithmConfig.getZoneId());
        }
        if (shardingValue instanceof Long) {
            if (algorithmConfig.isSecondTs()) {
                return LocalDateTimeConvert.fromTimestampSecond((Long) shardingValue, algorithmConfig.getZoneId());
            }
            return LocalDateTimeConvert.fromTimestamp((Long) shardingValue, algorithmConfig.getZoneId());
        }
        if (shardingValue instanceof String) {
            return LocalDateTimeConvert.fromString((String) shardingValue, algorithmConfig.getDateTimeFormatter());
        }
        throw new IllegalArgumentException(Strings.lenientFormat("The current shard type:[%s] is not supported!", shardingValue.getClass()));
    }

    private Range<LocalDateTime> convertRangeShardingValue(final Range<Comparable<?>> shardingValue) {
        if (Range.all().equals(shardingValue)) {
            return Range.all();
        }
        Comparable<?> endpointValue = shardingValue.hasLowerBound() ? shardingValue.lowerEndpoint() : shardingValue.upperEndpoint();
        if (endpointValue instanceof LocalDateTime) {
            @SuppressWarnings("unchecked")
            Range<LocalDateTime> targetRange = (Range<LocalDateTime>) (Object) shardingValue;
            return targetRange;
        }

        if (shardingValue.hasLowerBound() && shardingValue.hasUpperBound()) {
            LocalDateTime lower = convertShardingValue(shardingValue.lowerEndpoint());
            LocalDateTime upper = convertShardingValue(shardingValue.upperEndpoint());
            return Range.range(lower, shardingValue.lowerBoundType(), upper, shardingValue.upperBoundType());
        }

        if (shardingValue.hasLowerBound()) {
            LocalDateTime lower = convertShardingValue(shardingValue.lowerEndpoint());
            if (BoundType.OPEN.equals(shardingValue.lowerBoundType())) {
                return Range.greaterThan(lower);
            }
            return Range.atLeast(lower);
        }

        LocalDateTime upper = convertShardingValue(shardingValue.upperEndpoint());
        if (BoundType.OPEN.equals(shardingValue.upperBoundType())) {
            return Range.lessThan(upper);
        }
        return Range.atMost(upper);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    private static class AlgorithmConfig {

        private final boolean isSecondTs;

        private final DateTimeFormatter dateTimeFormatter;

        private final ZoneId zoneId;

        private final IntervalTimeline intervalTimeline;

        AlgorithmConfig(final boolean isSecondTs, final DateTimeFormatter dateTimeFormatter, final ZoneId zoneId, final IntervalTimeline intervalTimeline) {
            this.isSecondTs = isSecondTs;
            this.dateTimeFormatter = dateTimeFormatter;
            this.zoneId = zoneId;
            this.intervalTimeline = intervalTimeline;
        }

        boolean isSecondTs() {
            return isSecondTs;
        }

        DateTimeFormatter getDateTimeFormatter() {
            return dateTimeFormatter;
        }

        ZoneId getZoneId() {
            return zoneId;
        }

        IntervalTimeline getIntervalTimeline() {
            return intervalTimeline;
        }
    }
}

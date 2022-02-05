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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import lombok.Getter;
import lombok.Setter;
import me.ahoo.cosid.sharding.IntervalStep;
import me.ahoo.cosid.sharding.IntervalTimeline;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Properties;

/**
 * Interval-based time range sharding algorithm.
 */
public abstract class AbstractIntervalShardingAlgorithm<T extends Comparable<?>> implements StandardShardingAlgorithm<T> {
    
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);
    
    public static final String DATE_TIME_LOWER_KEY = "datetime-lower";
    
    public static final String DATE_TIME_UPPER_KEY = "datetime-upper";
    
    public static final String SHARDING_SUFFIX_FORMAT_KEY = "sharding-suffix-pattern";
    
    public static final String INTERVAL_UNIT_KEY = "datetime-interval-unit";
    
    public static final String INTERVAL_AMOUNT_KEY = "datetime-interval-amount";
    
    public static final String ZONE_ID_KEY = "zone-id";
    
    private volatile IntervalTimeline intervalTimeline;
    
    private ZoneId zoneId = ZoneId.systemDefault();
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    /**
     * get zone id.
     *
     * @return zone id
     */
    protected ZoneId getZoneId() {
        return zoneId;
    }
    
    @Override
    public void init() {
        if (getProps().containsKey(ZONE_ID_KEY)) {
            zoneId = ZoneId.of(getRequiredValue(ZONE_ID_KEY));
        }
        String logicNamePrefix = getRequiredValue(CosIdAlgorithm.LOGIC_NAME_PREFIX_KEY);
        LocalDateTime effectiveLower = LocalDateTime.parse(getRequiredValue(DATE_TIME_LOWER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        LocalDateTime effectiveUpper = LocalDateTime.parse(getRequiredValue(DATE_TIME_UPPER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        DateTimeFormatter suffixFormatter = DateTimeFormatter.ofPattern(getRequiredValue(SHARDING_SUFFIX_FORMAT_KEY));
        ChronoUnit stepUnit = ChronoUnit.valueOf(getRequiredValue(INTERVAL_UNIT_KEY));
        int stepAmount = Integer.parseInt(getProps().getProperty(INTERVAL_AMOUNT_KEY, "1"));
        intervalTimeline = new IntervalTimeline(logicNamePrefix, Range.closed(effectiveLower, effectiveUpper), IntervalStep.of(stepUnit, stepAmount), suffixFormatter);
    }
    
    private String getRequiredValue(final String key) {
        return PropertiesUtil.getRequiredValue(getProps(), key);
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<T> shardingValue) {
        LocalDateTime shardingTime = convertShardingValue(shardingValue.getValue());
        return this.intervalTimeline.sharding(shardingTime);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<T> shardingValue) {
        Range<LocalDateTime> shardingRangeTime = convertRangeShardingValue(shardingValue.getValueRange());
        return this.intervalTimeline.sharding(shardingRangeTime);
    }
    
    /**
     * convert sharding value to {@link LocalDateTime}.
     *
     * @param shardingValue sharding value
     * @return The {@link LocalDateTime} represented by the sharding value
     */
    protected abstract LocalDateTime convertShardingValue(T shardingValue);
    
    @SuppressWarnings("unchecked")
    private Range<LocalDateTime> convertRangeShardingValue(final Range<T> shardingValue) {
        if (Range.all().equals(shardingValue)) {
            return Range.all();
        }
        Object endpointValue = shardingValue.hasLowerBound() ? shardingValue.lowerEndpoint() : shardingValue.upperEndpoint();
        if (endpointValue instanceof LocalDateTime) {
            return (Range<LocalDateTime>) shardingValue;
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
}

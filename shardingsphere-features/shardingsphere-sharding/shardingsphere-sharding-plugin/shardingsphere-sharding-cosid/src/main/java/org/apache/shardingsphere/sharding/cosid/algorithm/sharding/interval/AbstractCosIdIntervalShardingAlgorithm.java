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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval;

import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.Getter;
import me.ahoo.cosid.sharding.IntervalStep;
import me.ahoo.cosid.sharding.IntervalTimeline;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Properties;

/**
 * Abstract interval range sharding algorithm with CosId.
 */
public abstract class AbstractCosIdIntervalShardingAlgorithm<T extends Comparable<?>> implements StandardShardingAlgorithm<T> {
    
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);
    
    public static final String DATE_TIME_LOWER_KEY = "datetime-lower";
    
    public static final String DATE_TIME_UPPER_KEY = "datetime-upper";
    
    public static final String SHARDING_SUFFIX_FORMAT_KEY = "sharding-suffix-pattern";
    
    public static final String INTERVAL_UNIT_KEY = "datetime-interval-unit";
    
    public static final String INTERVAL_AMOUNT_KEY = "datetime-interval-amount";
    
    public static final String ZONE_ID_KEY = "zone-id";
    
    @Getter
    private Properties props;
    
    @Getter(AccessLevel.PROTECTED)
    private ZoneId zoneId;
    
    private IntervalTimeline intervalTimeline;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        zoneId = getZoneId(props);
        intervalTimeline = getIntervalTimeline(props);
    }
    
    private ZoneId getZoneId(final Properties props) {
        return props.containsKey(ZONE_ID_KEY) ? ZoneId.of(props.getProperty(ZONE_ID_KEY)) : ZoneId.systemDefault();
    }
    
    private IntervalTimeline getIntervalTimeline(final Properties props) {
        String logicNamePrefix = getRequiredValue(props, CosIdAlgorithmConstants.LOGIC_NAME_PREFIX_KEY);
        LocalDateTime effectiveLower = LocalDateTime.parse(getRequiredValue(props, DATE_TIME_LOWER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        LocalDateTime effectiveUpper = LocalDateTime.parse(getRequiredValue(props, DATE_TIME_UPPER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        DateTimeFormatter suffixFormatter = DateTimeFormatter.ofPattern(getRequiredValue(props, SHARDING_SUFFIX_FORMAT_KEY));
        ChronoUnit stepUnit = ChronoUnit.valueOf(getRequiredValue(props, INTERVAL_UNIT_KEY));
        int stepAmount = Integer.parseInt(props.getOrDefault(INTERVAL_AMOUNT_KEY, 1).toString());
        return new IntervalTimeline(logicNamePrefix, Range.closed(effectiveLower, effectiveUpper), IntervalStep.of(stepUnit, stepAmount), suffixFormatter);
    }
    
    private String getRequiredValue(final Properties props, final String key) {
        Preconditions.checkArgument(props.containsKey(key), "%s can not be null", key);
        return props.getProperty(key);
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<T> shardingValue) {
        LocalDateTime shardingTime = convertShardingValue(shardingValue.getValue());
        return intervalTimeline.sharding(shardingTime);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<T> shardingValue) {
        Range<LocalDateTime> shardingRangeTime = convertRangeShardingValue(shardingValue.getValueRange());
        return intervalTimeline.sharding(shardingRangeTime);
    }
    
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
            return BoundType.OPEN.equals(shardingValue.lowerBoundType()) ? Range.greaterThan(lower) : Range.atLeast(lower);
        }
        LocalDateTime upper = convertShardingValue(shardingValue.upperEndpoint());
        return BoundType.OPEN.equals(shardingValue.upperBoundType()) ? Range.lessThan(upper) : Range.atMost(upper);
    }
    
    protected abstract LocalDateTime convertShardingValue(T shardingValue);
}

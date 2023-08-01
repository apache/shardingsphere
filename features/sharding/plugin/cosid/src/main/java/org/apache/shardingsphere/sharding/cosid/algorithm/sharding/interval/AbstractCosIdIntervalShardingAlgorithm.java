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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import me.ahoo.cosid.sharding.IntervalStep;
import me.ahoo.cosid.sharding.IntervalTimeline;
import me.ahoo.cosid.sharding.LocalDateTimeConvertor;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.exception.ShardingPluginException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Properties;

/**
 * Abstract interval range sharding algorithm with CosId.
 * 
 * @param <T> type of sharding value
 */
public abstract class AbstractCosIdIntervalShardingAlgorithm<T extends Comparable<?>> implements StandardShardingAlgorithm<T> {
    
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);
    
    public static final String DATE_TIME_LOWER_KEY = "datetime-lower";
    
    public static final String DATE_TIME_UPPER_KEY = "datetime-upper";
    
    public static final String SHARDING_SUFFIX_FORMAT_KEY = "sharding-suffix-pattern";
    
    public static final String INTERVAL_UNIT_KEY = "datetime-interval-unit";
    
    public static final String INTERVAL_AMOUNT_KEY = "datetime-interval-amount";
    
    private IntervalTimeline intervalTimeline;
    
    private LocalDateTimeConvertor localDateTimeConvertor;
    
    @Override
    public void init(final Properties props) {
        intervalTimeline = createIntervalTimeline(props);
        localDateTimeConvertor = createLocalDateTimeConvertor(props);
    }
    
    private IntervalTimeline createIntervalTimeline(final Properties props) {
        String logicNamePrefix = getRequiredValue(props, CosIdAlgorithmConstants.LOGIC_NAME_PREFIX_KEY);
        LocalDateTime effectiveLower = LocalDateTime.parse(getRequiredValue(props, DATE_TIME_LOWER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        LocalDateTime effectiveUpper = LocalDateTime.parse(getRequiredValue(props, DATE_TIME_UPPER_KEY), DEFAULT_DATE_TIME_FORMATTER);
        ChronoUnit stepUnit = ChronoUnit.valueOf(getRequiredValue(props, INTERVAL_UNIT_KEY));
        int stepAmount = Integer.parseInt(props.getOrDefault(INTERVAL_AMOUNT_KEY, 1).toString());
        DateTimeFormatter suffixFormatter = DateTimeFormatter.ofPattern(getRequiredValue(props, SHARDING_SUFFIX_FORMAT_KEY));
        return new IntervalTimeline(logicNamePrefix, Range.closed(effectiveLower, effectiveUpper), IntervalStep.of(stepUnit, stepAmount), suffixFormatter);
    }
    
    private String getRequiredValue(final Properties props, final String key) {
        ShardingSpherePreconditions.checkState(props.containsKey(key), () -> new ShardingPluginException("%s can not be null.", key));
        return props.getProperty(key);
    }
    
    protected abstract LocalDateTimeConvertor createLocalDateTimeConvertor(Properties props);
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<T> shardingValue) {
        return intervalTimeline.sharding(localDateTimeConvertor.toLocalDateTime(shardingValue.getValue()));
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<T> shardingValue) {
        return intervalTimeline.sharding(toLocalDateTimeRange(shardingValue.getValueRange()));
    }
    
    @SuppressWarnings("unchecked")
    private Range<LocalDateTime> toLocalDateTimeRange(final Range<T> shardingValue) {
        if (Range.all().equals(shardingValue)) {
            return Range.all();
        }
        Object endpointValue = shardingValue.hasLowerBound() ? shardingValue.lowerEndpoint() : shardingValue.upperEndpoint();
        if (endpointValue instanceof LocalDateTime) {
            return (Range<LocalDateTime>) shardingValue;
        }
        if (shardingValue.hasLowerBound() && shardingValue.hasUpperBound()) {
            LocalDateTime lower = localDateTimeConvertor.toLocalDateTime(shardingValue.lowerEndpoint());
            LocalDateTime upper = localDateTimeConvertor.toLocalDateTime(shardingValue.upperEndpoint());
            return Range.range(lower, shardingValue.lowerBoundType(), upper, shardingValue.upperBoundType());
        }
        if (shardingValue.hasLowerBound()) {
            LocalDateTime lower = localDateTimeConvertor.toLocalDateTime(shardingValue.lowerEndpoint());
            return BoundType.OPEN == shardingValue.lowerBoundType() ? Range.greaterThan(lower) : Range.atLeast(lower);
        }
        LocalDateTime upper = localDateTimeConvertor.toLocalDateTime(shardingValue.upperEndpoint());
        return BoundType.OPEN == shardingValue.upperBoundType() ? Range.lessThan(upper) : Range.atMost(upper);
    }
}

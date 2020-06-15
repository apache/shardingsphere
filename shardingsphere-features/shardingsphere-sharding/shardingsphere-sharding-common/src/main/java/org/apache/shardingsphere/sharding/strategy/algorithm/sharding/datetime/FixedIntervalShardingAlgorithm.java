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

package org.apache.shardingsphere.sharding.strategy.algorithm.sharding.datetime;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Fixed interval sharding algorithm.
 * 
 * <p>
 *     Shard by `y = floor(x/v)` algorithm, which means y begins from 0. v is `sharding.seconds`, and the minimum time unit is 1 sec.
 *     `datetime.lower` decides the beginning datetime to shard. On the other hand, `datetime.upper` decides the end datetime to shard.
 *     
 *     Notice: Anytime less then `datetime.lower` will route to the first partition, and anytime great than `datetime.upper` will route to the last sharding.
 * </p>
 */
@Getter
public final class FixedIntervalShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    public static final String DATETIME_LOWER = "datetime.lower";
    
    public static final String DATETIME_UPPER = "datetime.upper";
    
    public static final String SHARDING_SECONDS_KEY = "sharding.seconds";
    
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    
    private int autoTablesAmount;
    
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void init() {
        Preconditions.checkNotNull(props.get(SHARDING_SECONDS_KEY), "Sharding partition volume cannot be null.");
        Preconditions.checkState(null != props.get(DATETIME_LOWER) && checkDatetimePattern(props.get(DATETIME_LOWER).toString()), "%s pattern is required.", DATETIME_PATTERN);
        Preconditions.checkState(null != props.get(DATETIME_UPPER) && checkDatetimePattern(props.get(DATETIME_UPPER).toString()), "%s pattern is required.", DATETIME_PATTERN);
        autoTablesAmount = (int) (Math.ceil(parseDate(props.get(DATETIME_UPPER).toString()) / getPartitionValue()) + 2);
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(doSharding(parseDate(shardingValue.getValue())) + "")) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        int firstPartition = getFirstPartition(shardingValue.getValueRange());
        int lastPartition = getLastPartition(shardingValue.getValueRange());
        for (int i = firstPartition; i <= lastPartition; i++) {
            for (String each : availableTargetNames) {
                if (each.endsWith(i + "")) {
                    result.add(each);
                }
                if (result.size() == availableTargetNames.size()) {
                    return result;
                }
            }
        }
        return result;
    }
    
    private int doSharding(final long shardingValue) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String position = decimalFormat.format((float) shardingValue / getPartitionValue());
        return Math.min(Math.max(0, (int) Math.ceil(Float.parseFloat(position))), autoTablesAmount - 1);
    }
    
    private int getFirstPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasLowerBound() ? doSharding(parseDate(valueRange.lowerEndpoint())) : 0;
    }
    
    private int getLastPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasUpperBound() ? doSharding(parseDate(valueRange.upperEndpoint())) : autoTablesAmount - 1;
    }
    
    private boolean checkDatetimePattern(final String datetime) {
        try {
            DATE_FORMAT.parse(datetime);
            return true;
        } catch (final DateTimeParseException ex) {
            return false;
        }
    }
    
    private long parseDate(final Comparable<?> shardingValue) {
        LocalDateTime dateValue = LocalDateTime.parse(shardingValue.toString(), DATE_FORMAT);
        LocalDateTime sinceDate = LocalDateTime.parse(props.get(DATETIME_LOWER).toString(), DATE_FORMAT);
        return Duration.between(sinceDate, dateValue).toMillis() / 1000;
    }
    
    private long getPartitionValue() {
        return Long.parseLong(props.get(SHARDING_SECONDS_KEY).toString());
    }
    
    @Override
    public String getType() {
        return "DATETIME";
    }
}

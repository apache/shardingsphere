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

package org.apache.shardingsphere.sharding.strategy.algorithm.sharding;

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
 * Datetime sharding algorithm.
 * 
 * <p>Shard by `y = floor(x/v)` algorithm, which means y begins from 0.
 * v is `PARTITION_SECONDS`, and the minimum time unit is 1 sec.
 * `DATETIME_LOWER` decides the beginning datetime to shard. On the other hand, `DATETIME_UPPER` decides the end datetime to shard.</p>
 * <p>Notice: Anytime less then `DATETIME_LOWER` will route to the first partition, and anytime great than `DATETIME_UPPER` will route to the last partition.</p>
 */
public final class DatetimeShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    private static final String PARTITION_SECONDS = "partition.seconds";
    
    private static final String DATETIME_LOWER = "datetime.lower";
    
    private static final String DATETIME_UPPER = "datetime.upper";

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Getter
    private int autoTablesAmount;
    
    @Override
    public void init() {
        Preconditions.checkNotNull(properties.get(PARTITION_SECONDS), "Sharding partition volume cannot be null.");
        Preconditions.checkState(null != properties.get(DATETIME_LOWER) && checkDatetimePattern(properties.get(DATETIME_LOWER).toString()), "%s pattern is required.", DATETIME_PATTERN);
        Preconditions.checkState(null != properties.get(DATETIME_UPPER) && checkDatetimePattern(properties.get(DATETIME_UPPER).toString()),
                "%s pattern is required.", DATETIME_PATTERN);
        autoTablesAmount = (int) (Math.ceil(parseDate(properties.get(DATETIME_UPPER).toString()) / getPartitionValue()) + 2);
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(doSharding(parseDate(shardingValue.getValue())) + "")) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
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
        LocalDateTime sinceDate = LocalDateTime.parse(properties.get(DATETIME_LOWER).toString(), DATE_FORMAT);
        return Duration.between(sinceDate, dateValue).toMillis() / 1000;
    }
    
    private long getPartitionValue() {
        return Long.parseLong(properties.get(PARTITION_SECONDS).toString());
    }
    
    @Override
    public String getType() {
        return "DATETIME";
    }
}

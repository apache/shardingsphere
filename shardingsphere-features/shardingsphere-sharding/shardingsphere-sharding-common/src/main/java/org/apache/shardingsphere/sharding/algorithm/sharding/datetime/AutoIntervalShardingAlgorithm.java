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
import com.google.common.collect.Range;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Auto interval sharding algorithm.
 */
@Getter
public final class AutoIntervalShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    private static final String DATE_TIME_LOWER_KEY = "datetime-lower";
    
    private static final String DATE_TIME_UPPER_KEY = "datetime-upper";
    
    private static final String SHARDING_SECONDS_KEY = "sharding-seconds";
    
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Setter
    private Properties props = new Properties();
    
    private LocalDateTime dateTimeLower;
    
    private long shardingSeconds;
    
    private int autoTablesAmount;
    
    @Override
    public void init() {
        dateTimeLower = getDateTime(DATE_TIME_LOWER_KEY);
        shardingSeconds = getShardingSeconds();
        autoTablesAmount = (int) (Math.ceil(parseDate(props.getProperty(DATE_TIME_UPPER_KEY)) / shardingSeconds) + 2);
    }
    
    private LocalDateTime getDateTime(final String dateTimeKey) {
        String value = props.getProperty(dateTimeKey);
        Preconditions.checkNotNull(value, "%s cannot be null.", dateTimeKey);
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMAT);
        } catch (final DateTimeParseException ex) {
            throw new ShardingSphereConfigurationException("Invalid %s, datetime pattern should be `yyyy-MM-dd HH:mm:ss`, value is `%s`", dateTimeKey, value);
        }
    }
    
    private long getShardingSeconds() {
        Preconditions.checkArgument(props.containsKey(SHARDING_SECONDS_KEY), "%s cannot be null.", SHARDING_SECONDS_KEY);
        return Long.parseLong(props.getProperty(SHARDING_SECONDS_KEY));
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(String.valueOf(doSharding(parseDate(shardingValue.getValue()))))) {
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
                if (each.endsWith(String.valueOf(i))) {
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
        String position = new DecimalFormat("0.00").format((float) shardingValue / shardingSeconds);
        return Math.min(Math.max(0, (int) Math.ceil(Float.parseFloat(position))), autoTablesAmount - 1);
    }
    
    private int getFirstPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasLowerBound() ? doSharding(parseDate(valueRange.lowerEndpoint())) : 0;
    }
    
    private int getLastPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasUpperBound() ? doSharding(parseDate(valueRange.upperEndpoint())) : autoTablesAmount - 1;
    }
    
    private long parseDate(final Comparable<?> shardingValue) {
        LocalDateTime dateValue = LocalDateTime.parse(shardingValue.toString(), DATE_TIME_FORMAT);
        return Duration.between(dateTimeLower, dateValue).toMillis() / 1000;
    }
    
    @Override
    public String getType() {
        return "AUTO_INTERVAL";
    }
    
    @Override
    public Collection<String> getAllPropertyKeys() {
        return Arrays.asList(DATE_TIME_LOWER_KEY, DATE_TIME_UPPER_KEY, SHARDING_SECONDS_KEY);
    }
}

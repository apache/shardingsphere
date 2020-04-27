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

package org.apache.shardingsphere.core.strategy.algorithm.sharding;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.api.sharding.standard.StandardShardingAlgorithm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Datetime sharding algorithm.
 * 
 * <p>Shard by `y = floor(x/v)` algorithm, which means y begins from 0.
 * v is `PARTITION_VOLUME`, and the minimum time unit is 1 sec.
 * `SINCE_DATETIME` decides the beginning datetime to shard. </p>
 */
public final class DatetimeShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {
    
    private static final String PARTITION_VOLUME = "partition.volume";
    
    private static final String SINCE_DATETIME = "since.datetime";
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static final int SECOND_UNIT = 1000;
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        checkProperties();
        for (String each : availableTargetNames) {
            if (each.endsWith(doSharding(parseDate(shardingValue.getValue())))) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        checkProperties();
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        for (long i = parseDate(shardingValue.getValueRange().lowerEndpoint()); i <= parseDate(shardingValue.getValueRange().upperEndpoint()); i++) {
            for (String each : availableTargetNames) {
                if (each.endsWith(doSharding(i))) {
                    result.add(each);
                }
            }
            if (result.size() == availableTargetNames.size()) {
                return result;
            }
        }
        return result;
    }
    
    private String doSharding(final long shardingValue) {
        long position = (long) (Math.floor(shardingValue / getPartitionValue()));
        return 0 > position ? "0" : position + "";
    }
    
    private void checkProperties() {
        Preconditions.checkNotNull(properties.get(PARTITION_VOLUME), "Sharding partition volume cannot be null.");
        Preconditions.checkState(null != properties.get(SINCE_DATETIME) && checkDatetimePattern(properties.get(SINCE_DATETIME).toString()), 
                "%s pattern is required.", DATE_FORMAT.toPattern());
    }
    
    private boolean checkDatetimePattern(final String datetime) {
        try {
            DATE_FORMAT.parse(datetime);
            return true;
        } catch (final ParseException ex) {
            return false;
        }
    }
    
    private long parseDate(final Comparable<?> shardingValue) {
        try {
            Date dateValue = DATE_FORMAT.parse(shardingValue.toString());
            if (null != properties.get(SINCE_DATETIME)) {
                return parseDate(dateValue);
            }
            return dateValue.getTime() / SECOND_UNIT;
        } catch (final ParseException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }
    
    private Long parseDate(final Date dateValue) throws ParseException {
        Date sinceDate = DATE_FORMAT.parse(properties.get(SINCE_DATETIME).toString());
        return (dateValue.getTime() - sinceDate.getTime()) / SECOND_UNIT;
    }
    
    private long getPartitionValue() {
        return Long.parseLong(properties.get(PARTITION_VOLUME).toString());
    }
    
    @Override
    public String getType() {
        return "DATETIME";
    }
}

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

package org.apache.shardingsphere.sharding.algorithm.sharding.range;

import com.google.common.collect.Range;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Abstract range sharding algorithm.
 */
public abstract class AbstractRangeShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    private volatile Map<Integer, Range<Comparable<?>>> partitionRange;
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public final void init() {
        partitionRange = calculatePartitionRange(props);
    }
    
    protected abstract Map<Integer, Range<Comparable<?>>> calculatePartitionRange(Properties props);
    
    @Override
    public final String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        String suffix = String.valueOf(getPartition(shardingValue.getValue()));
        return findMatchedTargetName(availableTargetNames, suffix, shardingValue.getDataNodeInfo()).orElse(null);
    }
    
    @Override
    public final Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        int firstPartition = getFirstPartition(shardingValue.getValueRange());
        int lastPartition = getLastPartition(shardingValue.getValueRange());
        for (int partition = firstPartition; partition <= lastPartition; partition++) {
            String suffix = String.valueOf(partition);
            findMatchedTargetName(availableTargetNames, suffix, shardingValue.getDataNodeInfo()).ifPresent(result::add);
        }
        return result;
    }
    
    private int getFirstPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasLowerBound() ? getPartition(valueRange.lowerEndpoint()) : 0;
    }
    
    private int getLastPartition(final Range<Comparable<?>> valueRange) {
        return valueRange.hasUpperBound() ? getPartition(valueRange.upperEndpoint()) : partitionRange.size() - 1;
    }
    
    private Integer getPartition(final Comparable<?> value) {
        for (Entry<Integer, Range<Comparable<?>>> entry : partitionRange.entrySet()) {
            if (entry.getValue().contains(getLongValue(value))) {
                return entry.getKey();
            }
        }
        throw new UnsupportedOperationException("");
    }
    
    private Long getLongValue(final Comparable<?> value) {
        return Long.parseLong(value.toString());
    }
    
    @Override
    public final int getAutoTablesAmount() {
        return partitionRange.size();
    }
}

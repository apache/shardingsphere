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
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.primitives.Longs;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Range sharding algorithm.
 * <p>
 * Range sharding algorithm is similar to the rule of partition table.
 * User can specify the range by setting the `range.partition.split.value` parameter.
 * The `range.partition.split.value` parameter is an ordered list of numbers, separated by commas.
 * </p>
 * <p>
 * For example: If the `range.partition.split.value` parameter is set to `1,5,10`,
 * the parameter will split all values into four intervals——(-∞, 1), [1,5), [5,10), [10, +∞),
 * which corresponding to partition_0, partition_1, partition_2, partition_3.
 * The sharding values will be divided into different partition by its value.
 * </p>
 */
public final class RangeShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    private static final String RANGE_PARTITION_SPLIT_VALUE = "range.partition.split.value";

    private Properties properties = new Properties();

    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Long> shardingValue) {
        Preconditions.checkNotNull(properties.get(RANGE_PARTITION_SPLIT_VALUE), "Range sharding algorithm range partition split value cannot be null.");
        Map<Integer, Range<Long>> partitionRangeMap = getPartitionRangeMap();
        for (String each : availableTargetNames) {
            if (each.endsWith(getPartition(partitionRangeMap, shardingValue.getValue()))) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Long> shardingValue) {
        Preconditions.checkNotNull(properties.get(RANGE_PARTITION_SPLIT_VALUE), "Range sharding algorithm range partition split value cannot be null.");
        Map<Integer, Range<Long>> partitionRangeMap = getPartitionRangeMap();
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        for (long value = shardingValue.getValueRange().lowerEndpoint(); value <= shardingValue.getValueRange().upperEndpoint(); value++) {
            for (String each : availableTargetNames) {
                if (each.endsWith(getPartition(partitionRangeMap, value))) {
                    result.add(each);
                }
            }
        }
        return result;
    }

    private Map<Integer, Range<Long>> getPartitionRangeMap() {
        List<Long> splitValues = Splitter.on(",").trimResults().splitToList(properties.get(RANGE_PARTITION_SPLIT_VALUE).toString())
                .stream().map(Longs::tryParse).filter(Objects::nonNull).sorted().collect(Collectors.toList());
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(splitValues), "Range sharding algorithm range partition split value is not valid.");
        Map<Integer, Range<Long>> partitionRangeMap = Maps.newHashMapWithExpectedSize(splitValues.size() + 1);
        for (int i = 0; i < splitValues.size(); i++) {
            Long splitValue = splitValues.get(i);
            if (i == 0) {
                partitionRangeMap.put(i, Range.lessThan(splitValue));
            } else {
                Long previousSplitValue = splitValues.get(i - 1);
                partitionRangeMap.put(i, Range.closedOpen(previousSplitValue, splitValue));
            }
            if (i == splitValues.size() - 1) {
                partitionRangeMap.put(i + 1, Range.atLeast(splitValue));
            }
        }
        return partitionRangeMap;
    }

    private String getPartition(final Map<Integer, Range<Long>> partitionRangeMap, final Long value) {
        for (Map.Entry<Integer, Range<Long>> entry : partitionRangeMap.entrySet()) {
            if (entry.getValue().contains(value)) {
                return entry.getKey().toString();
            }
        }
        return partitionRangeMap.keySet().stream().mapToInt(Integer::valueOf).max().toString();
    }

    @Override
    public String getType() {
        return "RANGE";
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setProperties(final Properties properties) {
        this.properties = properties;
    }
}

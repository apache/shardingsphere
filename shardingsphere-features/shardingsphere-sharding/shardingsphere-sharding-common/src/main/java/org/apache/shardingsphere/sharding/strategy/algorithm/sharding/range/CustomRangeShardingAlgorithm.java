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

package org.apache.shardingsphere.sharding.strategy.algorithm.sharding.range;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.primitives.Longs;
import org.apache.commons.collections4.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Custom range sharding algorithm.
 * <p>
 * Custom range sharding algorithm is similar to the rule of partition table.
 * User can specify the range by setting the `partition.ranges` parameter.
 * The `partition.ranges` parameter is an ordered list of numbers, separated by commas.
 * </p>
 * <p>
 * For example: If the `partition.ranges` parameter is set to `1,5,10`,
 * the parameter will split all values into four intervals——(-∞, 1), [1,5), [5,10), [10, +∞),
 * which corresponding to partition_0, partition_1, partition_2, partition_3.
 * The sharding values will be divided into different partition by its value.
 * </p>
 */
public final class CustomRangeShardingAlgorithm extends AbstractRangeShardingAlgorithm {
    
    private static final String PARTITION_RANGES = "partition.ranges";
    
    @Override
    public String getType() {
        return "CUSTOM_RANGE";
    }
    
    @Override
    public Map<Integer, Range<Long>> createPartitionRangeMap(final Properties properties) {
        Preconditions.checkNotNull(properties.get(PARTITION_RANGES), "Custom range sharding algorithm partition ranges cannot be null.");
        List<Long> partitionRanges = Splitter.on(",").trimResults().splitToList(properties.get(PARTITION_RANGES).toString())
                .stream().map(Longs::tryParse).filter(Objects::nonNull).sorted().collect(Collectors.toList());
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(partitionRanges), "Custom range sharding algorithm partition ranges is not valid.");
        Map<Integer, Range<Long>> result = Maps.newHashMapWithExpectedSize(partitionRanges.size() + 1);
        for (int i = 0; i < partitionRanges.size(); i++) {
            Long rangeValue = partitionRanges.get(i);
            if (i == 0) {
                result.put(i, Range.lessThan(rangeValue));
            } else {
                Long previousRangeValue = partitionRanges.get(i - 1);
                result.put(i, Range.closedOpen(previousRangeValue, rangeValue));
            }
            if (i == partitionRanges.size() - 1) {
                result.put(i + 1, Range.atLeast(rangeValue));
            }
        }
        return result;
    }
}

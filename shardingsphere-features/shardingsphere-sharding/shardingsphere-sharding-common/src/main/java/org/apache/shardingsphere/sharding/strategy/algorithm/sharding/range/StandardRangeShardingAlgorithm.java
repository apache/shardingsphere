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
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.math.LongMath;

import java.math.RoundingMode;
import java.util.Map;
import java.util.Properties;

/**
 * Standard range sharding algorithm.
 * 
 * <p>
 * Standard range sharding algorithm is similar to the rule of partition table, but it can only be split by the same size.
 * User can specify the range by setting `range.lower`, `range.upper` and `partition.volume` parameters.
 * The `partition.volume` parameter determines the size of each partition.
 * </p>
 * <p>
 * For example: If the `range.lower` parameter is set to `10`, the `range.upper` parameter is set to `45`,
 * and the `partition.volume` parameter is set to `10`. The values in range [10,45] will be split to different partitions
 * ——[10,20), [20, 30), [30, 40), [40, 45), and other values will be split to (-∞, 10) and [45, +∞).
 * </p>
 */
public final class StandardRangeShardingAlgorithm extends AbstractRangeShardingAlgorithm {
    
    private static final String RANGE_LOWER = "range.lower";
    
    private static final String RANGE_UPPER = "range.upper";
    
    private static final String PARTITION_VOLUME = "partition.volume";
    
    @Override
    public String getType() {
        return "STANDARD_RANGE";
    }
    
    @Override
    public Map<Integer, Range<Long>> createPartitionRangeMap(final Properties properties) {
        Preconditions.checkNotNull(properties.get(RANGE_LOWER), "Standard range sharding algorithm partition lower cannot be null.");
        Preconditions.checkNotNull(properties.get(RANGE_UPPER), "Standard range sharding algorithm partition upper cannot be null.");
        Preconditions.checkNotNull(properties.get(PARTITION_VOLUME), "Standard range sharding algorithm partition volume cannot be null.");
        long lower = Long.parseLong(properties.get(RANGE_LOWER).toString());
        long upper = Long.parseLong(properties.get(RANGE_UPPER).toString());
        long volume = Long.parseLong(properties.get(PARTITION_VOLUME).toString());
        Preconditions.checkArgument(upper - lower >= volume, "Standard range sharding algorithm partition range can not be smaller than volume.");
        int partitionSize = Math.toIntExact(LongMath.divide(upper - lower, volume, RoundingMode.CEILING));
        Map<Integer, Range<Long>> result = Maps.newHashMapWithExpectedSize(partitionSize + 2);
        result.put(0, Range.lessThan(lower));
        for (int i = 0; i < partitionSize; i++) {
            result.put(i + 1, Range.closedOpen(lower + i * volume, Math.min(lower + (i + 1) * volume, upper)));
        }
        result.put(partitionSize + 1, Range.atLeast(upper));
        return result;
    }
}

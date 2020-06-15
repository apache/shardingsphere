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
 * Volume based range sharding algorithm.
 * 
 * <p>
 *     This algorithm is similar to the rule of partition table, but it can only be split by the same size.
 *     
 *     For example: If the `range.lower` parameter is set to `10`, the `range.upper` parameter is set to `45`, and the `sharding.volume` parameter is set to `10`. 
 *     The values in range [10, 45] will be split to different partitions with [10, 20), [20, 30), [30, 40), [40, 45), and other values will throw exception.
 * </p>
 */
public final class VolumeBasedRangeShardingAlgorithm extends AbstractRangeShardingAlgorithm {
    
    public static final String RANGE_LOWER_KEY = "range.lower";
    
    public static final String RANGE_UPPER_KEY = "range.upper";
    
    public static final String SHARDING_VOLUME_KEY = "sharding.volume";
    
    @Override
    public Map<Integer, Range<Long>> createPartitionRangeMap(final Properties props) {
        Preconditions.checkNotNull(props.get(RANGE_LOWER_KEY), "Lower range cannot be null.");
        Preconditions.checkNotNull(props.get(RANGE_UPPER_KEY), "Upper range cannot be null.");
        Preconditions.checkNotNull(props.get(SHARDING_VOLUME_KEY), "Sharding volume cannot be null.");
        long lower = Long.parseLong(props.get(RANGE_LOWER_KEY).toString());
        long upper = Long.parseLong(props.get(RANGE_UPPER_KEY).toString());
        long volume = Long.parseLong(props.get(SHARDING_VOLUME_KEY).toString());
        Preconditions.checkArgument(upper - lower >= volume, "Range can not be smaller than volume.");
        int partitionSize = Math.toIntExact(LongMath.divide(upper - lower, volume, RoundingMode.CEILING));
        Map<Integer, Range<Long>> result = Maps.newHashMapWithExpectedSize(partitionSize + 2);
        result.put(0, Range.lessThan(lower));
        for (int i = 0; i < partitionSize; i++) {
            result.put(i + 1, Range.closedOpen(lower + i * volume, Math.min(lower + (i + 1) * volume, upper)));
        }
        result.put(partitionSize + 1, Range.atLeast(upper));
        return result;
    }
    
    @Override
    public String getType() {
        return "VOLUME_RANGE";
    }
}

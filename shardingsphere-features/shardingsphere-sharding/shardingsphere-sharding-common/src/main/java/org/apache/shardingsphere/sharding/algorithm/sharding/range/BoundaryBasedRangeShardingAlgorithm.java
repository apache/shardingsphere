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
 * Boundary based range sharding algorithm.
 */
public final class BoundaryBasedRangeShardingAlgorithm extends AbstractRangeShardingAlgorithm {
    
    private static final String SHARDING_RANGES_KEY = "sharding.ranges";
    
    @Override
    public Map<Integer, Range<Long>> calculatePartitionRange(final Properties props) {
        Preconditions.checkState(props.containsKey(SHARDING_RANGES_KEY), "Sharding ranges cannot be null.");
        List<Long> partitionRanges = Splitter.on(",").trimResults().splitToList(props.getProperty(SHARDING_RANGES_KEY))
                .stream().map(Longs::tryParse).filter(Objects::nonNull).sorted().collect(Collectors.toList());
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(partitionRanges), "Sharding ranges is not valid.");
        Map<Integer, Range<Long>> result = Maps.newHashMapWithExpectedSize(partitionRanges.size() + 1);
        for (int i = 0; i < partitionRanges.size(); i++) {
            Long rangeValue = partitionRanges.get(i);
            if (i == 0) {
                result.put(0, Range.lessThan(rangeValue));
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
    
    @Override
    public String getType() {
        return "BOUNDARY_RANGE";
    }
}

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

import com.google.common.collect.Range;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract range sharding algorithm.
 */
public abstract class AbstractRangeShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    
    private volatile boolean initialized;
    
    protected final void checkInit() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initProperties();
                    initialized = true;
                }
            }
        }
    }
    
    protected abstract void initProperties();
    
    protected final String getTargetNameByPreciseShardingValue(final Collection<String> availableTargetNames, 
                                                               final PreciseShardingValue<Long> shardingValue, final Map<Integer, Range<Long>> partitionRangeMap) {
        return availableTargetNames.stream().filter(each -> each.endsWith(getPartition(partitionRangeMap, shardingValue.getValue()) + "")).findFirst().orElseThrow(UnsupportedOperationException::new);
    }
    
    protected final Collection<String> getTargetNameByRangeShardingValue(final Collection<String> availableTargetNames, 
                                                                         final RangeShardingValue<Long> shardingValue, final Map<Integer, Range<Long>> partitionRangeMap) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        int lowerEndpointPartition = getPartition(partitionRangeMap, shardingValue.getValueRange().lowerEndpoint());
        int upperEndpointPartition = getPartition(partitionRangeMap, shardingValue.getValueRange().upperEndpoint());
        for (int partition = lowerEndpointPartition; partition <= upperEndpointPartition; partition++) {
            for (String each : availableTargetNames) {
                if (each.endsWith(partition + "")) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    
    private Integer getPartition(final Map<Integer, Range<Long>> partitionRangeMap, final Long value) {
        for (Entry<Integer, Range<Long>> entry : partitionRangeMap.entrySet()) {
            if (entry.getValue().contains(value)) {
                return entry.getKey();
            }
        }
        throw new UnsupportedOperationException();
    }
}

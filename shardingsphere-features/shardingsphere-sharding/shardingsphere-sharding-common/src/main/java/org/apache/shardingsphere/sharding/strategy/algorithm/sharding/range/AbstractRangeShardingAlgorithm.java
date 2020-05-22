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

/**
 * Abstract range sharding algorithm.
 */
public abstract class AbstractRangeShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    private volatile boolean init;

    /**
     * getTargetNameByPreciseShardingValue.
     *
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue        sharding value
     * @param partitionRangeMap    the mapping of partition and range
     * @return sharding result for data source or table's name
     */
    protected String getTargetNameByPreciseShardingValue(final Collection<String> availableTargetNames,
                                                         final PreciseShardingValue<Long> shardingValue,
                                                         final Map<Integer, Range<Long>> partitionRangeMap) {
        return availableTargetNames.stream().filter(each -> each.endsWith(getPartition(partitionRangeMap, shardingValue.getValue()) + ""))
                .findFirst().orElseThrow(UnsupportedOperationException::new);
    }

    /**
     * getTargetNameByRangeShardingValue.
     *
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue        sharding value
     * @param partitionRangeMap    the mapping of partition and range
     * @return sharding results for data sources or tables's names
     */
    protected Collection<String> getTargetNameByRangeShardingValue(final Collection<String> availableTargetNames,
                                                                   final RangeShardingValue<Long> shardingValue,
                                                                   final Map<Integer, Range<Long>> partitionRangeMap) {
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

    /**
     * check properties whether init.
     */
    protected void checkInit() {
        if (!init) {
            synchronized (this) {
                if (!init) {
                    initProperties();
                    init = true;
                }
            }
        }
    }

    /**
     * init properties.
     */
    protected abstract void initProperties();

    private Integer getPartition(final Map<Integer, Range<Long>> partitionRangeMap, final Long value) {
        for (Map.Entry<Integer, Range<Long>> entry : partitionRangeMap.entrySet()) {
            if (entry.getValue().contains(value)) {
                return entry.getKey();
            }
        }
        throw new UnsupportedOperationException();
    }
}

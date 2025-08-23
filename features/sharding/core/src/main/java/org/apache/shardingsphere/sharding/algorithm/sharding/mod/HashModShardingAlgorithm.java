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

package org.apache.shardingsphere.sharding.algorithm.sharding.mod;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.algorithm.sharding.ShardingAutoTableAlgorithmUtils;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.data.NullShardingValueException;

import java.util.Collection;
import java.util.Properties;

/**
 * Hash sharding algorithm.
 */
public final class HashModShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    private static final String SHARDING_COUNT_KEY = "sharding-count";
    
    private int shardingCount;
    
    @Override
    public void init(final Properties props) {
        shardingCount = getShardingCount(props);
    }
    
    private int getShardingCount(final Properties props) {
        ShardingSpherePreconditions.checkContainsKey(props, SHARDING_COUNT_KEY, () -> new AlgorithmInitializationException(this, "Sharding count cannot be null."));
        int result = Integer.parseInt(String.valueOf(props.getProperty(SHARDING_COUNT_KEY)));
        ShardingSpherePreconditions.checkState(result > 0, () -> new AlgorithmInitializationException(this, "Sharding count must be a positive integer."));
        return result;
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        ShardingSpherePreconditions.checkNotNull(shardingValue.getValue(), NullShardingValueException::new);
        String suffix = String.valueOf(hashShardingValue(shardingValue.getValue()) % shardingCount);
        return ShardingAutoTableAlgorithmUtils.findMatchedTargetName(availableTargetNames, suffix, shardingValue.getDataNodeInfo()).orElse(null);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return availableTargetNames;
    }
    
    private long hashShardingValue(final Object shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
    
    @Override
    public int getAutoTablesAmount() {
        return shardingCount;
    }
    
    @Override
    public String getType() {
        return "HASH_MOD";
    }
}

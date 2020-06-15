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

package org.apache.shardingsphere.sharding.strategy.algorithm.sharding;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Modulo sharding algorithm.
 * 
 * <p>
 *     Shard by `y = x mod v` algorithm, v is sharding count. 
 * </p>
 */
@Getter
@Setter
public final class ModShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    public static final String SHARDING_COUNT_KEY = "sharding.count";
    
    private Properties props = new Properties();
    
    @Override
    public void init() {
        Preconditions.checkNotNull(props.get(SHARDING_COUNT_KEY), "Modulo value cannot be null.");
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(getLongValue(shardingValue.getValue()) % getModuloValue() + "")) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        if (isContainAllTargets(shardingValue)) {
            return availableTargetNames;
        }
        return getAvailableTargetNames(availableTargetNames, shardingValue);
    }
    
    private boolean isContainAllTargets(final RangeShardingValue<Comparable<?>> shardingValue) {
        return !shardingValue.getValueRange().hasUpperBound() || shardingValue.getValueRange().hasLowerBound()
                && getLongValue(shardingValue.getValueRange().upperEndpoint()) - getLongValue(shardingValue.getValueRange().lowerEndpoint()) >= getModuloValue() - 1;
    }
    
    private Collection<String> getAvailableTargetNames(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        for (long i = getLongValue(shardingValue.getValueRange().lowerEndpoint()); i <= getLongValue(shardingValue.getValueRange().upperEndpoint()); i++) {
            for (String each : availableTargetNames) {
                if (each.endsWith(i % getModuloValue() + "")) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    
    private long getModuloValue() {
        return Long.parseLong(props.get(SHARDING_COUNT_KEY).toString());
    }
    
    @Override
    public int getAutoTablesAmount() {
        Preconditions.checkNotNull(props.get(SHARDING_COUNT_KEY), "Modulo value cannot be null.");
        return Integer.parseInt(props.get(SHARDING_COUNT_KEY).toString());
    }
    
    private long getLongValue(final Comparable<?> value) {
        return Long.parseLong(value.toString());
    }
    
    @Override
    public String getType() {
        return "MOD";
    }
}

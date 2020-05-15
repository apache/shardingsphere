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
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Modulo sharding algorithm.
 * 
 * <p>Shard by `y = x mod v` algorithm. 
 * v is `MODULO_VALUE`. </p>
 */
public final class ModuloShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    
    private static final String MODULO_VALUE = "mod.value";
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Long> shardingValue) {
        Preconditions.checkNotNull(properties.get(MODULO_VALUE), "Modulo value cannot be null.");
        for (String each : availableTargetNames) {
            if (each.endsWith(shardingValue.getValue() % getModuloValue() + "")) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Long> shardingValue) {
        Preconditions.checkNotNull(properties.get(MODULO_VALUE), "Modulo value cannot be null.");
        if (isContainAllTargets(shardingValue)) {
            return availableTargetNames;
        }
        return getAvailableTargetNames(availableTargetNames, shardingValue);
    }
    
    private boolean isContainAllTargets(final RangeShardingValue<Long> shardingValue) {
        return (shardingValue.getValueRange().upperEndpoint() - shardingValue.getValueRange().lowerEndpoint()) >= getModuloValue() - 1;
    }
    
    private Collection<String> getAvailableTargetNames(final Collection<String> availableTargetNames, final RangeShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        for (Long i = shardingValue.getValueRange().lowerEndpoint(); i <= shardingValue.getValueRange().upperEndpoint(); i++) {
            for (String each : availableTargetNames) {
                if (each.endsWith(i % getModuloValue() + "")) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    
    private long getModuloValue() {
        return Long.parseLong(properties.get(MODULO_VALUE).toString());
    }
    
    @Override
    public String getType() {
        return "MOD";
    }
}

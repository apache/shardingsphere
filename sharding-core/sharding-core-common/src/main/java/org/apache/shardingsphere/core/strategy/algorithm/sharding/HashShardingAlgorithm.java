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
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * Hash sharding algorithm.
 * 
 * <p>Shard by `y = z mod v` algorithm with z = hash(x). 
 * v is `MODULO_VALUE`.
 * All available targets will be returned if sharding value is `RangeShardingValue`</p>
 */
public final class HashShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {
    
    private static final String MODULO_VALUE = "mod.value";
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        Preconditions.checkNotNull(properties.get(MODULO_VALUE), "Modulo value cannot be null.");
        for (String each : availableTargetNames) {
            if (each.endsWith(hashShardingValue(shardingValue.getValue()) % getModuloValue() + "")) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return availableTargetNames;
    }
    
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
    
    private long getModuloValue() {
        return Long.parseLong(properties.get(MODULO_VALUE).toString());
    }
    
    @Override
    public String getType() {
        return "HASH_MOD";
    }
}

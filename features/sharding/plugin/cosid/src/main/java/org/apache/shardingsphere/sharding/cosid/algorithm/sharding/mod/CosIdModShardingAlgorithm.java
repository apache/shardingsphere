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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.mod;

import me.ahoo.cosid.sharding.ModCycle;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.exception.ShardingPluginException;

import java.util.Collection;
import java.util.Properties;

/**
 * Modular sharding algorithm with CosId.
 * 
 * @param <T> type of sharding value
 */
public final class CosIdModShardingAlgorithm<T extends Number & Comparable<T>> implements StandardShardingAlgorithm<T> {
    
    private static final String MODULO_KEY = "mod";
    
    private ModCycle<T> modCycle;
    
    @Override
    public void init(final Properties props) {
        String divisorStr = getRequiredValue(props, MODULO_KEY);
        int divisor = Integer.parseInt(divisorStr);
        String logicNamePrefix = getRequiredValue(props, CosIdAlgorithmConstants.LOGIC_NAME_PREFIX_KEY);
        modCycle = new ModCycle<>(divisor, logicNamePrefix);
    }
    
    private String getRequiredValue(final Properties props, final String key) {
        ShardingSpherePreconditions.checkState(props.containsKey(key), () -> new ShardingPluginException("%s can not be null.", key));
        return props.getProperty(key);
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<T> shardingValue) {
        return modCycle.sharding(shardingValue.getValue());
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<T> shardingValue) {
        return modCycle.sharding(shardingValue.getValueRange());
    }
    
    @Override
    public String getType() {
        return CosIdAlgorithmConstants.TYPE_PREFIX + "MOD";
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.algorithm.sharding.cosid;

import lombok.Getter;
import lombok.Setter;
import me.ahoo.cosid.sharding.ModCycle;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * Modular sharding algorithm.
 */
public final class CosIdModShardingAlgorithm<T extends Number & Comparable<T>> implements StandardShardingAlgorithm<T> {

    public static final String TYPE = CosIdAlgorithm.TYPE_PREFIX + "MOD";

    public static final String MODULO_KEY = "mod";

    @Getter
    @Setter
    private Properties props = new Properties();

    private volatile ModCycle<T> modCycle;

    @Override
    public String getType() {
        return TYPE;
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
    public void init() {
        String divisorStr = PropertiesUtil.getRequiredValue(getProps(), MODULO_KEY);
        int divisor = Integer.parseInt(divisorStr);
        String logicNamePrefix = PropertiesUtil.getRequiredValue(getProps(), CosIdAlgorithm.LOGIC_NAME_PREFIX_KEY);
        modCycle = new ModCycle<>(divisor, logicNamePrefix);
    }
}

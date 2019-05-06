/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.api.algorithm.masterslave;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Master-slave database load-balance algorithm type.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum MasterSlaveLoadBalanceAlgorithmType {
    
    ROUND_ROBIN(new RoundRobinMasterSlaveLoadBalanceAlgorithm()),
    RANDOM(new RandomMasterSlaveLoadBalanceAlgorithm());
    
    private final MasterSlaveLoadBalanceAlgorithm algorithm;
    
    /**
     * Get default master-slave database load-balance algorithm type.
     * 
     * @return default master-slave database load-balance algorithm type
     */
    public static MasterSlaveLoadBalanceAlgorithmType getDefaultAlgorithmType() {
        return ROUND_ROBIN;
    }
}

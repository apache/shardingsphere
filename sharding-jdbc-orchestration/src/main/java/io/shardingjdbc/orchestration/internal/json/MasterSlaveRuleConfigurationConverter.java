/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.json;

import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Master-slave rule configuration json converter.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MasterSlaveRuleConfigurationConverter {
    
    static {
        GsonFactory.registerTypeAdapter(MasterSlaveLoadBalanceAlgorithm.class, new MasterSlaveLoadBalanceAlgorithmGsonTypeAdapter());
    }
    
    /**
     * Convert master-slave rule configuration to json.
     * 
     * @param masterSlaveRuleConfiguration master-slave rule configuration
     * @return master-slave rule configuration json string
     */
    public static String toJson(final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        return GsonFactory.getGson().toJson(masterSlaveRuleConfiguration);
    }
    
    /**
     * Convert master-slave rule configuration from json.
     *
     * @param json master-slave rule configuration json string
     * @return master-slave rule configuration
     */
    public static MasterSlaveRuleConfiguration fromJson(final String json) {
        return GsonFactory.getGson().fromJson(json, MasterSlaveRuleConfiguration.class);
    }
}

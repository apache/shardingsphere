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

package org.apache.shardingsphere.api.config.shadow;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;

import java.util.Map;

/**
 * Shadow rule configuration.
 *
 * @author xiayan
 */
@Getter
@Setter
public class ShadowRuleConfiguration implements RuleConfiguration {
    
    private String column;
    
    private ShardingRuleConfiguration shardingRuleConfig;
    
    private MasterSlaveRuleConfiguration masterSlaveRuleConfig;
    
    private EncryptRuleConfiguration encryptRuleConfig;
    
    private Map<String, String> shadowMappings;
    
    /**
     * Raw datasource is encrypt.
     *
     * @return is the encrypt datasource
     */
    public boolean isEncrypt() {
        return null != encryptRuleConfig;
    }
    
    /**
     * Raw datasource is sharding.
     *
     * @return is the sharding
     */
    public boolean isSharding() {
        return null != shardingRuleConfig;
    }
    
    /**
     * Raw datasource is master slave.
     *
     * @return is the master slave
     */
    public boolean isMasterSlave() {
        return null != masterSlaveRuleConfig;
    }
}

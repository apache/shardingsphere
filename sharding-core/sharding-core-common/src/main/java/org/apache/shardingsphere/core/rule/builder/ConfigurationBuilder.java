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

package org.apache.shardingsphere.core.rule.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Rule configuration builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationBuilder {
    
    /**
     * Build rules with sharding rule configuration.
     * 
     * @param shardingRuleConfig sharding rule configuration
     * @return rule configurations
     */
    public static Collection<RuleConfiguration> buildSharding(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        result.add(shardingRuleConfig);
        result.addAll(shardingRuleConfig.getMasterSlaveRuleConfigs());
        if (null != shardingRuleConfig.getEncryptRuleConfig() && !shardingRuleConfig.getEncryptRuleConfig().getTables().isEmpty()) {
            result.add(shardingRuleConfig.getEncryptRuleConfig());
        }
        return result;
    }
}

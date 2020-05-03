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
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Rule builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleBuilder {
    
    /**
     * Build rules.
     *
     * @param dataSourceNames data source names
     * @param ruleConfigurations rule configurations
     * @return rules
     */
    public static Collection<ShardingSphereRule> build(final Collection<String> dataSourceNames, final Collection<RuleConfiguration> ruleConfigurations) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (RuleConfiguration each : ruleConfigurations) {
            if (each instanceof ShardingRuleConfiguration) {
                result.add(new ShardingRule((ShardingRuleConfiguration) each, dataSourceNames));
            } else if (each instanceof MasterSlaveRuleConfiguration) {
                result.add(new MasterSlaveRule((MasterSlaveRuleConfiguration) each));
            } else if (each instanceof EncryptRuleConfiguration) {
                result.add(new EncryptRule((EncryptRuleConfiguration) each));
            }
        }
        return result;
    }
}

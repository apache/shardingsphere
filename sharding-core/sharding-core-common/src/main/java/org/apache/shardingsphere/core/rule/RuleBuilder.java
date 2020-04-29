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

package org.apache.shardingsphere.core.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Rule builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleBuilder {
    
    /**
     * Build rules.
     * 
     * @param dataSourceNames data source names
     * @param shardingRuleConfig sharding rule configuration
     * @return rules
     */
    public static Collection<BaseRule> build(final Collection<String> dataSourceNames, final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<BaseRule> result = new LinkedList<>();
        result.add(new ShardingRule(shardingRuleConfig, dataSourceNames));
        result.addAll(shardingRuleConfig.getMasterSlaveRuleConfigs().stream().map(MasterSlaveRule::new).collect(Collectors.toList()));
        if (null != shardingRuleConfig.getEncryptRuleConfig()) {
            result.add(new EncryptRule(shardingRuleConfig.getEncryptRuleConfig()));
        }
        return result;
    }
}

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

package io.shardingsphere.orchestration.internal.rule;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Orchestration sharding rule.
 *
 * @author panjuan
 */
public final class OrchestrationShardingRule extends ShardingRule {
    
    private final Collection<OrchestrationMasterSlaveRule> masterSlaveRules = new LinkedList<>();
    
    public OrchestrationShardingRule(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames) {
        super(shardingRuleConfig, dataSourceNames);
        initMasterSlaveRules(shardingRuleConfig);
    }
    
    private void initMasterSlaveRules(final ShardingRuleConfiguration shardingRuleConfig) {
        for (MasterSlaveRuleConfiguration each : shardingRuleConfig.getMasterSlaveRuleConfigs()) {
            masterSlaveRules.add(new OrchestrationMasterSlaveRule(each));
        }
    }
    
    @Override
    public Collection<MasterSlaveRule> getMasterSlaveRules() {
        return Collections2.transform(masterSlaveRules, new Function<OrchestrationMasterSlaveRule, MasterSlaveRule>() {
            
            @Override
            public MasterSlaveRule apply(final OrchestrationMasterSlaveRule input) {
                return input;
            }
        });
    }
}

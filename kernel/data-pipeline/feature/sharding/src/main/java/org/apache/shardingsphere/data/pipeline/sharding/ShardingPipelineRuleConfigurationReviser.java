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

package org.apache.shardingsphere.data.pipeline.sharding;

import org.apache.shardingsphere.data.pipeline.core.datasource.rule.PipelineRuleConfigurationReviser;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;

/**
 * Sharding pipeline rule configuration reviser.
 */
public final class ShardingPipelineRuleConfigurationReviser implements PipelineRuleConfigurationReviser<ShardingRuleConfiguration> {
    
    @Override
    public void revise(final ShardingRuleConfiguration ruleConfig) {
        enableRangeQueryForInline(ruleConfig);
        removeAuditStrategy(ruleConfig);
    }
    
    private void enableRangeQueryForInline(final ShardingRuleConfiguration ruleConfig) {
        for (AlgorithmConfiguration each : ruleConfig.getShardingAlgorithms().values()) {
            if ("INLINE".equalsIgnoreCase(each.getType()) || "COMPLEX_INLINE".equalsIgnoreCase(each.getType())) {
                each.getProps().setProperty("allow-range-query-with-inline-sharding", Boolean.TRUE.toString());
            }
        }
    }
    
    private void removeAuditStrategy(final ShardingRuleConfiguration ruleConfig) {
        ruleConfig.setDefaultAuditStrategy(null);
        ruleConfig.getAuditors().clear();
        for (ShardingTableRuleConfiguration each : ruleConfig.getTables()) {
            each.setAuditStrategy(null);
        }
        for (ShardingAutoTableRuleConfiguration each : ruleConfig.getAutoTables()) {
            each.setAuditStrategy(null);
        }
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getTypeClass() {
        return ShardingRuleConfiguration.class;
    }
}

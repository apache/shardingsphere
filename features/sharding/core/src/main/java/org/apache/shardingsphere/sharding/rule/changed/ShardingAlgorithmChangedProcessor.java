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

package org.apache.shardingsphere.sharding.rule.changed;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mode.processor.AlgorithmChangedProcessor;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Map;

/**
 * Sharding algorithm changed processor.
 */
public final class ShardingAlgorithmChangedProcessor extends AlgorithmChangedProcessor<ShardingRuleConfiguration> {
    
    public ShardingAlgorithmChangedProcessor() {
        super(ShardingRule.class);
    }
    
    @Override
    protected ShardingRuleConfiguration createEmptyRuleConfiguration() {
        return new ShardingRuleConfiguration();
    }
    
    @Override
    protected Map<String, AlgorithmConfiguration> getAlgorithmConfigurations(final ShardingRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getShardingAlgorithms();
    }
    
    @Override
    public String getType() {
        return ShardingRuleNodePathProvider.RULE_TYPE + "." + ShardingRuleNodePathProvider.SHARDING_ALGORITHMS;
    }
}

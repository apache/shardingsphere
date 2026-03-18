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

package org.apache.shardingsphere.readwritesplitting.rule.changed;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.processor.AlgorithmChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Readwrite-splitting load-balancer changed processor.
 */
public final class ReadwriteSplittingLoadBalancerChangedProcessor extends AlgorithmChangedProcessor<ReadwriteSplittingRuleConfiguration> {
    
    public ReadwriteSplittingLoadBalancerChangedProcessor() {
        super(ReadwriteSplittingRule.class);
    }
    
    @Override
    protected ReadwriteSplittingRuleConfiguration createEmptyRuleConfiguration() {
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>());
    }
    
    @Override
    protected Map<String, AlgorithmConfiguration> getAlgorithmConfigurations(final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getLoadBalancers();
    }
    
    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("readwrite_splitting", "load_balancers");
    }
}

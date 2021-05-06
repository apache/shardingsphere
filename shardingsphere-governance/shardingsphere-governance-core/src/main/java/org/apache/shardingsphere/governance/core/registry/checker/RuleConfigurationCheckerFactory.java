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

package org.apache.shardingsphere.governance.core.registry.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleConfigurationCheckerFactory {
    
    private static final Map<Class<? extends RuleConfiguration>, RuleConfigurationChecker<?>> REGISTRY = new HashMap<>();
    
    static {
        REGISTRY.put(ShardingRuleConfiguration.class, new ShardingRuleConfigurationChecker());
        REGISTRY.put(AlgorithmProvidedShardingRuleConfiguration.class, new AlgorithmProvidedShardingRuleConfigurationChecker());
        REGISTRY.put(ReadwriteSplittingRuleConfiguration.class, new ReadwriteSplittingRuleConfigurationChecker());
        REGISTRY.put(AlgorithmProvidedReadwriteSplittingRuleConfiguration.class, new AlgorithmProvidedReadwriteSplittingRuleConfigurationChecker());
        REGISTRY.put(EncryptRuleConfiguration.class, new EncryptRuleConfigurationChecker());
        REGISTRY.put(AlgorithmProvidedEncryptRuleConfiguration.class, new AlgorithmProvidedEncryptRuleConfigurationChecker());
        REGISTRY.put(ShadowRuleConfiguration.class, new ShadowRuleConfigurationChecker());
        REGISTRY.put(DatabaseDiscoveryRuleConfiguration.class, new DatabaseDiscoveryRuleConfigurationChecker());
    }
    
    /**
     * Get rule configuration checker.
     * 
     * @param ruleConfiguration rule configuration
     * @return rule configuration checker
     */
    public static Optional<RuleConfigurationChecker> newInstance(final RuleConfiguration ruleConfiguration) {
        return REGISTRY.entrySet().stream().filter(entry -> entry.getKey().isAssignableFrom(ruleConfiguration.getClass())).findFirst().map(Entry::getValue);
    }
}

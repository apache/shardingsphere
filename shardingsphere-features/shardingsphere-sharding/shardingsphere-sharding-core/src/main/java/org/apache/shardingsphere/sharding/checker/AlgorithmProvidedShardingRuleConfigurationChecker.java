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

package org.apache.shardingsphere.sharding.checker;

import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;

import java.util.Collection;

/**
 * Algorithm provided sharding rule configuration checker.
 */
public final class AlgorithmProvidedShardingRuleConfigurationChecker extends AbstractShardingRuleConfigurationChecker<AlgorithmProvidedShardingRuleConfiguration> {
    
    @Override
    public int getOrder() {
        return ShardingOrder.ALGORITHM_PROVIDER_ORDER;
    }
    
    @Override
    public Class<AlgorithmProvidedShardingRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedShardingRuleConfiguration.class;
    }
    
    @Override
    protected Collection<String> getKeyGenerators(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getKeyGenerators().keySet();
    }
    
    @Override
    protected Collection<String> getAuditors(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getAuditors().keySet();
    }
    
    @Override
    protected Collection<String> getShardingAlgorithms(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getShardingAlgorithms().keySet();
    }
    
    @Override
    protected Collection<ShardingTableRuleConfiguration> getTables(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getTables();
    }
    
    @Override
    protected Collection<ShardingAutoTableRuleConfiguration> getAutoTables(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getAutoTables();
    }
    
    @Override
    protected KeyGenerateStrategyConfiguration getDefaultKeyGenerateStrategy(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getDefaultKeyGenerateStrategy();
    }
    
    @Override
    protected ShardingAuditStrategyConfiguration getDefaultAuditStrategy(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getDefaultAuditStrategy();
    }
    
    @Override
    protected ShardingStrategyConfiguration getDefaultDatabaseShardingStrategy(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getDefaultDatabaseShardingStrategy();
    }
    
    @Override
    protected ShardingStrategyConfiguration getDefaultTableShardingStrategy(final AlgorithmProvidedShardingRuleConfiguration config) {
        return config.getDefaultTableShardingStrategy();
    }
}

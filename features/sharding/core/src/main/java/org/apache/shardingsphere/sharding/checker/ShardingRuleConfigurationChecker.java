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

import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;

import java.util.Collection;

/**
 * Sharding rule configuration checker.
 */
public final class ShardingRuleConfigurationChecker extends AbstractShardingRuleConfigurationChecker<ShardingRuleConfiguration> {
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getTypeClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    protected Collection<String> getKeyGenerators(final ShardingRuleConfiguration config) {
        return config.getKeyGenerators().keySet();
    }
    
    @Override
    protected Collection<String> getAuditors(final ShardingRuleConfiguration config) {
        return config.getAuditors().keySet();
    }
    
    @Override
    protected Collection<String> getShardingAlgorithms(final ShardingRuleConfiguration config) {
        return config.getShardingAlgorithms().keySet();
    }
    
    @Override
    protected Collection<ShardingTableRuleConfiguration> getTables(final ShardingRuleConfiguration config) {
        return config.getTables();
    }
    
    @Override
    protected Collection<ShardingAutoTableRuleConfiguration> getAutoTables(final ShardingRuleConfiguration config) {
        return config.getAutoTables();
    }
    
    @Override
    protected KeyGenerateStrategyConfiguration getDefaultKeyGenerateStrategy(final ShardingRuleConfiguration config) {
        return config.getDefaultKeyGenerateStrategy();
    }
    
    @Override
    protected ShardingAuditStrategyConfiguration getDefaultAuditStrategy(final ShardingRuleConfiguration config) {
        return config.getDefaultAuditStrategy();
    }
    
    @Override
    protected ShardingStrategyConfiguration getDefaultDatabaseShardingStrategy(final ShardingRuleConfiguration config) {
        return config.getDefaultDatabaseShardingStrategy();
    }
    
    @Override
    protected ShardingStrategyConfiguration getDefaultTableShardingStrategy(final ShardingRuleConfiguration config) {
        return config.getDefaultTableShardingStrategy();
    }
}

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

package org.apache.shardingsphere.sharding.rule.builder;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Sharding rule builder.
 */
public final class ShardingRuleBuilder implements DatabaseRuleBuilder<ShardingRuleConfiguration> {
    
    @Override
    public ShardingRule build(final ShardingRuleConfiguration config, final String databaseName,
                              final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> builtRules, final InstanceContext instanceContext) {
        Preconditions.checkArgument(null != dataSources && !dataSources.isEmpty(), "Data source names cannot be empty.");
        Preconditions.checkArgument(isValidRuleConfiguration(config), "Invalid sharding configuration in ShardingRuleConfiguration.");
        return new ShardingRule(config, dataSources.keySet(), instanceContext);
    }
    
    private boolean isValidRuleConfiguration(final ShardingRuleConfiguration config) {
        Map<String, ShardingSphereAlgorithmConfiguration> keyGenerators = config.getKeyGenerators();
        Map<String, ShardingSphereAlgorithmConfiguration> auditors = config.getAuditors();
        Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithms = config.getShardingAlgorithms();
        if (isInvalidKeyGenerateStrategy(config.getDefaultKeyGenerateStrategy(), keyGenerators)
                || isInvalidAuditStrategy(config.getDefaultAuditStrategy(), auditors)
                || isInvalidShardingStrategy(config.getDefaultDatabaseShardingStrategy(), shardingAlgorithms)
                || isInvalidShardingStrategy(config.getDefaultTableShardingStrategy(), shardingAlgorithms)) {
            return false;
        }
        for (ShardingTableRuleConfiguration each : config.getTables()) {
            if (isInvalidKeyGenerateStrategy(each.getKeyGenerateStrategy(), keyGenerators) || isInvalidAuditStrategy(each.getAuditStrategy(), auditors)
                    || isInvalidShardingStrategy(each.getDatabaseShardingStrategy(), shardingAlgorithms) || isInvalidShardingStrategy(each.getTableShardingStrategy(), shardingAlgorithms)) {
                return false;
            }
        }
        for (ShardingAutoTableRuleConfiguration each : config.getAutoTables()) {
            if (isInvalidKeyGenerateStrategy(each.getKeyGenerateStrategy(), keyGenerators) || isInvalidAuditStrategy(each.getAuditStrategy(), auditors)
                    || isInvalidShardingStrategy(each.getShardingStrategy(), shardingAlgorithms)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isInvalidKeyGenerateStrategy(final KeyGenerateStrategyConfiguration keyGenerateStrategy, final Map<String, ShardingSphereAlgorithmConfiguration> keyGenerators) {
        if (null == keyGenerateStrategy) {
            return false;
        }
        return !keyGenerators.containsKey(keyGenerateStrategy.getKeyGeneratorName());
    }
    
    private boolean isInvalidShardingStrategy(final ShardingStrategyConfiguration shardingStrategy, final Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithms) {
        if (null == shardingStrategy || shardingStrategy instanceof NoneShardingStrategyConfiguration) {
            return false;
        }
        return !shardingAlgorithms.containsKey(shardingStrategy.getShardingAlgorithmName());
    }
    
    private boolean isInvalidAuditStrategy(final ShardingAuditStrategyConfiguration auditStrategy, final Map<String, ShardingSphereAlgorithmConfiguration> auditors) {
        if (null == auditStrategy) {
            return false;
        }
        return !auditors.keySet().containsAll(auditStrategy.getAuditorNames());
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

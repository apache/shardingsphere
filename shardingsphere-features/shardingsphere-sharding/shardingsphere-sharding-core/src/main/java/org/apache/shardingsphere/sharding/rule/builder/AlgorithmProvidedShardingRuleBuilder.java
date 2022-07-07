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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Algorithm provided sharding rule builder.
 */
public final class AlgorithmProvidedShardingRuleBuilder implements DatabaseRuleBuilder<AlgorithmProvidedShardingRuleConfiguration> {
    
    @Override
    public ShardingRule build(final AlgorithmProvidedShardingRuleConfiguration config, final String databaseName,
                              final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> builtRules, final InstanceContext instanceContext) {
        Preconditions.checkArgument(null != dataSources && !dataSources.isEmpty(), "Data sources cannot be empty.");
        Preconditions.checkArgument(isValidRuleConfiguration(config), "Invalid sharding configuration in AlgorithmProvidedShardingRuleConfiguration.");
        return new ShardingRule(config, dataSources.keySet(), instanceContext);
    }
    
    private boolean isValidRuleConfiguration(final AlgorithmProvidedShardingRuleConfiguration config) {
        Map<String, KeyGenerateAlgorithm> keyGenerators = config.getKeyGenerators();
        Map<String, ShardingAlgorithm> shardingAlgorithms = config.getShardingAlgorithms();
        if (isInvalidKeyGenerateStrategy(config.getDefaultKeyGenerateStrategy(), keyGenerators)
                || isInvalidShardingStrategy(config.getDefaultDatabaseShardingStrategy(), shardingAlgorithms)
                || isInvalidShardingStrategy(config.getDefaultTableShardingStrategy(), shardingAlgorithms)) {
            return false;
        }
        for (ShardingTableRuleConfiguration each : config.getTables()) {
            if (isInvalidKeyGenerateStrategy(each.getKeyGenerateStrategy(), keyGenerators) || isInvalidShardingStrategy(each.getDatabaseShardingStrategy(), shardingAlgorithms)
                    || isInvalidShardingStrategy(each.getTableShardingStrategy(), shardingAlgorithms)) {
                return false;
            }
        }
        for (ShardingAutoTableRuleConfiguration each : config.getAutoTables()) {
            if (isInvalidKeyGenerateStrategy(each.getKeyGenerateStrategy(), keyGenerators) || isInvalidShardingStrategy(each.getShardingStrategy(), shardingAlgorithms)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isInvalidKeyGenerateStrategy(final KeyGenerateStrategyConfiguration keyGenerateStrategy, final Map<String, KeyGenerateAlgorithm> keyGenerators) {
        if (null == keyGenerateStrategy) {
            return false;
        }
        return !keyGenerators.containsKey(keyGenerateStrategy.getKeyGeneratorName());
    }
    
    private boolean isInvalidShardingStrategy(final ShardingStrategyConfiguration shardingStrategy, final Map<String, ShardingAlgorithm> shardingAlgorithms) {
        if (null == shardingStrategy || shardingStrategy instanceof NoneShardingStrategyConfiguration) {
            return false;
        }
        return !shardingAlgorithms.containsKey(shardingStrategy.getShardingAlgorithmName());
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ALGORITHM_PROVIDER_ORDER;
    }
    
    @Override
    public Class<AlgorithmProvidedShardingRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedShardingRuleConfiguration.class;
    }
}

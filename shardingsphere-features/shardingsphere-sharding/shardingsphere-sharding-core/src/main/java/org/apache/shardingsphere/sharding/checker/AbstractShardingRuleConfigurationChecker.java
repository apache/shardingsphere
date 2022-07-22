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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;

import java.util.Collection;

/**
 * Abstract sharding rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractShardingRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String databaseName, final T config) {
        Collection<String> keyGenerators = getKeyGenerators(config);
        Collection<String> auditors = getAuditors(config);
        Collection<String> shardingAlgorithms = getShardingAlgorithms(config);
        checkTableConfiguration(getTables(config), getAutoTables(config), keyGenerators, auditors, shardingAlgorithms, databaseName);
        checkKeyGenerateStrategy(getDefaultKeyGenerateStrategy(config), keyGenerators, databaseName);
        checkAuditStrategy(getDefaultAuditStrategy(config), auditors, databaseName);
        checkShardingStrategy(getDefaultDatabaseShardingStrategy(config), shardingAlgorithms, databaseName);
        checkShardingStrategy(getDefaultTableShardingStrategy(config), shardingAlgorithms, databaseName);
    }
    
    private void checkTableConfiguration(final Collection<ShardingTableRuleConfiguration> tables, final Collection<ShardingAutoTableRuleConfiguration> autoTables,
                                         final Collection<String> keyGenerators, final Collection<String> auditors, final Collection<String> shardingAlgorithms, final String databaseName) {
        Preconditions.checkState(!tables.isEmpty() || !autoTables.isEmpty(),
                "No available sharding table or autoTable configurations in database `%s`.", databaseName);
        for (ShardingTableRuleConfiguration each : tables) {
            checkKeyGenerateStrategy(each.getKeyGenerateStrategy(), keyGenerators, databaseName);
            checkAuditStrategy(each.getAuditStrategy(), auditors, databaseName);
            checkShardingStrategy(each.getDatabaseShardingStrategy(), shardingAlgorithms, databaseName);
            checkShardingStrategy(each.getTableShardingStrategy(), shardingAlgorithms, databaseName);
        }
        for (ShardingAutoTableRuleConfiguration each : autoTables) {
            checkKeyGenerateStrategy(each.getKeyGenerateStrategy(), keyGenerators, databaseName);
            checkAuditStrategy(each.getAuditStrategy(), auditors, databaseName);
            checkShardingStrategy(each.getShardingStrategy(), shardingAlgorithms, databaseName);
        }
    }
    
    private void checkKeyGenerateStrategy(final KeyGenerateStrategyConfiguration keyGenerateStrategy, final Collection<String> keyGenerators, final String databaseName) {
        if (null == keyGenerateStrategy) {
            return;
        }
        Preconditions.checkState(keyGenerators.contains(keyGenerateStrategy.getKeyGeneratorName()),
                "Can not find keyGenerator `%s` in database `%s`.", keyGenerateStrategy.getKeyGeneratorName(), databaseName);
    }
    
    private void checkAuditStrategy(final ShardingAuditStrategyConfiguration auditStrategy, final Collection<String> auditors, final String databaseName) {
        if (null == auditStrategy) {
            return;
        }
        Preconditions.checkState(auditors.containsAll(auditStrategy.getAuditorNames()),
                "Can not find all auditors `%s` in database `%s`.", auditStrategy.getAuditorNames(), databaseName);
    }
    
    private void checkShardingStrategy(final ShardingStrategyConfiguration shardingStrategy, final Collection<String> shardingAlgorithms, final String databaseName) {
        if (null == shardingStrategy || shardingStrategy instanceof NoneShardingStrategyConfiguration) {
            return;
        }
        Preconditions.checkState(shardingAlgorithms.contains(shardingStrategy.getShardingAlgorithmName()),
                "Can not find shardingAlgorithm `%s` in database `%s`.", shardingStrategy.getShardingAlgorithmName(), databaseName);
    }
    
    protected abstract Collection<String> getKeyGenerators(T config);
    
    protected abstract Collection<String> getAuditors(T config);
    
    protected abstract Collection<String> getShardingAlgorithms(T config);
    
    protected abstract Collection<ShardingTableRuleConfiguration> getTables(T config);
    
    protected abstract Collection<ShardingAutoTableRuleConfiguration> getAutoTables(T config);
    
    protected abstract KeyGenerateStrategyConfiguration getDefaultKeyGenerateStrategy(T config);
    
    protected abstract ShardingAuditStrategyConfiguration getDefaultAuditStrategy(T config);
    
    protected abstract ShardingStrategyConfiguration getDefaultDatabaseShardingStrategy(T config);
    
    protected abstract ShardingStrategyConfiguration getDefaultTableShardingStrategy(T config);
}

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
        checkShardingRuleConfiguration(databaseName, config);
    }
    
    /**
     * Check sharding rule configuration.
     * 
     * @param databaseName database name
     * @param config config
     */
    protected abstract void checkShardingRuleConfiguration(String databaseName, T config);
    
    /**
     * Check table configuration.
     * 
     * @param tables table configurations
     * @param autoTables autoTable configurations
     * @param keyGenerators keyGenerators
     * @param auditors auditors
     * @param shardingAlgorithms shardingAlgorithms
     * @param databaseName database name
     */
    protected void checkTableConfiguration(final Collection<ShardingTableRuleConfiguration> tables, final Collection<ShardingAutoTableRuleConfiguration> autoTables,
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
    
    /**
     * Check key generate strategy.
     * 
     * @param keyGenerateStrategy key generate strategy
     * @param keyGenerators keyGenerators
     * @param databaseName database name
     */
    protected void checkKeyGenerateStrategy(final KeyGenerateStrategyConfiguration keyGenerateStrategy, final Collection<String> keyGenerators, final String databaseName) {
        if (null == keyGenerateStrategy) {
            return;
        }
        Preconditions.checkState(keyGenerators.contains(keyGenerateStrategy.getKeyGeneratorName()),
                "Can not find keyGenerator `%s` in database `%s`.", keyGenerateStrategy.getKeyGeneratorName(), databaseName);
    }
    
    /**
     * Check audit strategy.
     * 
     * @param auditStrategy audit strategy
     * @param auditors auditors
     * @param databaseName database name
     */
    protected void checkAuditStrategy(final ShardingAuditStrategyConfiguration auditStrategy, final Collection<String> auditors, final String databaseName) {
        if (null == auditStrategy) {
            return;
        }
        Preconditions.checkState(auditors.containsAll(auditStrategy.getAuditorNames()),
                "Can not find all auditors `%s` in database `%s`.", auditStrategy.getAuditorNames(), databaseName);
    }
    
    /**
     * Check sharding strategy.
     * 
     * @param shardingStrategy sharding strategy
     * @param shardingAlgorithms shardingAlgorithms
     * @param databaseName database name
     */
    protected void checkShardingStrategy(final ShardingStrategyConfiguration shardingStrategy, final Collection<String> shardingAlgorithms, final String databaseName) {
        if (null == shardingStrategy || shardingStrategy instanceof NoneShardingStrategyConfiguration) {
            return;
        }
        Preconditions.checkState(shardingAlgorithms.contains(shardingStrategy.getShardingAlgorithmName()),
                "Can not find shardingAlgorithm `%s` in database `%s`.", shardingStrategy.getShardingAlgorithmName(), databaseName);
    }
}

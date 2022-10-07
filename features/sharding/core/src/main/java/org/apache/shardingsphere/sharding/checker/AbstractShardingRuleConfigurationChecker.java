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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Abstract sharding rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractShardingRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String databaseName, final T config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        Collection<String> keyGenerators = getKeyGenerators(config);
        Collection<String> auditors = getAuditors(config);
        Collection<String> shardingAlgorithms = getShardingAlgorithms(config);
        checkTableConfiguration(databaseName, getTables(config), getAutoTables(config), keyGenerators, auditors, shardingAlgorithms);
        checkKeyGenerateStrategy(databaseName, getDefaultKeyGenerateStrategy(config), keyGenerators);
        checkAuditStrategy(databaseName, getDefaultAuditStrategy(config), auditors);
        checkShardingStrategy(databaseName, getDefaultDatabaseShardingStrategy(config), shardingAlgorithms);
        checkShardingStrategy(databaseName, getDefaultTableShardingStrategy(config), shardingAlgorithms);
    }
    
    private void checkTableConfiguration(final String databaseName, final Collection<ShardingTableRuleConfiguration> tables, final Collection<ShardingAutoTableRuleConfiguration> autoTables,
                                         final Collection<String> keyGenerators, final Collection<String> auditors, final Collection<String> shardingAlgorithms) {
        for (ShardingTableRuleConfiguration each : tables) {
            checkKeyGenerateStrategy(databaseName, each.getKeyGenerateStrategy(), keyGenerators);
            checkAuditStrategy(databaseName, each.getAuditStrategy(), auditors);
            checkShardingStrategy(databaseName, each.getDatabaseShardingStrategy(), shardingAlgorithms);
            checkShardingStrategy(databaseName, each.getTableShardingStrategy(), shardingAlgorithms);
        }
        for (ShardingAutoTableRuleConfiguration each : autoTables) {
            checkKeyGenerateStrategy(databaseName, each.getKeyGenerateStrategy(), keyGenerators);
            checkAuditStrategy(databaseName, each.getAuditStrategy(), auditors);
            checkShardingStrategy(databaseName, each.getShardingStrategy(), shardingAlgorithms);
        }
    }
    
    private void checkKeyGenerateStrategy(final String databaseName, final KeyGenerateStrategyConfiguration keyGenerateStrategy, final Collection<String> keyGenerators) {
        if (null == keyGenerateStrategy) {
            return;
        }
        Preconditions.checkState(keyGenerators.contains(keyGenerateStrategy.getKeyGeneratorName()),
                "Can not find keyGenerator `%s` in database `%s`.", keyGenerateStrategy.getKeyGeneratorName(), databaseName);
    }
    
    private void checkAuditStrategy(final String databaseName, final ShardingAuditStrategyConfiguration auditStrategy, final Collection<String> auditors) {
        if (null == auditStrategy) {
            return;
        }
        Preconditions.checkState(auditors.containsAll(auditStrategy.getAuditorNames()),
                "Can not find all auditors `%s` in database `%s`.", auditStrategy.getAuditorNames(), databaseName);
    }
    
    private void checkShardingStrategy(final String databaseName, final ShardingStrategyConfiguration shardingStrategy, final Collection<String> shardingAlgorithms) {
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

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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingAlgorithmException;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Sharding rule configuration checker.
 */
public final class ShardingRuleConfigurationChecker implements RuleConfigurationChecker<ShardingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ShardingRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Collection<String> keyGenerators = config.getKeyGenerators().keySet();
        Collection<String> auditors = config.getAuditors().keySet();
        Collection<String> shardingAlgorithms = config.getShardingAlgorithms().keySet();
        checkTableConfiguration(databaseName, config.getTables(), config.getAutoTables(), keyGenerators, auditors, shardingAlgorithms);
        checkKeyGenerateStrategy(databaseName, config.getDefaultKeyGenerateStrategy(), keyGenerators);
        checkAuditStrategy(databaseName, config.getDefaultAuditStrategy(), auditors);
        checkShardingStrategy(databaseName, config.getDefaultDatabaseShardingStrategy(), shardingAlgorithms);
        checkShardingStrategy(databaseName, config.getDefaultTableShardingStrategy(), shardingAlgorithms);
    }
    
    private void checkTableConfiguration(final String databaseName, final Collection<ShardingTableRuleConfiguration> tables, final Collection<ShardingAutoTableRuleConfiguration> autoTables,
                                         final Collection<String> keyGenerators, final Collection<String> auditors, final Collection<String> shardingAlgorithms) {
        for (ShardingTableRuleConfiguration each : tables) {
            checkLogicTable(databaseName, each.getLogicTable());
            checkKeyGenerateStrategy(databaseName, each.getKeyGenerateStrategy(), keyGenerators);
            checkAuditStrategy(databaseName, each.getAuditStrategy(), auditors);
            checkShardingStrategy(databaseName, each.getDatabaseShardingStrategy(), shardingAlgorithms);
            checkShardingStrategy(databaseName, each.getTableShardingStrategy(), shardingAlgorithms);
        }
        for (ShardingAutoTableRuleConfiguration each : autoTables) {
            checkLogicTable(databaseName, each.getLogicTable());
            checkKeyGenerateStrategy(databaseName, each.getKeyGenerateStrategy(), keyGenerators);
            checkAuditStrategy(databaseName, each.getAuditStrategy(), auditors);
            checkShardingStrategy(databaseName, each.getShardingStrategy(), shardingAlgorithms);
        }
    }
    
    private void checkLogicTable(final String databaseName, final String logicTable) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(logicTable), () -> new MissingRequiredShardingConfigurationException("Sharding logic table", databaseName));
    }
    
    private void checkKeyGenerateStrategy(final String databaseName, final KeyGenerateStrategyConfiguration keyGenerateStrategy, final Collection<String> keyGenerators) {
        if (null == keyGenerateStrategy) {
            return;
        }
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(keyGenerateStrategy.getColumn()), () -> new MissingRequiredShardingConfigurationException("Key generate column", databaseName));
        ShardingSpherePreconditions.checkState(keyGenerators.contains(keyGenerateStrategy.getKeyGeneratorName()),
                () -> new MissingRequiredShardingAlgorithmException(keyGenerateStrategy.getKeyGeneratorName(), databaseName));
    }
    
    private void checkAuditStrategy(final String databaseName, final ShardingAuditStrategyConfiguration auditStrategy, final Collection<String> auditors) {
        if (null == auditStrategy) {
            return;
        }
        ShardingSpherePreconditions.checkState(auditors.containsAll(auditStrategy.getAuditorNames()),
                () -> new MissingRequiredShardingAlgorithmException(Joiner.on(",").join(auditStrategy.getAuditorNames()), databaseName));
    }
    
    private void checkShardingStrategy(final String databaseName, final ShardingStrategyConfiguration shardingStrategy, final Collection<String> shardingAlgorithms) {
        if (null == shardingStrategy || shardingStrategy instanceof NoneShardingStrategyConfiguration) {
            return;
        }
        if (shardingStrategy instanceof ComplexShardingStrategyConfiguration) {
            ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(((ComplexShardingStrategyConfiguration) shardingStrategy).getShardingColumns()),
                    () -> new MissingRequiredShardingConfigurationException("Complex sharding columns", databaseName));
        }
        ShardingSpherePreconditions.checkNotNull(shardingStrategy.getShardingAlgorithmName(), () -> new MissingRequiredShardingConfigurationException("Sharding algorithm name", databaseName));
        ShardingSpherePreconditions.checkState(shardingAlgorithms.contains(shardingStrategy.getShardingAlgorithmName()),
                () -> new MissingRequiredShardingAlgorithmException(shardingStrategy.getShardingAlgorithmName(), databaseName));
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

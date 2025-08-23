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

package org.apache.shardingsphere.sharding.checker.config;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Sharding rule configuration checker.
 */
public final class ShardingRuleConfigurationChecker implements DatabaseRuleConfigurationChecker<ShardingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ShardingRuleConfiguration ruleConfig, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkShardingAlgorithms(ruleConfig.getShardingAlgorithms().values());
        checkKeyGeneratorAlgorithms(ruleConfig.getKeyGenerators().values());
        Collection<String> keyGenerators = ruleConfig.getKeyGenerators().keySet();
        Collection<String> auditors = ruleConfig.getAuditors().keySet();
        Collection<String> shardingAlgorithms = ruleConfig.getShardingAlgorithms().keySet();
        checkTables(databaseName, ruleConfig.getTables(), ruleConfig.getAutoTables(), keyGenerators, auditors, shardingAlgorithms);
        checkKeyGenerateStrategy(databaseName, ruleConfig.getDefaultKeyGenerateStrategy(), keyGenerators);
        checkAuditStrategy(databaseName, ruleConfig.getDefaultAuditStrategy(), auditors);
        checkShardingStrategy(databaseName, ruleConfig.getDefaultDatabaseShardingStrategy(), shardingAlgorithms);
        checkShardingStrategy(databaseName, ruleConfig.getDefaultTableShardingStrategy(), shardingAlgorithms);
    }
    
    private void checkShardingAlgorithms(final Collection<AlgorithmConfiguration> algorithmConfigs) {
        algorithmConfigs.forEach(each -> TypedSPILoader.checkService(ShardingAlgorithm.class, each.getType(), each.getProps()));
    }
    
    private void checkKeyGeneratorAlgorithms(final Collection<AlgorithmConfiguration> algorithmConfigs) {
        algorithmConfigs.stream().filter(Objects::nonNull).forEach(each -> TypedSPILoader.checkService(KeyGenerateAlgorithm.class, each.getType(), each.getProps()));
    }
    
    private void checkTables(final String databaseName, final Collection<ShardingTableRuleConfiguration> tables, final Collection<ShardingAutoTableRuleConfiguration> autoTables,
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
        ShardingSpherePreconditions.checkNotEmpty(logicTable, () -> new MissingRequiredShardingConfigurationException("Sharding logic table", databaseName));
    }
    
    private void checkKeyGenerateStrategy(final String databaseName, final KeyGenerateStrategyConfiguration keyGenerateStrategy, final Collection<String> keyGenerators) {
        if (null == keyGenerateStrategy) {
            return;
        }
        ShardingSpherePreconditions.checkNotEmpty(keyGenerateStrategy.getColumn(), () -> new MissingRequiredShardingConfigurationException("Key generate column", databaseName));
        ShardingSpherePreconditions.checkContains(keyGenerators, keyGenerateStrategy.getKeyGeneratorName(),
                () -> new UnregisteredAlgorithmException("Key generate", keyGenerateStrategy.getKeyGeneratorName(), new SQLExceptionIdentifier(databaseName)));
    }
    
    private void checkAuditStrategy(final String databaseName, final ShardingAuditStrategyConfiguration auditStrategy, final Collection<String> auditors) {
        if (null == auditStrategy) {
            return;
        }
        ShardingSpherePreconditions.checkState(auditors.containsAll(auditStrategy.getAuditorNames()),
                () -> new UnregisteredAlgorithmException("Sharding audit", Joiner.on(",").join(auditStrategy.getAuditorNames()), new SQLExceptionIdentifier(databaseName)));
    }
    
    private void checkShardingStrategy(final String databaseName, final ShardingStrategyConfiguration shardingStrategy, final Collection<String> shardingAlgorithms) {
        if (null == shardingStrategy || shardingStrategy instanceof NoneShardingStrategyConfiguration) {
            return;
        }
        if (shardingStrategy instanceof ComplexShardingStrategyConfiguration) {
            ShardingSpherePreconditions.checkNotEmpty(((ComplexShardingStrategyConfiguration) shardingStrategy).getShardingColumns(),
                    () -> new MissingRequiredShardingConfigurationException("Complex sharding columns", databaseName));
        }
        ShardingSpherePreconditions.checkNotNull(shardingStrategy.getShardingAlgorithmName(), () -> new MissingRequiredShardingConfigurationException("Sharding algorithm name", databaseName));
        ShardingSpherePreconditions.checkContains(shardingAlgorithms, shardingStrategy.getShardingAlgorithmName(),
                () -> new UnregisteredAlgorithmException("sharding", shardingStrategy.getShardingAlgorithmName(), new SQLExceptionIdentifier(databaseName)));
    }
    
    @Override
    public Collection<String> getRequiredDataSourceNames(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        ruleConfig.getTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        ruleConfig.getAutoTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        return result;
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Collection<String> actualDataNodes = InlineExpressionParserFactory.newInstance(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        return new HashSet<>(InlineExpressionParserFactory.newInstance(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate());
    }
    
    @Override
    public Collection<String> getTableNames(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> result = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        result.addAll(ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
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

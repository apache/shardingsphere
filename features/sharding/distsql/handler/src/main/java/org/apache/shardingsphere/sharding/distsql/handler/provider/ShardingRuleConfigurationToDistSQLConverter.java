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

package org.apache.shardingsphere.sharding.distsql.handler.provider;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.constant.DistSQLConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.AlgorithmDistSQLConverter;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.constant.ShardingDistSQLConstants;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Sharding rule configuration to DistSQL converter.
 */
public final class ShardingRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<ShardingRuleConfiguration> {
    
    @Override
    public String convert(final ShardingRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty() && ruleConfig.getAutoTables().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        appendShardingTableRules(ruleConfig, result);
        if (!ruleConfig.getBindingTableGroups().isEmpty()) {
            result.append(System.lineSeparator()).append(System.lineSeparator());
            appendShardingBindingTableRules(ruleConfig, result);
        }
        if (null != ruleConfig.getDefaultDatabaseShardingStrategy() || null != ruleConfig.getDefaultTableShardingStrategy()) {
            result.append(System.lineSeparator()).append(System.lineSeparator());
            appendDefaultShardingStrategy(ruleConfig, result);
        }
        return result.toString();
    }
    
    private void appendShardingTableRules(final ShardingRuleConfiguration ruleConfig, final StringBuilder stringBuilder) {
        String tableRules = getTableRules(ruleConfig);
        String autoTableRules = getAutoTableRules(ruleConfig);
        stringBuilder.append(ShardingDistSQLConstants.CREATE_SHARDING_TABLE).append(tableRules);
        if (!Strings.isNullOrEmpty(tableRules) && !Strings.isNullOrEmpty(autoTableRules)) {
            stringBuilder.append(DistSQLConstants.COMMA);
        }
        stringBuilder.append(autoTableRules).append(DistSQLConstants.SEMI);
    }
    
    private void appendShardingBindingTableRules(final ShardingRuleConfiguration ruleConfig, final StringBuilder stringBuilder) {
        stringBuilder.append(ShardingDistSQLConstants.SHARDING_BINDING_TABLE_RULES);
        Iterator<ShardingTableReferenceRuleConfiguration> iterator = ruleConfig.getBindingTableGroups().iterator();
        while (iterator.hasNext()) {
            ShardingTableReferenceRuleConfiguration referenceRuleConfig = iterator.next();
            stringBuilder.append(String.format(ShardingDistSQLConstants.BINDING_TABLES, referenceRuleConfig.getName(), referenceRuleConfig.getReference()));
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLConstants.COMMA);
            }
        }
        stringBuilder.append(DistSQLConstants.SEMI);
    }
    
    private void appendDefaultShardingStrategy(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (null != ruleConfig.getDefaultDatabaseShardingStrategy()) {
            appendStrategy(ruleConfig.getDefaultDatabaseShardingStrategy(), ShardingDistSQLConstants.DEFAULT_DATABASE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
            result.append(DistSQLConstants.SEMI);
        }
        if (null != ruleConfig.getDefaultTableShardingStrategy()) {
            if (null != ruleConfig.getDefaultDatabaseShardingStrategy()) {
                result.append(System.lineSeparator()).append(System.lineSeparator());
            }
            appendStrategy(ruleConfig.getDefaultTableShardingStrategy(), ShardingDistSQLConstants.DEFAULT_TABLE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
            result.append(DistSQLConstants.SEMI);
        }
    }
    
    private String getTableRules(final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        if (!ruleConfig.getTables().isEmpty()) {
            Iterator<ShardingTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
            while (iterator.hasNext()) {
                ShardingTableRuleConfiguration tableRuleConfig = iterator.next();
                result.append(String.format(ShardingDistSQLConstants.SHARDING_TABLE, tableRuleConfig.getLogicTable(), tableRuleConfig.getActualDataNodes(),
                        appendTableStrategy(tableRuleConfig, ruleConfig)));
                if (iterator.hasNext()) {
                    result.append(DistSQLConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    private String getAutoTableRules(final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        if (!ruleConfig.getAutoTables().isEmpty()) {
            Iterator<ShardingAutoTableRuleConfiguration> iterator = ruleConfig.getAutoTables().iterator();
            while (iterator.hasNext()) {
                ShardingAutoTableRuleConfiguration autoTableRuleConfig = iterator.next();
                result.append(String.format(ShardingDistSQLConstants.SHARDING_AUTO_TABLE, autoTableRuleConfig.getLogicTable(), autoTableRuleConfig.getActualDataSources(),
                        appendAutoTableStrategy(autoTableRuleConfig, ruleConfig)));
                if (iterator.hasNext()) {
                    result.append(DistSQLConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    private String appendTableStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        appendStrategy(tableRuleConfig.getDatabaseShardingStrategy(), ShardingDistSQLConstants.DATABASE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
        appendStrategy(tableRuleConfig.getTableShardingStrategy(), ShardingDistSQLConstants.TABLE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
        appendKeyGenerateStrategy(ruleConfig.getKeyGenerators(), tableRuleConfig.getKeyGenerateStrategy(), result);
        appendAuditStrategy(ruleConfig.getAuditors(), null != tableRuleConfig.getAuditStrategy() ? tableRuleConfig.getAuditStrategy() : ruleConfig.getDefaultAuditStrategy(), result);
        return result.toString();
    }
    
    private String appendAutoTableStrategy(final ShardingAutoTableRuleConfiguration autoTableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        StandardShardingStrategyConfiguration strategyConfig = (StandardShardingStrategyConfiguration) autoTableRuleConfig.getShardingStrategy();
        String shardingColumn = Strings.isNullOrEmpty(strategyConfig.getShardingColumn()) ? ruleConfig.getDefaultShardingColumn() : strategyConfig.getShardingColumn();
        result.append(String.format(ShardingDistSQLConstants.AUTO_TABLE_STRATEGY,
                shardingColumn, AlgorithmDistSQLConverter.getAlgorithmType(ruleConfig.getShardingAlgorithms().get(strategyConfig.getShardingAlgorithmName()))));
        appendKeyGenerateStrategy(ruleConfig.getKeyGenerators(), autoTableRuleConfig.getKeyGenerateStrategy(), result);
        appendAuditStrategy(ruleConfig.getAuditors(), null != autoTableRuleConfig.getAuditStrategy() ? autoTableRuleConfig.getAuditStrategy() : ruleConfig.getDefaultAuditStrategy(), result);
        return result.toString();
    }
    
    private void appendStrategy(final ShardingStrategyConfiguration strategyConfig, final String strategyType,
                                final StringBuilder stringBuilder, final Map<String, AlgorithmConfiguration> shardingAlgorithms) {
        if (null == strategyConfig) {
            return;
        }
        if (Objects.equals(strategyType, ShardingDistSQLConstants.DATABASE_STRATEGY) || Objects.equals(strategyType, ShardingDistSQLConstants.TABLE_STRATEGY)) {
            stringBuilder.append(DistSQLConstants.COMMA).append(System.lineSeparator());
        }
        String type = strategyConfig.getType().toLowerCase();
        String algorithmDefinition = AlgorithmDistSQLConverter.getAlgorithmType(shardingAlgorithms.get(strategyConfig.getShardingAlgorithmName()));
        switch (type) {
            case ShardingDistSQLConstants.STANDARD:
                StandardShardingStrategyConfiguration standardShardingStrategyConfig = (StandardShardingStrategyConfiguration) strategyConfig;
                stringBuilder.append(String.format(ShardingDistSQLConstants.SHARDING_STRATEGY_STANDARD, strategyType, type, standardShardingStrategyConfig.getShardingColumn(), algorithmDefinition));
                break;
            case ShardingDistSQLConstants.COMPLEX:
                ComplexShardingStrategyConfiguration complexShardingStrategyConfig = (ComplexShardingStrategyConfiguration) strategyConfig;
                stringBuilder.append(String.format(ShardingDistSQLConstants.SHARDING_STRATEGY_COMPLEX, strategyType, type, complexShardingStrategyConfig.getShardingColumns(), algorithmDefinition));
                break;
            case ShardingDistSQLConstants.HINT:
                stringBuilder.append(String.format(ShardingDistSQLConstants.SHARDING_STRATEGY_HINT, strategyType, type, algorithmDefinition));
                break;
            case ShardingDistSQLConstants.NONE:
                stringBuilder.append(String.format(ShardingDistSQLConstants.SHARDING_STRATEGY_NONE, strategyType, "none"));
                break;
            default:
                break;
        }
    }
    
    private void appendKeyGenerateStrategy(final Map<String, AlgorithmConfiguration> keyGenerators,
                                           final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig, final StringBuilder stringBuilder) {
        if (null == keyGenerateStrategyConfig) {
            return;
        }
        stringBuilder.append(DistSQLConstants.COMMA).append(System.lineSeparator());
        String algorithmDefinition = AlgorithmDistSQLConverter.getAlgorithmType(keyGenerators.get(keyGenerateStrategyConfig.getKeyGeneratorName()));
        stringBuilder.append(String.format(ShardingDistSQLConstants.KEY_GENERATOR_STRATEGY, keyGenerateStrategyConfig.getColumn(), algorithmDefinition));
    }
    
    private void appendAuditStrategy(final Map<String, AlgorithmConfiguration> auditors, final ShardingAuditStrategyConfiguration auditStrategy, final StringBuilder stringBuilder) {
        if (null != auditStrategy) {
            stringBuilder.append(DistSQLConstants.COMMA).append(System.lineSeparator());
            stringBuilder.append(String.format(ShardingDistSQLConstants.AUDIT_STRATEGY, getAlgorithmTypes(auditors, auditStrategy.getAuditorNames()), auditStrategy.isAllowHintDisable()));
        }
    }
    
    private String getAlgorithmTypes(final Map<String, AlgorithmConfiguration> auditors, final Collection<String> auditorNames) {
        StringBuilder result = new StringBuilder();
        if (!auditorNames.isEmpty()) {
            Iterator<String> iterator = auditorNames.iterator();
            while (iterator.hasNext()) {
                result.append(AlgorithmDistSQLConverter.getAlgorithmType(auditors.get(iterator.next())));
                if (iterator.hasNext()) {
                    result.append(DistSQLConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getType() {
        return ShardingRuleConfiguration.class;
    }
}

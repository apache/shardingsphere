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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        result.append(convertShardingTableRules(ruleConfig));
        if (!ruleConfig.getBindingTableGroups().isEmpty()) {
            result.append(System.lineSeparator()).append(System.lineSeparator());
            result.append(convertShardingBindingTableRules(ruleConfig));
        }
        if (null != ruleConfig.getDefaultDatabaseShardingStrategy() || null != ruleConfig.getDefaultTableShardingStrategy()) {
            result.append(System.lineSeparator()).append(System.lineSeparator());
            result.append(convertDefaultShardingStrategy(ruleConfig));
        }
        return result.toString();
    }
    
    private String convertShardingTableRules(final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        result.append(ShardingConvertDistSQLConstants.CREATE_SHARDING_TABLE_RULE).append(convertTableRules(ruleConfig));
        if (!ruleConfig.getTables().isEmpty() && !ruleConfig.getAutoTables().isEmpty()) {
            result.append(DistSQLConstants.COMMA);
        }
        result.append(convertAutoTableRules(ruleConfig)).append(DistSQLConstants.SEMI);
        return result.toString();
    }
    
    private String convertTableRules(final ShardingRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty()) {
            return "";
        }
        return ruleConfig.getTables().stream()
                .map(each -> String.format(ShardingConvertDistSQLConstants.SHARDING_TABLE_RULE, each.getLogicTable(), each.getActualDataNodes(), convertTableStrategy(each, ruleConfig)))
                .collect(Collectors.joining(DistSQLConstants.COMMA));
    }
    
    private String convertTableStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        return convertShardingStrategy(tableRuleConfig.getDatabaseShardingStrategy(), ShardingConvertDistSQLConstants.DATABASE_STRATEGY, ruleConfig.getShardingAlgorithms())
                + convertShardingStrategy(tableRuleConfig.getTableShardingStrategy(), ShardingConvertDistSQLConstants.TABLE_STRATEGY, ruleConfig.getShardingAlgorithms())
                + convertKeyGenerateStrategy(ruleConfig.getKeyGenerators(), tableRuleConfig.getKeyGenerateStrategy())
                + convertAuditStrategy(ruleConfig.getAuditors(), null == tableRuleConfig.getAuditStrategy() ? ruleConfig.getDefaultAuditStrategy() : tableRuleConfig.getAuditStrategy());
    }
    
    private String convertAutoTableRules(final ShardingRuleConfiguration ruleConfig) {
        if (ruleConfig.getAutoTables().isEmpty()) {
            return "";
        }
        return ruleConfig.getAutoTables().stream()
                .map(each -> String.format(ShardingConvertDistSQLConstants.SHARDING_AUTO_TABLE_RULE, each.getLogicTable(), each.getActualDataSources(), convertAutoTableStrategy(each, ruleConfig)))
                .collect(Collectors.joining(DistSQLConstants.COMMA));
    }
    
    private String convertAutoTableStrategy(final ShardingAutoTableRuleConfiguration autoTableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        StandardShardingStrategyConfiguration strategyConfig = (StandardShardingStrategyConfiguration) autoTableRuleConfig.getShardingStrategy();
        String shardingColumn = Strings.isNullOrEmpty(strategyConfig.getShardingColumn()) ? ruleConfig.getDefaultShardingColumn() : strategyConfig.getShardingColumn();
        result.append(String.format(ShardingConvertDistSQLConstants.AUTO_TABLE_STRATEGY,
                shardingColumn, AlgorithmDistSQLConverter.getAlgorithmType(ruleConfig.getShardingAlgorithms().get(strategyConfig.getShardingAlgorithmName()))));
        result.append(convertKeyGenerateStrategy(ruleConfig.getKeyGenerators(), autoTableRuleConfig.getKeyGenerateStrategy()));
        result.append(convertAuditStrategy(ruleConfig.getAuditors(), null == autoTableRuleConfig.getAuditStrategy() ? ruleConfig.getDefaultAuditStrategy() : autoTableRuleConfig.getAuditStrategy()));
        return result.toString();
    }
    
    private String convertKeyGenerateStrategy(final Map<String, AlgorithmConfiguration> keyGenerators, final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        if (null == keyGenerateStrategyConfig) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(DistSQLConstants.COMMA).append(System.lineSeparator());
        String algorithmDefinition = AlgorithmDistSQLConverter.getAlgorithmType(keyGenerators.get(keyGenerateStrategyConfig.getKeyGeneratorName()));
        result.append(String.format(ShardingConvertDistSQLConstants.KEY_GENERATOR_STRATEGY, keyGenerateStrategyConfig.getColumn(), algorithmDefinition));
        return result.toString();
    }
    
    private String convertShardingBindingTableRules(final ShardingRuleConfiguration ruleConfig) {
        return ShardingConvertDistSQLConstants.SHARDING_BINDING_TABLE_RULES + convertShardingBindingTableRules(ruleConfig.getBindingTableGroups()) + DistSQLConstants.SEMI;
    }
    
    private String convertShardingBindingTableRules(final Collection<ShardingTableReferenceRuleConfiguration> bindingTableGroups) {
        return bindingTableGroups.stream()
                .map(each -> String.format(ShardingConvertDistSQLConstants.BINDING_TABLES, each.getName(), each.getReference())).collect(Collectors.joining(DistSQLConstants.COMMA));
    }
    
    private String convertDefaultShardingStrategy(final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        if (null != ruleConfig.getDefaultDatabaseShardingStrategy()) {
            result.append(convertShardingStrategy(ruleConfig.getDefaultDatabaseShardingStrategy(), ShardingConvertDistSQLConstants.DEFAULT_DATABASE_STRATEGY, ruleConfig.getShardingAlgorithms()));
            result.append(DistSQLConstants.SEMI);
        }
        if (null != ruleConfig.getDefaultTableShardingStrategy()) {
            if (null != ruleConfig.getDefaultDatabaseShardingStrategy()) {
                result.append(System.lineSeparator()).append(System.lineSeparator());
            }
            result.append(convertShardingStrategy(ruleConfig.getDefaultTableShardingStrategy(), ShardingConvertDistSQLConstants.DEFAULT_TABLE_STRATEGY, ruleConfig.getShardingAlgorithms()));
            result.append(DistSQLConstants.SEMI);
        }
        return result.toString();
    }
    
    private String convertShardingStrategy(final ShardingStrategyConfiguration strategyConfig, final String strategyType, final Map<String, AlgorithmConfiguration> shardingAlgorithms) {
        if (null == strategyConfig) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        if (Objects.equals(strategyType, ShardingConvertDistSQLConstants.DATABASE_STRATEGY) || Objects.equals(strategyType, ShardingConvertDistSQLConstants.TABLE_STRATEGY)) {
            result.append(DistSQLConstants.COMMA).append(System.lineSeparator());
        }
        String type = strategyConfig.getType().toLowerCase();
        String algorithmDefinition = AlgorithmDistSQLConverter.getAlgorithmType(shardingAlgorithms.get(strategyConfig.getShardingAlgorithmName()));
        switch (type) {
            case ShardingConvertDistSQLConstants.STANDARD:
                StandardShardingStrategyConfiguration standardConfig = (StandardShardingStrategyConfiguration) strategyConfig;
                result.append(String.format(ShardingConvertDistSQLConstants.SHARDING_STRATEGY_STANDARD, strategyType, type, standardConfig.getShardingColumn(), algorithmDefinition));
                break;
            case ShardingConvertDistSQLConstants.COMPLEX:
                ComplexShardingStrategyConfiguration complexConfig = (ComplexShardingStrategyConfiguration) strategyConfig;
                result.append(String.format(ShardingConvertDistSQLConstants.SHARDING_STRATEGY_COMPLEX, strategyType, type, complexConfig.getShardingColumns(), algorithmDefinition));
                break;
            case ShardingConvertDistSQLConstants.HINT:
                result.append(String.format(ShardingConvertDistSQLConstants.SHARDING_STRATEGY_HINT, strategyType, type, algorithmDefinition));
                break;
            case ShardingConvertDistSQLConstants.NONE:
                result.append(String.format(ShardingConvertDistSQLConstants.SHARDING_STRATEGY_NONE, strategyType, "none"));
                break;
            default:
                break;
        }
        return result.toString();
    }
    
    private String convertAuditStrategy(final Map<String, AlgorithmConfiguration> auditors, final ShardingAuditStrategyConfiguration auditStrategy) {
        return null == auditStrategy
                ? ""
                : DistSQLConstants.COMMA + System.lineSeparator()
                        + String.format(ShardingConvertDistSQLConstants.AUDIT_STRATEGY, convertAlgorithmTypes(auditStrategy.getAuditorNames(), auditors), auditStrategy.isAllowHintDisable());
    }
    
    private String convertAlgorithmTypes(final Collection<String> auditorNames, final Map<String, AlgorithmConfiguration> auditors) {
        return auditorNames.stream().map(each -> AlgorithmDistSQLConverter.getAlgorithmType(auditors.get(each))).collect(Collectors.joining(DistSQLConstants.COMMA));
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getType() {
        return ShardingRuleConfiguration.class;
    }
}

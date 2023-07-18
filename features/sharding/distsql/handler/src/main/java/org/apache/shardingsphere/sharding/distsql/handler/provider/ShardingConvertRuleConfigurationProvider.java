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
import org.apache.shardingsphere.distsql.handler.ral.constant.DistSQLScriptConstants;
import org.apache.shardingsphere.distsql.handler.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Sharding convert rule configuration provider.
 */
public final class ShardingConvertRuleConfigurationProvider implements ConvertRuleConfigurationProvider {
    
    @Override
    public String convert(final RuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) ruleConfig;
        appendShardingTableRules(shardingRuleConfig, result);
        appendShardingBindingTableRules(shardingRuleConfig, result);
        appendDefaultShardingStrategy(shardingRuleConfig, result);
        return result.toString();
    }
    
    private void appendShardingTableRules(final ShardingRuleConfiguration ruleConfig, final StringBuilder stringBuilder) {
        if (ruleConfig.getTables().isEmpty() && ruleConfig.getAutoTables().isEmpty()) {
            return;
        }
        String tableRules = getTableRules(ruleConfig);
        String autoTableRules = getAutoTableRules(ruleConfig);
        stringBuilder.append(DistSQLScriptConstants.CREATE_SHARDING_TABLE).append(tableRules);
        if (!Strings.isNullOrEmpty(tableRules) && !Strings.isNullOrEmpty(autoTableRules)) {
            stringBuilder.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
        }
        stringBuilder.append(autoTableRules).append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendShardingBindingTableRules(final ShardingRuleConfiguration ruleConfig, final StringBuilder stringBuilder) {
        if (ruleConfig.getBindingTableGroups().isEmpty()) {
            return;
        }
        stringBuilder.append(DistSQLScriptConstants.SHARDING_BINDING_TABLE_RULES);
        Iterator<ShardingTableReferenceRuleConfiguration> iterator = ruleConfig.getBindingTableGroups().iterator();
        while (iterator.hasNext()) {
            ShardingTableReferenceRuleConfiguration referenceRuleConfig = iterator.next();
            stringBuilder.append(String.format(DistSQLScriptConstants.BINDING_TABLES, referenceRuleConfig.getName(), referenceRuleConfig.getReference()));
            if (iterator.hasNext()) {
                stringBuilder.append(DistSQLScriptConstants.COMMA);
            }
        }
        stringBuilder.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
    }
    
    private void appendDefaultShardingStrategy(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (null == ruleConfig.getDefaultDatabaseShardingStrategy() && null == ruleConfig.getDefaultTableShardingStrategy()) {
            return;
        }
        if (null != ruleConfig.getDefaultDatabaseShardingStrategy()) {
            appendStrategy(ruleConfig.getDefaultDatabaseShardingStrategy(), DistSQLScriptConstants.DEFAULT_DATABASE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
            result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        }
        if (null != ruleConfig.getDefaultTableShardingStrategy()) {
            appendStrategy(ruleConfig.getDefaultTableShardingStrategy(), DistSQLScriptConstants.DEFAULT_TABLE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
            result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        }
    }
    
    private String getTableRules(final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        if (!ruleConfig.getTables().isEmpty()) {
            Iterator<ShardingTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
            while (iterator.hasNext()) {
                ShardingTableRuleConfiguration tableRuleConfig = iterator.next();
                result.append(String.format(DistSQLScriptConstants.SHARDING_TABLE, tableRuleConfig.getLogicTable(), tableRuleConfig.getActualDataNodes(),
                        appendTableStrategy(tableRuleConfig, ruleConfig)));
                if (iterator.hasNext()) {
                    result.append(DistSQLScriptConstants.COMMA);
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
                result.append(String.format(DistSQLScriptConstants.SHARDING_AUTO_TABLE, autoTableRuleConfig.getLogicTable(), autoTableRuleConfig.getActualDataSources(),
                        appendAutoTableStrategy(autoTableRuleConfig, ruleConfig)));
                if (iterator.hasNext()) {
                    result.append(DistSQLScriptConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    private String appendTableStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        appendStrategy(tableRuleConfig.getDatabaseShardingStrategy(), DistSQLScriptConstants.DATABASE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
        appendStrategy(tableRuleConfig.getTableShardingStrategy(), DistSQLScriptConstants.TABLE_STRATEGY, result, ruleConfig.getShardingAlgorithms());
        appendKeyGenerateStrategy(ruleConfig.getKeyGenerators(), tableRuleConfig.getKeyGenerateStrategy(), result);
        appendAuditStrategy(ruleConfig.getAuditors(), null != tableRuleConfig.getAuditStrategy() ? tableRuleConfig.getAuditStrategy() : ruleConfig.getDefaultAuditStrategy(), result);
        return result.toString();
    }
    
    private String appendAutoTableStrategy(final ShardingAutoTableRuleConfiguration autoTableRuleConfig, final ShardingRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        StandardShardingStrategyConfiguration strategyConfig = (StandardShardingStrategyConfiguration) autoTableRuleConfig.getShardingStrategy();
        String shardingColumn = Strings.isNullOrEmpty(strategyConfig.getShardingColumn()) ? ruleConfig.getDefaultShardingColumn() : strategyConfig.getShardingColumn();
        result.append(String.format(DistSQLScriptConstants.AUTO_TABLE_STRATEGY, shardingColumn, getAlgorithmType(ruleConfig.getShardingAlgorithms().get(strategyConfig.getShardingAlgorithmName()))));
        appendKeyGenerateStrategy(ruleConfig.getKeyGenerators(), autoTableRuleConfig.getKeyGenerateStrategy(), result);
        appendAuditStrategy(ruleConfig.getAuditors(), null != autoTableRuleConfig.getAuditStrategy() ? autoTableRuleConfig.getAuditStrategy() : ruleConfig.getDefaultAuditStrategy(), result);
        return result.toString();
    }
    
    private void appendStrategy(final ShardingStrategyConfiguration strategyConfig, final String strategyType,
                                final StringBuilder stringBuilder, final Map<String, AlgorithmConfiguration> shardingAlgorithms) {
        if (null == strategyConfig) {
            return;
        }
        if (Objects.equals(strategyType, DistSQLScriptConstants.DATABASE_STRATEGY) || Objects.equals(strategyType, DistSQLScriptConstants.TABLE_STRATEGY)) {
            stringBuilder.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
        }
        String type = strategyConfig.getType().toLowerCase();
        String algorithmDefinition = getAlgorithmType(shardingAlgorithms.get(strategyConfig.getShardingAlgorithmName()));
        switch (type) {
            case DistSQLScriptConstants.STANDARD:
                StandardShardingStrategyConfiguration standardShardingStrategyConfig = (StandardShardingStrategyConfiguration) strategyConfig;
                stringBuilder.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_STANDARD, strategyType, type, standardShardingStrategyConfig.getShardingColumn(), algorithmDefinition));
                break;
            case DistSQLScriptConstants.COMPLEX:
                ComplexShardingStrategyConfiguration complexShardingStrategyConfig = (ComplexShardingStrategyConfiguration) strategyConfig;
                stringBuilder.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_COMPLEX, strategyType, type, complexShardingStrategyConfig.getShardingColumns(), algorithmDefinition));
                break;
            case DistSQLScriptConstants.HINT:
                stringBuilder.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_HINT, strategyType, type, algorithmDefinition));
                break;
            case DistSQLScriptConstants.NONE:
                stringBuilder.append(String.format(DistSQLScriptConstants.SHARDING_STRATEGY_NONE, strategyType, "none"));
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
        stringBuilder.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
        String algorithmDefinition = getAlgorithmType(keyGenerators.get(keyGenerateStrategyConfig.getKeyGeneratorName()));
        stringBuilder.append(String.format(DistSQLScriptConstants.KEY_GENERATOR_STRATEGY, keyGenerateStrategyConfig.getColumn(), algorithmDefinition));
    }
    
    private void appendAuditStrategy(final Map<String, AlgorithmConfiguration> auditors, final ShardingAuditStrategyConfiguration auditStrategy, final StringBuilder stringBuilder) {
        if (null != auditStrategy) {
            stringBuilder.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            stringBuilder.append(String.format(DistSQLScriptConstants.AUDIT_STRATEGY, getAlgorithmTypes(auditors, auditStrategy.getAuditorNames()), auditStrategy.isAllowHintDisable()));
        }
    }
    
    private String getAlgorithmType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        if (null == algorithmConfig) {
            return result.toString();
        }
        String type = algorithmConfig.getType().toLowerCase();
        if (algorithmConfig.getProps().isEmpty()) {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE_WITHOUT_PROPS, type));
        } else {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE, type, getAlgorithmProperties(algorithmConfig.getProps())));
        }
        return result.toString();
    }
    
    private String getAlgorithmTypes(final Map<String, AlgorithmConfiguration> auditors, final Collection<String> auditorNames) {
        StringBuilder result = new StringBuilder();
        if (!auditorNames.isEmpty()) {
            Iterator<String> iterator = auditorNames.iterator();
            while (iterator.hasNext()) {
                result.append(getAlgorithmType(auditors.get(iterator.next())));
                if (iterator.hasNext()) {
                    result.append(DistSQLScriptConstants.COMMA);
                }
            }
        }
        return result.toString();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String getAlgorithmProperties(final Properties props) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = new TreeMap(props).keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = props.get(key);
            if (null == value) {
                continue;
            }
            result.append(String.format(DistSQLScriptConstants.PROPERTY, key, value));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(' ');
            }
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return ShardingRuleConfiguration.class.getName();
    }
}

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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Show sharding table rules executor.
 */
@Setter
public final class ShowShardingTableRuleExecutor implements DistSQLQueryExecutor<ShowShardingTableRulesStatement>, DistSQLExecutorRuleAware<ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowShardingTableRulesStatement sqlStatement) {
        return Arrays.asList("table", "actual_data_nodes", "actual_data_sources", "database_strategy_type", "database_sharding_column", "database_sharding_algorithm_type",
                "database_sharding_algorithm_props", "table_strategy_type", "table_sharding_column", "table_sharding_algorithm_type", "table_sharding_algorithm_props",
                "key_generate_column", "key_generator_type", "key_generator_props", "auditor_types", "allow_hint_disable");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShardingTableRulesStatement sqlStatement, final ContextManager contextManager) {
        String tableName = sqlStatement.getTableName();
        Collection<ShardingTableRuleConfiguration> tables;
        Collection<ShardingAutoTableRuleConfiguration> autoTables;
        if (null == tableName) {
            tables = rule.getConfiguration().getTables();
            autoTables = rule.getConfiguration().getAutoTables();
        } else {
            tables = rule.getConfiguration().getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList());
            autoTables = rule.getConfiguration().getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList());
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (ShardingTableRuleConfiguration each : tables) {
            result.add(buildTableRowData(rule.getConfiguration(), each));
        }
        for (ShardingAutoTableRuleConfiguration each : autoTables) {
            result.add(buildAutoTableRowData(rule.getConfiguration(), each));
        }
        return result;
    }
    
    private LocalDataQueryResultRow buildTableRowData(final ShardingRuleConfiguration ruleConfig, final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Optional<ShardingStrategyConfiguration> databaseShardingStrategyConfig = getDatabaseShardingStrategy(ruleConfig, shardingTableRuleConfig);
        Optional<ShardingStrategyConfiguration> tableShardingStrategyConfig = getTableShardingStrategy(ruleConfig, shardingTableRuleConfig.getTableShardingStrategy());
        return new LocalDataQueryResultRow(shardingTableRuleConfig.getLogicTable(), shardingTableRuleConfig.getActualDataNodes(), "",
                databaseShardingStrategyConfig.map(this::getStrategyType), databaseShardingStrategyConfig.map(this::getShardingColumn),
                databaseShardingStrategyConfig.map(optional -> getAlgorithmType(ruleConfig, optional)),
                databaseShardingStrategyConfig.map(optional -> getAlgorithmProperties(ruleConfig, optional)),
                tableShardingStrategyConfig.map(this::getStrategyType).orElse(""), tableShardingStrategyConfig.map(this::getShardingColumn),
                tableShardingStrategyConfig.map(optional -> getAlgorithmType(ruleConfig, optional)),
                tableShardingStrategyConfig.map(optional -> getAlgorithmProperties(ruleConfig, optional)),
                getKeyGenerateColumn(ruleConfig, shardingTableRuleConfig.getKeyGenerateStrategy()), getKeyGeneratorType(ruleConfig, shardingTableRuleConfig.getKeyGenerateStrategy()),
                getKeyGeneratorProps(ruleConfig, shardingTableRuleConfig.getKeyGenerateStrategy()), getAuditorTypes(ruleConfig, shardingTableRuleConfig.getAuditStrategy()),
                getAllowHintDisable(ruleConfig, shardingTableRuleConfig.getAuditStrategy()));
    }
    
    private LocalDataQueryResultRow buildAutoTableRowData(final ShardingRuleConfiguration ruleConfig, final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        Optional<ShardingStrategyConfiguration> tableShardingStrategyConfig = getTableShardingStrategy(ruleConfig, shardingAutoTableRuleConfig.getShardingStrategy());
        return new LocalDataQueryResultRow(shardingAutoTableRuleConfig.getLogicTable(), "", shardingAutoTableRuleConfig.getActualDataSources(), "", "", "", "",
                tableShardingStrategyConfig.map(this::getStrategyType), tableShardingStrategyConfig.map(this::getShardingColumn),
                tableShardingStrategyConfig.map(optional -> getAlgorithmType(ruleConfig, optional)),
                tableShardingStrategyConfig.map(optional -> getAlgorithmProperties(ruleConfig, optional)),
                getKeyGenerateColumn(ruleConfig, shardingAutoTableRuleConfig.getKeyGenerateStrategy()), getKeyGeneratorType(ruleConfig, shardingAutoTableRuleConfig.getKeyGenerateStrategy()),
                getKeyGeneratorProps(ruleConfig, shardingAutoTableRuleConfig.getKeyGenerateStrategy()), getAuditorTypes(ruleConfig, shardingAutoTableRuleConfig.getAuditStrategy()),
                getAllowHintDisable(ruleConfig, shardingAutoTableRuleConfig.getAuditStrategy()));
    }
    
    private String getShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig) {
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            return ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn();
        }
        if (shardingStrategyConfig instanceof ComplexShardingStrategyConfiguration) {
            return ((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns();
        }
        return "";
    }
    
    private String getAlgorithmType(final ShardingRuleConfiguration ruleConfig, final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig instanceof NoneShardingStrategyConfiguration ? "" : getAlgorithmConfiguration(ruleConfig, shardingStrategyConfig.getShardingAlgorithmName()).getType();
    }
    
    private Properties getAlgorithmProperties(final ShardingRuleConfiguration ruleConfig, final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig instanceof NoneShardingStrategyConfiguration
                ? new Properties()
                : getAlgorithmConfiguration(ruleConfig, shardingStrategyConfig.getShardingAlgorithmName()).getProps();
    }
    
    private Optional<ShardingStrategyConfiguration> getDatabaseShardingStrategy(final ShardingRuleConfiguration ruleConfig, final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        return null == shardingTableRuleConfig.getDatabaseShardingStrategy()
                ? Optional.ofNullable(ruleConfig.getDefaultDatabaseShardingStrategy())
                : Optional.of(shardingTableRuleConfig.getDatabaseShardingStrategy());
    }
    
    private AlgorithmConfiguration getAlgorithmConfiguration(final ShardingRuleConfiguration ruleConfig, final String algorithmName) {
        return ruleConfig.getShardingAlgorithms().get(algorithmName);
    }
    
    private String getStrategyType(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig.getType();
    }
    
    private Optional<ShardingStrategyConfiguration> getTableShardingStrategy(final ShardingRuleConfiguration ruleConfig, final ShardingStrategyConfiguration shardingStrategyConfig) {
        return null == shardingStrategyConfig ? Optional.ofNullable(ruleConfig.getDefaultTableShardingStrategy()) : Optional.of(shardingStrategyConfig);
    }
    
    private String getKeyGenerateColumn(final ShardingRuleConfiguration ruleConfig, final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return getKeyGenerateStrategyConfiguration(ruleConfig, keyGenerateStrategyConfig).isPresent()
                ? getKeyGenerateStrategyConfiguration(ruleConfig, keyGenerateStrategyConfig).get().getColumn()
                : "";
    }
    
    private String getKeyGeneratorType(final ShardingRuleConfiguration ruleConfig, final KeyGenerateStrategyConfiguration originalKeyGenerateStrategyConfig) {
        Optional<KeyGenerateStrategyConfiguration> keyGenerateStrategyConfig = getKeyGenerateStrategyConfiguration(ruleConfig, originalKeyGenerateStrategyConfig);
        return keyGenerateStrategyConfig.isPresent() ? ruleConfig.getKeyGenerators().get(keyGenerateStrategyConfig.get().getKeyGeneratorName()).getType() : "";
    }
    
    private Properties getKeyGeneratorProps(final ShardingRuleConfiguration ruleConfig, final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return getKeyGenerateStrategyConfiguration(ruleConfig, keyGenerateStrategyConfig)
                .map(optional -> ruleConfig.getKeyGenerators().get(optional.getKeyGeneratorName()).getProps()).orElse(new Properties());
    }
    
    private Optional<KeyGenerateStrategyConfiguration> getKeyGenerateStrategyConfiguration(final ShardingRuleConfiguration ruleConfig,
                                                                                           final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return null == keyGenerateStrategyConfig ? Optional.ofNullable(ruleConfig.getDefaultKeyGenerateStrategy()) : Optional.of(keyGenerateStrategyConfig);
    }
    
    private String getAuditorTypes(final ShardingRuleConfiguration ruleConfig, final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        Optional<ShardingAuditStrategyConfiguration> auditStrategyConfig = getShardingAuditStrategyConfiguration(ruleConfig, shardingAuditStrategyConfig);
        Collection<String> auditorTypes = new LinkedList<>();
        if (auditStrategyConfig.isPresent()) {
            for (String each : auditStrategyConfig.get().getAuditorNames()) {
                auditorTypes.add(ruleConfig.getAuditors().get(each).getType());
            }
        }
        return auditorTypes.isEmpty() ? "" : String.join(",", auditorTypes);
    }
    
    private String getAllowHintDisable(final ShardingRuleConfiguration ruleConfig, final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        return getShardingAuditStrategyConfiguration(ruleConfig, shardingAuditStrategyConfig).isPresent()
                ? Boolean.toString(getShardingAuditStrategyConfiguration(ruleConfig, shardingAuditStrategyConfig).get().isAllowHintDisable())
                : "";
    }
    
    private Optional<ShardingAuditStrategyConfiguration> getShardingAuditStrategyConfiguration(final ShardingRuleConfiguration ruleConfig,
                                                                                               final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        return null == shardingAuditStrategyConfig ? Optional.ofNullable(ruleConfig.getDefaultAuditStrategy()) : Optional.of(shardingAuditStrategyConfig);
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowShardingTableRulesStatement> getType() {
        return ShowShardingTableRulesStatement.class;
    }
}

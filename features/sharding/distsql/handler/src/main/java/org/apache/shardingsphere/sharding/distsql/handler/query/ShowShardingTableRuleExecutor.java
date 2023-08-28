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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show sharding table rules executor.
 */
public final class ShowShardingTableRuleExecutor implements RQLExecutor<ShowShardingTableRulesStatement> {
    
    private ShardingRuleConfiguration shardingRuleConfig;
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShardingTableRulesStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.get().getConfiguration();
        String tableName = sqlStatement.getTableName();
        Iterator<ShardingTableRuleConfiguration> tables;
        Iterator<ShardingAutoTableRuleConfiguration> autoTables;
        if (null == tableName) {
            tables = config.getTables().iterator();
            autoTables = config.getAutoTables().iterator();
        } else {
            tables = config.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()).iterator();
            autoTables = config.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()).iterator();
        }
        shardingRuleConfig = config;
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (tables.hasNext()) {
            result.add(buildTableRowData(tables.next()));
        }
        while (autoTables.hasNext()) {
            result.add(buildAutoTableRowData(autoTables.next()));
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "actual_data_nodes", "actual_data_sources", "database_strategy_type", "database_sharding_column", "database_sharding_algorithm_type",
                "database_sharding_algorithm_props", "table_strategy_type", "table_sharding_column", "table_sharding_algorithm_type", "table_sharding_algorithm_props",
                "key_generate_column", "key_generator_type", "key_generator_props", "auditor_types", "allow_hint_disable");
    }
    
    private LocalDataQueryResultRow buildTableRowData(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Optional<ShardingStrategyConfiguration> databaseShardingStrategyConfig = getDatabaseShardingStrategy(shardingTableRuleConfig);
        Optional<ShardingStrategyConfiguration> tableShardingStrategyConfig = getTableShardingStrategy(shardingTableRuleConfig.getTableShardingStrategy());
        return new LocalDataQueryResultRow(shardingTableRuleConfig.getLogicTable(), shardingTableRuleConfig.getActualDataNodes(), "",
                databaseShardingStrategyConfig.map(this::getStrategyType).orElse(""), databaseShardingStrategyConfig.map(this::getShardingColumn).orElse(""),
                databaseShardingStrategyConfig.map(this::getAlgorithmType).orElse(""), databaseShardingStrategyConfig.map(this::getAlgorithmProperties).orElse(""),
                tableShardingStrategyConfig.map(this::getStrategyType).orElse(""), tableShardingStrategyConfig.map(this::getShardingColumn).orElse(""),
                tableShardingStrategyConfig.map(this::getAlgorithmType).orElse(""), tableShardingStrategyConfig.map(this::getAlgorithmProperties).orElse(""),
                getKeyGenerateColumn(shardingTableRuleConfig.getKeyGenerateStrategy()), getKeyGeneratorType(shardingTableRuleConfig.getKeyGenerateStrategy()),
                getKeyGeneratorProps(shardingTableRuleConfig.getKeyGenerateStrategy()), getAuditorTypes(shardingTableRuleConfig.getAuditStrategy()),
                getAllowHintDisable(shardingTableRuleConfig.getAuditStrategy()));
    }
    
    private LocalDataQueryResultRow buildAutoTableRowData(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        Optional<ShardingStrategyConfiguration> tableShardingStrategyConfig = getTableShardingStrategy(shardingAutoTableRuleConfig.getShardingStrategy());
        return new LocalDataQueryResultRow(shardingAutoTableRuleConfig.getLogicTable(), "", shardingAutoTableRuleConfig.getActualDataSources(), "", "", "", "",
                tableShardingStrategyConfig.map(this::getStrategyType).orElse(""), tableShardingStrategyConfig.map(this::getShardingColumn).orElse(""),
                tableShardingStrategyConfig.map(this::getAlgorithmType).orElse(""), tableShardingStrategyConfig.map(this::getAlgorithmProperties).orElse(""),
                getKeyGenerateColumn(shardingAutoTableRuleConfig.getKeyGenerateStrategy()), getKeyGeneratorType(shardingAutoTableRuleConfig.getKeyGenerateStrategy()),
                getKeyGeneratorProps(shardingAutoTableRuleConfig.getKeyGenerateStrategy()), getAuditorTypes(shardingAutoTableRuleConfig.getAuditStrategy()),
                getAllowHintDisable(shardingAutoTableRuleConfig.getAuditStrategy()));
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
    
    private String getAlgorithmType(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig instanceof NoneShardingStrategyConfiguration ? "" : getAlgorithmConfiguration(shardingStrategyConfig.getShardingAlgorithmName()).getType();
    }
    
    private String getAlgorithmProperties(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig instanceof NoneShardingStrategyConfiguration
                ? ""
                : PropertiesConverter.convert(getAlgorithmConfiguration(shardingStrategyConfig.getShardingAlgorithmName()).getProps());
    }
    
    private Optional<ShardingStrategyConfiguration> getDatabaseShardingStrategy(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        return null == shardingTableRuleConfig.getDatabaseShardingStrategy()
                ? Optional.ofNullable(shardingRuleConfig.getDefaultDatabaseShardingStrategy())
                : Optional.of(shardingTableRuleConfig.getDatabaseShardingStrategy());
    }
    
    private AlgorithmConfiguration getAlgorithmConfiguration(final String algorithmName) {
        return shardingRuleConfig.getShardingAlgorithms().get(algorithmName);
    }
    
    private String getStrategyType(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig.getType();
    }
    
    private Optional<ShardingStrategyConfiguration> getTableShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return null == shardingStrategyConfig ? Optional.ofNullable(shardingRuleConfig.getDefaultTableShardingStrategy()) : Optional.of(shardingStrategyConfig);
    }
    
    private String getKeyGenerateColumn(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfig).isPresent() ? getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfig).get().getColumn() : "";
    }
    
    private String getKeyGeneratorType(final KeyGenerateStrategyConfiguration originalKeyGenerateStrategyConfig) {
        Optional<KeyGenerateStrategyConfiguration> keyGenerateStrategyConfig = getKeyGenerateStrategyConfiguration(originalKeyGenerateStrategyConfig);
        return keyGenerateStrategyConfig.isPresent() ? shardingRuleConfig.getKeyGenerators().get(keyGenerateStrategyConfig.get().getKeyGeneratorName()).getType() : "";
    }
    
    private String getKeyGeneratorProps(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfig)
                .map(optional -> PropertiesConverter.convert(shardingRuleConfig.getKeyGenerators().get(optional.getKeyGeneratorName()).getProps())).orElse("");
    }
    
    private Optional<KeyGenerateStrategyConfiguration> getKeyGenerateStrategyConfiguration(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return null == keyGenerateStrategyConfig ? Optional.ofNullable(shardingRuleConfig.getDefaultKeyGenerateStrategy()) : Optional.of(keyGenerateStrategyConfig);
    }
    
    private String getAuditorTypes(final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        Optional<ShardingAuditStrategyConfiguration> auditStrategyConfig = getShardingAuditStrategyConfiguration(shardingAuditStrategyConfig);
        Collection<String> auditorTypes = new LinkedList<>();
        if (auditStrategyConfig.isPresent()) {
            for (String each : auditStrategyConfig.get().getAuditorNames()) {
                auditorTypes.add(shardingRuleConfig.getAuditors().get(each).getType());
            }
        }
        return auditorTypes.isEmpty() ? "" : String.join(",", auditorTypes);
    }
    
    private String getAllowHintDisable(final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        return getShardingAuditStrategyConfiguration(shardingAuditStrategyConfig).isPresent()
                ? Boolean.toString(getShardingAuditStrategyConfiguration(shardingAuditStrategyConfig).get().isAllowHintDisable())
                : "";
    }
    
    private Optional<ShardingAuditStrategyConfiguration> getShardingAuditStrategyConfiguration(final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        return null == shardingAuditStrategyConfig ? Optional.ofNullable(shardingRuleConfig.getDefaultAuditStrategy()) : Optional.of(shardingAuditStrategyConfig);
    }
    
    @Override
    public Class<ShowShardingTableRulesStatement> getType() {
        return ShowShardingTableRulesStatement.class;
    }
}

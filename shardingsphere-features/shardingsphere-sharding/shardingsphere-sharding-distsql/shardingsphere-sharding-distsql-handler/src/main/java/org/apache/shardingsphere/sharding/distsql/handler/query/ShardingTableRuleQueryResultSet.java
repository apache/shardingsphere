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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Query result set for show sharding table rules.
 */
public final class ShardingTableRuleQueryResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<ShardingTableRuleConfiguration> tables = Collections.emptyIterator();
    
    private Iterator<ShardingAutoTableRuleConfiguration> autoTables = Collections.emptyIterator();
    
    private ShardingRuleConfiguration shardingRuleConfig;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!rule.isPresent()) {
            return;
        }
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.get().getConfiguration();
        String tableName = ((ShowShardingTableRulesStatement) sqlStatement).getTableName();
        if (Objects.isNull(tableName)) {
            tables = config.getTables().iterator();
            autoTables = config.getAutoTables().iterator();
        } else {
            tables = config.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()).iterator();
            autoTables = config.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()).iterator();
        }
        shardingRuleConfig = config;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "actual_data_nodes", "actual_data_sources", "database_strategy_type", "database_sharding_column", "database_sharding_algorithm_type",
                "database_sharding_algorithm_props", "table_strategy_type", "table_sharding_column", "table_sharding_algorithm_type", "table_sharding_algorithm_props",
                "key_generate_column", "key_generator_type", "key_generator_props");
    }
    
    @Override
    public boolean next() {
        return tables.hasNext() || autoTables.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return tables.hasNext() ? buildTableRowData(tables.next()) : buildAutoTableRowData(autoTables.next());
    }
    
    private Collection<Object> buildTableRowData(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Collection<Object> result = new LinkedList<>();
        result.add(shardingTableRuleConfig.getLogicTable());
        result.add(shardingTableRuleConfig.getActualDataNodes());
        result.add("");
        Optional<ShardingStrategyConfiguration> databaseShardingStrategyConfig = getDatabaseShardingStrategy(shardingTableRuleConfig);
        result.add(databaseShardingStrategyConfig.map(this::getStrategyType).orElse(""));
        result.add(databaseShardingStrategyConfig.map(this::getShardingColumn).orElse(""));
        result.add(databaseShardingStrategyConfig.map(this::getAlgorithmType).orElse(""));
        result.add(databaseShardingStrategyConfig.map(this::getAlgorithmProperties).orElse(""));
        Optional<ShardingStrategyConfiguration> tableShardingStrategyConfig = getTableShardingStrategy(shardingTableRuleConfig.getTableShardingStrategy());
        result.add(tableShardingStrategyConfig.map(this::getStrategyType).orElse(""));
        result.add(tableShardingStrategyConfig.map(this::getShardingColumn).orElse(""));
        result.add(tableShardingStrategyConfig.map(this::getAlgorithmType).orElse(""));
        result.add(tableShardingStrategyConfig.map(this::getAlgorithmProperties).orElse(""));
        result.add(getKeyGenerateColumn(shardingTableRuleConfig.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorType(shardingTableRuleConfig.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorProps(shardingTableRuleConfig.getKeyGenerateStrategy()));
        return result;
    }
    
    private Collection<Object> buildAutoTableRowData(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        Collection<Object> result = new LinkedList<>();
        result.add(shardingAutoTableRuleConfig.getLogicTable());
        result.add("");
        result.add(shardingAutoTableRuleConfig.getActualDataSources());
        result.add("");
        result.add("");
        result.add("");
        result.add("");
        Optional<ShardingStrategyConfiguration> tableShardingStrategyConfig = getTableShardingStrategy(shardingAutoTableRuleConfig.getShardingStrategy());
        result.add(tableShardingStrategyConfig.map(this::getStrategyType).orElse(""));
        result.add(tableShardingStrategyConfig.map(this::getShardingColumn).orElse(""));
        result.add(tableShardingStrategyConfig.map(this::getAlgorithmType).orElse(""));
        result.add(tableShardingStrategyConfig.map(this::getAlgorithmProperties).orElse(""));
        result.add(getKeyGenerateColumn(shardingAutoTableRuleConfig.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorType(shardingAutoTableRuleConfig.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorProps(shardingAutoTableRuleConfig.getKeyGenerateStrategy()));
        return result;
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
                : Optional.ofNullable(shardingTableRuleConfig.getDatabaseShardingStrategy());
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
    
    @Override
    public String getType() {
        return ShowShardingTableRulesStatement.class.getName();
    }
}

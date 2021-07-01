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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.infra.distsql.query.RQLResultSet;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
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
 * Result set for show sharding table rules.
 */
public final class ShardingTableRuleQueryResultSet implements RQLResultSet {
    
    private Iterator<ShardingTableRuleConfiguration> tables;
    
    private Iterator<ShardingAutoTableRuleConfiguration> autoTables;
    
    private ShardingRuleConfiguration shardingRuleConfig;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        String tableName = ((ShowShardingTableRulesStatement) sqlStatement).getTableName();
        Optional<ShardingRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findAny();
        if (Objects.isNull(tableName)) {
            tables = ruleConfig.map(optional -> optional.getTables().iterator()).orElse(Collections.emptyIterator());
            autoTables = ruleConfig.map(optional -> optional.getAutoTables().iterator()).orElse(Collections.emptyIterator());
        } else {
            tables = ruleConfig.map(optional
                -> optional.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()).iterator()).orElse(Collections.emptyIterator());
            autoTables = ruleConfig.map(optional
                -> optional.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()).iterator()).orElse(Collections.emptyIterator());
        }
        shardingRuleConfig = ruleConfig.orElse(null);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "actualDataNodes", "actualDataSources", "databaseStrategyType", "databaseShardingColumn", "databaseShardingAlgorithmType", "databaseShardingAlgorithmProps", 
                "tableStrategyType", "tableShardingColumn", "tableShardingAlgorithmType", "tableShardingAlgorithmProps", "keyGenerateColumn", "keyGeneratorType", "keyGeneratorProps");
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
        result.add(getDatabaseStrategyType(shardingTableRuleConfig));
        result.add(getDatabaseShardingColumn(shardingTableRuleConfig));
        result.add(getAlgorithmType(getDatabaseShardingStrategy(shardingTableRuleConfig)));
        result.add(getAlgorithmProps(getDatabaseShardingStrategy(shardingTableRuleConfig)));
        result.add(getTableStrategyType(shardingTableRuleConfig.getTableShardingStrategy()));
        result.add(getTableShardingColumn(shardingTableRuleConfig.getTableShardingStrategy()));
        result.add(getAlgorithmType(getTableShardingStrategy(shardingTableRuleConfig.getTableShardingStrategy())));
        result.add(getAlgorithmProps(getTableShardingStrategy(shardingTableRuleConfig.getTableShardingStrategy())));
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
        result.add(getTableStrategyType(shardingAutoTableRuleConfig.getShardingStrategy()));
        result.add(getTableShardingColumn(shardingAutoTableRuleConfig.getShardingStrategy()));
        result.add(getAlgorithmType(getTableShardingStrategy(shardingAutoTableRuleConfig.getShardingStrategy())));
        result.add(getAlgorithmProps(getTableShardingStrategy(shardingAutoTableRuleConfig.getShardingStrategy())));
        result.add(getKeyGenerateColumn(shardingAutoTableRuleConfig.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorType(shardingAutoTableRuleConfig.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorProps(shardingAutoTableRuleConfig.getKeyGenerateStrategy()));
        return result;
    }
    
    private String getDatabaseStrategyType(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Optional<ShardingStrategyConfiguration> databaseShardingStrategy = getDatabaseShardingStrategy(shardingTableRuleConfig);
        return databaseShardingStrategy.isPresent() && !(databaseShardingStrategy.get() instanceof NoneShardingStrategyConfiguration)
                ? getAlgorithmConfiguration(databaseShardingStrategy.get().getShardingAlgorithmName()).getType() : "";
    }
    
    private String getDatabaseShardingColumn(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Optional<ShardingStrategyConfiguration> databaseShardingStrategy = getDatabaseShardingStrategy(shardingTableRuleConfig);
        return databaseShardingStrategy.isPresent() ? getShardingColumn(databaseShardingStrategy.get()) : "";
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
    
    private String getAlgorithmType(final Optional<ShardingStrategyConfiguration> databaseShardingStrategy) {
        return databaseShardingStrategy.isPresent() && !(databaseShardingStrategy.get() instanceof NoneShardingStrategyConfiguration)
                ? getAlgorithmConfiguration(databaseShardingStrategy.get().getShardingAlgorithmName()).getType() : "";
    }
    
    private String getAlgorithmProps(final Optional<ShardingStrategyConfiguration> databaseShardingStrategy) {
        return databaseShardingStrategy.isPresent() && !(databaseShardingStrategy.get() instanceof NoneShardingStrategyConfiguration)
                ? PropertiesConverter.convert(getAlgorithmConfiguration(databaseShardingStrategy.get().getShardingAlgorithmName()).getProps()) : "";
    }
    
    private Optional<ShardingStrategyConfiguration> getDatabaseShardingStrategy(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        return null == shardingTableRuleConfig.getDatabaseShardingStrategy()
                ? Optional.ofNullable(shardingRuleConfig.getDefaultDatabaseShardingStrategy()) : Optional.ofNullable(shardingTableRuleConfig.getDatabaseShardingStrategy());
    }
    
    private ShardingSphereAlgorithmConfiguration getAlgorithmConfiguration(final String algorithmName) {
        return shardingRuleConfig.getShardingAlgorithms().get(algorithmName);
    }
    
    private String getTableStrategyType(final ShardingStrategyConfiguration shardingStrategyConfig) {
        Optional<ShardingStrategyConfiguration> tableShardingStrategy = getTableShardingStrategy(shardingStrategyConfig);
        return tableShardingStrategy.isPresent() ? getStrategyType(tableShardingStrategy.get()) : "";
    }
    
    private String getStrategyType(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig instanceof NoneShardingStrategyConfiguration ? "none" : getAlgorithmConfiguration(shardingStrategyConfig.getShardingAlgorithmName()).getType();
    }
    
    private Optional<ShardingStrategyConfiguration> getTableShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return null == shardingStrategyConfig ? Optional.ofNullable(shardingRuleConfig.getDefaultTableShardingStrategy()) : Optional.of(shardingStrategyConfig);
    }
    
    private String getTableShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig) {
        Optional<ShardingStrategyConfiguration> tableShardingStrategy = getTableShardingStrategy(shardingStrategyConfig);
        return tableShardingStrategy.isPresent() ? getShardingColumn(tableShardingStrategy.get()) : "";
    }
    
    private String getKeyGenerateColumn(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfig).isPresent() ? getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfig).get().getColumn() : "";
    }
    
    private String getKeyGeneratorType(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration) {
        Optional<KeyGenerateStrategyConfiguration> keyGenerateStrategyConfig = getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfiguration);
        return keyGenerateStrategyConfig.isPresent() ? shardingRuleConfig.getKeyGenerators().get(keyGenerateStrategyConfig.get().getKeyGeneratorName()).getType() : "";
    }
    
    private String getKeyGeneratorProps(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfig).map(
            optional -> PropertiesConverter.convert(shardingRuleConfig.getKeyGenerators().get(optional.getKeyGeneratorName()).getProps())).orElse("");
    }
    
    private Optional<KeyGenerateStrategyConfiguration> getKeyGenerateStrategyConfiguration(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return null == keyGenerateStrategyConfig ? Optional.ofNullable(shardingRuleConfig.getDefaultKeyGenerateStrategy()) : Optional.of(keyGenerateStrategyConfig);
    }
    
    @Override
    public String getType() {
        return ShowShardingTableRulesStatement.class.getCanonicalName();
    }
}

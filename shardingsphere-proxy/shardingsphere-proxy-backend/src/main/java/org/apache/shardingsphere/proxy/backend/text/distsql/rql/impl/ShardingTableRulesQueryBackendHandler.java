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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Backend handler for show sharding table rules.
 */
public final class ShardingTableRulesQueryBackendHandler extends SchemaRequiredBackendHandler<ShowShardingTableRulesStatement> {
    
    private Iterator<ShardingTableRuleConfiguration> tables;
    
    private Iterator<ShardingAutoTableRuleConfiguration> autoTables;
    
    private ShardingRuleConfiguration shardingRuleConfiguration;
    
    public ShardingTableRulesQueryBackendHandler(final ShowShardingTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowShardingTableRulesStatement sqlStatement) {
        loadRuleConfiguration(schemaName, sqlStatement.getTableName());
        return new QueryResponseHeader(getQueryHeader(schemaName));
    }
    
    private void loadRuleConfiguration(final String schemaName, final String tableName) {
        Optional<ShardingRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()
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
        shardingRuleConfiguration = ruleConfig.orElse(null);
    }
    
    private List<QueryHeader> getQueryHeader(final String schemaName) {
        List<QueryHeader> result = new LinkedList<>();
        result.add(new QueryHeader(schemaName, "", "table", "table", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "actualDataNodes", "actualDataNodes", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "actualDataSources", "actualDataSources", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "databaseStrategyType", "databaseStrategyType", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "databaseShardingColumn", "databaseShardingColumn", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "databaseShardingAlgorithmType", "databaseShardingAlgorithmType", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "databaseShardingAlgorithmProps", "databaseShardingAlgorithmProps", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "tableStrategyType", "tableStrategyType", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "tableShardingColumn", "tableShardingColumn", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "tableShardingAlgorithmType", "tableShardingAlgorithmType", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "tableShardingAlgorithmProps", "tableShardingAlgorithmProps", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "keyGenerateColumn", "keyGenerateColumn", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "keyGeneratorType", "keyGeneratorType", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "keyGeneratorProps", "keyGeneratorProps", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
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
                ? Optional.ofNullable(shardingRuleConfiguration.getDefaultDatabaseShardingStrategy()) : Optional.ofNullable(shardingTableRuleConfig.getDatabaseShardingStrategy());
    }
    
    private ShardingSphereAlgorithmConfiguration getAlgorithmConfiguration(final String algorithmName) {
        return shardingRuleConfiguration.getShardingAlgorithms().get(algorithmName);
    }
    
    private String getTableStrategyType(final ShardingStrategyConfiguration shardingStrategyConfig) {
        Optional<ShardingStrategyConfiguration> tableShardingStrategy = getTableShardingStrategy(shardingStrategyConfig);
        return tableShardingStrategy.isPresent() ? getStrategyType(tableShardingStrategy.get()) : "";
    }
    
    private String getStrategyType(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return shardingStrategyConfig instanceof NoneShardingStrategyConfiguration ? "none" : getAlgorithmConfiguration(shardingStrategyConfig.getShardingAlgorithmName()).getType();
    }
    
    private Optional<ShardingStrategyConfiguration> getTableShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfig) {
        return null == shardingStrategyConfig ? Optional.ofNullable(shardingRuleConfiguration.getDefaultTableShardingStrategy()) : Optional.of(shardingStrategyConfig);
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
        return keyGenerateStrategyConfig.isPresent() ? shardingRuleConfiguration.getKeyGenerators().get(keyGenerateStrategyConfig.get().getKeyGeneratorName()).getType() : "";
    }
    
    private String getKeyGeneratorProps(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfig).map(
            optional -> PropertiesConverter.convert(shardingRuleConfiguration.getKeyGenerators().get(optional.getKeyGeneratorName()).getProps())).orElse("");
    }
    
    private Optional<KeyGenerateStrategyConfiguration> getKeyGenerateStrategyConfiguration(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        return null == keyGenerateStrategyConfig ? Optional.ofNullable(shardingRuleConfiguration.getDefaultKeyGenerateStrategy()) : Optional.of(keyGenerateStrategyConfig);
    }
}

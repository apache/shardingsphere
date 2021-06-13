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

import com.google.common.base.Joiner;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
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

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
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
            tables = ruleConfig.isPresent() ? ruleConfig.get().getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable()))
                    .collect(Collectors.toList()).iterator() : Collections.emptyIterator();
            autoTables = ruleConfig.isPresent() ? ruleConfig.get().getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable()))
                    .collect(Collectors.toList()).iterator() : Collections.emptyIterator();
        }
        shardingRuleConfiguration = ruleConfig.isPresent() ? ruleConfig.get() : null;
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

    private Collection<Object> buildTableRowData(final ShardingTableRuleConfiguration shardingTableRuleConfiguration) {
        Collection<Object> result = new LinkedList<>();
        result.add(shardingTableRuleConfiguration.getLogicTable());
        result.add(shardingTableRuleConfiguration.getActualDataNodes());
        result.add("");
        result.add(getDatabaseStrategyType(shardingTableRuleConfiguration));
        result.add(getDatabaseShardingColumn(shardingTableRuleConfiguration));
        result.add(getAlgorithmType(getDatabaseShardingStrategy(shardingTableRuleConfiguration)));
        result.add(getAlgorithmProps(getDatabaseShardingStrategy(shardingTableRuleConfiguration)));
        result.add(getTableStrategyType(shardingTableRuleConfiguration.getTableShardingStrategy()));
        result.add(getTableShardingColumn(shardingTableRuleConfiguration.getTableShardingStrategy()));
        result.add(getAlgorithmType(getTableShardingStrategy(shardingTableRuleConfiguration.getTableShardingStrategy())));
        result.add(getAlgorithmProps(getTableShardingStrategy(shardingTableRuleConfiguration.getTableShardingStrategy())));
        result.add(getKeyGenerateColumn(shardingTableRuleConfiguration.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorType(shardingTableRuleConfiguration.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorProps(shardingTableRuleConfiguration.getKeyGenerateStrategy()));
        return result;
    }

    private Collection<Object> buildAutoTableRowData(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfiguration) {
        Collection<Object> result = new LinkedList<>();
        result.add(shardingAutoTableRuleConfiguration.getLogicTable());
        result.add("");
        result.add(shardingAutoTableRuleConfiguration.getActualDataSources());
        result.add("");
        result.add("");
        result.add("");
        result.add("");
        result.add(getTableStrategyType(shardingAutoTableRuleConfiguration.getShardingStrategy()));
        result.add(getTableShardingColumn(shardingAutoTableRuleConfiguration.getShardingStrategy()));
        result.add(getAlgorithmType(getTableShardingStrategy(shardingAutoTableRuleConfiguration.getShardingStrategy())));
        result.add(getAlgorithmProps(getTableShardingStrategy(shardingAutoTableRuleConfiguration.getShardingStrategy())));
        result.add(getKeyGenerateColumn(shardingAutoTableRuleConfiguration.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorType(shardingAutoTableRuleConfiguration.getKeyGenerateStrategy()));
        result.add(getKeyGeneratorProps(shardingAutoTableRuleConfiguration.getKeyGenerateStrategy()));
        return result;
    }

    private String getDatabaseStrategyType(final ShardingTableRuleConfiguration shardingTableRuleConfiguration) {
        Optional<ShardingStrategyConfiguration> databaseShardingStrategy = getDatabaseShardingStrategy(shardingTableRuleConfiguration);
        return databaseShardingStrategy.isPresent() && !(databaseShardingStrategy.get() instanceof NoneShardingStrategyConfiguration)
                ? getAlgorithmConfiguration(databaseShardingStrategy.get().getShardingAlgorithmName()).getType() : "";
    }

    private String getDatabaseShardingColumn(final ShardingTableRuleConfiguration shardingTableRuleConfiguration) {
        Optional<ShardingStrategyConfiguration> databaseShardingStrategy = getDatabaseShardingStrategy(shardingTableRuleConfiguration);
        return databaseShardingStrategy.isPresent() ? getShardingColumn(databaseShardingStrategy.get()) : "";
    }

    private String getShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        if (shardingStrategyConfiguration instanceof StandardShardingStrategyConfiguration) {
            return ((StandardShardingStrategyConfiguration) shardingStrategyConfiguration).getShardingColumn();
        } else if (shardingStrategyConfiguration instanceof ComplexShardingStrategyConfiguration) {
            return ((ComplexShardingStrategyConfiguration) shardingStrategyConfiguration).getShardingColumns();
        }
        return "";
    }

    private String getAlgorithmType(final Optional<ShardingStrategyConfiguration> databaseShardingStrategy) {
        return databaseShardingStrategy.isPresent() && !(databaseShardingStrategy.get() instanceof NoneShardingStrategyConfiguration)
                ? getAlgorithmConfiguration(databaseShardingStrategy.get().getShardingAlgorithmName()).getType() : "";
    }

    private String getAlgorithmProps(final Optional<ShardingStrategyConfiguration> databaseShardingStrategy) {
        return databaseShardingStrategy.isPresent() && !(databaseShardingStrategy.get() instanceof NoneShardingStrategyConfiguration)
                ? buildProperties(getAlgorithmConfiguration(databaseShardingStrategy.get().getShardingAlgorithmName()).getProps()) : "";
    }

    private Optional<ShardingStrategyConfiguration> getDatabaseShardingStrategy(final ShardingTableRuleConfiguration shardingTableRuleConfiguration) {
        return Objects.nonNull(shardingTableRuleConfiguration.getDatabaseShardingStrategy())
                ? Optional.ofNullable(shardingTableRuleConfiguration.getDatabaseShardingStrategy())
                : Optional.ofNullable(shardingRuleConfiguration.getDefaultDatabaseShardingStrategy());
    }

    private ShardingSphereAlgorithmConfiguration getAlgorithmConfiguration(final String algorithmName) {
        return shardingRuleConfiguration.getShardingAlgorithms().get(algorithmName);
    }

    private String getTableStrategyType(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        Optional<ShardingStrategyConfiguration> tableShardingStrategy = getTableShardingStrategy(shardingStrategyConfiguration);
        return tableShardingStrategy.isPresent() ? getStrategyType(tableShardingStrategy.get()) : "";
    }

    private String getStrategyType(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        return shardingStrategyConfiguration instanceof NoneShardingStrategyConfiguration ? "none"
                : getAlgorithmConfiguration(shardingStrategyConfiguration.getShardingAlgorithmName()).getType();
    }

    private Optional<ShardingStrategyConfiguration> getTableShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        return Objects.nonNull(shardingStrategyConfiguration)
                ? Optional.ofNullable(shardingStrategyConfiguration)
                : Optional.ofNullable(shardingRuleConfiguration.getDefaultTableShardingStrategy());
    }

    private String getTableShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        Optional<ShardingStrategyConfiguration> tableShardingStrategy = getTableShardingStrategy(shardingStrategyConfiguration);
        return tableShardingStrategy.isPresent() ? getShardingColumn(tableShardingStrategy.get()) : "";
    }

    private String getKeyGenerateColumn(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration) {
        return getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfiguration).isPresent()
                ? getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfiguration).get().getColumn() : "";
    }

    private String getKeyGeneratorType(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration) {
        Optional<KeyGenerateStrategyConfiguration> optional = getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfiguration);
        return optional.isPresent() ? shardingRuleConfiguration.getKeyGenerators().get(optional.get().getKeyGeneratorName()).getType() : "";
    }

    private String getKeyGeneratorProps(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration) {
        Optional<KeyGenerateStrategyConfiguration> optional = getKeyGenerateStrategyConfiguration(keyGenerateStrategyConfiguration);
        return optional.isPresent() ? buildProperties(shardingRuleConfiguration.getKeyGenerators().get(optional.get().getKeyGeneratorName()).getProps()) : "";
    }

    private Optional<KeyGenerateStrategyConfiguration> getKeyGenerateStrategyConfiguration(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration) {
        return Objects.nonNull(keyGenerateStrategyConfiguration)
                ? Optional.ofNullable(keyGenerateStrategyConfiguration)
                : Optional.ofNullable(shardingRuleConfiguration.getDefaultKeyGenerateStrategy());
    }

    private String buildProperties(final Properties properties) {
        return Objects.nonNull(properties) ? Joiner.on(",").join(properties.entrySet().stream()
                .map(each -> Joiner.on("=").join(each.getKey(), each.getValue())).collect(Collectors.toList())) : "";
    }
}

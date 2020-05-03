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

package org.apache.shardingsphere.core.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.core.strategy.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.core.strategy.route.ShardingStrategy;
import org.apache.shardingsphere.core.strategy.route.ShardingStrategyFactory;
import org.apache.shardingsphere.core.strategy.route.none.NoneShardingStrategy;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.keygen.KeyGenerateAlgorithm;
import org.apache.shardingsphere.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.underlying.common.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.underlying.common.rule.DataNode;
import org.apache.shardingsphere.underlying.common.rule.DataNodeRoutedRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Databases and tables sharding rule.
 */
@Getter
public final class ShardingRule implements DataNodeRoutedRule {
    
    static {
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    private final ShardingRuleConfiguration ruleConfiguration;
    
    private final Collection<String> dataSourceNames;
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules;
    
    private final Collection<String> broadcastTables;
    
    private final ShardingStrategy defaultDatabaseShardingStrategy;
    
    private final ShardingStrategy defaultTableShardingStrategy;
    
    private final KeyGenerateAlgorithm defaultKeyGenerateAlgorithm;
    
    public ShardingRule(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames) {
        Preconditions.checkArgument(null != shardingRuleConfig, "ShardingRuleConfig cannot be null.");
        Preconditions.checkArgument(null != dataSourceNames && !dataSourceNames.isEmpty(), "Data sources cannot be empty.");
        this.ruleConfiguration = shardingRuleConfig;
        this.dataSourceNames = getDataSourceNames(shardingRuleConfig.getTableRuleConfigs(), dataSourceNames);
        tableRules = createTableRules(shardingRuleConfig);
        broadcastTables = shardingRuleConfig.getBroadcastTables();
        bindingTableRules = createBindingTableRules(shardingRuleConfig.getBindingTableGroups());
        defaultDatabaseShardingStrategy = createDefaultShardingStrategy(shardingRuleConfig.getDefaultDatabaseShardingStrategyConfig());
        defaultTableShardingStrategy = createDefaultShardingStrategy(shardingRuleConfig.getDefaultTableShardingStrategyConfig());
        defaultKeyGenerateAlgorithm = createDefaultKeyGenerateAlgorithm(shardingRuleConfig.getDefaultKeyGeneratorConfig());
    }
    
    private Collection<String> getDataSourceNames(final Collection<TableRuleConfiguration> tableRuleConfigs, final Collection<String> dataSourceNames) {
        Collection<String> result = new LinkedHashSet<>();
        if (tableRuleConfigs.isEmpty()) {
            return dataSourceNames;
        }
        for (TableRuleConfiguration each : tableRuleConfigs) {
            if (null == each.getActualDataNodes()) {
                return dataSourceNames;
            }
            result.addAll(getDataSourceNames(each));
        }
        return result;
    }
    
    private Collection<String> getDataSourceNames(final TableRuleConfiguration tableRuleConfiguration) {
        Collection<String> result = new LinkedHashSet<>();
        for (String each : new InlineExpressionParser(tableRuleConfiguration.getActualDataNodes()).splitAndEvaluate()) {
            result.add(new DataNode(each).getDataSourceName());
        }
        return result;
    }
    
    private Collection<TableRule> createTableRules(final ShardingRuleConfiguration shardingRuleConfig) {
        return shardingRuleConfig.getTableRuleConfigs().stream().map(each ->
                new TableRule(each, dataSourceNames, getDefaultGenerateKeyColumn(shardingRuleConfig))).collect(Collectors.toList());
    }
    
    private String getDefaultGenerateKeyColumn(final ShardingRuleConfiguration shardingRuleConfig) {
        return Optional.ofNullable(shardingRuleConfig.getDefaultKeyGeneratorConfig()).map(KeyGeneratorConfiguration::getColumn).orElse(null);
    }
    
    private Collection<BindingTableRule> createBindingTableRules(final Collection<String> bindingTableGroups) {
        return bindingTableGroups.stream().map(this::createBindingTableRule).collect(Collectors.toList());
    }
    
    private BindingTableRule createBindingTableRule(final String bindingTableGroup) {
        return new BindingTableRule(Splitter.on(",").trimResults().splitToList(bindingTableGroup).stream().map(this::getTableRule).collect(Collectors.toList()));
    }
    
    private ShardingStrategy createDefaultShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        return Optional.ofNullable(shardingStrategyConfiguration).map(ShardingStrategyFactory::newInstance).orElse(new NoneShardingStrategy());
    }
    
    private KeyGenerateAlgorithm createDefaultKeyGenerateAlgorithm(final KeyGeneratorConfiguration keyGeneratorConfiguration) {
        return containsKeyGenerateAlgorithm(keyGeneratorConfiguration) ? keyGeneratorConfiguration.getKeyGenerateAlgorithm() : TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class);
    }
    
    private boolean containsKeyGenerateAlgorithm(final KeyGeneratorConfiguration keyGeneratorConfiguration) {
        return null != keyGeneratorConfiguration && null != keyGeneratorConfiguration.getKeyGenerateAlgorithm();
    }
    
    /**
     * Find table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TableRule> findTableRule(final String logicTableName) {
        return tableRules.stream().filter(each -> each.getLogicTable().equalsIgnoreCase(logicTableName)).findFirst();
    }
    
    /**
     * Find table rule via actual table name.
     *
     * @param actualTableName actual table name
     * @return table rule
     */
    public Optional<TableRule> findTableRuleByActualTable(final String actualTableName) {
        return tableRules.stream().filter(each -> each.isExisted(actualTableName)).findFirst();
    }
    
    /**
     * Get table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public TableRule getTableRule(final String logicTableName) {
        Optional<TableRule> tableRule = findTableRule(logicTableName);
        if (tableRule.isPresent()) {
            return tableRule.get();
        }
        if (isBroadcastTable(logicTableName)) {
            return new TableRule(dataSourceNames, logicTableName);
        }
        throw new ShardingSphereConfigurationException("Cannot find table rule with logic table: '%s'", logicTableName);
    }
    
    /**
     * Get database sharding strategy.
     *
     * <p>
     * Use default database sharding strategy if not found.
     * </p>
     *
     * @param tableRule table rule
     * @return database sharding strategy
     */
    public ShardingStrategy getDatabaseShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getDatabaseShardingStrategy() ? defaultDatabaseShardingStrategy : tableRule.getDatabaseShardingStrategy();
    }
    
    /**
     * Get table sharding strategy.
     *
     * <p>
     * Use default table sharding strategy if not found.
     * </p>
     *
     * @param tableRule table rule
     * @return table sharding strategy
     */
    public ShardingStrategy getTableShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getTableShardingStrategy() ? defaultTableShardingStrategy : tableRule.getTableShardingStrategy();
    }
    
    /**
     * Judge logic tables is all belong to binding encryptors.
     *
     * @param logicTableNames logic table names
     * @return logic tables is all belong to binding encryptors or not
     */
    public boolean isAllBindingTables(final Collection<String> logicTableNames) {
        if (logicTableNames.isEmpty()) {
            return false;
        }
        Optional<BindingTableRule> bindingTableRule = findBindingTableRule(logicTableNames);
        if (!bindingTableRule.isPresent()) {
            return false;
        }
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(bindingTableRule.get().getAllLogicTables());
        return !result.isEmpty() && result.containsAll(logicTableNames);
    }
    
    private Optional<BindingTableRule> findBindingTableRule(final Collection<String> logicTableNames) {
        return logicTableNames.stream().map(this::findBindingTableRule).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
    }
    
    /**
     * Find binding table rule via logic table name.
     *
     * @param logicTableName logic table name
     * @return binding table rule
     */
    public Optional<BindingTableRule> findBindingTableRule(final String logicTableName) {
        return bindingTableRules.stream().filter(each -> each.hasLogicTable(logicTableName)).findFirst();
    }
    
    /**
     * Judge logic tables is all belong to broadcast encryptors.
     *
     * @param logicTableNames logic table names
     * @return logic tables is all belong to broadcast encryptors or not
     */
    public boolean isAllBroadcastTables(final Collection<String> logicTableNames) {
        if (logicTableNames.isEmpty()) {
            return false;
        }
        for (String each : logicTableNames) {
            if (!isBroadcastTable(each)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Judge logic table is belong to broadcast tables.
     *
     * @param logicTableName logic table name
     * @return logic table is belong to broadcast tables or not
     */
    public boolean isBroadcastTable(final String logicTableName) {
        return broadcastTables.stream().anyMatch(each -> each.equalsIgnoreCase(logicTableName));
    }
    
    /**
     * Judge if there is at least one table rule for logic tables.
     *
     * @param logicTableNames logic table names
     * @return whether a table rule exists for logic tables
     */
    public boolean tableRuleExists(final Collection<String> logicTableNames) {
        return logicTableNames.stream().anyMatch(each -> findTableRule(each).isPresent() || isBroadcastTable(each));
    }
    
    /**
     * Judge is sharding column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return is sharding column or not
     */
    public boolean isShardingColumn(final String columnName, final String tableName) {
        return tableRules.stream().anyMatch(each -> each.getLogicTable().equalsIgnoreCase(tableName) && isShardingColumn(each, columnName));
    }
    
    private boolean isShardingColumn(final TableRule tableRule, final String columnName) {
        return getDatabaseShardingStrategy(tableRule).getShardingColumns().contains(columnName) || getTableShardingStrategy(tableRule).getShardingColumns().contains(columnName);
    }
    
    /**
     * Find column name of generated key.
     *
     * @param logicTableName logic table name
     * @return column name of generated key
     */
    public Optional<String> findGenerateKeyColumnName(final String logicTableName) {
        return tableRules.stream().filter(each -> each.getLogicTable().equalsIgnoreCase(logicTableName) && each.getGenerateKeyColumn().isPresent())
                .map(TableRule::getGenerateKeyColumn).findFirst().orElse(Optional.empty());
    }
    
    /**
     * Generate key.
     *
     * @param logicTableName logic table name
     * @return generated key
     */
    public Comparable<?> generateKey(final String logicTableName) {
        Optional<TableRule> tableRule = findTableRule(logicTableName);
        if (!tableRule.isPresent()) {
            throw new ShardingSphereConfigurationException("Cannot find strategy for generate keys.");
        }
        return Optional.ofNullable(tableRule.get().getKeyGenerateAlgorithm()).orElse(defaultKeyGenerateAlgorithm).generateKey();
    }
    
    /**
     * Find data node by logic table name.
     *
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode getDataNode(final String logicTableName) {
        TableRule tableRule = getTableRule(logicTableName);
        return tableRule.getActualDataNodes().get(0);
    }
    
    /**
     * Find data node by data source and logic table.
     *
     * @param dataSourceName data source name
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode getDataNode(final String dataSourceName, final String logicTableName) {
        TableRule tableRule = getTableRule(logicTableName);
        return tableRule.getActualDataNodes().stream().filter(each -> dataSourceNames.contains(each.getDataSourceName())
                && each.getDataSourceName().equals(dataSourceName)).findFirst()
                .orElseThrow(() -> new ShardingSphereConfigurationException("Cannot find actual data node for data source name: '%s' and logic table name: '%s'", dataSourceName, logicTableName));
    }
    
    /**
     * Get sharding logic table names.
     *
     * @param logicTableNames logic table names
     * @return sharding logic table names
     */
    public Collection<String> getShardingLogicTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(each -> findTableRule(each).isPresent()).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Get logic and actual binding tables.
     *
     * @param dataSourceName data source name
     * @param logicTable logic table name
     * @param actualTable actual table name
     * @param availableLogicBindingTables available logic binding table names
     * @return logic and actual binding tables
     */
    public Map<String, String> getLogicAndActualTablesFromBindingTable(final String dataSourceName,
                                                                       final String logicTable, final String actualTable, final Collection<String> availableLogicBindingTables) {
        Map<String, String> result = new LinkedHashMap<>();
        findBindingTableRule(logicTable).ifPresent(bindingTableRule -> result.putAll(bindingTableRule.getLogicAndActualTables(dataSourceName, logicTable, actualTable, availableLogicBindingTables)));
        return result;
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        Map<String, Collection<DataNode>> result = new HashMap<>(tableRules.size(), 1);
        for (TableRule each : tableRules) {
            result.put(each.getLogicTable(), each.getActualDataNodes());
        }
        return result;
    }
    
    @Override
    public Collection<String> getAllActualTables() {
        Collection<String> result = new HashSet<>();
        for (TableRule each : tableRules) {
            result.addAll(each.getActualDataNodes().stream().map(DataNode::getTableName).collect(Collectors.toSet()));
        }
        return result;
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return findTableRule(logicTable).map(tableRule -> tableRule.getActualDataNodes().get(0).getTableName());
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return !isAllBroadcastTables(tables);
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return findTableRuleByActualTable(actualTable).map(TableRule::getLogicTable);
    }
}

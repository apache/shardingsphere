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

package org.apache.shardingsphere.sharding.rule;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.CreateTableEvent;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.DropTableEvent;
import org.apache.shardingsphere.infra.rule.level.FeatureRule;
import org.apache.shardingsphere.infra.rule.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.rule.single.SingleTableRule;
import org.apache.shardingsphere.sharding.rule.single.SingleTableRuleLoader;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Sharding rule.
 */
@Getter
public final class ShardingRule implements FeatureRule, SchemaRule, DataNodeContainedRule, TableContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, ShardingAlgorithm> shardingAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, KeyGenerateAlgorithm> keyGenerators = new LinkedHashMap<>();
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules;
    
    private final Collection<String> broadcastTables;
    
    private final Map<String, SingleTableRule> singleTableRules;
    
    private final ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig;
    
    private final ShardingStrategyConfiguration defaultTableShardingStrategyConfig;
    
    private final KeyGenerateAlgorithm defaultKeyGenerateAlgorithm;
    
    public ShardingRule(final ShardingRuleConfiguration config, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
        dataSourceNames = getDataSourceNames(config.getTables(), config.getAutoTables(), dataSourceMap.keySet());
        config.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, ShardingAlgorithm.class)));
        config.getKeyGenerators().forEach((key, value) -> keyGenerators.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, KeyGenerateAlgorithm.class)));
        tableRules = new LinkedList<>(createTableRules(config.getTables(), config.getDefaultKeyGenerateStrategy()));
        tableRules.addAll(createAutoTableRules(config.getAutoTables(), config.getDefaultKeyGenerateStrategy()));
        broadcastTables = config.getBroadcastTables();
        bindingTableRules = createBindingTableRules(config.getBindingTableGroups());
        singleTableRules = SingleTableRuleLoader.load(databaseType, dataSourceMap, getExcludedTables());
        defaultDatabaseShardingStrategyConfig = null == config.getDefaultDatabaseShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultDatabaseShardingStrategy();
        defaultTableShardingStrategyConfig = null == config.getDefaultTableShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultTableShardingStrategy();
        defaultKeyGenerateAlgorithm = null == config.getDefaultKeyGenerateStrategy()
                ? TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class) : keyGenerators.get(config.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    public ShardingRule(final AlgorithmProvidedShardingRuleConfiguration config, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
        dataSourceNames = getDataSourceNames(config.getTables(), config.getAutoTables(), dataSourceMap.keySet());
        shardingAlgorithms.putAll(config.getShardingAlgorithms());
        keyGenerators.putAll(config.getKeyGenerators());
        tableRules = new LinkedList<>(createTableRules(config.getTables(), config.getDefaultKeyGenerateStrategy()));
        tableRules.addAll(createAutoTableRules(config.getAutoTables(), config.getDefaultKeyGenerateStrategy()));
        broadcastTables = config.getBroadcastTables();
        bindingTableRules = createBindingTableRules(config.getBindingTableGroups());
        singleTableRules = SingleTableRuleLoader.load(databaseType, dataSourceMap, getExcludedTables());
        defaultDatabaseShardingStrategyConfig = null == config.getDefaultDatabaseShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultDatabaseShardingStrategy();
        defaultTableShardingStrategyConfig = null == config.getDefaultTableShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultTableShardingStrategy();
        defaultKeyGenerateAlgorithm = null == config.getDefaultKeyGenerateStrategy()
                ? TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class) : keyGenerators.get(config.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
    }
    
    private Collection<String> getDataSourceNames(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs, 
                                                  final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigs, final Collection<String> dataSourceNames) {
        if (tableRuleConfigs.isEmpty()) {
            return dataSourceNames;
        }
        if (tableRuleConfigs.stream().map(ShardingTableRuleConfiguration::getActualDataNodes).anyMatch(each -> null == each || each.isEmpty())) {
            return dataSourceNames;
        }
        Collection<String> result = new LinkedHashSet<>();
        tableRuleConfigs.forEach(each -> result.addAll(getDataSourceNames(each)));
        autoTableRuleConfigs.forEach(each -> result.addAll(getDataSourceNames(each)));
        return result;
    }
    
    private Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        return Strings.isNullOrEmpty(shardingAutoTableRuleConfig.getActualDataSources()) 
                ? Collections.emptyList() : Splitter.on(",").trimResults().splitToList(shardingAutoTableRuleConfig.getActualDataSources());
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        List<String> actualDataNodes = new InlineExpressionParser(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private Collection<TableRule> createTableRules(final Collection<ShardingTableRuleConfiguration> tableRuleConfigurations, final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return tableRuleConfigurations.stream().map(each -> new TableRule(each, dataSourceNames, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig))).collect(Collectors.toList());
    }
    
    private Collection<TableRule> createAutoTableRules(final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigurations, 
                                                       final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return autoTableRuleConfigurations.stream().map(
            each -> createAutoTableRule(defaultKeyGenerateStrategyConfig, each)).collect(Collectors.toList());
    }
    
    private TableRule createAutoTableRule(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final ShardingAutoTableRuleConfiguration each) {
        ShardingAlgorithm shardingAlgorithm = null == each.getShardingStrategy() ? null : shardingAlgorithms.get(each.getShardingStrategy().getShardingAlgorithmName());
        Preconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm, "Sharding auto table rule configuration must match sharding auto table algorithm.");
        return new TableRule(each, dataSourceNames, (ShardingAutoTableAlgorithm) shardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig));
    }
    
    private String getDefaultGenerateKeyColumn(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return Optional.ofNullable(defaultKeyGenerateStrategyConfig).map(KeyGenerateStrategyConfiguration::getColumn).orElse(null);
    }
    
    private Collection<BindingTableRule> createBindingTableRules(final Collection<String> bindingTableGroups) {
        return bindingTableGroups.stream().map(this::createBindingTableRule).collect(Collectors.toList());
    }
    
    private BindingTableRule createBindingTableRule(final String bindingTableGroup) {
        return new BindingTableRule(Splitter.on(",").trimResults().splitToList(bindingTableGroup).stream().map(this::getTableRule).collect(Collectors.toList()));
    }
    
    private Collection<String> getExcludedTables() {
        Collection<String> result = new HashSet<>(getTables());
        result.addAll(getAllActualTables());
        result.addAll(broadcastTables);
        return result;
    }
    
    /**
     * Get database sharding strategy configuration.
     * 
     * @param tableRule table rule
     * @return database sharding strategy configuration
     */
    public ShardingStrategyConfiguration getDatabaseShardingStrategyConfiguration(final TableRule tableRule) {
        return null == tableRule.getDatabaseShardingStrategyConfig() ? defaultDatabaseShardingStrategyConfig : tableRule.getDatabaseShardingStrategyConfig();
    }
    
    /**
     * Get table sharding strategy configuration.
     *
     * @param tableRule table rule
     * @return table sharding strategy configuration
     */
    public ShardingStrategyConfiguration getTableShardingStrategyConfiguration(final TableRule tableRule) {
        return null == tableRule.getTableShardingStrategyConfig() ? defaultTableShardingStrategyConfig : tableRule.getTableShardingStrategyConfig();
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
        return !logicTableNames.isEmpty() && logicTableNames.stream().allMatch(this::isBroadcastTable);
    }
    
    /**
     * Judge logic tables is all belong to sharding tables.
     *
     * @param logicTableNames logic table names
     * @return logic tables is all belong to sharding tables or not
     */
    public boolean isAllShardingTables(final Collection<String> logicTableNames) {
        return logicTableNames.stream().allMatch(each -> findTableRule(each).isPresent());
    }
    
    /**
     * Judge logic table is belong to sharding tables.
     *
     * @param logicTableName logic table name
     * @return logic table is belong to sharding tables or not
     */
    public boolean isShardingTable(final String logicTableName) {
        return findTableRule(logicTableName).isPresent();
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
     * Judge if all single tables exist in same data source.
     *
     * @param logicTableNames logic table names
     * @return all single tables exist in same data source or not
     */
    public boolean isSingleTablesInSameDataSource(final Collection<String> logicTableNames) {
        if (!singleTableRules.keySet().containsAll(logicTableNames)) {
            return false;
        }
        return 1 == singleTableRules.values().stream().filter(each -> logicTableNames.contains(each.getTableName())).map(SingleTableRule::getDataSourceName).collect(Collectors.toSet()).size();
    }
    
    /**
     * Judge whether all tables are in same data source or not.
     * 
     * @param logicTableNames logic table names
     * @return whether all tables are in same data source or not
     */
    public boolean isAllTablesInSameDataSource(final Collection<String> logicTableNames) {
        Set<String> tableNames = Sets.newHashSet(logicTableNames);
        Set<String> dataSourceNames = Sets.newHashSet();
        dataSourceNames.addAll(tableRules.stream().filter(each -> tableNames.contains(each.getLogicTable())).flatMap(each 
            -> each.getActualDataNodes().stream()).map(DataNode::getDataSourceName).collect(Collectors.toSet()));
        dataSourceNames.addAll(broadcastTables.stream().filter(tableNames::contains).flatMap(each -> getDataSourceNames().stream()).collect(Collectors.toSet()));
        dataSourceNames.addAll(singleTableRules.values().stream().filter(each -> tableNames.contains(each.getTableName())).map(SingleTableRule::getDataSourceName).collect(Collectors.toSet()));
        return 1 == dataSourceNames.size();
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
     * Judge if single table rule exists or not.
     *
     * @param logicTableNames logic table names
     * @return whether single table rule exists for logic tables
     */
    public boolean singleTableRuleExists(final Collection<String> logicTableNames) {
        return singleTableRules.keySet().stream().anyMatch(logicTableNames::contains);
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
        return isShardingColumn(getDatabaseShardingStrategyConfiguration(tableRule), columnName) || isShardingColumn(getTableShardingStrategyConfiguration(tableRule), columnName);
    }
    
    private boolean isShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig, final String columnName) {
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            return ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn().equalsIgnoreCase(columnName);
        }
        if (shardingStrategyConfig instanceof ComplexShardingStrategyConfiguration) {
            return ((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns().contains(columnName);
        }
        return false;
    } 
    
    /**
     * Judge is generate key column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return is generate key column or not
     */
    public boolean isGenerateKeyColumn(final String columnName, final String tableName) {
        return tableRules.stream().anyMatch(each -> each.getLogicTable().equalsIgnoreCase(tableName) && isGenerateKeyColumn(each, columnName));
    }
    
    private boolean isGenerateKeyColumn(final TableRule tableRule, final String columnName) {
        Optional<String> generateKeyColumn = tableRule.getGenerateKeyColumn();
        return generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName);
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
        KeyGenerateAlgorithm keyGenerator = null != tableRule.get().getKeyGeneratorName() ? keyGenerators.get(tableRule.get().getKeyGeneratorName()) : defaultKeyGenerateAlgorithm;
        return keyGenerator.generateKey();
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
    
    /**
     * Add single table.
     * 
     * @param event create table event
     */
    @Subscribe
    public void createSingleTable(final CreateTableEvent event) {
        if (!isConfiguredTable(event.getTableName())) {
            singleTableRules.put(event.getTableName(), new SingleTableRule(event.getTableName(), event.getDataSourceName()));
        }
    }
    
    private boolean isConfiguredTable(final String tableName) {
        return findTableRule(tableName).isPresent() || findBindingTableRule(tableName).isPresent() || broadcastTables.contains(tableName) || singleTableRules.containsKey(tableName);
    }
    
    /**
     * Drop single table.
     *
     * @param event drop table event
     */
    @Subscribe
    public void dropSingleTable(final DropTableEvent event) {
        singleTableRules.remove(event.getTableName());
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
        singleTableRules.forEach((key, value) -> result.put(key, Collections.singleton(new DataNode(value.getDataSourceName(), value.getTableName()))));
        result.putAll(tableRules.stream().collect(Collectors.toMap(TableRule::getLogicTable, TableRule::getActualDataNodes, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        return result;
    }
    
    @Override
    public Collection<String> getAllActualTables() {
        return tableRules.stream().flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toSet());
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
    
    @Override
    public Collection<String> getTables() {
        return tableRules.stream().map((Function<TableRule, String>) TableRule::getLogicTable).collect(Collectors.toList());
    }
    
    @Override
    public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
        return findTableRule(logicTable).flatMap(tableRule -> findActualTableFromActualDataNode(catalog, tableRule.getActualDataNodes()));
    }
    
    private Optional<String> findActualTableFromActualDataNode(final String catalog, final List<DataNode> actualDataNodes) {
        return actualDataNodes.stream().filter(each -> each.getDataSourceName().equalsIgnoreCase(catalog)).findFirst().map(DataNode::getTableName);
    }
}

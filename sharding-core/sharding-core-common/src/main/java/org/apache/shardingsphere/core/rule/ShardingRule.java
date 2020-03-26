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
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.core.strategy.route.ShardingStrategy;
import org.apache.shardingsphere.core.strategy.route.ShardingStrategyFactory;
import org.apache.shardingsphere.core.strategy.route.none.NoneShardingStrategy;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.spi.algorithm.keygen.ShardingKeyGeneratorServiceLoader;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;
import org.apache.shardingsphere.underlying.common.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.common.rule.DataNode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Databases and tables sharding rule.
 */
@Getter
public class ShardingRule implements BaseRule {
    
    private final ShardingRuleConfiguration ruleConfiguration;
    
    private final ShardingDataSourceNames shardingDataSourceNames;
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules;
    
    private final Collection<String> broadcastTables;
    
    private final ShardingStrategy defaultDatabaseShardingStrategy;
    
    private final ShardingStrategy defaultTableShardingStrategy;
    
    private final ShardingKeyGenerator defaultShardingKeyGenerator;
    
    private final Collection<MasterSlaveRule> masterSlaveRules;
    
    private final EncryptRule encryptRule;
    
    public ShardingRule(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames) {
        Preconditions.checkArgument(null != shardingRuleConfig, "ShardingRuleConfig cannot be null.");
        Preconditions.checkArgument(null != dataSourceNames && !dataSourceNames.isEmpty(), "Data sources cannot be empty.");
        this.ruleConfiguration = shardingRuleConfig;
        shardingDataSourceNames = new ShardingDataSourceNames(shardingRuleConfig, dataSourceNames);
        tableRules = createTableRules(shardingRuleConfig);
        broadcastTables = shardingRuleConfig.getBroadcastTables();
        bindingTableRules = createBindingTableRules(shardingRuleConfig.getBindingTableGroups());
        defaultDatabaseShardingStrategy = createDefaultShardingStrategy(shardingRuleConfig.getDefaultDatabaseShardingStrategyConfig());
        defaultTableShardingStrategy = createDefaultShardingStrategy(shardingRuleConfig.getDefaultTableShardingStrategyConfig());
        defaultShardingKeyGenerator = createDefaultKeyGenerator(shardingRuleConfig.getDefaultKeyGeneratorConfig());
        masterSlaveRules = createMasterSlaveRules(shardingRuleConfig.getMasterSlaveRuleConfigs());
        encryptRule = createEncryptRule(shardingRuleConfig.getEncryptRuleConfig());
    }
    
    private Collection<TableRule> createTableRules(final ShardingRuleConfiguration shardingRuleConfig) {
        return shardingRuleConfig.getTableRuleConfigs().stream().map(each ->
                new TableRule(each, shardingDataSourceNames, getDefaultGenerateKeyColumn(shardingRuleConfig))).collect(Collectors.toList());
    }
    
    private String getDefaultGenerateKeyColumn(final ShardingRuleConfiguration shardingRuleConfig) {
        return Optional.ofNullable(shardingRuleConfig.getDefaultKeyGeneratorConfig()).map(KeyGeneratorConfiguration::getColumn).orElse(null);
    }
    
    private Collection<BindingTableRule> createBindingTableRules(final Collection<String> bindingTableGroups) {
        return bindingTableGroups.stream().map(this::createBindingTableRule).collect(Collectors.toList());
    }
    
    private BindingTableRule createBindingTableRule(final String bindingTableGroup) {
        List<TableRule> tableRules = Splitter.on(",").trimResults().splitToList(bindingTableGroup).stream().map(this::getTableRule).collect(Collectors.toCollection(LinkedList::new));
        return new BindingTableRule(tableRules);
    }
    
    private ShardingStrategy createDefaultShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        return Optional.ofNullable(shardingStrategyConfiguration).map(ShardingStrategyFactory::newInstance).orElse(new NoneShardingStrategy());
    }
    
    private ShardingKeyGenerator createDefaultKeyGenerator(final KeyGeneratorConfiguration keyGeneratorConfiguration) {
        ShardingKeyGeneratorServiceLoader serviceLoader = new ShardingKeyGeneratorServiceLoader();
        return containsKeyGeneratorConfiguration(keyGeneratorConfiguration)
                ? serviceLoader.newService(keyGeneratorConfiguration.getType(), keyGeneratorConfiguration.getProperties()) : serviceLoader.newService();
    }
    
    private boolean containsKeyGeneratorConfiguration(final KeyGeneratorConfiguration keyGeneratorConfiguration) {
        return null != keyGeneratorConfiguration && !Strings.isNullOrEmpty(keyGeneratorConfiguration.getType());
    }
    
    private Collection<MasterSlaveRule> createMasterSlaveRules(final Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigurations) {
        return masterSlaveRuleConfigurations.stream().map(MasterSlaveRule::new).collect(Collectors.toList());
    }
    
    private EncryptRule createEncryptRule(final EncryptRuleConfiguration encryptRuleConfig) {
        return Optional.ofNullable(encryptRuleConfig).map(e -> new EncryptRule(ruleConfiguration.getEncryptRuleConfig())).orElse(new EncryptRule());
    }
    
    /**
     * Find table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TableRule> findTableRule(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find table rule via actual table name.
     *
     * @param actualTableName actual table name
     * @return table rule
     */
    public Optional<TableRule> findTableRuleByActualTable(final String actualTableName) {
        for (TableRule each : tableRules) {
            if (each.isExisted(actualTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
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
            return new TableRule(shardingDataSourceNames.getDataSourceNames(), logicTableName);
        }
        if (!Strings.isNullOrEmpty(shardingDataSourceNames.getDefaultDataSourceName())) {
            return new TableRule(shardingDataSourceNames.getDefaultDataSourceName(), logicTableName);
        }
        throw new ShardingSphereConfigurationException("Cannot find table rule and default data source with logic table: '%s'", logicTableName);
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
        return Optional.ofNullable(tableRule.getTableShardingStrategy()).orElse(defaultDatabaseShardingStrategy);
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
        for (String each : logicTableNames) {
            Optional<BindingTableRule> result = findBindingTableRule(each);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find binding table rule via logic table name.
     *
     * @param logicTableName logic table name
     * @return binding table rule
     */
    public Optional<BindingTableRule> findBindingTableRule(final String logicTableName) {
        for (BindingTableRule each : bindingTableRules) {
            if (each.hasLogicTable(logicTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
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
        for (String each : broadcastTables) {
            if (each.equalsIgnoreCase(logicTableName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge logic tables is all belong to default data source.
     *
     * @param logicTableNames logic table names
     * @return logic tables is all belong to default data source
     */
    public boolean isAllInDefaultDataSource(final Collection<String> logicTableNames) {
        if (!hasDefaultDataSourceName()) {
            return false;
        }
        for (String each : logicTableNames) {
            if (findTableRule(each).isPresent() || isBroadcastTable(each)) {
                return false;
            }
        }
        return !logicTableNames.isEmpty();
    }
    
    /**
     * Judge if there is at least one table rule for logic tables.
     *
     * @param logicTableNames logic table names
     * @return whether a table rule exists for logic tables
     */
    public boolean tableRuleExists(final Collection<String> logicTableNames) {
        for (String each : logicTableNames) {
            if (findTableRule(each).isPresent() || isBroadcastTable(each)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge is sharding column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return is sharding column or not
     */
    public boolean isShardingColumn(final String columnName, final String tableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(tableName) && isShardingColumn(each, columnName)) {
                return true;
            }
        }
        return false;
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
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName) && each.getGenerateKeyColumn().isPresent()) {
                return each.getGenerateKeyColumn();
            }
        }
        return Optional.empty();
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
        return Optional.ofNullable(tableRule.get().getShardingKeyGenerator()).orElse(defaultShardingKeyGenerator).generateKey();
    }
    
    /**
     * Get logic table names based on actual table name.
     *
     * @param actualTableName actual table name
     * @return logic table name
     */
    public Collection<String> getLogicTableNames(final String actualTableName) {
        Collection<String> result = new LinkedList<>();
        for (TableRule each : tableRules) {
            if (each.isExisted(actualTableName)) {
                result.add(each.getLogicTable());
            }
        }
        return result;
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
        for (DataNode each : tableRule.getActualDataNodes()) {
            if (shardingDataSourceNames.getDataSourceNames().contains(each.getDataSourceName()) && each.getDataSourceName().equals(dataSourceName)) {
                return each;
            }
        }
        throw new ShardingSphereConfigurationException("Cannot find actual data node for data source name: '%s' and logic table name: '%s'", dataSourceName, logicTableName);
    }
    
    /**
     * Judge if default data source mame exists.
     *
     * @return if default data source name exists
     */
    public boolean hasDefaultDataSourceName() {
        String defaultDataSourceName = shardingDataSourceNames.getDefaultDataSourceName();
        return !Strings.isNullOrEmpty(defaultDataSourceName);
    }
    
    /**
     * Find actual default data source name.
     *
     * <p>If use master-slave rule, return master data source name.</p>
     *
     * @return actual default data source name
     */
    public Optional<String> findActualDefaultDataSourceName() {
        String defaultDataSourceName = shardingDataSourceNames.getDefaultDataSourceName();
        if (Strings.isNullOrEmpty(defaultDataSourceName)) {
            return Optional.empty();
        }
        Optional<String> masterDefaultDataSourceName = findMasterDataSourceName(defaultDataSourceName);
        return masterDefaultDataSourceName.isPresent() ? masterDefaultDataSourceName : Optional.of(defaultDataSourceName);
    }
    
    private Optional<String> findMasterDataSourceName(final String masterSlaveRuleName) {
        for (MasterSlaveRule each : masterSlaveRules) {
            if (each.getName().equals(masterSlaveRuleName)) {
                return Optional.of(each.getMasterDataSourceName());
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find master slave rule.
     *
     * @param dataSourceName data source name
     * @return master slave rule
     */
    public Optional<MasterSlaveRule> findMasterSlaveRule(final String dataSourceName) {
        for (MasterSlaveRule each : masterSlaveRules) {
            if (each.containDataSourceName(dataSourceName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get sharding logic table names.
     *
     * @param logicTableNames logic table names
     * @return sharding logic table names
     */
    public Collection<String> getShardingLogicTableNames(final Collection<String> logicTableNames) {
        Collection<String> result = new LinkedList<>();
        for (String each : logicTableNames) {
            if (findTableRule(each).isPresent()) {
                result.add(each);
            }
        }
        return result;
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
        findBindingTableRule(logicTable).ifPresent(
            bindingTableRule -> result.putAll(bindingTableRule.getLogicAndActualTables(dataSourceName, logicTable, actualTable, availableLogicBindingTables)));
        return result;
    }
}

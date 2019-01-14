/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.rule;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.shardingsphere.api.config.KeyGeneratorConfiguration;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.keygen.generator.KeyGenerator;
import io.shardingsphere.core.keygen.generator.SnowflakeKeyGenerator;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.routing.strategy.ShardingStrategy;
import io.shardingsphere.core.routing.strategy.ShardingStrategyFactory;
import io.shardingsphere.core.routing.strategy.none.NoneShardingStrategy;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Databases and tables sharding rule configuration.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@Getter
public class ShardingRule {
    
    private final ShardingRuleConfiguration shardingRuleConfig;
    
    private final ShardingDataSourceNames shardingDataSourceNames;
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules;
    
    private final Collection<String> broadcastTables;
    
    private final ShardingStrategy defaultDatabaseShardingStrategy;
    
    private final ShardingStrategy defaultTableShardingStrategy;
    
    private final KeyGenerator defaultKeyGenerator;
    
    private final Collection<MasterSlaveRule> masterSlaveRules;
    
    public ShardingRule(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames) {
        Preconditions.checkArgument(!dataSourceNames.isEmpty(), "Data sources cannot be empty.");
        this.shardingRuleConfig = shardingRuleConfig;
        shardingDataSourceNames = new ShardingDataSourceNames(shardingRuleConfig, dataSourceNames);
        tableRules = createTableRules(shardingRuleConfig.getTableRuleConfigs());
        bindingTableRules = createBindingTableRules(shardingRuleConfig.getBindingTableGroups());
        broadcastTables = shardingRuleConfig.getBroadcastTables();
        defaultDatabaseShardingStrategy = createDefaultShardingStrategy(shardingRuleConfig.getDefaultDatabaseShardingStrategyConfig());
        defaultTableShardingStrategy = createDefaultShardingStrategy(shardingRuleConfig.getDefaultTableShardingStrategyConfig());
        defaultKeyGenerator = createDefaultKeyGenerator(shardingRuleConfig.getDefaultKeyGeneratorConfig());
        masterSlaveRules = createMasterSlaveRules(shardingRuleConfig.getMasterSlaveRuleConfigs());
    }
    
    private Collection<TableRule> createTableRules(final Collection<TableRuleConfiguration> tableRuleConfigurations) {
        Collection<TableRule> result = new ArrayList<>(tableRuleConfigurations.size());
        for (TableRuleConfiguration each : tableRuleConfigurations) {
            result.add(new TableRule(each, shardingDataSourceNames));
        }
        return result;
    }
    
    private Collection<BindingTableRule> createBindingTableRules(final Collection<String> bindingTableGroups) {
        Collection<BindingTableRule> result = new ArrayList<>(bindingTableGroups.size());
        for (String each : bindingTableGroups) {
            result.add(createBindingTableRule(each));
        }
        return result;
    }
    
    private BindingTableRule createBindingTableRule(final String bindingTableGroup) {
        List<TableRule> tableRules = new LinkedList<>();
        for (String each : Splitter.on(",").trimResults().splitToList(bindingTableGroup)) {
            tableRules.add(getTableRule(each));
        }
        return new BindingTableRule(tableRules);
    }
    
    private ShardingStrategy createDefaultShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        return null == shardingStrategyConfiguration ? new NoneShardingStrategy() : ShardingStrategyFactory.newInstance(shardingStrategyConfiguration);
    }
    
    private KeyGenerator createDefaultKeyGenerator(final KeyGeneratorConfiguration keyGeneratorConfiguration) {
        return null == keyGeneratorConfiguration ? new SnowflakeKeyGenerator() : keyGeneratorConfiguration.getKeyGenerator();
    }
    
    private Collection<MasterSlaveRule> createMasterSlaveRules(final Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigurations) {
        Collection<MasterSlaveRule> result = new ArrayList<>(masterSlaveRuleConfigurations.size());
        for (MasterSlaveRuleConfiguration each : masterSlaveRuleConfigurations) {
            result.add(new MasterSlaveRule(each));
        }
        return result;
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
        return Optional.absent();
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
        return Optional.absent();
    }
    
    /**
     * Get table rule though logic table name.
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
        throw new ShardingConfigurationException("Cannot find table rule and default data source with logic table: '%s'", logicTableName);
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
     * Judge logic tables is all belong to binding tables.
     *
     * @param logicTableNames logic table names
     * @return logic tables is all belong to binding tables or not
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
        return Optional.absent();
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
        return Optional.absent();
    }
    
    /**
     * Judge logic tables is all belong to broadcast tables.
     *
     * @param logicTableNames  logic table names
     * @return logic tables is all belong to broadcast tables or not
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
        for (String each : logicTableNames) {
            if (findTableRule(each).isPresent() || isBroadcastTable(each)) {
                return false;
            }
        }
        return !logicTableNames.isEmpty();
    }
    
    /**
     * Judge is sharding column or not.
     *
     * @param column column object
     * @return is sharding column or not
     */
    public boolean isShardingColumn(final Column column) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(column.getTableName()) && isShardingColumn(each, column)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isShardingColumn(final TableRule tableRule, final Column column) {
        return getDatabaseShardingStrategy(tableRule).getShardingColumns().contains(column.getName()) || getTableShardingStrategy(tableRule).getShardingColumns().contains(column.getName());
    }
    
    /**
     * Get column of generated key.
     *
     * @param logicTableName logic table name
     * @return generated key's column
     */
    public Optional<Column> getGenerateKeyColumn(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName) && null != each.getGenerateKeyColumn()) {
                return Optional.of(new Column(each.getGenerateKeyColumn(), logicTableName));
            }
        }
        return Optional.absent();
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
            throw new ShardingConfigurationException("Cannot find strategy for generate keys.");
        }
        KeyGenerator keyGenerator = null == tableRule.get().getKeyGenerator() ? defaultKeyGenerator : tableRule.get().getKeyGenerator();
        return keyGenerator.generateKey();
    }
    
    /**
     * Get logic table name base on logic index name.
     *
     * @param logicIndexName logic index name
     * @return logic table name
     */
    public String getLogicTableName(final String logicIndexName) {
        for (TableRule each : tableRules) {
            if (logicIndexName.equals(each.getLogicIndex())) {
                return each.getLogicTable();
            }
        }
        throw new ShardingConfigurationException("Cannot find logic table name with logic index name: '%s'", logicIndexName);
    }
    
    /**
     * Find data node by logic table name.
     *
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode findDataNode(final String logicTableName) {
        return findDataNode(null, logicTableName);
    }
    
    /**
     * Find data node by data source and logic table.
     *
     * @param dataSourceName data source name
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode findDataNode(final String dataSourceName, final String logicTableName) {
        TableRule tableRule = getTableRule(logicTableName);
        for (DataNode each : tableRule.getActualDataNodes()) {
            if (shardingDataSourceNames.getDataSourceNames().contains(each.getDataSourceName()) && (null == dataSourceName || each.getDataSourceName().equals(dataSourceName))) {
                return each;
            }
        }
        if (null == dataSourceName) {
            throw new ShardingConfigurationException("Cannot find actual data node for logic table name: '%s'", logicTableName);
        } else {
            throw new ShardingConfigurationException("Cannot find actual data node for data source name: '%s' and logic table name: '%s'", dataSourceName, logicTableName);
        }
    }
    
    /**
     * Judge is logic index or not.
     *
     * @param logicIndexName logic index name
     * @param logicTableName logic table name
     * @return is logic index or not
     */
    public boolean isLogicIndex(final String logicIndexName, final String logicTableName) {
        return logicIndexName.equals(getTableRule(logicTableName).getLogicIndex());
    }
    
    /**
     * Find actual default data source name.
     *
     * <p>If use master-slave rule, return master data source name.</p>
     *
     * @return actual default data source name
     */
    public Optional<String> findActualDefaultDataSourceName() {
        String result = shardingDataSourceNames.getDefaultDataSourceName();
        if (Strings.isNullOrEmpty(result)) {
            return Optional.absent();
        }
        return Optional.of(getMasterDataSourceName(result));
    }
    
    private String getMasterDataSourceName(final String masterSlaveRuleName) {
        for (MasterSlaveRule each : masterSlaveRules) {
            if (each.getName().equals(masterSlaveRuleName)) {
                return each.getMasterDataSourceName();
            }
        }
        return masterSlaveRuleName;
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
        return Optional.absent();
    }
    
    /**
     * Get actual data source name by actual table name.
     *
     * @param actualTableName actual table name
     * @return actual data source name
     */
    public String getActualDataSourceNameByActualTableName(final String actualTableName) {
        Optional<TableRule> tableRule = findTableRuleByActualTable(actualTableName);
        if (tableRule.isPresent()) {
            return tableRule.get().getActualDatasourceNames().iterator().next();
        }
        if (!Strings.isNullOrEmpty(shardingDataSourceNames.getDefaultDataSourceName())) {
            return shardingDataSourceNames.getDefaultDataSourceName();
        }
        throw new ShardingException("Cannot found actual data source name of '%s' in sharding rule.", actualTableName);
    }
    
    /**
     * Judge contains table in sharding rule.
     * 
     * @param logicTableName logic table name
     * @return contains table in sharding rule or not
     */
    public boolean contains(final String logicTableName) {
        return findTableRule(logicTableName).isPresent() || findBindingTableRule(logicTableName).isPresent() || isBroadcastTable(logicTableName);
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
            Optional<TableRule> tableRule = findTableRule(each);
            if (tableRule.isPresent()) {
                result.add(each);
            }
        }
        return result;
    }
}

/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.rule;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.exception.ShardingConfigurationException;
import io.shardingjdbc.core.keygen.DefaultKeyGenerator;
import io.shardingjdbc.core.keygen.KeyGenerator;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import io.shardingjdbc.core.routing.strategy.ShardingStrategyFactory;
import io.shardingjdbc.core.routing.strategy.none.NoneShardingStrategy;
import io.shardingjdbc.core.util.StringUtil;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Databases and tables sharding rule configuration.
 * 
 * @author zhangliang
 */
@Getter
public final class ShardingRule {
    
    private final Collection<String> dataSourceNames;
    
    private final String defaultDataSourceName;
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules = new LinkedList<>();
    
    private final ShardingStrategy defaultDatabaseShardingStrategy;
    
    private final ShardingStrategy defaultTableShardingStrategy;
    
    private final KeyGenerator defaultKeyGenerator;
    
    public ShardingRule(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames) {
        Preconditions.checkNotNull(dataSourceNames, "Data sources cannot be null.");
        Preconditions.checkArgument(!dataSourceNames.isEmpty(), "Data sources cannot be empty.");
        this.dataSourceNames = new LinkedHashSet<>(dataSourceNames);
        defaultDataSourceName = getDefaultDataSourceName(dataSourceNames, shardingRuleConfig.getDefaultDataSourceName());
        tableRules = new LinkedList<>();
        for (TableRuleConfiguration each : shardingRuleConfig.getTableRuleConfigs()) {
            tableRules.add(new TableRule(each, dataSourceNames));
        }
        for (String group : shardingRuleConfig.getBindingTableGroups()) {
            List<TableRule> tableRulesForBinding = new LinkedList<>();
            for (String logicTableNameForBindingTable : StringUtil.splitWithComma(group)) {
                tableRulesForBinding.add(getTableRule(logicTableNameForBindingTable));
            }
            bindingTableRules.add(new BindingTableRule(tableRulesForBinding));
        }
        defaultDatabaseShardingStrategy = null == shardingRuleConfig.getDefaultDatabaseShardingStrategyConfig()
                ? new NoneShardingStrategy() : ShardingStrategyFactory.newInstance(shardingRuleConfig.getDefaultDatabaseShardingStrategyConfig());
        defaultTableShardingStrategy = null == shardingRuleConfig.getDefaultTableShardingStrategyConfig()
                ? new NoneShardingStrategy() : ShardingStrategyFactory.newInstance(shardingRuleConfig.getDefaultTableShardingStrategyConfig());
        defaultKeyGenerator = null == shardingRuleConfig.getDefaultKeyGenerator() ? new DefaultKeyGenerator() : shardingRuleConfig.getDefaultKeyGenerator();
    }
    
    private String getDefaultDataSourceName(final Collection<String> dataSourceNames, final String defaultDataSourceName) {
        if (1 == dataSourceNames.size()) {
            return dataSourceNames.iterator().next();
        }
        if (Strings.isNullOrEmpty(defaultDataSourceName)) {
            return null;
        }
        return defaultDataSourceName;
    }
    
    /**
     * Try to find table rule though logic table name.
     * 
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TableRule> tryFindTableRuleByLogicTable(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equals(logicTableName.toLowerCase())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Try to find table rule though actual table name.
     *
     * @param actualTableName actual table name
     * @return table rule
     */
    public Optional<TableRule> tryFindTableRuleByActualTable(final String actualTableName) {
        for (TableRule each : tableRules) {
            if (each.isExisted(actualTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Find table rule though logic table name.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public TableRule getTableRule(final String logicTableName) {
        Optional<TableRule> tableRule = tryFindTableRuleByLogicTable(logicTableName.toLowerCase());
        if (tableRule.isPresent()) {
            return tableRule.get();
        }
        if (null != defaultDataSourceName) {
            return createTableRuleWithDefaultDataSource(logicTableName.toLowerCase());
        }
        throw new ShardingConfigurationException("Cannot find table rule and default data source with logic table: '%s'", logicTableName);
    }
    
    private TableRule createTableRuleWithDefaultDataSource(final String logicTableName) {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable(logicTableName);
        return new TableRule(tableRuleConfig, Collections.singletonList(defaultDataSourceName));
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
     * Adjust logic tables is all belong to binding tables.
     *
     * @param logicTables names of logic tables
     * @return logic tables is all belong to binding tables or not
     */
    public boolean isAllBindingTables(final Collection<String> logicTables) {
        if (logicTables.isEmpty()) {
            return false;
        }
        Optional<BindingTableRule> bindingTableRule = findBindingTableRule(logicTables);
        if (!bindingTableRule.isPresent()) {
            return false;
        }
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(bindingTableRule.get().getAllLogicTables());
        return !result.isEmpty() && result.containsAll(logicTables);
    }
    
    /**
     * Adjust logic tables is all belong to default data source.
     *
     * @param logicTables names of logic tables
     * @return logic tables is all belong to default data source
     */
    public boolean isAllInDefaultDataSource(final Collection<String> logicTables) {
        for (String each : logicTables) {
            if (tryFindTableRuleByLogicTable(each).isPresent()) {
                return false;
            }
        }
        return !logicTables.isEmpty();
    }
    
    private Optional<BindingTableRule> findBindingTableRule(final Collection<String> logicTables) {
        for (String each : logicTables) {
            Optional<BindingTableRule> result = findBindingTableRule(each);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get binding table rule via logic table name.
     *
     * @param logicTable logic table name
     * @return binding table rule
     */
    public Optional<BindingTableRule> findBindingTableRule(final String logicTable) {
        for (BindingTableRule each : bindingTableRules) {
            if (each.hasLogicTable(logicTable)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Adjust is sharding column or not.
     *
     * @param column column object
     * @return is sharding column or not
     */
    public boolean isShardingColumn(final Column column) {
        if (defaultDatabaseShardingStrategy.getShardingColumns().contains(column.getName()) || defaultTableShardingStrategy.getShardingColumns().contains(column.getName())) {
            return true;
        }
        for (TableRule each : tableRules) {
            if (!each.getLogicTable().equalsIgnoreCase(column.getTableName())) {
                continue;
            }
            if (null != each.getDatabaseShardingStrategy() && each.getDatabaseShardingStrategy().getShardingColumns().contains(column.getName())) {
                return true;
            }
            if (null != each.getTableShardingStrategy() && each.getTableShardingStrategy().getShardingColumns().contains(column.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * get generated key's column name.
     * 
     * @param logicTableName logic table name
     * @return generated key's column name
     */
    public Optional<String> getGenerateKeyColumn(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName)) {
                return Optional.fromNullable(each.getGenerateKeyColumn());
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
    public Number generateKey(final String logicTableName) {
        Optional<TableRule> tableRule = tryFindTableRuleByLogicTable(logicTableName);
        if (!tableRule.isPresent()) {
            throw new ShardingConfigurationException("Cannot find strategy for generate keys.");
        }
        if (null != tableRule.get().getKeyGenerator()) {
            return tableRule.get().getKeyGenerator().generateKey();
        }
        return defaultKeyGenerator.generateKey();
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
     * Find data node by logic table.
     * 
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode findDataNodeByLogicTable(final String logicTableName) {
        TableRule tableRule = getTableRule(logicTableName);
        for (DataNode each : tableRule.getActualDataNodes()) {
            if (dataSourceNames.contains(each.getDataSourceName())) {
                return each;
            }
        }
        throw new ShardingConfigurationException("Cannot find actual data node for logic table name: '%s'", tableRule.getLogicTable());
    }
    
    /**
     * Adjust is logic index or not.
     *
     * @param logicIndexName logic index name
     * @param logicTableName logic table name
     * @return is logic index or not
     */
    public boolean isLogicIndex(final String logicIndexName, final String logicTableName) {
        return logicIndexName.equals(getTableRule(logicTableName).getLogicIndex());
    }
}

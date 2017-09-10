/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.rule;

import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.none.NoneShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.util.StringUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Databases and tables sharding rule configuration.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class ShardingRule {
    
    private final DataSourceRule dataSourceRule;
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules = new LinkedList<>();
    
    private final ShardingStrategy defaultDatabaseShardingStrategy;
    
    private final ShardingStrategy defaultTableShardingStrategy;
    
    private final KeyGenerator defaultKeyGenerator;
    
    public ShardingRule(final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules, final Collection<String> bindingTableGroups, 
                        final ShardingStrategy defaultDatabaseShardingStrategy, final ShardingStrategy defaultTableShardingStrategy, final KeyGenerator defaultKeyGenerator) {
        this.dataSourceRule = dataSourceRule;
        this.tableRules = tableRules;
        for (String group : bindingTableGroups) {
            List<TableRule> tableRulesForBinding = new LinkedList<>();
            for (String logicTableNameForBindingTable : StringUtil.splitWithComma(group)) {
                tableRulesForBinding.add(getTableRule(logicTableNameForBindingTable));
            }
            this.bindingTableRules.add(new BindingTableRule(tableRulesForBinding));
        }
        this.defaultDatabaseShardingStrategy = null == defaultDatabaseShardingStrategy ? new NoneShardingStrategy() : defaultDatabaseShardingStrategy;
        this.defaultTableShardingStrategy = null == defaultTableShardingStrategy ? new NoneShardingStrategy() : defaultTableShardingStrategy;
        this.defaultKeyGenerator = defaultKeyGenerator;
    }
    
    /**
     * Try to find table rule though logic table name.
     * 
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TableRule> tryFindTableRule(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(logicTableName)) {
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
        Optional<TableRule> tableRule = tryFindTableRule(logicTableName);
        if (tableRule.isPresent()) {
            return tableRule.get();
        }
        if (dataSourceRule.getDefaultDataSource().isPresent()) {
            return createTableRuleWithDefaultDataSource(logicTableName);
        }
        throw new ShardingJdbcException("Cannot find table rule and default data source with logic table: '%s'", logicTableName);
    }
    
    private TableRule createTableRuleWithDefaultDataSource(final String logicTableName) {
        Map<String, DataSource> defaultDataSourceMap = new HashMap<>(1, 1);
        defaultDataSourceMap.put(dataSourceRule.getDefaultDataSourceName(), dataSourceRule.getDefaultDataSource().get());
        TableRuleConfig config = new TableRuleConfig();
        config.setLogicTable(logicTableName);
        config.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfig());
        config.setTableShardingStrategyConfig(new NoneShardingStrategyConfig());
        return new TableRule(logicTableName, false, null, null, defaultDataSourceMap, null, null, null, null);
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
        Collection<String> bindingTables = filterAllBindingTables(logicTables);
        return !bindingTables.isEmpty() && bindingTables.containsAll(logicTables);
    }
    
    /**
     * Filter all binding tables.
     * 
     * @param logicTables names of logic tables
     * @return names for filtered binding tables
     */
    public Collection<String> filterAllBindingTables(final Collection<String> logicTables) {
        if (logicTables.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<BindingTableRule> bindingTableRule = findBindingTableRule(logicTables);
        if (!bindingTableRule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<String> result = new ArrayList<>(bindingTableRule.get().getAllLogicTables());
        result.retainAll(logicTables);
        return result;
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
        Optional<TableRule> tableRule = tryFindTableRule(logicTableName);
        if (!tableRule.isPresent()) {
            throw new ShardingJdbcException("Cannot find strategy for generate keys.");
        }
        if (null != tableRule.get().getKeyGenerator()) {
            return tableRule.get().getKeyGenerator().generateKey();
        }
        return defaultKeyGenerator.generateKey();
    }
}

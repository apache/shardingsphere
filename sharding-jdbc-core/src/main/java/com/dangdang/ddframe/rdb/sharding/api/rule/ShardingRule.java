/**
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

package com.dangdang.ddframe.rdb.sharding.api.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分库分表规则配置对象.
 * 
 * @author zhangliang
 */
@AllArgsConstructor
@Getter
public final class ShardingRule {
    
    private final DataSourceRule dataSourceRule;
    
    private final Collection<TableRule> tableRules;
    
    private Collection<BindingTableRule> bindingTableRules;
    
    private DatabaseShardingStrategy databaseShardingStrategy;
    
    private TableShardingStrategy tableShardingStrategy;
    
    public ShardingRule(final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules) {
        this(dataSourceRule, tableRules, Collections.<BindingTableRule>emptyList(),
                new DatabaseShardingStrategy(Collections.<String>emptyList(), new NoneDatabaseShardingAlgorithm()), 
                new TableShardingStrategy(Collections.<String>emptyList(), new NoneTableShardingAlgorithm()));
    }
    
    public ShardingRule(final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules, final Collection<BindingTableRule> bindingTableRules) {
        this(dataSourceRule, tableRules, bindingTableRules, 
                new DatabaseShardingStrategy(Collections.<String>emptyList(), new NoneDatabaseShardingAlgorithm()), 
                new TableShardingStrategy(Collections.<String>emptyList(), new NoneTableShardingAlgorithm()));
    }
    
    public ShardingRule(final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules, final DatabaseShardingStrategy databaseShardingStrategy) {
        this(dataSourceRule, tableRules, Collections.<BindingTableRule>emptyList(), 
                databaseShardingStrategy, new TableShardingStrategy(Collections.<String>emptyList(), new NoneTableShardingAlgorithm()));
    }
    
    public ShardingRule(final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules, final TableShardingStrategy tableShardingStrategy) {
        this(dataSourceRule, tableRules, Collections.<BindingTableRule>emptyList(), 
                new DatabaseShardingStrategy(Collections.<String>emptyList(), new NoneDatabaseShardingAlgorithm()), tableShardingStrategy);
    }
    
    public ShardingRule(final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules, 
            final DatabaseShardingStrategy databaseShardingStrategy, final TableShardingStrategy tableShardingStrategy) {
        this(dataSourceRule, tableRules, Collections.<BindingTableRule>emptyList(), databaseShardingStrategy, tableShardingStrategy);
    }
    
    /**
     * 根据逻辑表名称查找分片规则.
     * 
     * @param logicTableName 逻辑表名称
     * @return 该逻辑表的分片规则
     */
    public Optional<TableRule> findTableRule(final String logicTableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equals(logicTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * 获取数据库分片策略.
     * 
     * <p>
     * 根据表规则配置对象获取分片策略, 如果获取不到则获取默认分片策略.
     * </p>
     * 
     * @param tableRule 表规则配置对象
     * @return 数据库分片策略
     */
    public DatabaseShardingStrategy getDatabaseShardingStrategy(final TableRule tableRule) {
        DatabaseShardingStrategy result = tableRule.getDatabaseShardingStrategy();
        if (null == result) {
            result = databaseShardingStrategy;
        }
        Preconditions.checkNotNull(result, "no database sharding strategy");
        return result;
    }
    
    /**
     * 获取表分片策略.
     * 
     * <p>
     * 根据表规则配置对象获取分片策略, 如果获取不到则获取默认分片策略.
     * </p>
     * 
     * @param tableRule 表规则配置对象
     * @return 表分片策略
     */
    public TableShardingStrategy getTableShardingStrategy(final TableRule tableRule) {
        TableShardingStrategy result = tableRule.getTableShardingStrategy();
        if (null == result) {
            result = tableShardingStrategy;
        }
        Preconditions.checkNotNull(result, "no table sharding strategy");
        return result;
    }
    
    /**
     * 根据逻辑表名称获取binding表配置的逻辑表名称集合.
     * 
     * @param logicTable 逻辑表名称
     * @return binding表配置的逻辑表名称集合
     */
    public Optional<BindingTableRule> getBindingTableRule(final String logicTable) {
        if (null == bindingTableRules) {
            return Optional.absent();
        }
        for (BindingTableRule each : bindingTableRules) {
            if (each.hasLogicTable(logicTable)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * 过滤出所有的Binding表名称.
     * 
     * @param logicTables 逻辑表名称集合
     * @return 所有的Binding表名称集合
     */
    public Collection<String> filterAllBindingTables(final Collection<String> logicTables) {
        if (logicTables.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<BindingTableRule> bindingTableRule = Optional.absent();
        for (String each : logicTables) {
            bindingTableRule = getBindingTableRule(each);
            if (bindingTableRule.isPresent()) {
                break;
            }
        }
        if (!bindingTableRule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<String> result = new ArrayList<>(bindingTableRule.get().getAllLogicTables());
        result.retainAll(logicTables);
        return result;
    }
    
    /**
     * 判断逻辑表名称集合是否全部属于Binding表.
     * 
     * @param logicTables 逻辑表名称集合
     * @return 是否全部属于Binding表
     */
    public boolean isAllBindingTable(final Collection<String> logicTables) {
        Collection<String> bindingTables = filterAllBindingTables(logicTables);
        return !bindingTables.isEmpty() && bindingTables.containsAll(logicTables);
    }
    
    /**
     * 获取所有的分片列名.
     *
     * @return 分片列名集合
     */
    // TODO 目前使用分片列名称, 为了进一步提升解析性能，应考虑使用表名 + 列名
    public Collection<String> getAllShardingColumns() {
        Set<String> result = new HashSet<>();
        if (null != databaseShardingStrategy) {
            result.addAll(databaseShardingStrategy.getShardingColumns());
        }
        if (null != tableShardingStrategy) {
            result.addAll(tableShardingStrategy.getShardingColumns());
        }
        for (TableRule each : tableRules) {
            if (null != each.getDatabaseShardingStrategy()) {
                result.addAll(each.getDatabaseShardingStrategy().getShardingColumns());
            }
            if (null != each.getTableShardingStrategy()) {
                result.addAll(each.getTableShardingStrategy().getShardingColumns());
            }
        }
        return result;
    }
}

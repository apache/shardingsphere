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

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.keygen.DefaultKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGeneratorFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Column;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 分库分表规则配置对象.
 * 
 * @author zhangliang
 */
@Getter
public final class ShardingRule {
    
    private final DataSourceRule dataSourceRule;
    
    private final Collection<TableRule> tableRules;
    
    private final Collection<BindingTableRule> bindingTableRules;
    
    private final DatabaseShardingStrategy databaseShardingStrategy;
    
    private final TableShardingStrategy tableShardingStrategy;
    
    @Getter(AccessLevel.NONE)
    private final KeyGenerator keyGenerator;
    
    @Getter(AccessLevel.NONE)
    private final KeyGenerator defaultGenerator;
    
    /**
     * 全属性构造器.
     * 
     * <p>用于Spring非命名空间的配置.</p>
     * 
     * <p>未来将改为private权限, 不在对外公开, 不建议使用非Spring命名空间的配置.</p>
     * 
     * @deprecated 未来将改为private权限, 不在对外公开, 不建议使用非Spring命名空间的配置.
     */
    @Deprecated
    public ShardingRule(
            final DataSourceRule dataSourceRule, final Collection<TableRule> tableRules, final Collection<BindingTableRule> bindingTableRules, 
            final DatabaseShardingStrategy databaseShardingStrategy, final TableShardingStrategy tableShardingStrategy, final KeyGenerator keyGenerator) {
        Preconditions.checkNotNull(dataSourceRule);
        this.dataSourceRule = dataSourceRule;
        this.tableRules = null == tableRules ? Collections.<TableRule>emptyList() : tableRules;
        this.bindingTableRules = null == bindingTableRules ? Collections.<BindingTableRule>emptyList() : bindingTableRules;
        this.databaseShardingStrategy = null == databaseShardingStrategy ? new DatabaseShardingStrategy(
                Collections.<String>emptyList(), new NoneDatabaseShardingAlgorithm()) : databaseShardingStrategy;
        this.tableShardingStrategy = null == tableShardingStrategy ? new TableShardingStrategy(
                Collections.<String>emptyList(), new NoneTableShardingAlgorithm()) : tableShardingStrategy;
        this.keyGenerator = keyGenerator;
        defaultGenerator = KeyGeneratorFactory.createKeyGenerator(DefaultKeyGenerator.class);
    }
    
    /**
     * 获取表规则配置对象构建器.
     *
     * @return 分片规则配置对象构建器
     */
    public static ShardingRuleBuilder builder() {
        return new ShardingRuleBuilder();
    }
    
    /**
     * 试着根据逻辑表名称查找分片规则.
     * 
     * @param logicTableName 逻辑表名称
     * @return 该逻辑表的分片规则
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
     * 根据逻辑表名称查找分片规则.
     *
     * @param logicTableName 逻辑表名称
     * @return 该逻辑表的分片规则
     */
    public TableRule getTableRule(final String logicTableName) {
        Optional<TableRule> tableRule = tryFindTableRule(logicTableName);
        if (tableRule.isPresent()) {
            return tableRule.get();
        }
        if (dataSourceRule.getDefaultDataSource().isPresent()) {
            return createTableRuleWithDefaultDataSource(logicTableName, dataSourceRule);
        }
        throw new ShardingJdbcException("Cannot find table rule and default data source with logic table: '%s'", logicTableName);
    }
    
    private TableRule createTableRuleWithDefaultDataSource(final String logicTableName, final DataSourceRule defaultDataSourceRule) {
        Map<String, DataSource> defaultDataSourceMap = new HashMap<>(1);
        defaultDataSourceMap.put(defaultDataSourceRule.getDefaultDataSourceName(), defaultDataSourceRule.getDefaultDataSource().get());
        return TableRule.builder(logicTableName)
                .dataSourceRule(new DataSourceRule(defaultDataSourceMap))
                .databaseShardingStrategy(new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy("", new NoneTableShardingAlgorithm())).build();
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
        return null == tableRule.getDatabaseShardingStrategy() ? databaseShardingStrategy : tableRule.getDatabaseShardingStrategy();
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
        return null == tableRule.getTableShardingStrategy() ? tableShardingStrategy : tableRule.getTableShardingStrategy();
    }
    
    /**
     * 判断逻辑表名称集合是否全部属于Binding表.
     *
     * @param logicTables 逻辑表名称集合
     * @return 是否全部属于Binding表
     */
    public boolean isAllBindingTables(final Collection<String> logicTables) {
        Collection<String> bindingTables = filterAllBindingTables(logicTables);
        return !bindingTables.isEmpty() && bindingTables.containsAll(logicTables);
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
     * 根据逻辑表名称获取binding表配置的逻辑表名称集合.
     *
     * @param logicTable 逻辑表名称
     * @return binding表配置的逻辑表名称集合
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
     * 判断是否为分片列.
     *
     * @param column 列对象
     * @return 是否为分片列
     */
    public boolean isShardingColumn(final Column column) {
        if (databaseShardingStrategy.getShardingColumns().contains(column.getName()) || tableShardingStrategy.getShardingColumns().contains(column.getName())) {
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
     * 获取自增列名称.
     * 
     * @param tableName 表名称
     * @return 自增列名称
     */
    public Optional<String> getGenerateKeyColumn(final String tableName) {
        for (TableRule each : tableRules) {
            if (each.getLogicTable().equalsIgnoreCase(tableName)) {
                return Optional.fromNullable(each.getGenerateKeyColumn());
            }
        }
        return Optional.absent();
    }
    
    /**
     * 获取自增主键.
     *
     * @return 自增列与主键映射表
     */
    public Number generateKey(final String tableName) {
        Optional<TableRule> tableRule = tryFindTableRule(tableName);
        if (!tableRule.isPresent()) {
            throw new ShardingJdbcException("Cannot find strategy for generate keys.");
        }
        if (null != tableRule.get().getKeyGenerator()) {
            return tableRule.get().getKeyGenerator().generateKey();
        }
        if (null != keyGenerator) {
            return keyGenerator.generateKey();
        }
        return defaultGenerator.generateKey();
    }
    
    /**
     * 分片规则配置对象构建器.
     */
    @RequiredArgsConstructor
    public static class ShardingRuleBuilder {
        
        private DataSourceRule dataSourceRule;
        
        private Collection<TableRule> tableRules;
        
        private Collection<BindingTableRule> bindingTableRules;
        
        private DatabaseShardingStrategy databaseShardingStrategy;
        
        private TableShardingStrategy tableShardingStrategy;
        
        private Class<? extends KeyGenerator> keyGeneratorClass;
        
        /**
         * 构建数据源配置规则.
         *
         * @param dataSourceRule 数据源配置规则
         * @return 分片规则配置对象构建器
         */
        public ShardingRuleBuilder dataSourceRule(final DataSourceRule dataSourceRule) {
            this.dataSourceRule = dataSourceRule;
            return this;
        }
        
        /**
         * 构建表配置规则.
         *
         * @param tableRules 表配置规则
         * @return 分片规则配置对象构建器
         */
        public ShardingRuleBuilder tableRules(final Collection<TableRule> tableRules) {
            this.tableRules = tableRules;
            return this;
        }
        
        /**
         * 构建绑定表配置规则.
         *
         * @param bindingTableRules 绑定表配置规则
         * @return 分片规则配置对象构建器
         */
        public ShardingRuleBuilder bindingTableRules(final Collection<BindingTableRule> bindingTableRules) {
            this.bindingTableRules = bindingTableRules;
            return this;
        }
        
        /**
         * 构建默认分库策略.
         *
         * @param databaseShardingStrategy 默认分库策略
         * @return 分片规则配置对象构建器
         */
        public ShardingRuleBuilder databaseShardingStrategy(final DatabaseShardingStrategy databaseShardingStrategy) {
            this.databaseShardingStrategy = databaseShardingStrategy;
            return this;
        }
        
        /**
         * 构建数据源分片规则.
         *
         * @param tableShardingStrategy 默认分表策略
         * @return 分片规则配置对象构建器
         */
        public ShardingRuleBuilder tableShardingStrategy(final TableShardingStrategy tableShardingStrategy) {
            this.tableShardingStrategy = tableShardingStrategy;
            return this;
        }
    
        /**
         * 构建默认主键生成器.
         * 
         * @param keyGeneratorClass 默认主键生成器
         * @return 分片规则配置对象构建器
         */
        public ShardingRuleBuilder keyGenerator(final Class<? extends KeyGenerator> keyGeneratorClass) {
            this.keyGeneratorClass = keyGeneratorClass;
            return this;
        }
        
        /**
         * 构建分片规则配置对象.
         *
         * @return 分片规则配置对象
         */
        public ShardingRule build() {
            KeyGenerator keyGenerator = null;
            if (null != keyGeneratorClass) {
                keyGenerator = KeyGeneratorFactory.createKeyGenerator(keyGeneratorClass);
            }
            return new ShardingRule(dataSourceRule, tableRules, bindingTableRules, databaseShardingStrategy, tableShardingStrategy, keyGenerator);
        }
    }
}

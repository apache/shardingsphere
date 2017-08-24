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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
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
 * Databases and tables sharding rule configuration.
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
     * Constructs a full properties sharding rule.
     * 
     * <p>Should not use for spring namespace.</p>
     * 
     * @deprecated should be private
     * @param dataSourceRule data source rule
     * @param tableRules table rules
     * @param bindingTableRules binding table rules
     * @param databaseShardingStrategy default database sharding strategy
     * @param tableShardingStrategy default table sharding strategy
     * @param keyGenerator default primary key generator
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
     * Get sharding rule builder.
     *
     * @return sharding rule builder
     */
    public static ShardingRuleBuilder builder() {
        return new ShardingRuleBuilder();
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
     * Get database sharding strategy.
     * 
     * <p>
     * Use default database sharding strategy if not found.
     * </p>
     * 
     * @param tableRule table rule
     * @return database sharding strategy
     */
    public DatabaseShardingStrategy getDatabaseShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getDatabaseShardingStrategy() ? databaseShardingStrategy : tableRule.getDatabaseShardingStrategy();
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
    public TableShardingStrategy getTableShardingStrategy(final TableRule tableRule) {
        return null == tableRule.getTableShardingStrategy() ? tableShardingStrategy : tableRule.getTableShardingStrategy();
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
        if (null != keyGenerator) {
            return keyGenerator.generateKey();
        }
        return defaultGenerator.generateKey();
    }
    
    /**
     * Sharding rule builder.
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
         * Build data source rule.
         *
         * @param dataSourceRule data source rule
         * @return this builder
         */
        public ShardingRuleBuilder dataSourceRule(final DataSourceRule dataSourceRule) {
            this.dataSourceRule = dataSourceRule;
            return this;
        }
        
        /**
         * Build table rules.
         *
         * @param tableRules table rules
         * @return this builder
         */
        public ShardingRuleBuilder tableRules(final Collection<TableRule> tableRules) {
            this.tableRules = tableRules;
            return this;
        }
        
        /**
         * Build binding table rules.
         *
         * @param bindingTableRules binding table rules
         * @return this builder
         */
        public ShardingRuleBuilder bindingTableRules(final Collection<BindingTableRule> bindingTableRules) {
            this.bindingTableRules = bindingTableRules;
            return this;
        }
        
        /**
         * Build default database strategy.
         *
         * @param databaseShardingStrategy default database strategy
         * @return this builder
         */
        public ShardingRuleBuilder databaseShardingStrategy(final DatabaseShardingStrategy databaseShardingStrategy) {
            this.databaseShardingStrategy = databaseShardingStrategy;
            return this;
        }
        
        /**
         * Build default table strategy.
         *
         * @param tableShardingStrategy default table strategy
         * @return this builder
         */
        public ShardingRuleBuilder tableShardingStrategy(final TableShardingStrategy tableShardingStrategy) {
            this.tableShardingStrategy = tableShardingStrategy;
            return this;
        }
    
        /**
         * Build default key generator class.
         * 
         * @param keyGeneratorClass key generator class
         * @return this builder
         */
        public ShardingRuleBuilder keyGenerator(final Class<? extends KeyGenerator> keyGeneratorClass) {
            this.keyGeneratorClass = keyGeneratorClass;
            return this;
        }
        
        /**
         * Build sharding rule.
         *
         * @return built sharding rule
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

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

package com.dangdang.ddframe.rdb.sharding.config.common.api;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.MultipleKeysDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.SingleKeyDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.MultipleKeysTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.GenerateKeyColumnConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.StrategyConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ClosureDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ClosureTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.parser.InlineParser;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.MultipleKeysShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.SingleKeyShardingAlgorithm;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 分片规则构建器.
 * 
 * @author gaohongtao
 */
@AllArgsConstructor
public final class ShardingRuleBuilder {
    
    private final String logRoot;
    
    private final Map<String, DataSource> externalDataSourceMap;
    
    private final ShardingRuleConfig shardingRuleConfig;
    
    public ShardingRuleBuilder(final ShardingRuleConfig shardingRuleConfig) {
        this("default", shardingRuleConfig);
    }
    
    public ShardingRuleBuilder(final String logRoot, final ShardingRuleConfig shardingRuleConfig) {
        this(logRoot, Collections.<String, DataSource>emptyMap(), shardingRuleConfig);
    }
    
    /**
     * 构建分片规则.
     * 
     * @return 分片规则对象
     */
    public ShardingRule build() {
        DataSourceRule dataSourceRule = buildDataSourceRule();
        Collection<TableRule> tableRules = buildTableRules(dataSourceRule);
        com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule.ShardingRuleBuilder shardingRuleBuilder = ShardingRule.builder().dataSourceRule(dataSourceRule);
        if (!Strings.isNullOrEmpty(shardingRuleConfig.getKeyGeneratorClass())) {
            shardingRuleBuilder.keyGenerator(loadClass(shardingRuleConfig.getKeyGeneratorClass(), KeyGenerator.class));
        }
        return shardingRuleBuilder.tableRules(tableRules).bindingTableRules(buildBindingTableRules(tableRules))
                .databaseShardingStrategy(buildShardingStrategy(shardingRuleConfig.getDefaultDatabaseStrategy(), DatabaseShardingStrategy.class))
                .tableShardingStrategy(buildShardingStrategy(shardingRuleConfig.getDefaultTableStrategy(), TableShardingStrategy.class)).build();
    }
    
    private DataSourceRule buildDataSourceRule() {
        Preconditions.checkArgument(!shardingRuleConfig.getDataSource().isEmpty() || MapUtils.isNotEmpty(externalDataSourceMap), "Sharding JDBC: No data source config");
        return shardingRuleConfig.getDataSource().isEmpty() ? new DataSourceRule(externalDataSourceMap, shardingRuleConfig.getDefaultDataSourceName())
                : new DataSourceRule(shardingRuleConfig.getDataSource(), shardingRuleConfig.getDefaultDataSourceName());
    }
    
    private Collection<TableRule> buildTableRules(final DataSourceRule dataSourceRule) {
        Collection<TableRule> result = new ArrayList<>(shardingRuleConfig.getTables().size());
        for (Entry<String, TableRuleConfig> each : shardingRuleConfig.getTables().entrySet()) {
            String logicTable = each.getKey();
            TableRuleConfig tableRuleConfig = each.getValue();
            TableRule.TableRuleBuilder tableRuleBuilder = TableRule.builder(logicTable).dataSourceRule(dataSourceRule)
                    .dynamic(tableRuleConfig.isDynamic())
                    .databaseShardingStrategy(buildShardingStrategy(tableRuleConfig.getDatabaseStrategy(), DatabaseShardingStrategy.class))
                    .tableShardingStrategy(buildShardingStrategy(tableRuleConfig.getTableStrategy(), TableShardingStrategy.class));
            if (null != tableRuleConfig.getActualTables()) {
                tableRuleBuilder.actualTables(new InlineParser(tableRuleConfig.getActualTables()).evaluate());
            }
            if (!Strings.isNullOrEmpty(tableRuleConfig.getDataSourceNames())) {
                tableRuleBuilder.dataSourceNames(new InlineParser(tableRuleConfig.getDataSourceNames()).evaluate());
            }
            buildGenerateKeyColumn(tableRuleBuilder, tableRuleConfig);
            result.add(tableRuleBuilder.build());
        }
        return result;
    }
    
    private void buildGenerateKeyColumn(final TableRule.TableRuleBuilder tableRuleBuilder, final TableRuleConfig tableRuleConfig) {
        for (GenerateKeyColumnConfig each : tableRuleConfig.getGenerateKeyColumns()) {
            if (Strings.isNullOrEmpty(each.getColumnKeyGeneratorClass())) {
                tableRuleBuilder.generateKeyColumn(each.getColumnName());
            } else {
                tableRuleBuilder.generateKeyColumn(each.getColumnName(), loadClass(each.getColumnKeyGeneratorClass(), KeyGenerator.class));
            }
        }
    }
    
    private Collection<BindingTableRule> buildBindingTableRules(final Collection<TableRule> tableRules) {
        Collection<BindingTableRule> result = new ArrayList<>(shardingRuleConfig.getBindingTables().size());
        for (BindingTableRuleConfig each : shardingRuleConfig.getBindingTables()) {
            result.add(new BindingTableRule(Lists.transform(new InlineParser(each.getTableNames()).split(), new Function<String, TableRule>() {
                
                @Override
                public TableRule apply(final String input) {
                    return findTableRuleByLogicTableName(tableRules, input);
                }
            })));
        }
        return result;
    }
    
    private TableRule findTableRuleByLogicTableName(final Collection<TableRule> tableRules, final String logicTableName) {
        for (TableRule each : tableRules) {
            if (logicTableName.equals(each.getLogicTable())) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Sharding JDBC: Binding table `%s` is not an available Table rule", logicTableName));
    }
    
    private <T extends ShardingStrategy> T buildShardingStrategy(final StrategyConfig config, final Class<T> returnClass) {
        if (null == config) {
            return null;
        }
        Preconditions.checkArgument(Strings.isNullOrEmpty(config.getAlgorithmExpression()) && !Strings.isNullOrEmpty(config.getAlgorithmClassName())
                || !Strings.isNullOrEmpty(config.getAlgorithmExpression()) && Strings.isNullOrEmpty(config.getAlgorithmClassName()));
        Preconditions.checkState(returnClass.isAssignableFrom(DatabaseShardingStrategy.class) || returnClass.isAssignableFrom(TableShardingStrategy.class), "Sharding-JDBC: returnClass is illegal");
        List<String> shardingColumns = new InlineParser(config.getShardingColumns()).split();
        if (Strings.isNullOrEmpty(config.getAlgorithmClassName())) {
            return buildShardingAlgorithmExpression(shardingColumns, config.getAlgorithmExpression(), returnClass);
        }
        return buildShardingAlgorithmClassName(shardingColumns, config.getAlgorithmClassName(), returnClass);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ShardingStrategy> T buildShardingAlgorithmExpression(final List<String> shardingColumns, final String algorithmExpression, final Class<T> returnClass) {
        return returnClass.isAssignableFrom(DatabaseShardingStrategy.class) ? (T) new DatabaseShardingStrategy(shardingColumns, new ClosureDatabaseShardingAlgorithm(algorithmExpression, logRoot))
                : (T) new TableShardingStrategy(shardingColumns, new ClosureTableShardingAlgorithm(algorithmExpression, logRoot));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ShardingStrategy> T buildShardingAlgorithmClassName(final List<String> shardingColumns, final String algorithmClassName, final Class<T> returnClass) {
        ShardingAlgorithm shardingAlgorithm;
        try {
            shardingAlgorithm = (ShardingAlgorithm) Class.forName(algorithmClassName).newInstance();
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
        Preconditions.checkState(shardingAlgorithm instanceof SingleKeyShardingAlgorithm || shardingAlgorithm instanceof MultipleKeysShardingAlgorithm, "Sharding-JDBC: algorithmClassName is illegal");
        if (shardingAlgorithm instanceof SingleKeyShardingAlgorithm) {
            Preconditions.checkArgument(1 == shardingColumns.size(), "Sharding-JDBC: SingleKeyShardingAlgorithm must have only ONE sharding column");
            return returnClass.isAssignableFrom(DatabaseShardingStrategy.class) ? (T) new DatabaseShardingStrategy(shardingColumns.get(0), (SingleKeyDatabaseShardingAlgorithm<?>) shardingAlgorithm)
                    : (T) new TableShardingStrategy(shardingColumns.get(0), (SingleKeyTableShardingAlgorithm<?>) shardingAlgorithm);
        }
        return returnClass.isAssignableFrom(DatabaseShardingStrategy.class) ? (T) new DatabaseShardingStrategy(shardingColumns, (MultipleKeysDatabaseShardingAlgorithm) shardingAlgorithm) 
                : (T) new TableShardingStrategy(shardingColumns, (MultipleKeysTableShardingAlgorithm) shardingAlgorithm);
    }
    
    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> loadClass(final String className, final Class<T> superClass) {
        try {
            return (Class<? extends T>) superClass.getClassLoader().loadClass(className);
        } catch (final ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}

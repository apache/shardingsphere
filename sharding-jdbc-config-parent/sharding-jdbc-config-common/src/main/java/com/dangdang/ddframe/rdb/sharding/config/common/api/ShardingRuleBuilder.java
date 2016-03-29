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

package com.dangdang.ddframe.rdb.sharding.config.common.api;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.common.MultipleKeysShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.common.ShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.common.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.common.SingleKeyShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.MultipleKeysDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.SingleKeyDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.MultipleKeysTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.StrategyConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.ConfigUtil;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ClosureDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ClosureTableShardingAlgorithm;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分片规则构建器.
 * 
 * @author gaohongtao
 */
public class ShardingRuleBuilder {
    
    private final String logRoot;
    
    private final Map<String, DataSource> externalDataSourceMap;
    
    private final ShardingRuleConfig shardingRuleConfig;
    
    public ShardingRuleBuilder(final ShardingRuleConfig shardingRuleConfig) {
        logRoot = "default";
        externalDataSourceMap = new HashMap<>();
        this.shardingRuleConfig = shardingRuleConfig;
    }
    
    public ShardingRuleBuilder(final String logRoot, final ShardingRuleConfig shardingRuleConfig) {
        this.logRoot = logRoot;
        externalDataSourceMap = new HashMap<>();
        this.shardingRuleConfig = shardingRuleConfig;
    }
    
    public ShardingRuleBuilder(final String logRoot, final Map<String, DataSource> externalDataSourceMap, final ShardingRuleConfig shardingRuleConfig) {
        this.logRoot = logRoot;
        this.externalDataSourceMap = externalDataSourceMap;
        this.shardingRuleConfig = shardingRuleConfig;
    }
    
    /**
     * 构建分片规则.
     * 
     * @return 分片规则对象
     */
    public ShardingRule build() {
        DataSourceRule dataSourceRule = buildDataSourceRule();
        Collection<TableRule> tableRules = buildTableRule(dataSourceRule);
        return new ShardingRule(dataSourceRule, tableRules, buildBindingTableRule(tableRules),
                buildShardingStrategy(shardingRuleConfig.getDefaultDatabaseStrategy(), DatabaseShardingStrategy.class),
                buildShardingStrategy(shardingRuleConfig.getDefaultTableStrategy(), TableShardingStrategy.class));
    }
    
    private DataSourceRule buildDataSourceRule() {
        Preconditions.checkArgument(!shardingRuleConfig.getDataSource().isEmpty() || MapUtils.isNotEmpty(externalDataSourceMap), "Sharding JDBC: No data source config");
        return !shardingRuleConfig.getDataSource().isEmpty() ? new DataSourceRule(shardingRuleConfig.getDataSource()) : new DataSourceRule(externalDataSourceMap);
    }
    
    private Collection<TableRule> buildTableRule(final DataSourceRule dataSourceRule) {
        Collection<TableRule> result = new ArrayList<>(shardingRuleConfig.getTables().size());
        for (Map.Entry<String, TableRuleConfig> each : shardingRuleConfig.getTables().entrySet()) {
            result.add(new TableRule(each.getKey(), ConfigUtil.transformCommaStringToList(each.getValue().getActualTables()), dataSourceRule,
                    buildShardingStrategy(each.getValue().getDatabaseStrategy(), DatabaseShardingStrategy.class),
                    buildShardingStrategy(each.getValue().getTableStrategy(), TableShardingStrategy.class)));
        }
        return result;
    }
    
    private Collection<BindingTableRule> buildBindingTableRule(final Collection<TableRule> tableRules) {
        Collection<BindingTableRule> result = new ArrayList<>(shardingRuleConfig.getBindingTables().size());
        for (BindingTableRuleConfig each : shardingRuleConfig.getBindingTables()) {
            result.add(new BindingTableRule(Lists.transform(ConfigUtil.transformCommaStringToList(each.getTableNames()), new Function<String, TableRule>() {
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
        throw new IllegalArgumentException("Sharding JDBC: Binding table %s is not an available Table rule");
    }
    
    private <T extends ShardingStrategy> T buildShardingStrategy(final StrategyConfig config, final Class<T> returnClass) {
        if (null == config) {
            return null;
        }
        Preconditions.checkArgument(Strings.isNullOrEmpty(config.getAlgorithmExpression()) && !Strings.isNullOrEmpty(config.getAlgorithmClassName())
                || !Strings.isNullOrEmpty(config.getAlgorithmExpression()) && Strings.isNullOrEmpty(config.getAlgorithmClassName()));
        Preconditions.checkState(returnClass.isAssignableFrom(DatabaseShardingStrategy.class) || returnClass.isAssignableFrom(TableShardingStrategy.class), "Sharding-JDBC: returnClass is illegal");
        List<String> shardingColumns = ConfigUtil.transformCommaStringToList(config.getShardingColumns());
        if (!Strings.isNullOrEmpty(config.getAlgorithmClassName())) {
            return buildClassNameAlgorithmShardingStrategy(shardingColumns, config.getAlgorithmClassName(), returnClass);
        } else {
            return buildExpressionAlgorithmShardingStrategy(shardingColumns, config.getAlgorithmExpression(), returnClass);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ShardingStrategy> T buildClassNameAlgorithmShardingStrategy(final List<String> shardingColumns, final String algorithmClassName, final Class<T> returnClass) {
        ShardingAlgorithm shardingAlgorithm;
        try {
            shardingAlgorithm = (ShardingAlgorithm) Class.forName(algorithmClassName).newInstance();
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        Preconditions.checkState(shardingAlgorithm instanceof SingleKeyShardingAlgorithm || shardingAlgorithm instanceof MultipleKeysShardingAlgorithm, "Sharding-JDBC: algorithmClassName is illegal");
        if (shardingAlgorithm instanceof SingleKeyShardingAlgorithm) {
            Preconditions.checkArgument(shardingColumns.size() == 1, "Sharding-JDBC: SingleKeyShardingAlgorithm must match only ONE shading column");
            return returnClass.isAssignableFrom(DatabaseShardingStrategy.class) ? (T) new DatabaseShardingStrategy(shardingColumns.get(0), (SingleKeyDatabaseShardingAlgorithm<?>) shardingAlgorithm)
                    : (T) new TableShardingStrategy(shardingColumns.get(0), (SingleKeyTableShardingAlgorithm<?>) shardingAlgorithm);
        } else {
            return returnClass.isAssignableFrom(DatabaseShardingStrategy.class) ? (T) new DatabaseShardingStrategy(shardingColumns, (MultipleKeysDatabaseShardingAlgorithm) shardingAlgorithm)
                    : (T) new TableShardingStrategy(shardingColumns, (MultipleKeysTableShardingAlgorithm) shardingAlgorithm);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ShardingStrategy> T buildExpressionAlgorithmShardingStrategy(final List<String> shardingColumns, final String algorithmExpression, final Class<T> returnClass) {
        return returnClass.isAssignableFrom(DatabaseShardingStrategy.class) ? (T) new DatabaseShardingStrategy(shardingColumns, new ClosureDatabaseShardingAlgorithm(algorithmExpression, logRoot))
                : (T) new TableShardingStrategy(shardingColumns, new ClosureTableShardingAlgorithm(algorithmExpression, logRoot));
    }
}

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

package com.dangdang.ddframe.rdb.sharding.config.common.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.common.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.config.common.exception.MissingConfigNodeException;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ClosureDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm.ClosureTableShardingAlgorithm;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import lombok.Getter;
import lombok.Setter;

/**
 * 构建分片策略文件委派.
 * 
 * @author gaohongtao
 */
@Getter
public abstract class AbstractShardingRuleConfigFileDelegate extends Script {
    
    @Setter
    private DataSourceRule dataSourceRule;
    
    private DatabaseShardingStrategy defaultDatabaseShardingStrategy;
    
    private TableShardingStrategy defaultTableShardingStrategy;
    
    private List<TableRule> tableRules = new ArrayList<>();
    
    private List<BindingTableRule> bindingTableRules = new ArrayList<>();
    
    void datasource(final Map<String, DataSource> datasourceMap) {
        this.dataSourceRule = new DataSourceRule(datasourceMap);
    }
    
    DatabaseShardingStrategy databaseStrategy(final List<String> shardingColumns, final Closure<String> algorithm) {
        checkStrategyArgument(shardingColumns, algorithm);
        return new DatabaseShardingStrategy(shardingColumns, new ClosureDatabaseShardingAlgorithm(algorithm));
    }
    
    TableShardingStrategy tableStrategy(final List<String> shardingColumns, final Closure<String> algorithm) {
        checkStrategyArgument(shardingColumns, algorithm);
        return new TableShardingStrategy(shardingColumns, new ClosureTableShardingAlgorithm(algorithm));
    }
    
    private void checkStrategyArgument(final List<String> shardingColumns, final Closure<String> algorithm) {
        Preconditions.checkNotNull(shardingColumns);
        Preconditions.checkArgument(shardingColumns.size() > 0);
        Preconditions.checkNotNull(algorithm);
    }
    
    void defaultStrategy(final ShardingStrategy shardingStrategy) {
        if (shardingStrategy instanceof DatabaseShardingStrategy) {
            defaultDatabaseShardingStrategy = (DatabaseShardingStrategy) shardingStrategy;
        } else if (shardingStrategy instanceof TableShardingStrategy) {
            defaultTableShardingStrategy = (TableShardingStrategy) shardingStrategy;
        }
    }
    
    void table(final CharSequence logicTable, final List actualTables, final DatabaseShardingStrategy databaseShardingStrategy) {
        table(logicTable, actualTables, databaseShardingStrategy, null);
    }
    
    void table(final CharSequence logicTable, final List actualTables, final TableShardingStrategy tableShardingStrategy) {
        table(logicTable, actualTables, null, tableShardingStrategy);
    }
    
    void table(final CharSequence logicTable, final List actualTables) {
        table(logicTable.toString(), actualTables, null, null);
    }
    
    void table(final CharSequence logicTable, final List actualTables, final DatabaseShardingStrategy databaseShardingStrategy, final TableShardingStrategy tableShardingStrategy) {
        Preconditions.checkNotNull(logicTable);
        Preconditions.checkArgument(logicTable.length() > 0);
        Preconditions.checkNotNull(actualTables);
        Preconditions.checkArgument(actualTables.size() > 0);
        
        tableRules.add(new TableRule(logicTable.toString(), ConfigUtil.generateList(actualTables), dataSourceRule, databaseShardingStrategy, tableShardingStrategy));
    }
    
    void bind(final List tableNames) {
        bindingTableRules.add(new BindingTableRule(Lists.transform(ConfigUtil.generateList(tableNames), new Function<String, TableRule>() {
            @Override
            public TableRule apply(final String tableName) {
                for (TableRule each : tableRules) {
                    if (each.getLogicTable().equals(tableName)) {
                        return each;
                    }
                }
                throw new NullPointerException(String.format("can not find table rule with logic table name : %s", tableName));
            }
        })));
    }
    
    /**
     * 调用拦截器.
     *
     * @param name 方法名称
     * @param args 调用方法的参数
     * @return 调用返回值
     */
    @Override
    public Object invokeMethod(final String name, final Object args) {
        try {
            return super.invokeMethod(name, args);
        } catch (final MissingMethodException mme) {
            throw new MissingConfigNodeException(mme, getMissingConfigSuggestion(name));
        }
    }
    
    private String getMissingConfigSuggestion(final String methodName) {
        switch (methodName) {
            case "table":
                return "String logicTable, List actualTables, (Optional)DatabaseShardingStrategy databaseShardingStrategy, (Optional)TableShardingStrategy tableShardingStrategy";
            case "databaseStrategy":
                return "List shardingColumns, {}";
            case "tableStrategy":
                return "List shardingColumns, {}";
            case "defaultStrategy":
                return "databaseStrategy or tableStrategy";
            case "bind":
                return "List tableNames";
            default:
                return "";
        }
    }
    
    /**
     * 该方法将被脚本实现.
     *
     * @return 脚本返回的结果
     */
    @Override
    public abstract Object run();
}

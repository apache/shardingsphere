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

import com.dangdang.ddframe.rdb.sharding.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * Sharding rule builder.
 * 
 * @author gaohongtao
 */
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
    
    public ShardingRuleBuilder(final String logRoot, final Map<String, DataSource> externalDataSourceMap, final ShardingRuleConfig shardingRuleConfig) {
        this.logRoot = logRoot;
        this.externalDataSourceMap = (null ==  externalDataSourceMap) ? Collections.<String, DataSource>emptyMap() : externalDataSourceMap;
        this.shardingRuleConfig = shardingRuleConfig;
    }
    
    /**
     * Build sharding rule.
     * 
     * @return databases and tables sharding rule
     */
    public ShardingRule build() {
        return null;
//        DataSourceRule dataSourceRule = buildDataSourceRule();
//        TableRule[] tableRuleConfigs = buildTableRules(dataSourceRule);
//        ShardingRule.ShardingRuleBuilder shardingRuleBuilder = ShardingRule.builder(dataSourceRule);
//        if (!Strings.isNullOrEmpty(shardingRuleConfig.getKeyGeneratorClass())) {
//            shardingRuleBuilder.defaultKeyGenerator(newInstance(shardingRuleConfig.getKeyGeneratorClass(), KeyGenerator.class));
//        }
//        return shardingRuleBuilder.tableRuleConfigs(tableRuleConfigs).bindingTableGroups(buildBindingTableRules(tableRuleConfigs))
//                .defaultDatabaseShardingStrategyConfig(buildShardingStrategy(shardingRuleConfig.getDefaultDatabaseStrategy()))
//                .defaultTableShardingStrategyConfig(buildShardingStrategy(shardingRuleConfig.getDefaultTableStrategy())).build();
    }
    
//    private DataSourceRule buildDataSourceRule() {
//        Preconditions.checkArgument(!shardingRuleConfig.getDataSource().isEmpty() || !externalDataSourceMap.isEmpty(), "Sharding JDBC: No data source config");
//        return shardingRuleConfig.getDataSource().isEmpty() ? new DataSourceRule(externalDataSourceMap, shardingRuleConfig.getDefaultDataSourceName())
//                : new DataSourceRule(shardingRuleConfig.getDataSource(), shardingRuleConfig.getDefaultDataSourceName());
//    }
//    
//    private TableRule[] buildTableRules(final DataSourceRule dataSourceRule) {
//        TableRule[] result = new TableRule[shardingRuleConfig.getTables().size()];
//        int count = 0;
//        for (Entry<String, TableRuleConfig> each : shardingRuleConfig.getTables().entrySet()) {
//            String logicTable = each.getKey();
//            TableRuleConfig tableRuleConfig = each.getValue();
//            TableRule.TableRuleBuilder tableRuleBuilder = TableRule.builder(logicTable).dataSourceRule(dataSourceRule)
//                    .dynamic(tableRuleConfig.isDynamic())
//                    .databaseShardingStrategyConfig(buildShardingStrategy(tableRuleConfig.getDatabaseStrategy()))
//                    .tableShardingStrategyConfig(buildShardingStrategy(tableRuleConfig.getTableStrategy()));
//            if (null != tableRuleConfig.getActualTables()) {
//                List<String> actualTables = new InlineParser(tableRuleConfig.getActualTables()).evaluate();
//                tableRuleBuilder.actualTables(actualTables.toArray(new String[actualTables.size()]));
//            }
//            if (!Strings.isNullOrEmpty(tableRuleConfig.getDataSourceNames())) {
//                List<String> actualDataSourceNames = new InlineParser(tableRuleConfig.getDataSourceNames()).evaluate();
//                tableRuleBuilder.dataSourceNames(actualDataSourceNames.toArray(new String[actualDataSourceNames.size()]));
//            }
//            buildGenerateKeyColumn(tableRuleBuilder, tableRuleConfig);
//            result[count] = tableRuleBuilder.build();
//            count++;
//        }
//        return result;
//    }
//    
//    private void buildGenerateKeyColumn(final TableRule.TableRuleBuilder tableRuleBuilder, final TableRuleConfig tableRuleConfig) {
//        for (GenerateKeyColumnConfig each : tableRuleConfig.getGenerateKeyColumns()) {
//            if (Strings.isNullOrEmpty(each.getColumnKeyGeneratorClass())) {
//                tableRuleBuilder.generateKeyColumn(each.getColumnName());
//            } else {
//                tableRuleBuilder.generateKeyColumn(each.getColumnName(), newInstance(each.getColumnKeyGeneratorClass(), KeyGenerator.class));
//            }
//        }
//    }
//    
//    private BindingTableRule[] buildBindingTableRules(final TableRule[] tableRuleConfigs) {
//        BindingTableRule[] result = new BindingTableRule[shardingRuleConfig.getBindingTables().size()];
//        int count = 0;
//        for (BindingTableRuleConfig each : shardingRuleConfig.getBindingTables()) {
//            List<TableRule> tableRuleList = Lists.transform(new InlineParser(each.getTableNames()).split(), new Function<String, TableRule>() {
//                
//                @Override
//                public TableRule apply(final String input) {
//                    return findTableRuleByLogicTableName(Arrays.asList(tableRuleConfigs), input);
//                }
//            });
//            result[count] = new BindingTableRule(tableRuleList.toArray(new TableRule[tableRuleList.size()]));
//            count++;
//        }
//        return result;
//    }
//    
//    private TableRule findTableRuleByLogicTableName(final Collection<TableRule> tableRuleConfigs, final String logicTableName) {
//        for (TableRule each : tableRuleConfigs) {
//            if (logicTableName.equalsIgnoreCase(each.getLogicTable())) {
//                return each;
//            }
//        }
//        throw new IllegalArgumentException(String.format("Sharding JDBC: Binding table `%s` is not an available Table rule", logicTableName));
//    }
//    
//    private <T extends ShardingStrategy> T buildShardingStrategy(final StrategyConfig config) {
//        if (null == config) {
//            return null;
//        }
//        Preconditions.checkArgument(Strings.isNullOrEmpty(config.getAlgorithmExpression()) && !Strings.isNullOrEmpty(config.getAlgorithmClassName())
//                || !Strings.isNullOrEmpty(config.getAlgorithmExpression()) && Strings.isNullOrEmpty(config.getAlgorithmClassName()));
//        List<String> shardingColumns = new InlineParser(config.getShardingColumns()).split();
//        if (Strings.isNullOrEmpty(config.getAlgorithmClassName())) {
//            return buildShardingAlgorithmExpression(shardingColumns, config.getAlgorithmExpression());
//        }
//        return buildShardingAlgorithmClassName(shardingColumns, config.getAlgorithmClassName());
//    }
//    
//    @SuppressWarnings("unchecked")
//    private <T extends ShardingStrategy> T buildShardingAlgorithmExpression(final List<String> shardingColumns, final String algorithmExpression) {
//        return (T) new ComplexShardingStrategy(shardingColumns, new ClosureShardingAlgorithm(algorithmExpression, logRoot));
//    }
//    
//    @SuppressWarnings("unchecked")
//    private <T extends ShardingStrategy> T buildShardingAlgorithmClassName(final List<String> shardingColumns, final String algorithmClassName) {
//        ShardingAlgorithm shardingAlgorithm;
//        try {
//            shardingAlgorithm = (ShardingAlgorithm) Class.forName(algorithmClassName).newInstance();
//        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
//            throw new IllegalArgumentException(ex);
//        }
//        Preconditions.checkState(shardingAlgorithm instanceof PreciseShardingAlgorithm
//                || shardingAlgorithm instanceof RangeShardingAlgorithm || shardingAlgorithm instanceof ComplexKeysShardingAlgorithm, "Sharding-JDBC: algorithmClassName is illegal");
//        if (shardingAlgorithm instanceof PreciseShardingAlgorithm) {
//            Preconditions.checkArgument(1 == shardingColumns.size(), "Sharding-JDBC: SingleKeyShardingAlgorithm must have only ONE sharding column");
//            return (T) new StandardShardingStrategy(shardingColumns.get(0), (PreciseShardingAlgorithm<?>) shardingAlgorithm);
//        }
//        if (shardingAlgorithm instanceof RangeShardingAlgorithm) {
//            // TODO for RangeShardingAlgorithm
//            throw new UnsupportedOperationException("");
//        }
//        // TODO should not force cast here
//        return (T) new ComplexShardingStrategy(shardingColumns, (ComplexKeysShardingAlgorithm) shardingAlgorithm);
//    }
//    
//    @SuppressWarnings("unchecked")
//    private <T> T newInstance(final String className, final Class<T> superClass) {
//        try {
//            return (T) superClass.getClassLoader().loadClass(className).newInstance();
//        } catch (final ReflectiveOperationException ex) {
//            throw new IllegalArgumentException(ex);
//        }
//    }
}

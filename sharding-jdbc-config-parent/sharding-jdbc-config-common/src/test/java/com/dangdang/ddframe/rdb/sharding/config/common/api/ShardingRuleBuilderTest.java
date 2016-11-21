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
import com.dangdang.ddframe.rdb.sharding.api.rule.DataNode;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.AutoIncrementColumnConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.StrategyConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.fixture.DecrementIdGenerator;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.fixture.MultiAlgorithm;
import com.dangdang.ddframe.rdb.sharding.config.common.internal.fixture.SingleAlgorithm;
import com.google.common.base.Joiner;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class ShardingRuleBuilderTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildFailureWhenDataSourceIsEmpty() {
        new ShardingRuleBuilder(new ShardingRuleConfig()).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildFailureWhenBindingTableError() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSource(createDataSourceMap());
        shardingRuleConfig.setTables(createTableRuleConfigMap());
        shardingRuleConfig.setBindingTables(Collections.singletonList(createBindingTableRule("t_other")));
        shardingRuleConfig.setDefaultDatabaseStrategy(getDatabaseStrategyConfig(SingleAlgorithm.class.getName()));
        shardingRuleConfig.setDefaultTableStrategy(getTableStrategyConfigForExpression());
        new ShardingRuleBuilder(shardingRuleConfig).build().getBindingTableRules().iterator().next().getTableRules().iterator().next();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildFailureWhenAlgorithmNotExisted() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSource(createDataSourceMap());
        shardingRuleConfig.setTables(createTableRuleConfigMap());
        shardingRuleConfig.setDefaultDatabaseStrategy(getDatabaseStrategyConfig("xxx.Algorithm"));
        new ShardingRuleBuilder(shardingRuleConfig).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildFailureWhenAutoIncrementClassNotExisted() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSource(createDataSourceMap());
        shardingRuleConfig.setDefaultDataSourceName("ds_0");
        shardingRuleConfig.setTables(createTableRuleConfigMap());
        shardingRuleConfig.setIdGeneratorClass("not.existed");
        shardingRuleConfig.setBindingTables(Collections.singletonList(createBindingTableRule("t_order", "t_order_item")));
        shardingRuleConfig.setDefaultDatabaseStrategy(getDatabaseStrategyConfig(SingleAlgorithm.class.getName()));
        shardingRuleConfig.setDefaultTableStrategy(getTableStrategyConfigForAlgorithmClass());
        new ShardingRuleBuilder(shardingRuleConfig).build();
    }
    
    @Test
    public void assertBuildSuccess() {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        shardingRuleConfig.setDataSource(createDataSourceMap());
        shardingRuleConfig.setDefaultDataSourceName("ds_0");
        shardingRuleConfig.setTables(createTableRuleConfigMap());
        shardingRuleConfig.setIdGeneratorClass("com.dangdang.ddframe.rdb.sharding.config.common.fixture.IncrementIdGenerator");
        shardingRuleConfig.setBindingTables(Collections.singletonList(createBindingTableRule("t_order", "t_order_item")));
        shardingRuleConfig.setDefaultDatabaseStrategy(getDatabaseStrategyConfig(SingleAlgorithm.class.getName()));
        shardingRuleConfig.setDefaultTableStrategy(getTableStrategyConfigForAlgorithmClass());
        ShardingRule actual = new ShardingRuleBuilder(shardingRuleConfig).build();
        assertThat(actual.getDataSourceRule().getDataSourceNames().size(), is(2));
        assertThat(actual.getDataSourceRule().getDataSourceNames(), hasItem("ds_0"));
        assertThat(actual.getDataSourceRule().getDataSourceNames(), hasItem("ds_1"));
        assertThat(actual.getTableRules().size(), is(3));
        for (TableRule each : actual.getTableRules()) {
            String logicTable = each.getLogicTable();
            switch (logicTable) {
                case "t_order":
                    assertFalse(each.isDynamic());
                    assertThat(each.getActualTables().size(), is(4));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_0", "t_order_0")));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_0", "t_order_0")));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_1", "t_order_0")));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_1", "t_order_1")));
                    break;
                case "t_order_item":
                    assertFalse(each.isDynamic());
                    assertThat(each.getActualTables().size(), is(4));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_0", "t_order_item_0")));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_0", "t_order_item_0")));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_1", "t_order_item_0")));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_1", "t_order_item_1")));
                    break;
                case "t_log":
                    assertTrue(each.isDynamic());
                    assertThat(each.getActualTables().size(), is(2));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_0", "SHARDING_JDBC DYNAMIC_TABLE_PLACEHOLDER")));
                    assertThat(each.getActualTables(), hasItem(new DataNode("ds_1", "SHARDING_JDBC DYNAMIC_TABLE_PLACEHOLDER")));
                    break;
                default:
                    fail();
            }
        }
        assertThat(actual.getBindingTableRules().size(), is(1));
        BindingTableRule bindingTableRule = actual.getBindingTableRules().iterator().next();
        assertTrue(bindingTableRule.hasLogicTable("t_order"));
        assertTrue(bindingTableRule.hasLogicTable("t_order_item"));
        assertFalse(bindingTableRule.hasLogicTable("t_log"));
        assertThat(actual.getDatabaseShardingStrategy().getShardingColumns().size(), is(1));
        assertThat(actual.getDatabaseShardingStrategy().getShardingColumns().iterator().next(), is("uid"));
        assertThat(actual.getTableShardingStrategy().getShardingColumns().size(), is(1));
        assertThat(actual.getTableShardingStrategy().getShardingColumns().iterator().next(), is("oid"));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds_0", null);
        result.put("ds_1", null);
        return result;
    }
    
    private Map<String, TableRuleConfig> createTableRuleConfigMap() {
        Map<String, TableRuleConfig> result = new HashMap<>(3);
        result.put("t_order", createTableRuleConfig("t_order"));
        result.put("t_order_item", createTableRuleConfig("t_order_item"));
        result.put("t_log", createDynamicTableRuleConfig());
        return result;
    }
    
    private TableRuleConfig createTableRuleConfig(final String logicTable) {
        TableRuleConfig result = new TableRuleConfig();
        result.setActualTables(logicTable + "_${[0, 1]}");
        result.setDataSourceNames("ds_${0..1}");
        result.setDatabaseStrategy(getDatabaseStrategyConfig(SingleAlgorithm.class.getName()));
        result.setTableStrategy(getTableStrategyConfigForExpression());
        Map<String, String> autoIncrementColumnMap = new HashMap<>();
        autoIncrementColumnMap.put("order_id", null);
        AutoIncrementColumnConfig orderIdConfig = new AutoIncrementColumnConfig();
        orderIdConfig.setColumnName("order_id");
        autoIncrementColumnMap.put("order_item_id", "com.dangdang.ddframe.rdb.sharding.config.common.fixture.DecrementIdGenerator");
        AutoIncrementColumnConfig orderItemIdConfig = new AutoIncrementColumnConfig();
        orderItemIdConfig.setColumnName("order_item_id");
        orderItemIdConfig.setColumnIdGeneratorClass(DecrementIdGenerator.class.getName());
        result.setAutoIncrementColumns(Arrays.asList(orderIdConfig, orderItemIdConfig));
        return result;
    }
    
    private StrategyConfig getDatabaseStrategyConfig(final String algorithmClassName) {
        StrategyConfig result = new StrategyConfig();
        result.setShardingColumns("uid");
        result.setAlgorithmClassName(algorithmClassName);
        return result;
    }
    
    private StrategyConfig getTableStrategyConfigForExpression() {
        StrategyConfig result = new StrategyConfig();
        result.setShardingColumns("oid");
        result.setAlgorithmExpression("${oid.longValue() % 2}");
        return result;
    }
    
    private StrategyConfig getTableStrategyConfigForAlgorithmClass() {
        StrategyConfig result = new StrategyConfig();
        result.setShardingColumns("oid");
        result.setAlgorithmClassName(MultiAlgorithm.class.getName());
        return result;
    }
    
    private TableRuleConfig createDynamicTableRuleConfig() {
        TableRuleConfig result = new TableRuleConfig();
        result.setDynamic(true);
        return result;
    }
    
    private BindingTableRuleConfig createBindingTableRule(final String... logicTables) {
        BindingTableRuleConfig result = new BindingTableRuleConfig();
        result.setTableNames(Joiner.on(",").join(logicTables));
        return result;
    }
}

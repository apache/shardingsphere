/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.routing.type.standard;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.fixture.OrderDatabaseHintShardingAlgorithm;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.metadata.table.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.routing.PreparedStatementRoutingEngine;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;

public class AbstractSQLRouteTest {
    
    protected SQLRouteResult assertRoute(final String sql, final List<Object> parameters) {
        ShardingRule shardingRule = createShardingRule();
        PreparedStatementRoutingEngine engine = new PreparedStatementRoutingEngine(
                sql, shardingRule, new ShardingMetaData(buildShardingDataSourceMetaData(), buildShardingTableMetaData()), DatabaseType.MySQL, true);
        SQLRouteResult result = engine.route(parameters);
        assertThat(result.getRouteUnits().size(), is(1));
        return result;
    }
    
    private ShardingDataSourceMetaData buildShardingDataSourceMetaData() {
        Map<String, String> shardingDataSourceURLs = new LinkedHashMap<>();
        shardingDataSourceURLs.put("ds_0", "jdbc:mysql://127.0.0.1:3306/actual_db");
        shardingDataSourceURLs.put("ds_1", "jdbc:mysql://127.0.0.1:3306/actual_db");
        return new ShardingDataSourceMetaData(shardingDataSourceURLs, createShardingRule(), DatabaseType.MySQL);
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();
        addTableRule(shardingRuleConfig, "t_order", "ds_${0..1}.t_order_${0..1}", "user_id", "t_order_${user_id % 2}", "ds_${user_id % 2}");
        addTableRule(shardingRuleConfig, "t_order_item", "ds_${0..1}.t_order_item_${0..1}", "user_id", "t_order_item_${user_id % 2}", "ds_${user_id % 2}");
        addTableRule(shardingRuleConfig, "t_user", "ds_${0..1}.t_user_${0..1}", "user_id", "t_user_${user_id % 2}", "ds_${user_id % 2}");
        addTableRuleWithHint(shardingRuleConfig, "t_hint_test", "ds_${0..1}.t_t_hint_test_${0..1}");
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        shardingRuleConfig.setDefaultDataSourceName("main");
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getBroadcastTables().add("t_product");
        result.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "ds_${user_id % 2}"));
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds_0", "ds_1", "main");
    }
    
    private void addTableRule(final ShardingRuleConfiguration shardingRuleConfig, final String tableName,
                                final String actualDataNodes, final String shardingColumn, final String tableAlgorithmExpression, final String dsAlgorithmExpression) {
        TableRuleConfiguration orderTableRuleConfig = createTableRuleConfig(tableName, actualDataNodes, shardingColumn, tableAlgorithmExpression, dsAlgorithmExpression);
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    }
    
    private TableRuleConfiguration createTableRuleConfig(final String tableName, final String actualDataNodes,
                                                           final String shardingColumn, final String algorithmExpression, final String dsAlgorithmExpression) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable(tableName);
        result.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(shardingColumn, algorithmExpression));
        result.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(shardingColumn, dsAlgorithmExpression));
        result.setActualDataNodes(actualDataNodes);
        return result;
    }
    
    private void addTableRuleWithHint(final ShardingRuleConfiguration shardingRuleConfig, final String tableName, final String actualDataNodes) {
        TableRuleConfiguration orderTableRuleConfig = createTableRuleWithHintConfig(tableName, actualDataNodes);
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    }
    
    private TableRuleConfiguration createTableRuleWithHintConfig(final String tableName, final String actualDataNodes) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable(tableName);
        result.setTableShardingStrategyConfig(new HintShardingStrategyConfiguration(new OrderDatabaseHintShardingAlgorithm()));
        result.setDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new OrderDatabaseHintShardingAlgorithm()));
        result.setActualDataNodes(actualDataNodes);
        return result;
    }
    
    private ShardingTableMetaData buildShardingTableMetaData() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order",
                new TableMetaData(Arrays.asList(new ColumnMetaData("order_id", "int", true), new ColumnMetaData("user_id", "int", false), new ColumnMetaData("status", "int", false))));
        tableMetaDataMap.put("t_order_item", new TableMetaData(Arrays.asList(new ColumnMetaData("item_id", "int", true), new ColumnMetaData("order_id", "int", false),
                new ColumnMetaData("user_id", "int", false), new ColumnMetaData("status", "varchar", false), new ColumnMetaData("c_date", "timestamp", false))));
        return new ShardingTableMetaData(tableMetaDataMap);
    }
}

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

package org.apache.shardingsphere.core.route.type.standard;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.route.PreparedStatementRoutingEngine;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.fixture.HintShardingAlgorithmFixture;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractSQLRouteTest {
    
    protected final SQLRouteResult assertRoute(final String sql, final List<Object> parameters) {
        ShardingRule shardingRule = createShardingRule();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(buildDataSourceMetas(), buildTableMetas());
        SQLParseEngine parseEngine = new SQLParseEngine(DatabaseTypes.getActualDatabaseType("MySQL"));
        PreparedStatementRoutingEngine engine = new PreparedStatementRoutingEngine(sql, shardingRule, metaData, DatabaseTypes.getActualDatabaseType("MySQL"), parseEngine);
        SQLRouteResult result = engine.route(parameters);
        assertThat(result.getRoutingResult().getRoutingUnits().size(), is(1));
        return result;
    }
    
    private DataSourceMetas buildDataSourceMetas() {
        Map<String, String> shardingDataSourceURLs = new LinkedHashMap<>();
        shardingDataSourceURLs.put("ds_0", "jdbc:mysql://127.0.0.1:3306/actual_db");
        shardingDataSourceURLs.put("ds_1", "jdbc:mysql://127.0.0.1:3306/actual_db");
        return new DataSourceMetas(shardingDataSourceURLs, DatabaseTypes.getActualDatabaseType("MySQL"));
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
        TableRuleConfiguration result = new TableRuleConfiguration(tableName, actualDataNodes);
        result.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(shardingColumn, algorithmExpression));
        result.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(shardingColumn, dsAlgorithmExpression));
        return result;
    }
    
    private void addTableRuleWithHint(final ShardingRuleConfiguration shardingRuleConfig, final String tableName, final String actualDataNodes) {
        TableRuleConfiguration orderTableRuleConfig = createTableRuleWithHintConfig(tableName, actualDataNodes);
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    }
    
    private TableRuleConfiguration createTableRuleWithHintConfig(final String tableName, final String actualDataNodes) {
        TableRuleConfiguration result = new TableRuleConfiguration(tableName, actualDataNodes);
        result.setTableShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        result.setDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        return result;
    }
    
    private TableMetas buildTableMetas() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order", new TableMetaData(Arrays.asList(new ColumnMetaData("order_id", "int", true), new ColumnMetaData("user_id", "int", false), 
                        new ColumnMetaData("status", "int", false)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_order_item", new TableMetaData(Arrays.asList(new ColumnMetaData("item_id", "int", true), new ColumnMetaData("order_id", "int", false),
                new ColumnMetaData("user_id", "int", false), new ColumnMetaData("status", "varchar", false), 
                new ColumnMetaData("c_date", "timestamp", false)), Collections.<String>emptySet()));
        return new TableMetas(tableMetaDataMap);
    }
}

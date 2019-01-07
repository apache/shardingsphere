/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.type.standard;

import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.routing.PreparedStatementRoutingEngine;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandardRoutingEngineForSubQueryTest {
    
    @Test(expected = IllegalStateException.class)
    public void assertOneTableError() {
        String sql = "select (select max(id) from t_order b where b.user_id =? ) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(3);
        parameters.add(2);
        assertSubQuery(sql, parameters);
    }
    
    @Test
    public void assertOneTable() {
        String sql = "select (select max(id) from t_order b where b.user_id = ? and b.user_id = a.user_id) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertSubQuery(sql, parameters);
    }
    
    @Test
    public void assertBindingTable() {
        String sql = "select (select max(id) from t_order_item b where b.user_id = ?) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertSubQuery(sql, parameters);
    }
    
    @Test
    public void assertNotShardingTable() {
        String sql = "select (select max(id) from t_user b where b.id = ?) from t_user a where id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertSubQuery(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertBindingTableWithDifferentValue() {
        String sql = "select (select max(id) from t_order_item b where b.user_id = ? ) from t_order a where user_id = ? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(3);
        assertSubQuery(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertTwoTableWithDifferentOperator() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(3);
        parameters.add(1);
        String sql = "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id = ? ";
        assertSubQuery(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertTwoTableWithIn() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(3);
        parameters.add(1);
        parameters.add(3);
        String sql = "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id in(?,?) ";
        assertSubQuery(sql, parameters);
    }
    
    private void assertSubQuery(final String sql, final List<Object> parameters) {
        ShardingRule shardingRule = createShardingRule();
        PreparedStatementRoutingEngine engine = new PreparedStatementRoutingEngine(
                sql, shardingRule, new ShardingMetaData(buildShardingDataSourceMetaData(), buildShardingTableMetaData()), DatabaseType.MySQL, true);
        assertThat(engine.route(parameters).getRouteUnits().size(), is(1));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSubQueryInSubQueryError() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(11);
        parameters.add(1);
        parameters.add(1);
        parameters.add(1);
        String sql = "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))";
        assertSubQuery(sql, parameters);
    }
    
    @Test
    public void assertSubQueryInSubQuery() {
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        parameters.add(1);
        parameters.add(1);
        String sql = "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))";
        assertSubQuery(sql, parameters);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSubQueryInFromError() {
        String sql = "select status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(11);
        parameters.add(1);
        assertSubQuery(sql, parameters);
    }
    
    @Test
    public void assertSubQueryInFrom() {
        String sql = "select status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        assertSubQuery(sql, parameters);
    }
    
    @Test
    public void assertSubQueryForAggregation() {
        String sql = "select count(*) from t_order where c.user_id = (select user_id from t_order where user_id =?) ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        assertSubQuery(sql, parameters);
    }
    
    @Test
    public void assertSubQueryForBinding() {
        String sql = "select count(*) from t_order where user_id = (select user_id from t_order_item where user_id =?) ";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        assertSubQuery(sql, parameters);
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
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "ds_${user_id % 2}"));
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds_0", "ds_1");
    }
    
    private void addTableRule(final ShardingRuleConfiguration shardingRuleConfig, final String tableName, final String actualDataNodes,
                              final String shardingColumn, final String tableAlgorithmExpression, final String dataSourceAlgorithmExpression) {
        TableRuleConfiguration orderTableRuleConfig = createTableRuleConfig(tableName, actualDataNodes, shardingColumn, tableAlgorithmExpression, dataSourceAlgorithmExpression);
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    }
    
    private TableRuleConfiguration createTableRuleConfig(final String tableName, final String actualDataNodes, final String shardingColumn,
                                                         final String algorithmExpression, final String dataSourceAlgorithmExpression) {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable(tableName);
        result.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(shardingColumn, algorithmExpression));
        result.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(shardingColumn, dataSourceAlgorithmExpression));
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

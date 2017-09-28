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

package io.shardingjdbc.spring;

import org.junit.Test;

import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.spring.util.FieldValueUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractShardingBothDataBasesAndTablesSpringDBUnitTest extends AbstractSpringDBUnitTest {
    
    @Test
    public abstract void testShardingNamespace();
    
    @Test
    public void testWithAllPlaceholders() throws SQLException {
        insertData();
        selectData();
    }
    
    private void insertData() throws SQLException {
        String orderSql = "INSERT INTO `t_order` (`order_id`, `user_id`, `status`) VALUES (?, ?, ?)";
        String orderItemSql = "INSERT INTO `t_order_item` (`order_item_id`, `order_id`, `user_id`, `status`) VALUES (?, ?, ?, ?)";
        String configSql = "INSERT INTO `t_config` (`id`, `status`) VALUES (?, ?)";
        for (int orderId = 1; orderId <= 4; orderId++) {
            for (int userId = 1; userId <= 2; userId++) {
                try (Connection connection = getShardingDataSource().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(orderSql);
                    preparedStatement.setInt(1, orderId);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.setString(3, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                    
                    preparedStatement = connection.prepareStatement(orderItemSql);
                    preparedStatement.setInt(1, orderId);
                    preparedStatement.setInt(2, orderId);
                    preparedStatement.setInt(3, userId);
                    preparedStatement.setString(4, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                    
                    preparedStatement = connection.prepareStatement(orderItemSql);
                    preparedStatement.setInt(1, orderId + 4);
                    preparedStatement.setInt(2, orderId);
                    preparedStatement.setInt(3, userId);
                    preparedStatement.setString(4, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                    
                    preparedStatement = connection.prepareStatement(configSql);
                    preparedStatement.setInt(1,  new Long(System.nanoTime()).intValue());
                    preparedStatement.setString(2, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                }
            }
        }
    }
    
    private void selectData() throws SQLException {
        String sql = "SELECT i.order_id, i.order_item_id  FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
            + " WHERE o.`user_id` = ? AND o.`order_id` = ? AND i.`order_id` = ? ORDER BY i.order_item_id DESC";
        try (Connection connection = getShardingDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, 1);
            preparedStatement.setInt(3, 1);
            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                if (0 == count) {
                    assertThat(resultSet.getInt(1), is(1));
                    assertThat(resultSet.getInt(2), is(5));
                } else if (1 == count) {
                    assertThat(resultSet.getInt(1), is(1));
                    assertThat(resultSet.getInt(2), is(1));
                }
                count++;
            }
            preparedStatement.close();
        }
    }
    
    protected void assertShardingStandardStrategy(final StandardShardingStrategyConfiguration databaseStrategy, final String shardingColumn, final String preciseAlgorithmClass) {
        Object shardingColumnName = FieldValueUtil.getFieldValue(databaseStrategy, "shardingColumn");
        assertNotNull(shardingColumnName);
        assertThat((String) shardingColumnName, is(shardingColumn));
        Object preciseAlgorithmClassName = FieldValueUtil.getFieldValue(databaseStrategy, "preciseAlgorithmClassName");
        assertNotNull(preciseAlgorithmClassName);
        assertThat((String) preciseAlgorithmClassName, is(preciseAlgorithmClass));
    }
    
    protected void assertShardingInlineStrategy(final InlineShardingStrategyConfiguration databaseStrategy, final String shardingColumn, final String algorithmExpression) {
        Object shardingColumnName = FieldValueUtil.getFieldValue(databaseStrategy, "shardingColumn");
        assertNotNull(shardingColumnName);
        assertThat((String) shardingColumnName, is(shardingColumn));
        Object algorithmInlineExpression = FieldValueUtil.getFieldValue(databaseStrategy, "algorithmInlineExpression");
        assertNotNull(algorithmInlineExpression);
        assertThat((String) algorithmInlineExpression, is(algorithmExpression));
    }
    
    @SuppressWarnings("unchecked")
    protected void assertDataSourceName(final ShardingDataSource shardingDataSource) {
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        assertNotNull(shardingContext);
        Object shardingRule = FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        assertNotNull(shardingRule);
        Object dataSourceMap = FieldValueUtil.getFieldValue(shardingRule, "dataSourceMap");
        assertNotNull(dataSourceMap);
        assertNotNull(((Map<String, DataSource>) dataSourceMap).get("dbtbl_0"));
        assertNotNull(((Map<String, DataSource>) dataSourceMap).get("dbtbl_1"));
    }
    
    protected void assertDefaultDataSourceName(final ShardingDataSource shardingDataSource) {
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        assertNotNull(shardingContext);
        Object shardingRule = FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        assertNotNull(shardingRule);
        Object defaultDataSourceName = FieldValueUtil.getFieldValue(shardingRule, "defaultDataSourceName");
        assertThat((String) defaultDataSourceName, is("dbtbl_0"));
    }
    
    protected void assertDefaultDatabaseShardingStrategy(final ShardingDataSource shardingDataSource, final ShardingStrategyConfiguration strategyConfiguration) {
        assertShardingStrategy(shardingDataSource, strategyConfiguration, "defaultDatabaseShardingStrategy");
    }
    
    protected void assertDefaultTableShardingStrategy(final ShardingDataSource shardingDataSource, final ShardingStrategyConfiguration strategyConfiguration) {
        assertShardingStrategy(shardingDataSource, strategyConfiguration, "defaultTableShardingStrategy");
    }
    
    private void assertShardingStrategy(final ShardingDataSource shardingDataSource, final ShardingStrategyConfiguration strategyConfiguration, final String fieldName) {
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        assertNotNull(shardingContext);
        Object shardingRule = FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        assertNotNull(shardingRule);
        ShardingStrategy defaultDatabaseShardingStrategy = (ShardingStrategy) FieldValueUtil.getFieldValue(shardingRule, fieldName);
        assertNotNull(defaultDatabaseShardingStrategy);
        assertShardingRuleStrategy(defaultDatabaseShardingStrategy, strategyConfiguration);
    }
    
    @SuppressWarnings("unchecked")
    protected Collection<TableRule> getTableRules(final ShardingDataSource shardingDataSource) {
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        Object shardingRule = FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        return (Collection<TableRule>) FieldValueUtil.getFieldValue(shardingRule, "tableRules");
    }
    
    protected void assertLogicTable(final TableRule tableRule, final String logicTableName) {
        assertThat(tableRule.getLogicTable(), is(logicTableName));
    }
    
    protected void assertActualTables(final TableRule tableRule, final String prefixName) {
        String[] dataSources = new String[]{"dbtbl_0", "dbtbl_1"};
        int shardingTableNum = 4;
        int i = 0;
        for (String dataSourceName : dataSources) {
            for (int j = shardingTableNum * i, k = 0; k < shardingTableNum; k++) {
                assertThat(tableRule.getActualDataNodes().get(j + k).getDataSourceName(), is(dataSourceName));
                assertThat(tableRule.getActualDataNodes().get(j + k).getTableName(), is(prefixName + k));
            }
            i++;
        }
    }
    
    protected void assertShardingRuleStrategy(final ShardingStrategy shardingStrategy, final ShardingStrategyConfiguration strategyConfiguration) {
        assertThat(shardingStrategy.getShardingColumns().size(), is(1));
        String shardingColumn = "";
        if (strategyConfiguration instanceof StandardShardingStrategyConfiguration) {
            shardingColumn = ((StandardShardingStrategyConfiguration) strategyConfiguration).getShardingColumn();
        } else if (strategyConfiguration instanceof InlineShardingStrategyConfiguration) {
            shardingColumn = ((InlineShardingStrategyConfiguration) strategyConfiguration).getShardingColumn();
        }
        assertThat(shardingStrategy.getShardingColumns().iterator().next(), is(shardingColumn));
    }
    
    protected void assertMasterDataSource(final MasterSlaveDataSource masterSlaveDataSource, final String name) {
        MasterSlaveRule masterSlaveRule = masterSlaveDataSource.getMasterSlaveRule();
        assertNotNull(masterSlaveRule);
        Object masterDataSourceName = FieldValueUtil.getFieldValue(masterSlaveRule, "masterDataSourceName");
        assertNotNull(masterDataSourceName);
        assertThat((String) masterDataSourceName, is(name));
        Object masterDataSource = FieldValueUtil.getFieldValue(masterSlaveRule, "masterDataSource");
        assertNotNull(masterDataSource);
    }
    
    @SuppressWarnings("unchecked")
    protected void assertSlaveDataSourceMap(final MasterSlaveDataSource masterSlaveDataSource, final String[] slaveDataSourceNames) {
        MasterSlaveRule masterSlaveRule = masterSlaveDataSource.getMasterSlaveRule();
        assertNotNull(masterSlaveRule);
        Object slaveDataSourceMap = FieldValueUtil.getFieldValue(masterSlaveRule, "slaveDataSourceMap");
        assertNotNull(slaveDataSourceMap);
        assertThat(((Map<String, DataSource>) slaveDataSourceMap).size(), is(slaveDataSourceNames.length));
        for (String each : slaveDataSourceNames) {
            assertNotNull(((Map<String, DataSource>) slaveDataSourceMap).get(each));
        }
    }
    
    protected void assertSlaveDataSourceMap(final MasterSlaveDataSource masterSlaveDataSource, final Class<?> loadBalanceAlgorithmClazz) {
        MasterSlaveRule masterSlaveRule = masterSlaveDataSource.getMasterSlaveRule();
        assertNotNull(masterSlaveRule);
        Object strategy = FieldValueUtil.getFieldValue(masterSlaveRule, "strategy");
        assertNotNull(strategy);
        assertTrue(strategy.getClass() == loadBalanceAlgorithmClazz);
    }
}

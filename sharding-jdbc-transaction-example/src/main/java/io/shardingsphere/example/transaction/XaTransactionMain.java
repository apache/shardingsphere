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

package io.shardingsphere.example.transaction;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.example.transaction.algorithm.ModuloShardingAlgorithm;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XaTransactionMain {
    
    public static void main(final String[] args) throws Exception {
        DataSource dataSource = getShardingDataSource();
        UserTransaction userTransaction = new UserTransactionImp();
        dropTable(dataSource);
        createTable(dataSource);
        insert(dataSource, userTransaction);
        updateFailure(dataSource, userTransaction);
    }
    
    private static void createTable(final DataSource dataSource) throws SQLException {
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order_item (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (item_id))");
    }
    
    private static void dropTable(final DataSource dataSource) throws SQLException {
        executeUpdate(dataSource, "DROP TABLE IF EXISTS t_order_item");
        executeUpdate(dataSource, "DROP TABLE IF EXISTS t_order");
    }
    
    private static void executeUpdate(final DataSource dataSource, final String sql) throws SQLException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }
    
    private static void insert(final DataSource dataSource, final UserTransaction userTransaction) throws Exception {
        userTransaction.begin();
        try {
            for (int i = 0; i < 100; i++) {
                String sql = String.format("INSERT INTO t_order VALUES (%s, %s, 'INIT');", 1000 + i, i);
                insert(dataSource, sql);
            }
            userTransaction.commit();
        } catch (SQLException ex) {
            userTransaction.rollback();
        }
    }
    
    private static void insert(final DataSource dataSource, final String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }
    
    private static void updateFailure(final DataSource dataSource, final UserTransaction userTransaction) throws Exception {
        String sql1 = "UPDATE t_order SET status='UPDATE_1' WHERE user_id=0 AND order_id=1000";
        String sql2 = "UPDATE t_order SET not_existed_column=1 WHERE user_id=1 AND order_id=?";
        String sql3 = "UPDATE t_order SET status='UPDATE_2' WHERE user_id=0 AND order_id=1000";
        userTransaction.begin();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            PreparedStatement preparedStatement3 = connection.prepareStatement(sql3)) {
            preparedStatement2.setObject(1, 1000);
            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();
            userTransaction.commit();
        } catch (SQLException ex) {
            userTransaction.rollback();
        }
    }
    
    private static DataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("ds_trans_${0..10}.t_order_${0..10}");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("ds_trans_${0..10}.t_order_item_${0..10}");
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
        
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new ModuloShardingAlgorithm()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingAlgorithm()));
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new HashMap<String, Object>(), new Properties());
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(10, 1);
        result.put("ds_trans_0", createDataSource("ds_trans_0"));
        result.put("ds_trans_1", createDataSource("ds_trans_1"));
        result.put("ds_trans_2", createDataSource("ds_trans_2"));
        result.put("ds_trans_3", createDataSource("ds_trans_3"));
        result.put("ds_trans_4", createDataSource("ds_trans_4"));
        result.put("ds_trans_5", createDataSource("ds_trans_5"));
        result.put("ds_trans_6", createDataSource("ds_trans_6"));
        result.put("ds_trans_7", createDataSource("ds_trans_7"));
        result.put("ds_trans_8", createDataSource("ds_trans_8"));
        result.put("ds_trans_9", createDataSource("ds_trans_9"));
        result.put("ds_trans_10", createDataSource("ds_trans_10"));
        return result;
    }
    
    private static DataSource createDataSource(final String dataSourceName) {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        result.setUniqueResourceName(dataSourceName);
        result.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        Properties xaProperties = new Properties();
        xaProperties.setProperty("user", "root");
        xaProperties.setProperty("password", "");
        xaProperties.setProperty("URL", String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        xaProperties.setProperty("pinGlobalTxToPhysicalConnection", "true");
        result.setXaProperties(xaProperties);
        return result;
    }
}

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

package io.shardingjdbc.example.transaction;

import io.shardingjdbc.example.transaction.algorithm.ModuloShardingAlgorithm;
import com.google.common.base.Optional;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.transaction.api.SoftTransactionManager;
import io.shardingjdbc.transaction.api.config.NestedBestEffortsDeliveryJobConfiguration;
import io.shardingjdbc.transaction.api.config.SoftTransactionConfiguration;
import io.shardingjdbc.transaction.bed.BEDSoftTransaction;
import io.shardingjdbc.transaction.constants.SoftTransactionType;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class Main {
    
    private static boolean useNestedJob = true;
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws SQLException {
    // CHECKSTYLE:ON
        DataSource dataSource = getShardingDataSource();
        updateFailure(dataSource);
    }
    
    private static void updateFailure(final DataSource dataSource) throws SQLException {
        String sql1 = "UPDATE t_order SET status='UPDATE_1' WHERE user_id=10 AND order_id=1000";
        String sql2 = "UPDATE t_order SET not_existed_column=1 WHERE user_id=1 AND order_id=?";
        String sql3 = "UPDATE t_order SET status='UPDATE_2' WHERE user_id=10 AND order_id=1000";
        SoftTransactionManager transactionManager = new SoftTransactionManager(getSoftTransactionConfiguration(dataSource));
        transactionManager.init();
        BEDSoftTransaction transaction = (BEDSoftTransaction) transactionManager.getTransaction(SoftTransactionType.BestEffortsDelivery);
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            transaction.begin(conn);
            PreparedStatement preparedStatement1 = conn.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = conn.prepareStatement(sql2);
            preparedStatement2.setObject(1, 1000);
            PreparedStatement preparedStatement3 = conn.prepareStatement(sql3);
            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();
        } finally {
            transaction.end();
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    private static DataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualTables("t_order_0, t_order_1");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualTables("t_order_item_0, t_order_item_1");
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
        
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", ModuloShardingAlgorithm.class.getName()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", ModuloShardingAlgorithm.class.getName()));
        return new ShardingDataSource(shardingRuleConfig.build(createDataSourceMap()));
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_trans_0", createDataSource("ds_trans_0"));
        result.put("ds_trans_1", createDataSource("ds_trans_1"));
        return result;
    }
    
    private static DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    private static DataSource createTransactionLogDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl("jdbc:mysql://localhost:3306/trans_log");
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    private static SoftTransactionConfiguration getSoftTransactionConfiguration(final DataSource dataSource) {
        SoftTransactionConfiguration result = new SoftTransactionConfiguration(dataSource);
        if (useNestedJob) {
            result.setBestEffortsDeliveryJobConfiguration(Optional.of(new NestedBestEffortsDeliveryJobConfiguration()));
        }
        result.setTransactionLogDataSource(createTransactionLogDataSource());
        return result;
    }
}

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

package com.dangdang.ddframe.rdb.sharding.example.transaction;

import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.example.transaction.algorithm.ModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.example.transaction.algorithm.ModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionManager;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.NestedBestEffortsDeliveryJobConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.bed.BEDSoftTransaction;
import com.dangdang.ddframe.rdb.transaction.soft.constants.SoftTransactionType;
import com.google.common.base.Optional;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// CHECKSTYLE:OFF
public final class Main {
    
    private static boolean useNestedJob = true;
    
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
    
    private static DataSource getShardingDataSource() {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap());
        TableRule orderTableRule = TableRule.builder("t_order").actualTables(Arrays.asList("t_order_0", "t_order_1")).dataSourceRule(dataSourceRule).build();
        TableRule orderItemTableRule = TableRule.builder("t_order_item").actualTables(Arrays.asList("t_order_item_0", "t_order_item_1")).dataSourceRule(dataSourceRule).build();
        ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Arrays.asList(orderTableRule, orderItemTableRule))
                .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm())).build();
        return ShardingDataSourceFactory.createDataSource(shardingRule);
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
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

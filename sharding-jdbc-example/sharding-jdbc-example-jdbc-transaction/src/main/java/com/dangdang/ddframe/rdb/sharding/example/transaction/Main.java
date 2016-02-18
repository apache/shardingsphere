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

package com.dangdang.ddframe.rdb.sharding.example.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.example.transaction.algorithm.ModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.example.transaction.algorithm.ModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.transaction.ec.api.EventualConsistencyTransactionManager;
import com.dangdang.ddframe.rdb.transaction.ec.api.EventualConsistencyTransactionType;
// CHECKSTYLE:OFF
public final class Main {
    
    public static void main(final String[] args) throws SQLException {
        // CHECKSTYLE:ON
        DataSource dataSource = getShardingDataSource();
        updateFailure(dataSource);
    }
    
    private static void updateFailure(final DataSource dataSource) throws SQLException {
        String sql1 = "UPDATE t_order SET status='UPDATE_1' WHERE user_id=10 AND order_id=1000";
        String sql2 = "UPDATE t_order SET not_existed_column=1 WHERE user_id=1 AND order_id=1";
        String sql3 = "UPDATE t_order SET status='UPDATE_2' WHERE user_id=10 AND order_id=1000";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            EventualConsistencyTransactionManager.begin(conn, EventualConsistencyTransactionType.BestEffortsDelivery);
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
            PreparedStatement pstmt3 = conn.prepareStatement(sql3);
            pstmt1.executeUpdate();
            pstmt2.executeUpdate();
            pstmt3.executeUpdate();
        } finally {
            EventualConsistencyTransactionManager.end();
            conn.close();
        }
    }
    
    private static ShardingDataSource getShardingDataSource() throws SQLException {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap());
        TableRule orderTableRule = new TableRule("t_order", Arrays.asList("t_order_0", "t_order_1"), dataSourceRule);
        TableRule orderItemTableRule = new TableRule("t_order_item", Arrays.asList("t_order_item_0", "t_order_item_1"), dataSourceRule);
        ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule),
                Arrays.asList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))),
                new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()),
                new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()));
        return new ShardingDataSource(shardingRule);
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
}

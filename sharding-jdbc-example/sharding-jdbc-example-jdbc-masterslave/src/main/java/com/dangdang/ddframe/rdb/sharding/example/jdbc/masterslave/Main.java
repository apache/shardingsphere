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

package com.dangdang.ddframe.rdb.sharding.example.jdbc.masterslave;

import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import com.dangdang.ddframe.rdb.sharding.api.MasterSlaveDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.config.MasterSlaveRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.example.jdbc.masterslave.algorithm.ModuloShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Main {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws SQLException {
    // CHECKSTYLE:ON
        DataSource dataSource = getShardingDataSource();
        printSimpleSelect(dataSource);
        System.out.println("--------------");
        printGroupBy(dataSource);
        System.out.println("--------------");
        printHintSimpleSelect(dataSource);
    }
    
    private static void printSimpleSelect(final DataSource dataSource) throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 1001);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt(1));
                    System.out.println(rs.getInt(2));
                    System.out.println(rs.getInt(3));
                }
            }
        }
    }
    
    private static void printGroupBy(final DataSource dataSource) throws SQLException {
        String sql = "SELECT o.user_id, COUNT(*) FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id GROUP BY o.user_id";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)
                ) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                System.out.println("user_id: " + rs.getInt(1) + ", count: " + rs.getInt(2));
            }
        }
    }
    
    private static void printHintSimpleSelect(final DataSource dataSource) throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id";
        try (
                HintManager hintManager = HintManager.getInstance();
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            hintManager.addDatabaseShardingValue("t_order", "user_id", 10);
            hintManager.addTableShardingValue("t_order", "order_id", 1001);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt(1));
                    System.out.println(rs.getInt(2));
                    System.out.println(rs.getInt(3));
                }
            }
        }
    }
    
    private static ShardingDataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
        
        TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualTables("t_order_0, t_order_1");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        
        TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualTables("t_order_item_0, t_order_item_1");
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
        
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        
        StandardShardingStrategyConfig databaseShardingStrategyConfig = new StandardShardingStrategyConfig();
        databaseShardingStrategyConfig.setShardingColumn("user_id");
        databaseShardingStrategyConfig.setPreciseAlgorithmClassName(ModuloShardingAlgorithm.class.getName());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);
        
        StandardShardingStrategyConfig tableShardingStrategyConfig = new StandardShardingStrategyConfig();
        tableShardingStrategyConfig.setShardingColumn("order_id");
        tableShardingStrategyConfig.setPreciseAlgorithmClassName(ModuloShardingAlgorithm.class.getName());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(tableShardingStrategyConfig);
        
        return new ShardingDataSource(shardingRuleConfig.build(createDataSourceMap()));
    }
    
    private static Map<String, DataSource> createDataSourceMap() throws SQLException {
        final Map<String, DataSource> result = new HashMap<>(2, 1);
        Map<String, DataSource> masterSlaveDataSourceMap1 = new HashMap<>(3, 1);
        masterSlaveDataSourceMap1.put("ds_0_master", createDataSource("ds_0_master"));
        masterSlaveDataSourceMap1.put("ds_0_slave_0", createDataSource("ds_0_slave_0"));
        masterSlaveDataSourceMap1.put("ds_0_slave_1", createDataSource("ds_0_slave_1"));
        MasterSlaveRuleConfig masterSlaveRuleConfig1 = new MasterSlaveRuleConfig();
        masterSlaveRuleConfig1.setName("ds_0");
        masterSlaveRuleConfig1.setMasterDataSourceName("ds_0_master");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("ds_0_slave_0", "ds_0_slave_1"));
        result.put("ds_0", MasterSlaveDataSourceFactory.createDataSource(masterSlaveDataSourceMap1, masterSlaveRuleConfig1));
        Map<String, DataSource> masterSlaveDataSourceMap2 = new HashMap<>(3, 1);
        masterSlaveDataSourceMap1.put("ds_1_master", createDataSource("ds_1_master"));
        masterSlaveDataSourceMap2.put("ds_1_slave_0", createDataSource("ds_1_slave_0"));
        masterSlaveDataSourceMap2.put("ds_1_slave_1", createDataSource("ds_1_slave_1"));
        MasterSlaveRuleConfig masterSlaveRuleConfig2 = new MasterSlaveRuleConfig();
        masterSlaveRuleConfig2.setName("ds_1");
        masterSlaveRuleConfig2.setMasterDataSourceName("ds_1_master");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("ds_1_slave_0", "ds_1_slave_1"));
        result.put("ds_1", MasterSlaveDataSourceFactory.createDataSource(masterSlaveDataSourceMap2, masterSlaveRuleConfig2));
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

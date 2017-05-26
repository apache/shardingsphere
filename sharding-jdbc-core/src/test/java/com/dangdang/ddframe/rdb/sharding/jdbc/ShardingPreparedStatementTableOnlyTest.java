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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.dangdang.ddframe.rdb.integrate.tbl.AbstractShardingTablesOnlyDBUnitTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingPreparedStatementTableOnlyTest extends AbstractShardingTablesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertExecuteQueryWithParameter() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(20L));
            ShardingPreparedStatement sps = (ShardingPreparedStatement) preparedStatement;
            assertThat(sps.getRoutedStatements().size(), is(10));
        }
    }
    
    @Test
    public void assertAddBatch() throws SQLException {
        String sql = "INSERT INTO `t_order`(`order_id`, `user_id`, `status`) VALUES (?,?,?)";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                java.sql.Statement queryStatement = connection.createStatement()) {
            preparedStatement.setInt(1, 3101);
            preparedStatement.setInt(2, 11);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3102);
            preparedStatement.setInt(2, 12);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3111);
            preparedStatement.setInt(2, 21);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3112);
            preparedStatement.setInt(2, 22);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(1));
            }
    
            try (ResultSet rs = queryStatement.executeQuery("SELECT `user_id` from `t_order` where `order_id` = 3101")) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(11));
                assertFalse(rs.next());
            }
            try (ResultSet rs = queryStatement.executeQuery("SELECT `user_id` from `t_order` where `order_id` = 3102")) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(12));
                assertFalse(rs.next());
            }
            try (ResultSet rs = queryStatement.executeQuery("SELECT `user_id` from `t_order` where `order_id` = 3111")) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(21));
                assertFalse(rs.next());
            }
            try (ResultSet rs = queryStatement.executeQuery("SELECT `user_id` from `t_order` where `order_id` = 3112")) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(22));
                assertFalse(rs.next());
            }
        }
    }
}

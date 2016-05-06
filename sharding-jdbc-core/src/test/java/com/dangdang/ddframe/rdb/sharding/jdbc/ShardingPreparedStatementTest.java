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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.mysql.jdbc.Statement;

public final class ShardingPreparedStatementTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
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
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithoutParameter() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = 'init'";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteUpdateWithParameter() throws SQLException {
        String sql = "DELETE FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "init");
            assertThat(preparedStatement.executeUpdate(), is(40));
        }
    }
    
    @Test
    public void assertExecuteUpdateWithoutParameter() throws SQLException {
        String sql = "DELETE FROM `t_order` WHERE `status` = 'init'";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            assertThat(preparedStatement.executeUpdate(), is(40));
        }
    }
    
    @Test
    public void assertExecuteWithParameter() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "init");
            assertTrue(preparedStatement.execute());
            assertTrue(preparedStatement.getResultSet().next());
            assertThat(preparedStatement.getResultSet().getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteWithoutParameter() throws SQLException {
        String sql = "DELETE FROM `t_order` WHERE `status` = 'init'";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            assertFalse(preparedStatement.execute());
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetHoldabilityIsZero() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 0)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithAutoGeneratedKeys() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithColumnIndexes() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new int[] {1})) {
            preparedStatement.setNull(1, java.sql.Types.VARCHAR);
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(0L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithColumnNames() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] {"orders_count"})) {
            preparedStatement.setNull(1, java.sql.Types.VARCHAR);
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(0L));
        }
    }
    
    @Test
    public void assertAddBatch() throws SQLException {
        String sql = "INSERT INTO `t_order`(`order_id`, `user_id`, `status`) VALUES (?,?,?)";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, 3101);
            preparedStatement.setInt(2, 11);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3102);
            preparedStatement.setInt(2, 12);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(1));
            }
        }
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertClearBatch() throws SQLException {
        String sql = "INSERT INTO `t_order`(`order_id`, `user_id`, `status`) VALUES (?,?,?)";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, 3101);
            preparedStatement.setInt(2, 11);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.clearBatch();
            preparedStatement.executeBatch();
        }
    }
}

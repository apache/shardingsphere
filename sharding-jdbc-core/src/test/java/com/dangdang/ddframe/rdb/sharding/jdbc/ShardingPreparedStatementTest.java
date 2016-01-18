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
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
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
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "init");
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithoutParameter() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = 'init'";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteUpdateWithParameter() throws SQLException {
        String sql = "DELETE FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "init");
            assertThat(pstmt.executeUpdate(), is(40));
        }
    }
    
    @Test
    public void assertExecuteUpdateWithoutParameter() throws SQLException {
        String sql = "DELETE FROM `t_order` WHERE `status` = 'init'";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            assertThat(pstmt.executeUpdate(), is(40));
        }
    }
    
    @Test
    public void assertExecuteWithParameter() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "init");
            assertTrue(pstmt.execute());
            assertTrue(pstmt.getResultSet().next());
            assertThat(pstmt.getResultSet().getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteWithoutParameter() throws SQLException {
        String sql = "DELETE FROM `t_order` WHERE `status` = 'init'";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            assertFalse(pstmt.execute());
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndRsultSetConcurrency() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            pstmt.setString(1, "init");
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndRsultSetConcurrencyAndResultSetHoldability() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            pstmt.setString(1, "init");
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetHoldabilityIsZero() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 0)) {
            pstmt.setString(1, "init");
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithAutoGeneratedKeys() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            pstmt.setString(1, "init");
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithColumnIndexes() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql, new int[] {1})) {
            pstmt.setNull(1, java.sql.Types.VARCHAR);
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(0L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithColumnNames() throws SQLException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order` WHERE `status` = ?";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql, new String[] {"orders_count"})) {
            pstmt.setNull(1, java.sql.Types.VARCHAR);
            ResultSet resultSet = pstmt.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(0L));
        }
    }
    
    @Test
    public void assertBatch() throws SQLException {
        String sql = "INSERT INTO `t_order`(`order_id`, `user_id`, `status`) VALUES (?,?,?)";
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, 3101);
            pstmt.setInt(2, 11);
            pstmt.setString(3, "BATCH");
            pstmt.addBatch();
            pstmt.setInt(1, 3102);
            pstmt.setInt(2, 12);
            pstmt.setString(3, "BATCH");
            pstmt.addBatch();
            int[] result = pstmt.executeBatch();
            for (int each : result) {
                assertThat(each, is(1));
            }
        }
    }
}

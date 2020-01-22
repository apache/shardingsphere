/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import org.apache.shardingsphere.shardingjdbc.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingPreparedStatementTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private static final String INSERT_WITH_GENERATE_KEY_SQL = "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (?, ?, ?, ?)";
    
    private static final String INSERT_WITHOUT_GENERATE_KEY_SQL = "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)";
    
    private static final String SELECT_SQL_WITHOUT_PARAMETER_MARKER = "SELECT item_id FROM t_order_item WHERE user_id = %d AND order_id= %s AND status = 'BATCH'";
    
    private static final String SELECT_SQL_WITH_PARAMETER_MARKER = "SELECT item_id FROM t_order_item WHERE user_id = ? AND order_id= ? AND status = 'BATCH'";
    
    private static final String UPDATE_SQL = "UPDATE t_order SET status = ? WHERE user_id = ? AND order_id = ?";
    
    private static final String UPDATE_BATCH_SQL = "UPDATE t_order SET status=? WHERE status=?";
    
    @Test
    public void assertAddBatch() throws SQLException {
        try (
            Connection connection = getShardingDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_WITH_GENERATE_KEY_SQL)) {
            preparedStatement.setInt(1, 3101);
            preparedStatement.setInt(2, 11);
            preparedStatement.setInt(3, 11);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3102);
            preparedStatement.setInt(2, 12);
            preparedStatement.setInt(3, 12);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3111);
            preparedStatement.setInt(2, 21);
            preparedStatement.setInt(3, 21);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3112);
            preparedStatement.setInt(2, 22);
            preparedStatement.setInt(3, 22);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(1));
            }
        }
    }
    
    @Test
    public void assertAddBatchWithoutGenerateKeyColumn() throws SQLException {
        try (
            Connection connection = getShardingDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_WITHOUT_GENERATE_KEY_SQL, Statement.RETURN_GENERATED_KEYS);
            Statement queryStatement = connection.createStatement()) {
            preparedStatement.setInt(1, 11);
            preparedStatement.setInt(2, 11);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 12);
            preparedStatement.setInt(2, 12);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 21);
            preparedStatement.setInt(2, 21);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 22);
            preparedStatement.setInt(2, 22);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(1));
            }
            ResultSet generateKeyResultSet = preparedStatement.getGeneratedKeys();
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(1L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(2L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(3L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(4L));
            assertFalse(generateKeyResultSet.next());
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 11, 11))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(1));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 12, 12))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(2));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 21, 21))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(3));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 22, 22))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertAddBatchWithGenerateKeyColumn() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_WITH_GENERATE_KEY_SQL, Statement.RETURN_GENERATED_KEYS);
                Statement queryStatement = connection.createStatement()) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, 11);
            preparedStatement.setInt(3, 11);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 2);
            preparedStatement.setInt(2, 12);
            preparedStatement.setInt(3, 12);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3);
            preparedStatement.setInt(2, 21);
            preparedStatement.setInt(3, 21);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 4);
            preparedStatement.setInt(2, 22);
            preparedStatement.setInt(3, 22);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(1));
            }
            ResultSet generateKeyResultSet = preparedStatement.getGeneratedKeys();
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(1L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(2L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(3L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(4L));
            assertFalse(generateKeyResultSet.next());
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 11, 11))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(1));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 12, 12))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(2));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 21, 21))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(3));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_SQL_WITHOUT_PARAMETER_MARKER, 22, 22))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertUpdateBatch() throws SQLException {
        try (
            Connection connection = getShardingDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_BATCH_SQL)) {
            preparedStatement.setString(1, "batch");
            preparedStatement.setString(2, "init");
            preparedStatement.addBatch();
            preparedStatement.setString(1, "batch");
            preparedStatement.setString(2, "init");
            preparedStatement.addBatch();
            preparedStatement.setString(1, "init");
            preparedStatement.setString(2, "batch");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            assertThat(result.length, is(3));
            assertThat(result[0], is(4));
            assertThat(result[1], is(0));
            assertThat(result[2], is(4));
        }
    }

    @Test
    public void assertExecuteGetResultSet() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingDataSource().getConnection().prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, "OK");
            preparedStatement.setInt(2, 11);
            preparedStatement.setInt(3, 11);
            preparedStatement.execute();
            assertNull(preparedStatement.getResultSet());
        }
    }

    @Test
    public void assertExecuteUpdateGetResultSet() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingDataSource().getConnection().prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, "OK");
            preparedStatement.setInt(2, 11);
            preparedStatement.setInt(3, 11);
            preparedStatement.executeUpdate();
            assertNull(preparedStatement.getResultSet());
        }
    }

    @Test
    public void assertClearBatch() throws SQLException {
        try (
            Connection connection = getShardingDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_WITH_GENERATE_KEY_SQL)) {
            preparedStatement.setInt(1, 3101);
            preparedStatement.setInt(2, 11);
            preparedStatement.setInt(3, 11);
            preparedStatement.setString(4, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.clearBatch();
            int[] result = preparedStatement.executeBatch();
            assertThat(result.length, is(0));
        }
    }
    
    @Test
    public void assertInitPreparedStatementExecutorWithReplayMethod() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingDataSource().getConnection().prepareStatement(SELECT_SQL_WITH_PARAMETER_MARKER)) {
            preparedStatement.setQueryTimeout(1);
            preparedStatement.setInt(1, 11);
            preparedStatement.setInt(2, 11);
            preparedStatement.executeQuery();
            assertThat(preparedStatement.getQueryTimeout(), is(1));
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingDataSource().getConnection().prepareStatement(null)) {
            preparedStatement.executeQuery();
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingDataSource().getConnection().prepareStatement("")) {
            preparedStatement.executeQuery();
        }
    }
}

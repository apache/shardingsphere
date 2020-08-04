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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.driver.fixture.ResetIncrementKeyGenerateAlgorithm;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSpherePreparedStatementTest extends AbstractShardingSphereDataSourceForShardingTest {

    private static final String INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL = "INSERT INTO t_user (name) VALUES (?),(?),(?),(?)";

    private static final String SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL = "SELECT name FROM t_user WHERE id=%d";
    
    private static final String INSERT_WITH_GENERATE_KEY_SQL = "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (?, ?, ?, ?)";
    
    private static final String INSERT_WITHOUT_GENERATE_KEY_SQL = "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)";
    
    private static final String INSERT_ON_DUPLICATE_KEY_SQL = "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (?, ?, ?, ?), (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = ?";
    
    private static final String SELECT_SQL_WITHOUT_PARAMETER_MARKER = "SELECT item_id FROM t_order_item WHERE user_id = %d AND order_id= %s AND status = 'BATCH'";
    
    private static final String SELECT_SQL_WITH_PARAMETER_MARKER = "SELECT item_id FROM t_order_item WHERE user_id = ? AND order_id= ? AND status = 'BATCH'";
    
    private static final String SELECT_SQL_WITH_PARAMETER_MARKER_RETURN_STATUS = "SELECT item_id, user_id, status FROM t_order_item WHERE  order_id= ? AND user_id = ?";
    
    private static final String SELECT_AUTO_SQL = "SELECT item_id, order_id, status FROM t_order_item_auto WHERE order_id >= ?";
    
    private static final String UPDATE_SQL = "UPDATE t_order SET status = ? WHERE user_id = ? AND order_id = ?";
    
    private static final String UPDATE_AUTO_SQL = "UPDATE t_order_auto SET status = ? WHERE order_id = ?";
    
    private static final String UPDATE_BATCH_SQL = "UPDATE t_order SET status=? WHERE status=?";
    
    @Test
    public void assertAddBatch() throws SQLException {
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
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
    
    @Ignore
    @Test
    public void assertMultiValuesWithGenerateShardingKeyColumn() throws SQLException {
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, Statement.RETURN_GENERATED_KEYS);
                Statement queryStatement = connection.createStatement()) {
            ResetIncrementKeyGenerateAlgorithm.getCOUNT().set(0);
            preparedStatement.setString(1, "BATCH1");
            preparedStatement.setString(2, "BATCH2");
            preparedStatement.setString(3, "BATCH3");
            preparedStatement.setString(4, "BATCH4");
            int result = preparedStatement.executeUpdate();
            assertThat(result, is(4));
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
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 1L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH1"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 2L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH2"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 3L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH3"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 4L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH4"));
            }
        }
    }
    
    @Ignore
    @Test
    public void assertAddBatchMultiValuesWithGenerateShardingKeyColumn() throws SQLException {
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, Statement.RETURN_GENERATED_KEYS);
                Statement queryStatement = connection.createStatement()) {
            ResetIncrementKeyGenerateAlgorithm.getCOUNT().set(10);
            preparedStatement.setString(1, "BATCH1");
            preparedStatement.setString(2, "BATCH2");
            preparedStatement.setString(3, "BATCH3");
            preparedStatement.setString(4, "BATCH4");
            preparedStatement.addBatch();
            preparedStatement.setString(1, "BATCH5");
            preparedStatement.setString(2, "BATCH6");
            preparedStatement.setString(3, "BATCH7");
            preparedStatement.setString(4, "BATCH8");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(4));
            }
            ResultSet generateKeyResultSet = preparedStatement.getGeneratedKeys();
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(11L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(12L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(13L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(14L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(15L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(16L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(17L));
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(18L));
            assertFalse(generateKeyResultSet.next());
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 11L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH1"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 12L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH2"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 13L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH3"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 14L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH4"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 15L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH5"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 16L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH6"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 17L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH7"));
            }
            try (ResultSet resultSet = queryStatement.executeQuery(String.format(SELECT_FOR_INSERT_MULTI_VALUES_WITH_GENERATE_SHARDING_KEY_SQL, 18L))) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("BATCH8"));
            }
        }
    }
    
    @Test
    public void assertAddBatchWithoutGenerateKeyColumn() throws SQLException {
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
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
                Connection connection = getShardingSphereDataSource().getConnection();
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
    public void assertAddOnDuplicateKey() throws SQLException {
        int itemId = 1;
        int userId1 = 101;
        int userId2 = 102;
        int orderId = 200;
        String status = "init";
        String updatedStatus = "updated on duplicate key";
        try (Connection connection = getShardingSphereDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ON_DUPLICATE_KEY_SQL);
             PreparedStatement queryStatement = connection.prepareStatement(SELECT_SQL_WITH_PARAMETER_MARKER_RETURN_STATUS)) {
            preparedStatement.setInt(1, itemId);
            preparedStatement.setInt(2, orderId);
            preparedStatement.setInt(3, userId1);
            preparedStatement.setString(4, status);
            preparedStatement.setInt(5, itemId);
            preparedStatement.setInt(6, orderId);
            preparedStatement.setInt(7, userId2);
            preparedStatement.setString(8, status);
            preparedStatement.setString(9, updatedStatus);
            int result = preparedStatement.executeUpdate();
            assertThat(result, is(2));
            queryStatement.setInt(1, orderId);
            queryStatement.setInt(2, userId1);
            try (ResultSet resultSet = queryStatement.executeQuery()) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(2), is(userId1));
                assertThat(resultSet.getString(3), is(status));
            }
            queryStatement.setInt(1, orderId);
            queryStatement.setInt(2, userId2);
            try (ResultSet resultSet = queryStatement.executeQuery()) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(2), is(userId2));
                assertThat(resultSet.getString(3), is(status));
            }
        }
        
        try (Connection connection = getShardingSphereDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ON_DUPLICATE_KEY_SQL);
             PreparedStatement queryStatement = connection.prepareStatement(SELECT_SQL_WITH_PARAMETER_MARKER_RETURN_STATUS)) {
            preparedStatement.setInt(1, itemId);
            preparedStatement.setInt(2, orderId);
            preparedStatement.setInt(3, userId1);
            preparedStatement.setString(4, status);
            preparedStatement.setInt(5, itemId);
            preparedStatement.setInt(6, orderId);
            preparedStatement.setInt(7, userId2);
            preparedStatement.setString(8, status);
            preparedStatement.setString(9, updatedStatus);
            int result = preparedStatement.executeUpdate();
            assertThat(result, is(2));
            queryStatement.setInt(1, orderId);
            queryStatement.setInt(2, userId1);
            try (ResultSet resultSet = queryStatement.executeQuery()) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(2), is(userId1));
                assertThat(resultSet.getString(3), is(updatedStatus));
            }
            queryStatement.setInt(1, orderId);
            queryStatement.setInt(2, userId2);
            try (ResultSet resultSet = queryStatement.executeQuery()) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(2), is(userId2));
                assertThat(resultSet.getString(3), is(updatedStatus));
            }
        }
    }
    
    @Test
    public void assertUpdateBatch() throws SQLException {
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
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
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, "OK");
            preparedStatement.setInt(2, 11);
            preparedStatement.setInt(3, 11);
            preparedStatement.execute();
            assertNull(preparedStatement.getResultSet());
        }
    }
    
    @Test
    public void assertExecuteUpdateGetResultSet() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, "OK");
            preparedStatement.setInt(2, 11);
            preparedStatement.setInt(3, 11);
            preparedStatement.executeUpdate();
            assertNull(preparedStatement.getResultSet());
        }
    }
    
    @Test
    public void assertExecuteUpdateAutoTableGetResultSet() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement(UPDATE_AUTO_SQL)) {
            preparedStatement.setString(1, "OK");
            preparedStatement.setInt(2, 10);
            preparedStatement.executeUpdate();
            assertNull(preparedStatement.getResultSet());
        }
    }
    
    @Test
    public void assertExecuteSelectAutoTableGetResultSet() throws SQLException {
        Collection<Integer> result = Lists.newArrayList(1001, 1100, 1101);
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement(SELECT_AUTO_SQL)) {
            preparedStatement.setInt(1, 1001);
            int count = 0;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.contains(resultSet.getInt(2));
                    count++;
                }
            }
            assertThat(result.size(), is(count));
        }
    }
    
    @Test
    public void assertClearBatch() throws SQLException {
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
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
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement(SELECT_SQL_WITH_PARAMETER_MARKER)) {
            preparedStatement.setQueryTimeout(1);
            preparedStatement.setInt(1, 11);
            preparedStatement.setInt(2, 11);
            preparedStatement.executeQuery();
            assertThat(preparedStatement.getQueryTimeout(), is(1));
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement(null)) {
            preparedStatement.executeQuery();
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement("")) {
            preparedStatement.executeQuery();
        }
    }
    
    @Test
    public void assertGetParameterMetaData() throws SQLException {
        try (PreparedStatement preparedStatement = getShardingSphereDataSource().getConnection().prepareStatement(SELECT_SQL_WITH_PARAMETER_MARKER)) {
            assertThat(preparedStatement.getParameterMetaData().getParameterCount(), is(2));
        }
    }
}

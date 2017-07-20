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

package com.dangdang.ddframe.rdb.sharding.jdbc.core.statement;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.util.EventBusInstance;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.google.common.eventbus.Subscribe;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingPreparedStatementTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    private String sql = "SELECT COUNT(*) AS orders_count FROM t_order WHERE status = ?";
    
    private String sql2 = "SELECT order_id from t_order where user_id = %d and status = 'BATCH'";
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertExecuteQueryWithParameter() throws SQLException {
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
            preparedStatement.setString(1, "null");
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(0L));
            preparedStatement.setString(1, "init");
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithoutParameter() throws SQLException {
        String sql = getDatabaseTestSQL().getSelectCountAliasSql();
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteUpdateWithParameter() throws SQLException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getDeleteWithoutShardingValueSql());
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "init");
            assertThat(preparedStatement.executeUpdate(), is(40));
            preparedStatement.setString(1, "null");
            assertThat(preparedStatement.executeUpdate(), is(0));
            preparedStatement.setString(1, "init");
            assertThat(preparedStatement.executeUpdate(), is(0));
        }
    }
    
    @Test
    public void assertExecuteUpdateWithoutParameter() throws SQLException {
        String sql = String.format(getDatabaseTestSQL().getDeleteWithoutShardingValueSql(), "'init'");
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            assertThat(preparedStatement.executeUpdate(), is(40));
            assertThat(preparedStatement.executeUpdate(), is(0));
            assertThat(preparedStatement.executeUpdate(), is(0));
        }
    }
    
    @Test
    public void assertExecuteWithParameter() throws SQLException {
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "init");
            assertTrue(preparedStatement.execute());
            assertTrue(preparedStatement.getResultSet().next());
            assertThat(preparedStatement.getResultSet().getLong(1), is(40L));
            preparedStatement.setString(1, "null");
            assertTrue(preparedStatement.execute());
            assertTrue(preparedStatement.getResultSet().next());
            assertThat(preparedStatement.getResultSet().getLong(1), is(0L));
            preparedStatement.setString(1, "init");
            assertTrue(preparedStatement.execute());
            assertTrue(preparedStatement.getResultSet().next());
            assertThat(preparedStatement.getResultSet().getLong(1), is(40L));
        }
    }
    
    @Test
    public void assertExecuteWithoutParameter() throws SQLException {
        String sql = String.format(getDatabaseTestSQL().getDeleteWithoutShardingValueSql(), "'init'");
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            assertFalse(preparedStatement.execute());
            assertFalse(preparedStatement.execute());
            assertFalse(preparedStatement.execute());
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrency() throws SQLException {
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
    public void assertExecuteQueryWithAutoGeneratedKeys() throws SQLException {
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
        Object listener = new Object() {
            
            private List<DMLExecutionEvent> beforeEvents = new ArrayList<>();
    
            @Subscribe
            public void subscribe(final DMLExecutionEvent event) {
                if (event.getEventExecutionType() == EventExecutionType.BEFORE_EXECUTE) {
                    beforeEvents.add(event);
                } else if (event.getEventExecutionType() == EventExecutionType.EXECUTE_SUCCESS) {
                    assertThat(beforeEvents, hasItem(event));
                }
            }
        };
        EventBusInstance.getInstance().register(listener);
        String sql = getDatabaseTestSQL().getInsertWithAllPlaceholdersSql();
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
        } finally {
            EventBusInstance.getInstance().unregister(listener);
        }
    }
    
    @Test
    public void assertAddBatchWithoutGenerateKeyColumn() throws SQLException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getInsertWithAutoIncrementColumnSql());
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                Statement queryStatement = connection.createStatement()) {
            preparedStatement.setInt(1, 11);
            preparedStatement.setString(2, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 12);
            preparedStatement.setString(2, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 21);
            preparedStatement.setString(2, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 22);
            preparedStatement.setString(2, "BATCH");
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
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 11))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(1));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 12))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(2));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 21))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(3));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 22))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertAddBatchWithGenerateKeyColumn() throws SQLException {
        String sql = getDatabaseTestSQL().getInsertWithAllPlaceholdersSql();
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                Statement queryStatement = connection.createStatement()) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, 11);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 2);
            preparedStatement.setInt(2, 12);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 3);
            preparedStatement.setInt(2, 21);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setInt(1, 4);
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
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 11))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(1));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 12))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(2));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 21))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(3));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(sql2, 22))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertUpdateBatch() throws SQLException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getUpdateWithoutShardingValueSql());
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
            assertThat(result[0], is(40));
            assertThat(result[1], is(0));
            assertThat(result[2], is(40));
        }
    }
    
    @Test
    public void assertClearBatch() throws SQLException {
        String sql = getDatabaseTestSQL().getInsertWithAllPlaceholdersSql();
        try (
                Connection connection = shardingDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, 3101);
            preparedStatement.setInt(2, 11);
            preparedStatement.setString(3, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.clearBatch();
            int[] result = preparedStatement.executeBatch();
            assertThat(result.length, is(0));
        }
    }
}

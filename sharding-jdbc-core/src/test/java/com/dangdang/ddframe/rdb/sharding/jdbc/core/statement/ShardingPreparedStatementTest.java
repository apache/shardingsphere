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

import com.dangdang.ddframe.rdb.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import com.dangdang.ddframe.rdb.integrate.sql.DatabaseTestSQL;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.jdbc.util.JDBCTestSQL;
import com.dangdang.ddframe.rdb.sharding.util.EventBusInstance;
import com.google.common.eventbus.Subscribe;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.dangdang.ddframe.rdb.common.util.SqlPlaceholderUtil.replacePreparedStatement;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingPreparedStatementTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    public ShardingPreparedStatementTest(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Test
    public void assertExecuteQueryWithParameter() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_COUNT_AS_ORDERS_COUNT_SQL)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
            preparedStatement.setString(1, "null");
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(0L));
            preparedStatement.setString(1, "init");
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithoutParameter() throws SQLException {
        String sql = JDBCTestSQL.SELECT_COUNT_ALIAS_SQL;
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
        }
    }
    
    
    @Test
    public void assertExecuteUpdateWithParameter() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(DatabaseTestSQL.DELETE_WITHOUT_SHARDING_VALUE_SQL))) {
            preparedStatement.setString(1, "init");
            assertThat(preparedStatement.executeUpdate(), is(4));
            preparedStatement.setString(1, "null");
            assertThat(preparedStatement.executeUpdate(), is(0));
            preparedStatement.setString(1, "init");
            assertThat(preparedStatement.executeUpdate(), is(0));
        }
    }
    
    @Test
    public void assertExecuteUpdateWithoutParameter() throws SQLException {
        String sql = String.format(DatabaseTestSQL.DELETE_WITHOUT_SHARDING_VALUE_SQL, "'init'");
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            assertThat(preparedStatement.executeUpdate(), is(4));
            assertThat(preparedStatement.executeUpdate(), is(0));
            assertThat(preparedStatement.executeUpdate(), is(0));
        }
    }
    
    @Test
    public void assertExecuteWithParameter() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_COUNT_AS_ORDERS_COUNT_SQL)) {
            preparedStatement.setString(1, "init");
            assertTrue(preparedStatement.execute());
            assertTrue(preparedStatement.getResultSet().next());
            assertThat(preparedStatement.getResultSet().getLong(1), is(4L));
            preparedStatement.setString(1, "null");
            assertTrue(preparedStatement.execute());
            // TODO 调研oracle为什么不可以
            if (DatabaseType.Oracle != getCurrentDatabaseType()) {
                assertTrue(preparedStatement.getResultSet().next());
                assertThat(preparedStatement.getResultSet().getLong(1), is(0L));
            }
            preparedStatement.setString(1, "init");
            assertTrue(preparedStatement.execute());
            // TODO 调研oracle为什么不可以
            if (DatabaseType.Oracle != getCurrentDatabaseType()) {
                assertTrue(preparedStatement.getResultSet().next());
                assertThat(preparedStatement.getResultSet().getLong(1), is(4L));
            }
        }
    }
    
    @Test
    public void assertExecuteWithoutParameter() throws SQLException {
        String sql = String.format(DatabaseTestSQL.DELETE_WITHOUT_SHARDING_VALUE_SQL, "'init'");
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            assertFalse(preparedStatement.execute());
            assertFalse(preparedStatement.execute());
            assertFalse(preparedStatement.execute());
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_COUNT_AS_ORDERS_COUNT_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_COUNT_AS_ORDERS_COUNT_SQL, ResultSet.TYPE_FORWARD_ONLY, 
                        ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithAutoGeneratedKeys() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_COUNT_AS_ORDERS_COUNT_SQL, Statement.NO_GENERATED_KEYS)) {
            preparedStatement.setString(1, "init");
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getLong(1), is(4L));
        }
    }
    
    @Test
    public void assertExecuteQueryWithColumnIndexes() throws SQLException {
        if (DatabaseType.PostgreSQL != getCurrentDatabaseType()) {
            try (
                    Connection connection = getShardingDataSource().getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_COUNT_AS_ORDERS_COUNT_SQL, new int[]{1})) {
                preparedStatement.setNull(1, java.sql.Types.VARCHAR);
                ResultSet resultSet = preparedStatement.executeQuery();
                assertTrue(resultSet.next());
                assertThat(resultSet.getLong(1), is(0L));
            }
        }
    }
    
    @Test
    public void assertExecuteQueryWithColumnNames() throws SQLException {
        if (DatabaseType.PostgreSQL != getCurrentDatabaseType()) {
            try (
                    Connection connection = getShardingDataSource().getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_COUNT_AS_ORDERS_COUNT_SQL, new String[]{"orders_count"})) {
                preparedStatement.setNull(1, java.sql.Types.VARCHAR);
                ResultSet resultSet = preparedStatement.executeQuery();
                assertTrue(resultSet.next());
                assertThat(resultSet.getLong(1), is(0L));
            }
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
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.INSERT_ORDER_ITEM_WITH_ALL_PLACEHOLDERS_SQL)) {
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
            if (DatabaseType.Oracle != getCurrentDatabaseType()) {
                for (int rs : result) {
                    assertThat(rs, is(1));
                }
            }
        }
        EventBusInstance.getInstance().unregister(listener);
    }
    
    @Test
    public void assertAddBatchWithoutGenerateKeyColumn() throws SQLException {
        // TODO 调研oracle为什么不可以
        if (DatabaseType.Oracle == getCurrentDatabaseType()) {
            return;
        }
        String sql = replacePreparedStatement(DatabaseTestSQL.INSERT_WITH_AUTO_INCREMENT_COLUMN_SQL);
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
            for (int rs : result) {
                assertThat(rs, is(1));
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
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 11, 11))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(1));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 12, 12))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(2));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 21, 21))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(3));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 22, 22))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertAddBatchWithGenerateKeyColumn() throws SQLException {
        // TODO 调研oracle为什么不可以
        if (DatabaseType.Oracle == getCurrentDatabaseType()) {
            return;
        }
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.INSERT_ORDER_ITEM_WITH_ALL_PLACEHOLDERS_SQL, Statement.RETURN_GENERATED_KEYS);
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
            for (int rs : result) {
                assertThat(rs, is(1));
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
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 11, 11))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(1));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 12, 12))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(2));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 21, 21))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(3));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(DatabaseTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 22, 22))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertUpdateBatch() throws SQLException {
        String sql = replacePreparedStatement(DatabaseTestSQL.UPDATE_WITHOUT_SHARDING_VALUE_SQL);
        try (
                Connection connection = getShardingDataSource().getConnection();
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
            if (DatabaseType.Oracle == getCurrentDatabaseType()) {
                assertThat(result[0], is(-2));
                assertThat(result[1], is(-2));
                assertThat(result[2], is(-2));
            } else {
                assertThat(result[0], is(4));
                assertThat(result[1], is(0));
                assertThat(result[2], is(4));
            }
        }
    }
    
    @Test
    public void assertClearBatch() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.INSERT_ORDER_ITEM_WITH_ALL_PLACEHOLDERS_SQL)) {
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
}

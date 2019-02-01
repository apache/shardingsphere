/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.core.statement;

import io.shardingsphere.shardingjdbc.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import io.shardingsphere.shardingjdbc.jdbc.JDBCTestSQL;
import io.shardingsphere.shardingjdbc.util.SQLPlaceholderUtil;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingPreparedStatementTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    @Test
    public void assertAddBatch() throws SQLException {
        try (
            Connection connection = getShardingDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(JDBCTestSQL.INSERT_ORDER_ITEM_WITH_ALL_PLACEHOLDERS_SQL)) {
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
            for (int rs : result) {
                assertThat(rs, is(1));
            }
        }
    }
    
    @Test
    public void assertAddBatchWithoutGenerateKeyColumn() throws SQLException {
        String sql = SQLPlaceholderUtil.replacePreparedStatement(JDBCTestSQL.INSERT_WITH_AUTO_INCREMENT_COLUMN_SQL);
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
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 11, 11))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(1));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 12, 12))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(2));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 21, 21))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(3));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 22, 22))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertAddBatchWithGenerateKeyColumn() throws SQLException {
        try (
            Connection connection = getShardingDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(JDBCTestSQL.INSERT_ORDER_ITEM_WITH_ALL_PLACEHOLDERS_SQL, Statement.RETURN_GENERATED_KEYS);
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
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 11, 11))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(1));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 12, 12))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(2));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 21, 21))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(3));
            }
            try (ResultSet rs = queryStatement.executeQuery(String.format(JDBCTestSQL.SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL, 22, 22))) {
                assertTrue(rs.next());
                assertThat(rs.getInt(1), is(4));
            }
        }
    }
    
    @Test
    public void assertUpdateBatch() throws SQLException {
        String sql = SQLPlaceholderUtil.replacePreparedStatement(JDBCTestSQL.UPDATE_WITHOUT_SHARDING_VALUE_SQL);
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
            assertThat(result[0], is(4));
            assertThat(result[1], is(0));
            assertThat(result[2], is(4));
        }
    }
    
    @Test
    public void assertClearBatch() throws SQLException {
        try (
            Connection connection = getShardingDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(JDBCTestSQL.INSERT_ORDER_ITEM_WITH_ALL_PLACEHOLDERS_SQL)) {
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

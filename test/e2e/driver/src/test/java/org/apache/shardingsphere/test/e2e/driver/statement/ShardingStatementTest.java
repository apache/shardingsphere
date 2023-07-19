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

package org.apache.shardingsphere.test.e2e.driver.statement;

import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.test.e2e.driver.AbstractShardingDriverTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingStatementTest extends AbstractShardingDriverTest {
    
    @Test
    void assertGetGeneratedKeys() throws SQLException {
        String sql = "INSERT INTO t_order_item(order_id, user_id, status) VALUES (%d, %d, '%s')";
        try (
                Connection connection = getShardingSphereDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            assertFalse(statement.execute(String.format(sql, 1, 1, "init")));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), Statement.NO_GENERATED_KEYS));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), Statement.RETURN_GENERATED_KEYS));
            ResultSet generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(3L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new int[]{1}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(4L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new String[]{"user_id"}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(5L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new int[]{2}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(6L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new String[]{"status"}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(7L));
        }
    }
    
    @Test
    void assertAddGetGeneratedKeysForNoGeneratedValues() throws SQLException {
        String sql = "INSERT INTO t_product (product_name) VALUES ('%s')";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            statement.execute(String.format(sql, "cup"), Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getInt(1), is(1));
        }
    }
    
    @Test
    void assertQueryWithNull() throws SQLException {
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery(null));
        }
    }
    
    @Test
    void assertQueryWithEmptyString() throws SQLException {
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery(""));
        }
    }
    
    @Test
    void assertExecuteGetResultSet() throws SQLException {
        String sql = "UPDATE t_order_item SET status = '%s' WHERE user_id = %d AND order_id = %d";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            assertFalse(statement.execute(String.format(sql, "OK", 1, 1)));
            assertNull(statement.getResultSet());
        }
    }
    
    @Test
    void assertExecuteUpdateGetResultSet() throws SQLException {
        String sql = "UPDATE t_order_item SET status = '%s' WHERE user_id = %d AND order_id = %d";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            statement.executeUpdate(String.format(sql, "OK", 1, 1));
            assertNull(statement.getResultSet());
        }
    }
    
    @Test
    void assertColumnNotFoundException() throws SQLException {
        String sql = "UPDATE t_order_item SET error_column = '%s'";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeUpdate(String.format(sql, "OK")));
        }
    }
    
    @Test
    void assertShowDatabases() throws SQLException {
        String sql = "SHOW DATABASES";
        try (Statement statement = getShardingSphereDataSource().getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            assertTrue(resultSet.next());
            assertThat(resultSet.getString(1), is(DefaultDatabase.LOGIC_NAME));
        }
    }
    
    @Test
    void assertExecuteBatch() throws SQLException {
        try (Connection connection = getShardingSphereDataSource().getConnection(); Statement statement = connection.createStatement()) {
            statement.addBatch("UPDATE t_order SET status = 'closed' WHERE order_id = 1001");
            statement.addBatch("UPDATE t_order SET status = 'closed' WHERE order_id = 1100 OR order_id = 1101");
            statement.addBatch("DELETE FROM t_order WHERE order_id = 1000");
            assertThat(statement.executeBatch(), is(new int[]{1, 2, 1}));
            statement.clearBatch();
            assertThat(statement.executeBatch(), is(new int[0]));
        }
    }
}

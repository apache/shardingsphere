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

package org.apache.shardingsphere.driver;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ServiceLoader;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereDriverTest {
    
    @Test
    void assertJavaSqlDriverRegistered() {
        assertTrue(isShardingSphereDriverSPIExisting(), "Could not load ShardingSphereDriver from META-INF/services/java.sql.Driver");
    }
    
    private boolean isShardingSphereDriverSPIExisting() {
        for (Driver each : ServiceLoader.load(Driver.class)) {
            if (each instanceof ShardingSphereDriver) {
                return true;
            }
        }
        return false;
    }
    
    @Test
    void assertConnectWithInvalidURL() {
        assertThrows(SQLException.class, () -> DriverManager.getConnection("jdbc:invalid:xxx"));
    }
    
    @Test
    void assertDriverWorks() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, isA(ShardingSphereConnection.class));
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT)");
            statement.execute("CREATE INDEX idx_uid ON t_order (user_id)");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 101), (2, 102)");
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM t_order")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(2));
            }
        }
    }
    
    @Test
    void assertHashModSetLongOnIntColumnWorks() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml")) {
            assertThat(connection, isA(ShardingSphereConnection.class));
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS t_order");
                statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT)");
            }
            int value = -1;
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (order_id, user_id) VALUES (?, ?)")) {
                preparedStatement.setObject(1, value);
                preparedStatement.setObject(2, 101);
                int updatedCount = preparedStatement.executeUpdate();
                assertThat(updatedCount, is(1));
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order WHERE order_id = ?")) {
                preparedStatement.setObject(1, value);
                ResultSet resultSet = preparedStatement.executeQuery();
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(value));
                resultSet.close();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM t_order WHERE order_id = ?")) {
                preparedStatement.setObject(1, (long) value);
                ResultSet resultSet = preparedStatement.executeQuery();
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(value));
                resultSet.close();
            }
        }
    }
    
    @Test
    void assertVarbinaryColumnWorks() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, isA(ShardingSphereConnection.class));
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id VARBINARY(64) PRIMARY KEY, user_id INT)");
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (order_id, user_id) VALUES (?, ?)");
            preparedStatement.setBytes(1, new byte[]{-1, 0, 1});
            preparedStatement.setInt(2, 101);
            int updatedCount = preparedStatement.executeUpdate();
            assertThat(updatedCount, is(1));
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM t_order")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(1));
            }
        }
    }
    
    @Test
    void assertDatabaseNameTransparentWithHintManager() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, isA(ShardingSphereConnection.class));
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT)");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 101), (2, 102)");
            try (HintManager hintManager = HintManager.getInstance()) {
                executeQueryWithHintManager(hintManager, statement);
            }
        }
    }
    
    private void executeQueryWithHintManager(final HintManager hintManager, final Statement statement) throws SQLException {
        hintManager.setDataSourceName("ds_0");
        try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM t_order_0")) {
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
        }
    }
    
    @Test
    void assertGetMaxRowsWhenSetMaxRowsForStatement() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, isA(ShardingSphereConnection.class));
            statement.setMaxRows(100);
            assertThat(statement.getMaxRows(), is(100));
            statement.execute("DROP TABLE IF EXISTS t_max_rows_test");
            statement.execute("CREATE TABLE t_max_rows_test (id INT PRIMARY KEY, name VARCHAR(50) NOT NULL)");
            statement.execute("INSERT INTO t_max_rows_test (id, name) VALUES (1, 'test1')");
            try (ResultSet resultSet = statement.executeQuery("SELECT id, name FROM t_max_rows_test")) {
                assertThat(statement.getMaxRows(), is(100));
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(1));
                assertThat(resultSet.getString(2), is("test1"));
                assertFalse(resultSet.next());
            }
        }
    }
    
    @Test
    void assertGetMaxRowsWhenSetMaxRowsForPreparedStatement() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, isA(ShardingSphereConnection.class));
            statement.execute("DROP TABLE IF EXISTS t_max_rows_ps_test");
            statement.execute("CREATE TABLE t_max_rows_ps_test (id INT PRIMARY KEY, name VARCHAR(50) NOT NULL)");
            statement.execute("INSERT INTO t_max_rows_ps_test (id, name) VALUES (1, 'test1')");
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, name FROM t_max_rows_ps_test")) {
                preparedStatement.setMaxRows(100);
                assertThat(preparedStatement.getMaxRows(), is(100));
                assertGetMaxRowsAndResultsetWhenSetMaxRowsForPreparedStatement(preparedStatement);
            }
        }
    }
    
    private void assertGetMaxRowsAndResultsetWhenSetMaxRowsForPreparedStatement(final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            assertThat(preparedStatement.getMaxRows(), is(100));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertThat(resultSet.getString(2), is("test1"));
            assertFalse(resultSet.next());
        }
    }
}

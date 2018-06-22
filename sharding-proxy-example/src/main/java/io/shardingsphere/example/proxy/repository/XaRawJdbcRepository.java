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

package io.shardingsphere.example.proxy.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class XaRawJdbcRepository {
    
    private final DataSource dataSource;
    
    public XaRawJdbcRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void demo() throws SQLException {
        createTable();
        insertData();
        insertFailure();
        System.out.println("1.Query with EQUAL--------------");
        queryWithEqual();
        System.out.println("2.Query with IN--------------");
        queryWithIn();
        dropTable();
    }
    
    private void createTable() throws SQLException {
        execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        execute("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (order_item_id))");
    }
    
    private void insertData() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        try {
            for (int i = 1; i < 10; i++) {
                long orderId = insertAndGetGeneratedKey(statement,"INSERT INTO t_order (user_id, status) VALUES (10, 'INIT')");
                statement.execute(String.format("INSERT INTO t_order_item (order_id, user_id) VALUES (%d, 10)", orderId));
                orderId = insertAndGetGeneratedKey(statement,"INSERT INTO t_order (user_id, status) VALUES (11, 'INIT')");
                statement.execute(String.format("INSERT INTO t_order_item (order_id, user_id) VALUES (%d, 11)", orderId));
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
        }
        finally {
            connection.close();
            statement.close();
        }
    }
    
    private void insertFailure() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        try {
            for (int i = 1; i < 10; i++) {
                long orderId = insertAndGetGeneratedKey(statement,"INSERT INTO t_order (user_id, status) VALUES (10, 'INIT')");
                statement.execute(String.format("INSERT INTO t_order_item (order_id, user_id) VALUES (%d, 10)", orderId));
                orderId = insertAndGetGeneratedKey(statement,"INSERT INTO t_order (user_id, status) VALUES (11, 'INIT')");
                statement.execute(String.format("INSERT INTO t_order_item (order_id, user_id) VALUES (%d, 11)", orderId));
            }
            
            int i = 10 / 0;
            connection.commit();
        } catch (Exception ex) {
            connection.rollback();
        }
        finally {
            connection.close();
            statement.close();
        }
    }
    
    private long insertAndGetGeneratedKey(final Statement statement, final String sql) throws SQLException {
        long result = -1;
        statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) {
                result = resultSet.getLong(1);
            }
        }
        return result;
    }
    
    private void queryWithEqual() throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=10";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
//            preparedStatement.setInt(1, 10);
            printQuery(statement, sql);
        }
    }
    
    private void queryWithIn() throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id IN (?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 11);
            printQuery(preparedStatement, sql);
        }
    }
    
    private void printQuery(final Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                System.out.print("order_item_id:" + resultSet.getLong(1) + ", ");
                System.out.print("order_id:" + resultSet.getLong(2) + ", ");
                System.out.print("user_id:" + resultSet.getInt(3));
                System.out.println();
            }
        }
    }
    
    private void dropTable() throws SQLException {
        execute("DROP TABLE t_order_item");
        execute("DROP TABLE t_order");
    }
    
    private void execute(final String sql) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}

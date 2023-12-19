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

package org.apache.shardingsphere.test.natived.jdbc.commons.repository;

import org.apache.shardingsphere.test.natived.jdbc.commons.entity.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public final class OrderRepository {
    
    private final DataSource dataSource;
    
    public OrderRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * create table if not exists in MySQL.
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsInMySQL() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_order "
                + "(order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * create table if not exists in Postgres.
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsInPostgres() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_order (\n"
                + "    order_id BIGSERIAL PRIMARY KEY,\n"
                + "    order_type INTEGER,\n"
                + "    user_id INTEGER NOT NULL,\n"
                + "    address_id BIGINT NOT NULL,\n"
                + "    status VARCHAR(50)\n"
                + ");";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * create table in MS SQL Server. `order_item_id` is not set to `IDENTITY(1,1)` to simplify the unit test.
     * This also ignored the default schema of the `dbo`.
     * @throws SQLException SQL exception
     */
    public void createTableInSQLServer() throws SQLException {
        String sql = "CREATE TABLE [t_order] (\n"
                + "    order_id bigint NOT NULL,\n"
                + "    order_type int NOT NULL,\n"
                + "    user_id int NOT NULL,\n"
                + "    address_id bigint NOT NULL,\n"
                + "    status varchar(50),\n"
                + "    PRIMARY KEY (order_id)\n"
                + ");";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * drop table.
     * TODO There is a bug in this function in shadow's unit test and requires additional fixes.
     * @throws SQLException SQL exception
     */
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_order";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * truncate table.
     * @throws SQLException SQL exception
     */
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_order";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * create shadow table if not exists.
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsShadow() throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), "
                        + "user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id)) "
                        + "/* SHARDINGSPHERE_HINT:shadow=true,foo=bar*/";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * drop shadow table.
     * @throws SQLException SQL exception
     */
    public void dropTableShadow() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_order /* SHARDINGSPHERE_HINT:shadow=true,foo=bar*/";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * truncate shadow table.
     * @throws SQLException SQL exception
     */
    public void truncateTableShadow() throws SQLException {
        String sql = "TRUNCATE TABLE t_order /* SHARDINGSPHERE_HINT:shadow=true,foo=bar*/";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * select Order from shadow table.
     * @return list of Order
     * @throws SQLException SQL exception
     */
    public List<Order> selectShadowOrder() throws SQLException {
        String sql = "SELECT * FROM t_order WHERE order_type=1";
        return getOrders(sql);
    }
    
    /**
     * delete Order from shadow table.
     * @param orderId orderId
     * @throws SQLException SQL Exception
     */
    public void deleteShadow(final Long orderId) throws SQLException {
        String sql = "DELETE FROM t_order WHERE order_id=? AND order_type=1";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderId);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * insert Order to table.
     * @param order order
     * @return orderId of the insert statement
     * @throws SQLException SQL Exception
     */
    public Long insert(final Order order) throws SQLException {
        String sql = "INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setInt(2, order.getOrderType());
            preparedStatement.setLong(3, order.getAddressId());
            preparedStatement.setString(4, order.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getLong(1));
                }
            }
        }
        return order.getOrderId();
    }
    
    /**
     * delete by orderId.
     * @param orderId orderId
     * @throws SQLException SQL exception
     */
    public void delete(final Long orderId) throws SQLException {
        String sql = "DELETE FROM t_order WHERE order_id=?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderId);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * select all.
     * @return list of Order
     * @throws SQLException SQL exception
     */
    public List<Order> selectAll() throws SQLException {
        return getOrders("SELECT * FROM t_order");
    }
    
    /**
     * get Orders by SQL.
     * @param sql SQL
     * @return list of Order
     * @throws SQLException SQL exception
     */
    private List<Order> getOrders(final String sql) throws SQLException {
        List<Order> result = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getLong(1));
                order.setOrderType(resultSet.getInt(2));
                order.setUserId(resultSet.getInt(3));
                order.setAddressId(resultSet.getLong(4));
                order.setStatus(resultSet.getString(5));
                result.add(order);
            }
        }
        return result;
    }
}

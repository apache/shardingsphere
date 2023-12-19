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

import org.apache.shardingsphere.test.natived.jdbc.commons.entity.OrderItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public final class OrderItemRepository {
    
    private final DataSource dataSource;
    
    public OrderItemRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * create table if not exists in MySQL.
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsInMySQL() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_order_item "
                + "(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id))";
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
        String sql = "CREATE TABLE IF NOT EXISTS t_order_item (\n"
                + "    order_item_id BIGSERIAL PRIMARY KEY,\n"
                + "    order_id BIGINT NOT NULL,\n"
                + "    user_id INTEGER NOT NULL,\n"
                + "    phone VARCHAR(50),\n"
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
        String sql = "CREATE TABLE [t_order_item] (\n"
                + "    order_item_id bigint NOT NULL,\n"
                + "    order_id bigint NOT NULL,\n"
                + "    user_id int NOT NULL,\n"
                + "    phone varchar(50),\n"
                + "    status varchar(50),\n"
                + "    PRIMARY KEY (order_item_id)\n"
                + ");";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * drop table.
     * @throws SQLException SQL exception
     */
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_order_item";
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
        String sql = "TRUNCATE TABLE t_order_item";
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
        String sql = "CREATE TABLE IF NOT EXISTS t_order_item "
                + "(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone "
                + "VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id)) /* SHARDINGSPHERE_HINT:shadow=true,foo=bar*/";
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
        String sql = "DROP TABLE t_order_item /* SHARDINGSPHERE_HINT:shadow=true,foo=bar*/";
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
        String sql = "TRUNCATE TABLE t_order_item /* SHARDINGSPHERE_HINT:shadow=true,foo=bar*/";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * insert something to table.
     * @param orderItem orderItem
     * @return orderItemId of the insert statement
     * @throws SQLException SQL exception
     */
    public Long insert(final OrderItem orderItem) throws SQLException {
        String sql = "INSERT INTO t_order_item (order_id, user_id, phone, status) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, orderItem.getOrderId());
            preparedStatement.setInt(2, orderItem.getUserId());
            preparedStatement.setString(3, orderItem.getPhone());
            preparedStatement.setString(4, orderItem.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    orderItem.setOrderItemId(resultSet.getLong(1));
                }
            }
        }
        return orderItem.getOrderItemId();
    }
    
    /**
     * delete by orderItemId.
     * @param orderItemId orderItemId
     * @throws SQLException SQL exception
     */
    public void delete(final Long orderItemId) throws SQLException {
        String sql = "DELETE FROM t_order_item WHERE order_id=?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderItemId);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * select all.
     * @return list of OrderItem
     * @throws SQLException SQL exception
     */
    public List<OrderItem> selectAll() throws SQLException {
        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        List<OrderItem> result = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderItemId(resultSet.getLong(1));
                orderItem.setOrderId(resultSet.getLong(2));
                orderItem.setUserId(resultSet.getInt(3));
                orderItem.setPhone(resultSet.getString(4));
                orderItem.setStatus(resultSet.getString(5));
                result.add(orderItem);
            }
        }
        return result;
    }
}

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

package org.apache.shardingsphere.example.core.jdbc.repository;

import org.apache.shardingsphere.example.core.api.ExampleDatabaseType;
import org.apache.shardingsphere.example.core.api.entity.Order;
import org.apache.shardingsphere.example.core.api.repository.OrderRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class OrderRepositoryImpl implements OrderRepository {
    
    private final ExampleDatabaseType databaseType;
    
    private final DataSource dataSource;
    
    public OrderRepositoryImpl(final DataSource dataSource) {
        this(ExampleDatabaseType.MYSQL, dataSource);
    }
    
    public OrderRepositoryImpl(final ExampleDatabaseType databaseType, final DataSource dataSource) {
        this.databaseType = databaseType;
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_order ("
            + (ExampleDatabaseType.POSTGRESQL == databaseType ? "order_id BIGSERIAL NOT NULL" : "order_id BIGINT NOT NULL AUTO_INCREMENT")
            + ", user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE t_order";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_order";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public Long insert(final Order order) throws SQLException {
        String sql = "INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setLong(2, order.getAddressId());
            preparedStatement.setString(3, order.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getLong(1));
                }
            }
        }
        return order.getOrderId();
    }
    
    @Override
    public void delete(final Long orderId) throws SQLException {
        String sql = "DELETE FROM t_order WHERE order_id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderId);
            preparedStatement.executeUpdate();
        }
    }
    
    @Override
    public List<Order> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_order";
        return getOrders(sql);
    }
    
    protected List<Order> getOrders(final String sql) throws SQLException {
        List<Order> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getLong(1));
                order.setUserId(resultSet.getInt(2));
                order.setAddressId(resultSet.getLong(3));
                order.setStatus(resultSet.getString(4));
                result.add(order);
            }
        }
        return result;
    }
}

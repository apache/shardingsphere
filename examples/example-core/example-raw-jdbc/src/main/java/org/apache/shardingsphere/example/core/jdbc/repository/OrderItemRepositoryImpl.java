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

import org.apache.shardingsphere.example.core.api.repository.OrderItemRepository;
import org.apache.shardingsphere.example.core.api.entity.OrderItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class OrderItemRepositoryImpl implements OrderItemRepository {
    
    private final DataSource dataSource;
    
    public OrderItemRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_order_item "
            + "(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE t_order_item";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_order_item";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public Long insert(final OrderItem orderItem) throws SQLException {
        String sql = "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, orderItem.getOrderId());
            preparedStatement.setInt(2, orderItem.getUserId());
            preparedStatement.setString(3, orderItem.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    orderItem.setOrderItemId(resultSet.getLong(1));
                }
            }
        }
        return orderItem.getOrderItemId();
    }
    
    @Override
    public void delete(final Long orderItemId) throws SQLException {
        String sql = "DELETE FROM t_order_item WHERE order_id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderItemId);
            preparedStatement.executeUpdate();
        }
    }
    
    @Override
    public List<OrderItem> selectAll() throws SQLException {
        // TODO Associated query with encrypt may query and decrypt failed. see https://github.com/apache/shardingsphere/issues/3352
//        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        String sql = "SELECT * FROM t_order_item";
        return getOrderItems(sql);
    }
    
    protected List<OrderItem> getOrderItems(final String sql) throws SQLException {
        List<OrderItem> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderItemId(resultSet.getLong(1));
                orderItem.setOrderId(resultSet.getLong(2));
                orderItem.setUserId(resultSet.getInt(3));
                orderItem.setStatus(resultSet.getString(4));
                result.add(orderItem);
            }
        }
        return result;
    }
}

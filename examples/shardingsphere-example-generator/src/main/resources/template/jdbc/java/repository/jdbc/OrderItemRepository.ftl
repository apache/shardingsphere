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
<#assign package = feature?replace('-', '')?replace(',', '.') />

package org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository;

import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity.OrderItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class OrderItemRepository {
    
    private final DataSource dataSource;
    
    public OrderItemRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_order_item " +
                        "(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE t_order_item";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_order_item";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
<#if feature?contains("shadow")>
    
    public void createTableIfNotExistsShadow() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_order_item "
            + "(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50), PRIMARY KEY (order_item_id)) /*shadow:true,foo:bar*/";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    public void dropTableShadow() throws SQLException {
        String sql = "DROP TABLE t_order_item /*shadow:true,foo:bar*/";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    public void truncateTableShadow() throws SQLException {
        String sql = "TRUNCATE TABLE t_order_item /*shadow:true,foo:bar*/";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
</#if>
    
    public Long insert(final OrderItem orderItem) throws SQLException {
        String sql = "INSERT INTO t_order_item (order_id, user_id, phone, status) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
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
    
    public void delete(final Long orderItemId) throws SQLException {
        String sql = "DELETE FROM t_order_item WHERE order_id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderItemId);
            preparedStatement.executeUpdate();
        }
    }
    
    public List<OrderItem> selectAll() throws SQLException {
        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        List<OrderItem> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
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

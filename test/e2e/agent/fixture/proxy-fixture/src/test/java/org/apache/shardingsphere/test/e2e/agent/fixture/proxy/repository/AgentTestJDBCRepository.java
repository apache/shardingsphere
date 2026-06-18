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

package org.apache.shardingsphere.test.e2e.agent.fixture.proxy.repository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.agent.fixture.proxy.entity.OrderEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Agent test JDBC repository.
 */
@RequiredArgsConstructor
public final class AgentTestJDBCRepository {
    
    private final Connection connection;
    
    /**
     * Insert order.
     *
     * @param orderEntity order entity
     */
    @SneakyThrows(SQLException.class)
    public void insertOrder(final OrderEntity orderEntity) {
        String sql = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            preparedStatement.setLong(1, orderEntity.getOrderId());
            preparedStatement.setInt(2, orderEntity.getUserId());
            preparedStatement.setString(3, orderEntity.getStatus());
            preparedStatement.executeUpdate();
            connection.commit();
        }
    }
    
    /**
     * Insert order and rollback.
     *
     * @param orderEntity order entity
     */
    @SneakyThrows(SQLException.class)
    public void insertOrderAndRollback(final OrderEntity orderEntity) {
        String sql = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            preparedStatement.setLong(1, orderEntity.getOrderId());
            preparedStatement.setInt(2, orderEntity.getUserId());
            preparedStatement.setString(3, orderEntity.getStatus());
            preparedStatement.executeUpdate();
            connection.rollback();
        }
    }
    
    /**
     * Delete order.
     *
     * @param orderId to be deleted order ID
     */
    @SneakyThrows(SQLException.class)
    public void deleteOrder(final Long orderId) {
        String sql = "DELETE FROM t_order WHERE order_id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderId);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Update order.
     *
     * @param orderEntity to be updated order entity
     */
    @SneakyThrows(SQLException.class)
    public void updateOrder(final OrderEntity orderEntity) {
        String sql = "UPDATE t_order SET status = ? WHERE order_id =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            preparedStatement.setString(1, orderEntity.getStatus());
            preparedStatement.setLong(2, orderEntity.getOrderId());
            preparedStatement.executeUpdate();
            connection.commit();
        }
    }
    
    /**
     * Query all orders.
     *
     * @return all orders
     */
    @SneakyThrows(SQLException.class)
    public Collection<OrderEntity> queryAllOrders() {
        String sql = "SELECT * FROM t_order";
        Collection<OrderEntity> result = new LinkedList<>();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                result.add(new OrderEntity(resultSet.getLong(1), resultSet.getInt(2), resultSet.getString(3)));
            }
        }
        return result;
    }
    
    /**
     * Query failed.
     */
    @SneakyThrows(SQLException.class)
    public void queryFailed() {
        String sql = "SELECT * FROM non_existent_table";
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery(sql);
        }
    }
}

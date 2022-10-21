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

package org.apache.shardingsphere.integration.agent.test.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.integration.agent.test.common.entity.OrderEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * JDBC agent test utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCAgentTestUtils {
    
    /**
     * Insert order.
     *
     * @param orderEntity order entity
     * @param dataSource data source
     */
    public static void insertOrder(final OrderEntity orderEntity, final DataSource dataSource) {
        String sql = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            preparedStatement.setLong(1, orderEntity.getOrderId());
            preparedStatement.setInt(2, orderEntity.getUserId());
            preparedStatement.setString(3, orderEntity.getStatus());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (final SQLException ignored) {
        }
    }
    
    /**
     * Insert order rollback.
     *
     * @param orderEntity order entity
     * @param dataSource data source
     */
    public static void insertOrderRollback(final OrderEntity orderEntity, final DataSource dataSource) {
        String sql = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            preparedStatement.setLong(1, orderEntity.getOrderId());
            preparedStatement.setInt(2, orderEntity.getUserId());
            preparedStatement.setString(3, orderEntity.getStatus());
            preparedStatement.executeUpdate();
            connection.rollback();
        } catch (final SQLException ignored) {
        }
    }
    
    /**
     * Delete order by order id.
     *
     * @param orderId order id
     * @param dataSource data source
     */
    public static void deleteOrderByOrderId(final Long orderId, final DataSource dataSource) {
        String sql = "DELETE FROM t_order WHERE order_id=?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderId);
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }
    
    /**
     * Update order status.
     *
     * @param orderEntity order entity
     * @param dataSource data source
     */
    public static void updateOrderStatus(final OrderEntity orderEntity, final DataSource dataSource) {
        String sql = "UPDATE t_order SET status = ? WHERE order_id =?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            preparedStatement.setString(1, orderEntity.getStatus());
            preparedStatement.setLong(2, orderEntity.getOrderId());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (final SQLException ignored) {
        }
    }
    
    /**
     * Select all orders collection.
     *
     * @param dataSource data source
     * @return collection
     */
    public static Collection<OrderEntity> selectAllOrders(final DataSource dataSource) {
        String sql = "SELECT * FROM t_order";
        return getOrders(sql, dataSource);
    }
    
    private static Collection<OrderEntity> getOrders(final String sql, final DataSource dataSource) {
        Collection<OrderEntity> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                OrderEntity orderEntity = new OrderEntity(resultSet.getLong(1), resultSet.getInt(2), resultSet.getString(3));
                result.add(orderEntity);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }
}

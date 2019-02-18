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

package io.shardingsphere.example.repository.jdbc.repository;

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.repository.OrderRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseOrderRepository implements OrderRepository {
    
    static final String SQL_INSERT_T_ORDER = "INSERT INTO t_order (user_id, status) VALUES (?, ?)";
    
    static final String SQL_DELETE_BY_ORDER_ID = "DELETE FROM t_order WHERE order_id=?";
    
    private static final String SQL_CREATE_T_ORDER = "CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))";
    
    private static final String SQL_DROP_T_ORDER = "DROP TABLE t_order";
    
    private static final String SQL_TRUNCATE_T_ORDER = "TRUNCATE TABLE t_order";
    
    private static final String SQL_SELECT_T_ORDER_ALL = "SELECT * FROM t_order";
    
    private static final String SQL_SELECT_T_ORDER_RANGE = "SELECT * FROM t_order WHERE order_id BETWEEN 200000000000000000 AND 400000000000000000";
    
    final void createOrderTableNotExist(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_CREATE_T_ORDER);
    }
    
    final void dropOrderTable(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_DROP_T_ORDER);
    }
    
    final void truncateOrderTable(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_TRUNCATE_T_ORDER);
    }
    
    final void insertOrder(final PreparedStatement preparedStatement, final Order order) throws SQLException {
        preparedStatement.setInt(1, order.getUserId());
        preparedStatement.setString(2, order.getStatus());
        preparedStatement.executeUpdate();
        try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                order.setOrderId(resultSet.getLong(1));
            }
        }
    }
    
    final void deleteById(final PreparedStatement preparedStatement, final Long orderId) throws SQLException {
        preparedStatement.setLong(1, orderId);
        preparedStatement.executeUpdate();
    }
    
    final List<Order> queryOrder(final PreparedStatement preparedStatement) {
        List<Order> result = new LinkedList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getLong(1));
                order.setUserId(resultSet.getInt(2));
                order.setStatus(resultSet.getString(3));
                result.add(order);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }
    
    @Override
    public final List<Order> selectAll() {
        return getOrders(SQL_SELECT_T_ORDER_ALL);
    }
    
    @Override
    public final List<Order> selectRange() {
        return getOrders(SQL_SELECT_T_ORDER_RANGE);
    }
    
    public abstract List<Order> getOrders(String sql);
    
}

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

import io.shardingsphere.example.repository.api.entity.OrderItem;
import io.shardingsphere.example.repository.api.repository.OrderItemRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseOrderEncryptRepository implements OrderItemRepository {
    
    static final String SQL_INSERT_T_ORDER_ITEM = "INSERT INTO t_order_encrypt (order_id, user_id, md5_id, aes_id, aes_query_id) VALUES (?, ?, ?, ?, ？)";
    
    static final String SQL_DELETE_BY_ITEM_ID = "DELETE FROM t_order_encrypt WHERE order_id=?";
    
    private static final String SQL_CREATE_T_ORDER_ITEM = "CREATE TABLE IF NOT EXISTS t_order_item "
        + "(order_id BIGINT NOT NULL, user_id INT NOT NULL, md5_id VARCHAR(200), aed_id VARCHAR(200), aed_query_id VARCHAR(200), PRIMARY KEY (order_id))";
    
    private static final String SQL_DROP_T_ORDER_ITEM = "DROP TABLE t_order_encrypt";
    
    private static final String SQL_TRUNCATE_T_ORDER_ITEM = "TRUNCATE TABLE t_order_encrypt";
    
    private static final String SQL_SELECT_T_ORDER_ITEM_ALL = "SELECT e.* FROM t_order o, t_order_encrypt e WHERE o.order_id = e.order_id";
    
    private static final String SQL_SELECT_T_ORDER_ITEM_RANGE = "SELECT e.* FROM t_order o, t_order_encrypt e WHERE o.order_id = e.order_id AND o.user_id BETWEEN 1 AND 5";
    
    final void createItemTableNotExist(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_CREATE_T_ORDER_ITEM);
    }
    
    final void dropItemTable(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_DROP_T_ORDER_ITEM);
    }
    
    final void truncateItemTable(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_TRUNCATE_T_ORDER_ITEM);
    }
    
    
    final void insertItem(final PreparedStatement preparedStatement, final OrderItem orderItem) throws SQLException {
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
    
    final void deleteById(final PreparedStatement preparedStatement, final Long orderItemId) throws SQLException {
        preparedStatement.setLong(1, orderItemId);
        preparedStatement.executeUpdate();
    }
    
    final List<OrderItem> queryOrderItem(final PreparedStatement preparedStatement) {
        List<OrderItem> result = new LinkedList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderItemId(resultSet.getLong(1));
                orderItem.setOrderId(resultSet.getLong(2));
                orderItem.setUserId(resultSet.getInt(3));
                orderItem.setStatus(resultSet.getString(4));
                result.add(orderItem);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }
    
    @Override
    public final List<OrderItem> selectAll() {
        return getOrderItems(SQL_SELECT_T_ORDER_ITEM_ALL);
    }
    
    @Override
    public final List<OrderItem> selectRange() {
        return getOrderItems(SQL_SELECT_T_ORDER_ITEM_RANGE);
    }
    
    public abstract List<OrderItem> getOrderItems(String sql);
    
}

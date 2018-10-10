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

package io.shardingsphere.example.repository.jdbc.repository;

import io.shardingsphere.example.repository.api.entity.OrderItem;
import io.shardingsphere.example.repository.api.repository.OrderItemRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class XaOrderItemRepository implements OrderItemRepository {

    private final DataSource dataSource;

    public XaOrderItemRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() {
        execute("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))");
    }
    
    @Override
    public void dropTable() {
        execute("DROP TABLE t_order_item");
    }
    
    @Override
    public void truncateTable() {
        execute("truncate table t_order_item");
    }
    
    @Override
    public Long insert(final OrderItem entity) {
        insertFailure(entity);
        return insertSuccess(entity);
    }
    
    @Override
    public void delete(final Long id) {
        execute(String.format("delete from t_order_item where order_item_id = %d", id));
    }
    
    @Override
    public List<OrderItem> selectAll() {
        List<OrderItem> result = new LinkedList<>();
        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            result.addAll(getOrderItems(resultSet));
        } catch (final SQLException ignored) {
        }
        return result;
    }
    
    private List<OrderItem> getOrderItems(final ResultSet resultSet) {
        List<OrderItem> result = new LinkedList<>();
        try {
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
    
    private Long insertSuccess(final OrderItem orderItem) {
        long orderItemId = -1;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            setAutoCommit(connection);
            orderItemId = insertAndGetGeneratedKey(statement, String.format("INSERT INTO t_order_item (order_id, user_id, status) VALUES (%s, %s,'%s')", orderItem.getOrderId(), orderItem.getUserId(), orderItem.getStatus()));
            orderItem.setOrderItemId(orderItemId);
            commit(connection);
        } catch (final SQLException ignored) {
        }
        return orderItemId;
    }
    
    private void insertFailure(final OrderItem orderItem) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            setAutoCommit(connection);
            statement = connection.createStatement();
            long orderItemId = insertAndGetGeneratedKey(statement, String.format("INSERT INTO t_order_item (order_id, user_id, status) VALUES (%s, %s,'%s')", orderItem.getOrderId(), orderItem.getUserId(), orderItem.getStatus()));
            orderItem.setOrderId(orderItemId);
            makeException();
            commit(connection);
        } catch (final Exception ex) {
            rollback(connection);
        }
        finally {
            close(connection, statement);
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
    
    private void close(final Connection connection, final Statement statement) {
        try {
            if (null != connection) {
                connection.close();
            }
            if (null != statement) {
                statement.close();
            }
        } catch (final SQLException ignored) {
        }
    }
    
    private void makeException() {
        System.out.println(10 / 0);
    }
    
    private void setAutoCommit(final Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (final SQLException ignored) {
        }
    }
    
    private void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (final SQLException ignored) {
        }
    }
    
    private void rollback(final Connection connection) {
        if (null == connection) {
            return;
        }
        try {
            connection.rollback();
        } catch (final SQLException ignored) {
        }
    }
    
    private void execute(final String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (final SQLException ignored) {
        }
    }
}

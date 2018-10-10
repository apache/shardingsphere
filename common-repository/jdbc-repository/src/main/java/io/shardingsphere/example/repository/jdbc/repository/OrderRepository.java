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

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.repository.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class OrderRepository implements Repository<Order> {
    
    private final DataSource dataSource;
    
    private final boolean isXA;
    
    public OrderRepository(final DataSource dataSource) {
        this(dataSource, false);
    }
    
    public OrderRepository(final DataSource dataSource, final boolean isXA) {
        this.dataSource = dataSource;
        this.isXA = isXA;
    }
    
    @Override
    public void createIfNotExistsTable() {
        execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        execute("CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (order_item_id))");
    }
    
    @Override
    public void dropTable() {
        execute("DROP TABLE t_order");
    }
    
    @Override
    public void truncateTable() {
        execute("truncate table t_order");
    }
    
    @Override
    public Long insert(final Order order) {
        Connection connection = null;
        Statement statement = null;
        long orderId = -1;
        try {
            connection = dataSource.getConnection();
            setAutoCommit(connection);
            statement = connection.createStatement();
            orderId = insertAndGetGeneratedKey(statement, String.format("INSERT INTO t_order (user_id, status) VALUES (%s, '%s')", order.getUserId(), order.getStatus()));
            order.setOrderId(orderId);
            commit(connection);
        } catch (SQLException ex) {
            rollback(connection);
        }
        finally {
            close(connection, statement);
        }
        return orderId;
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
        if (null != connection && null != statement) {
            try {
                connection.close();
                statement.close();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    @Override
    public void delete(final Long id) {
        execute(String.format("delete from t_order where orderId = %d", id));
    }
    
    @Override
    public List<Order> selectAll() {
        List<Order> result = new LinkedList<>();
        String sql = "SELECT o.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            result.addAll(getOrders(resultSet));
        } catch (final SQLException ignored) {
        }
        return result;
    }
    
    private List<Order> getOrders(final ResultSet resultSet) {
        List<Order> result = new LinkedList<>();
        try {
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
    
    private void insertFailure() throws SQLException {
        Connection connection = dataSource.getConnection();
        setAutoCommit(connection);
        Statement statement = connection.createStatement();
        try {
            for (int i = 1; i < 10; i++) {
                long orderId = insertAndGetGeneratedKey(statement,"INSERT INTO t_order (user_id, status) VALUES (10, 'INIT')");
                statement.execute(String.format("INSERT INTO t_order_item (order_id, user_id) VALUES (%d, 10)", orderId));
                orderId = insertAndGetGeneratedKey(statement,"INSERT INTO t_order (user_id, status) VALUES (11, 'INIT')");
                statement.execute(String.format("INSERT INTO t_order_item (order_id, user_id) VALUES (%d, 11)", orderId));
            }
            makeException();
            commit(connection);
        } catch (Exception ex) {
            rollback(connection);
        }
        finally {
            close(connection, statement);
        }
    }
    
    private void makeException() {
        System.out.println(10 / 0);
    }
    
    private void setAutoCommit(final Connection connection) {
        if (isXA) {
            try {
                connection.setAutoCommit(false);
            } catch (final SQLException ignored) {
            }
        }
    }
    
    private void commit(final Connection connection) {
        if (isXA) {
            try {
                connection.commit();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    private void rollback(final Connection connection) {
        if (isXA) {
            try {
                connection.rollback();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    private void execute(final String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
}

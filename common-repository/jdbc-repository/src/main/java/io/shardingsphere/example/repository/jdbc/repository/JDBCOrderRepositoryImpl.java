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
import io.shardingsphere.example.repository.api.repository.OrderRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class JDBCOrderRepositoryImpl extends BaseOrderRepository implements OrderRepository {
    
    private final DataSource dataSource;
    
    public JDBCOrderRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            createOrderTableNotExist(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void dropTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            dropOrderTable(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void truncateTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            truncateOrderTable(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public Long insert(final Order order) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_T_ORDER, Statement.RETURN_GENERATED_KEYS)) {
            insertOrder(preparedStatement, order);
        } catch (final SQLException ignored) {
        }
        return order.getOrderId();
    }
    
    @Override
    public void delete(final Long orderId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_BY_ORDER_ID)) {
            deleteById(preparedStatement, orderId);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public List<Order> getOrders(final String sql) {
        List<Order> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            result = queryOrder(preparedStatement);
        } catch (final SQLException ignored) {
        }
        return result;
    }
}

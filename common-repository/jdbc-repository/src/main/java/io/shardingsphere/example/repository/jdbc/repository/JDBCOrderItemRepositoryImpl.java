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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class JDBCOrderItemRepositoryImpl extends BaseOrderItemRepository implements OrderItemRepository {
    
    private final DataSource dataSource;
    
    public JDBCOrderItemRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            createItemTableNotExist(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void dropTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            dropItemTable(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void truncateTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            truncateItemTable(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public Long insert(final OrderItem orderItem) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_T_ORDER_ITEM, Statement.RETURN_GENERATED_KEYS)) {
            insertItem(preparedStatement, orderItem);
        } catch (final SQLException ignored) {
        }
        return orderItem.getOrderItemId();
    }
    
    @Override
    public void delete(final Long orderItemId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_BY_ITEM_ID)) {
            deleteById(preparedStatement, orderItemId);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public List<OrderItem> getOrderItems(final String sql) {
        List<OrderItem> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            result = queryOrderItem(preparedStatement);
        } catch (final SQLException ignored) {
        }
        return result;
    }
}

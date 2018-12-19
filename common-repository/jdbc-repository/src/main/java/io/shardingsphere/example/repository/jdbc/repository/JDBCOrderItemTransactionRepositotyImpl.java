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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class JDBCOrderItemTransactionRepositotyImpl implements OrderItemRepository {
    
    private final JDBCOrderItemRepositoryImpl jdbcOrderItemRepository;
    
    private Connection insertConnection;
    
    public JDBCOrderItemTransactionRepositotyImpl(final DataSource dataSource) {
        this.jdbcOrderItemRepository = new JDBCOrderItemRepositoryImpl(dataSource);
    }
    
    @Override
    public void createTableIfNotExists() {
        jdbcOrderItemRepository.createTableIfNotExists();
    }
    
    @Override
    public void dropTable() {
        jdbcOrderItemRepository.dropTable();
    }
    
    @Override
    public void truncateTable() {
        jdbcOrderItemRepository.truncateTable();
    }
    
    @Override
    public Long insert(final OrderItem orderItem) {
        String sql = "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = insertConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, orderItem.getOrderId());
            preparedStatement.setInt(2, orderItem.getUserId());
            preparedStatement.setString(3, orderItem.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    orderItem.setOrderItemId(resultSet.getLong(1));
                }
            }
        } catch (final SQLException ignored) {
        }
        return orderItem.getOrderItemId();
    }
    
    @Override
    public void delete(final Long id) {
        jdbcOrderItemRepository.delete(id);
    }
    
    @Override
    public List<OrderItem> selectAll() {
        return jdbcOrderItemRepository.selectAll();
    }
    
    @Override
    public List<OrderItem> selectRange() {
        return jdbcOrderItemRepository.selectRange();
    }
    
    public void setInsertConnection(final Connection insertConnection) {
        this.insertConnection = insertConnection;
    }
    
}

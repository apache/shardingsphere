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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class JDBCOrderTransactionRepositoryImpl implements OrderRepository {
    
    private final JDBCOrderRepositoryImpl jdbcOrderRepository;
    
    private Connection insertConnection;
    
    public JDBCOrderTransactionRepositoryImpl(final DataSource dataSource) {
        this.jdbcOrderRepository = new JDBCOrderRepositoryImpl(dataSource);
    }
    
    @Override
    public void createTableIfNotExists() {
        jdbcOrderRepository.createTableIfNotExists();
    }
    
    @Override
    public void dropTable() {
        jdbcOrderRepository.dropTable();
    }
    
    @Override
    public void truncateTable() {
        jdbcOrderRepository.truncateTable();
    }
    
    @Override
    public Long insert(final Order order) {
        String sql = "INSERT INTO t_order (user_id, status) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = insertConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setString(2, order.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getLong(1));
                }
            }
        } catch (final SQLException ignored) {
        }
        return order.getOrderId();
    }
    
    @Override
    public void delete(final Long id) {
        jdbcOrderRepository.delete(id);
    }
    
    @Override
    public List<Order> selectAll() {
        return jdbcOrderRepository.selectAll();
    }
    
    @Override
    public List<Order> selectRange() {
        return jdbcOrderRepository.selectRange();
    }
    
    public void setInsertConnection(final Connection insertConnection) {
        this.insertConnection = insertConnection;
    }
}

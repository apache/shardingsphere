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

package org.apache.shardingsphere.example.core.jdbc.repository;

import org.apache.shardingsphere.example.core.api.entity.OrderStatisticsInfo;
import org.apache.shardingsphere.example.core.api.repository.OrderStatisticsInfoRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class OrderStatisticsInfoRepositoryImpl implements OrderStatisticsInfoRepository {
    
    private final DataSource dataSource;
    
    public OrderStatisticsInfoRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS order_statistics_info (id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT NOT NULL, order_date DATE NOT NULL, order_num INT, PRIMARY KEY (id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE order_statistics_info";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE order_statistics_info";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public Long insert(final OrderStatisticsInfo orderStatisticsInfo) throws SQLException {
        String sql = "INSERT INTO order_statistics_info (user_id, order_date, order_num) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, orderStatisticsInfo.getUserId());
            preparedStatement.setDate(2, Date.valueOf(orderStatisticsInfo.getOrderDate()));
            preparedStatement.setInt(3, orderStatisticsInfo.getOrderNum());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    orderStatisticsInfo.setId(resultSet.getLong(1));
                }
            }
        }
        return orderStatisticsInfo.getId();
    }
    
    @Override
    public void delete(final Long id) throws SQLException {
        String sql = "DELETE FROM order_statistics_info WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }
    
    @Override
    public List<OrderStatisticsInfo> selectAll() throws SQLException {
        String sql = "SELECT * FROM order_statistics_info";
        return getOrderStatisticsInfos(sql);
    }
    
    protected List<OrderStatisticsInfo> getOrderStatisticsInfos(final String sql) throws SQLException {
        List<OrderStatisticsInfo> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OrderStatisticsInfo orderStatisticsInfo = new OrderStatisticsInfo();
                orderStatisticsInfo.setId(resultSet.getLong(1));
                orderStatisticsInfo.setUserId(resultSet.getLong(2));
                orderStatisticsInfo.setOrderDate(resultSet.getDate(3).toLocalDate());
                orderStatisticsInfo.setOrderNum(resultSet.getInt(4));
                result.add(orderStatisticsInfo);
            }
        }
        return result;
    }
}

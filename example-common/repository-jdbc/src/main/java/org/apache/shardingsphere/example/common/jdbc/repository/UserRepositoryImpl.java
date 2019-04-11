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

package org.apache.shardingsphere.example.common.jdbc.repository;

import org.apache.shardingsphere.example.common.entity.User;
import org.apache.shardingsphere.example.common.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class UserRepositoryImpl implements UserRepository {
    
    private final DataSource dataSource;
    
    public UserRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS t_user (user_id INT NOT NULL AUTO_INCREMENT, user_name VARCHAR(200), pwd VARCHAR(200), assisted_query_pwd VARCHAR(200), PRIMARY KEY (user_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void dropTable() {
        String sql = "DROP TABLE t_user";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void truncateTable() {
        String sql = "TRUNCATE TABLE t_user";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public Long insert(final User entity) {
        String sql = "INSERT INTO t_user (user_id, user_name, pwd) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, entity.getUserId());
            preparedStatement.setString(2, entity.getUserName());
            preparedStatement.setString(3, entity.getPwd());
            preparedStatement.executeUpdate();
            
        } catch (final SQLException ignored) {
        }
        return (long) entity.getUserId();
    }
    
    @Override
    public void delete(final Long id) {
        String sql = "DELETE FROM t_user WHERE user_id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public List<User> selectAll() {
        String sql = "SELECT * FROM t_user";
        return getUsers(sql);
    }
    
    private List<User> getUsers(final String sql) {
        List<User> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt(0));
                user.setUserName(resultSet.getString(1));
                user.setPwd(resultSet.getString(2));
                result.add(user);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }
}

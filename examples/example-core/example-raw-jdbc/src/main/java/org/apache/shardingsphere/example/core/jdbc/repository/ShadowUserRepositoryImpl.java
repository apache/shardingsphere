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

import org.apache.shardingsphere.example.core.api.entity.ShadowUser;
import org.apache.shardingsphere.example.core.api.repository.ShadowUserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class ShadowUserRepositoryImpl implements ShadowUserRepository {
    
    private final DataSource dataSource;
    
    public ShadowUserRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_user "
                + "(user_id INT NOT NULL AUTO_INCREMENT, user_name VARCHAR(200), user_name_plain VARCHAR(200), pwd VARCHAR(200), assisted_query_pwd VARCHAR(200), PRIMARY KEY (user_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE t_user";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_user";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public Long insert(final ShadowUser entity) throws SQLException {
        String sql = "INSERT INTO t_user (user_id, user_name, pwd,shadow) VALUES (?, ?, ?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, entity.getUserId());
            preparedStatement.setString(2, entity.getUserName());
            preparedStatement.setString(3, entity.getPwd());
            preparedStatement.setBoolean(4, entity.isShadow());
            preparedStatement.executeUpdate();
        }
        return (long) entity.getUserId();
    }
    
    @Override
    public void delete(final Long id) throws SQLException {
        String sql = "DELETE FROM t_user WHERE user_id=? and shadow=?";
        deleteUser(sql, id, true);
        deleteUser(sql, id, false);
    }
    
    private void deleteUser(final String sql, final Long id, final boolean isShadow) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setBoolean(2, isShadow);
            preparedStatement.executeUpdate();
        }
    }
    
    @Override
    public List<ShadowUser> selectAll() throws SQLException {
        List<ShadowUser> users;
        String sql = "SELECT * FROM t_user where shadow=?";
        users = getUsers(sql, false);
        users.addAll(getUsers(sql, true));
        return users;
    }
    
    private List<ShadowUser> getUsers(final String sql, final boolean isShadow) throws SQLException {
        List<ShadowUser> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBoolean(1, isShadow);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ShadowUser user = new ShadowUser();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUserName(resultSet.getString("user_name"));
                user.setPwd(resultSet.getString("pwd"));
                result.add(user);
            }
        }
        return result;
    }
}

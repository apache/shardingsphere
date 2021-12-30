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

package org.apache.shardingsphere.example.shadow.jdbc;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.example.shadow.jdbc.entity.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public final class MemoryLocalShadowJdbcExampleService {
    
    private final DataSource dataSource;

    /**
     * Execute test.
     *
     * @throws SQLException
     */
    public void run() throws SQLException {
        try {
            this.initEnvironment();
            this.processSuccess();
        } finally {
            this.cleanEnvironment();
        }
    }
    
    /**
     * Initialize the database test environment.
     * @throws SQLException
     */
    private void initEnvironment() throws SQLException {
        String createSql = "CREATE TABLE IF NOT EXISTS t_user (user_id INT NOT NULL AUTO_INCREMENT, user_type INT(11), user_name VARCHAR(200), pwd VARCHAR(200), PRIMARY KEY (user_id))";
        createTableIfNotExistsShadow(createSql);
        createTableIfNotExistsNative(createSql);
        String truncateSql = "TRUNCATE TABLE t_user";
        truncateTableShadow(truncateSql);
        truncateTableNative(truncateSql);
    }
    
    private void createTableIfNotExistsNative(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void createTableIfNotExistsShadow(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql + "/*shadow:true,foo:bar*/");
        }
    }
    
    private void truncateTableNative(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    private void truncateTableShadow(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql + "/*shadow:true,foo:bar*/");
        }
    }
    
    private void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> ids = insertData();
        printData(); 
        deleteData(ids);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            User user = new User();
            user.setUserId(i);
            user.setUserType(i % 2);
            user.setUserName("test_" + i);
            user.setPwd("pwd" + i);
            insert(user);
            result.add((long) user.getUserId());
        }
        return result;
    }
    
    public void insert(final User entity) throws SQLException {
        String sql = "INSERT INTO t_user (user_id, user_type, user_name, pwd) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, entity.getUserId());
            preparedStatement.setInt(2, entity.getUserType());
            preparedStatement.setString(3, entity.getUserName());
            preparedStatement.setString(4, entity.getPwd());
            preparedStatement.executeUpdate();
        }
    }
    
    private void printData() throws SQLException {
        System.out.println("---------------------------- Print User Data -----------------------");
        for (Object each : this.selectAll()) {
            System.out.println(each);
        }
    }
    
    private void deleteData(final List<Long> userIds) throws SQLException {
        String sql = "DELETE FROM t_user WHERE user_id = ? AND user_type = ?";
        for (long id : userIds) {
            deleteUser(sql, id, 0);
            deleteUser(sql, id, 1);
        }
    }
    
    /**
     * Restore the environment.
     * @throws SQLException
     */
    private void cleanEnvironment() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_user;";
        dropTableShadow(sql);
        dropTableNative(sql);
    }
    
    private void dropTableNative(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void dropTableShadow(final String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql + "/*shadow:true,foo:bar*/");
        }
    }
    
    private void deleteUser(final String sql, final Long id, final int userType) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setInt(2, userType);
            preparedStatement.executeUpdate();
        }
    }
    
    public List<User> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_user where user_type = ?";
        List<User> users = getUsers(sql, 1);
        users.addAll(getUsers(sql, 0));
        return users;
    }

    private List<User> getUsers(final String sql, final int userType) throws SQLException {
        List<User> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userType);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUserType(resultSet.getInt("user_type"));
                user.setUserName(resultSet.getString("user_name"));
                user.setPwd(resultSet.getString("pwd"));
                result.add(user);
            }
        }
        return result;
    }
}

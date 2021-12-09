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

package org.apache.shardingsphere.example.shadow.spring.namespace.jdbc;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.example.shadow.spring.namespace.jdbc.entity.User;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@AllArgsConstructor
public final class MemoryLocalShadowSpringNamespaceJdbcExampleService {
    
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
        String createUserTableSql = "CREATE TABLE IF NOT EXISTS t_user" 
                + "(user_id INT NOT NULL AUTO_INCREMENT, user_name VARCHAR(200), pwd VARCHAR(200), PRIMARY KEY (user_id))";
        String truncateUserTable = "TRUNCATE TABLE t_user";
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createUserTableSql);
            statement.executeUpdate(truncateUserTable);
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
            user.setUserName("test_" + i);
            user.setPwd("pwd" + i);
            insert(user);
            result.add((long) user.getUserId());
        }
        return result;
    }
    
    private long insert(final User user) throws SQLException {
        String sql = "INSERT INTO t_user (user_id, user_name, pwd) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, user.getUserId());
            preparedStatement.setString(2, user.getUserName());
            preparedStatement.setString(3, user.getPwd());
            preparedStatement.executeUpdate();
        }
        return user.getUserId();
    }

    private void deleteData(final List<Long> orderIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        String sql = "DELETE FROM t_user WHERE user_id=?";
        for (Long each : orderIds) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, each);
                preparedStatement.executeUpdate();
            }
        }
    }
    
    private void printData() throws SQLException {
        System.out.println("---------------------------- Print User Data -----------------------");
        for (Object each : this.getUsers()) {
            System.out.println(each);
        }
    }

    protected List<User> getUsers() throws SQLException {
        List<User> result = new LinkedList<>();
        String sql = "SELECT * FROM t_user";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUserName(resultSet.getString("user_name"));
                user.setPwd(resultSet.getString("pwd"));
                result.add(user);
            }
        }
        return result;
    }
    
    /**
     * Restore the environment.
     * @throws SQLException
     */
    private void cleanEnvironment() throws SQLException {
        String dropUserSql = "DROP TABLE t_user";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropUserSql);
        }
    }
}

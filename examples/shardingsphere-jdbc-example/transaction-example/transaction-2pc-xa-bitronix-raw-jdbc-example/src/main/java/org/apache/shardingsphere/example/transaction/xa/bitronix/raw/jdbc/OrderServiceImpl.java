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

package org.apache.shardingsphere.example.transaction.xa.bitronix.raw.jdbc;

import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class OrderServiceImpl implements ExampleService {
    
    private final DataSource dataSource;
    
    OrderServiceImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void initEnvironment() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id BIGINT AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        }
        int quantity = selectAll();
        System.out.printf("CREATE t_order IF NOT EXIST (count: %d)\n", quantity);
    }
    
    @Override
    public void cleanEnvironment() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS t_order");
        }
        System.out.println("DROP t_order");
    }
    
    @Override
    public void processSuccess() throws SQLException {
        System.out.println("-------------------- Process Start ---------------------");
        TransactionTypeHolder.set(TransactionType.XA);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (user_id, status) VALUES (?, ?)");
            doInsert(preparedStatement);
            connection.commit();
            System.out.println("INSERT 10 orders success");
        } finally {
            TransactionTypeHolder.clear();
        }
        int quantity = selectAll();
        System.out.printf("Commit, expect:10, actual:%d \n", quantity);
        printData();
        System.out.println("-------------------- Process End -----------------------");
    }
    
    @Override
    public void processFailure() throws SQLException {
        System.out.println("-------------------- Process Start ---------------------");
        TransactionTypeHolder.set(TransactionType.XA);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (user_id, status) VALUES (?, ?)");
            doInsert(preparedStatement);
            connection.rollback();
            System.out.println("INSERT 10 orders failed");
        } finally {
            TransactionTypeHolder.clear();
        }
        int quantity = selectAll();
        System.out.printf("Rollback, expect:0, actual:%d \n", quantity);
        printData();
        System.out.println("-------------------- Process End -----------------------");
    }
    
    @Override
    public void printData() throws SQLException {
        System.out.println("Print Order Data");
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT order_id, user_id, status FROM t_order");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                String orderId = resultSet.getString("order_id");
                String userId = resultSet.getString("user_id");
                String status = resultSet.getString("status");
                System.out.printf("orderId = %s, userId = %s, status = %s \n", orderId, userId, status);
            }
        }
    }

    private void doInsert(final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < 10; i++) {
            preparedStatement.setObject(1, i);
            preparedStatement.setObject(2, "init");
            preparedStatement.executeUpdate();
        }
    }
    
    private int selectAll() throws SQLException {
        int result = 0;
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT COUNT(1) AS count FROM t_order");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        }
        return result;
    }
}

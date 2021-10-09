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

import org.apache.shardingsphere.example.core.api.entity.Account;
import org.apache.shardingsphere.example.core.api.repository.AccountRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class AccountRepositoryImpl implements AccountRepository {
    
    private final DataSource dataSource;
    
    public AccountRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_account (account_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (account_id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE t_account";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_account";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    @Override
    public Long insert(final Account account) throws SQLException {
        String sql = "INSERT INTO t_account (user_id, status) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, account.getUserId());
            preparedStatement.setString(2, account.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    account.setAccountId(resultSet.getLong(1));
                }
            }
        }
        return account.getAccountId();
    }
    
    @Override
    public void delete(final Long accountId) throws SQLException {
        String sql = "DELETE FROM t_account WHERE account_id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, accountId);
            preparedStatement.executeUpdate();
        }
    }
    
    @Override
    public List<Account> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_account";
        return getAccounts(sql);
    }
    
    protected List<Account> getAccounts(final String sql) throws SQLException {
        List<Account> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Account account = new Account();
                account.setAccountId(resultSet.getLong(1));
                account.setUserId(resultSet.getInt(2));
                account.setStatus(resultSet.getString(3));
                result.add(account);
            }
        }
        return result;
    }
}

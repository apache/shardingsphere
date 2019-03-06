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

import io.shardingsphere.example.repository.api.entity.OrderEncrypt;
import io.shardingsphere.example.repository.api.repository.OrderEncryptRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class JDBCOrderEncryptRepositoryImpl extends BaseOrderEncryptRepository implements OrderEncryptRepository {
    
    private final DataSource dataSource;
    
    public JDBCOrderEncryptRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createTableIfNotExists() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            createEncryptTableNotExist(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void dropTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            dropEncryptTable(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void truncateTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            truncateEncryptTable(statement);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public Long insert(final OrderEncrypt orderEncrypt) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_T_ORDER_ENCRYPT, Statement.RETURN_GENERATED_KEYS)) {
            insertEncrypt(preparedStatement, orderEncrypt);
        } catch (final SQLException ignored) {
        }
        return orderEncrypt.getOrderId();
    }
    
    @Override
    public void delete(final Long encryptId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_BY_ENCRYPT_ID)) {
            deleteById(preparedStatement, encryptId.toString());
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public void update(final String encryptId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_BY_ENCRYPT_ID)) {
            updateById(preparedStatement, encryptId);
        } catch (final SQLException ignored) {
        }
    }
    
    @Override
    public List<OrderEncrypt> getOrderEncrypts(final String sql) {
        List<OrderEncrypt> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            result = queryOrderEncrypt(preparedStatement);
        } catch (final SQLException ignored) {
        }
        return result;
    }
}

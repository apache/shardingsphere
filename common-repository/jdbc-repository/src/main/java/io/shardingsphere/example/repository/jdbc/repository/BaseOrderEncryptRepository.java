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

package io.shardingsphere.example.repository.jdbc.repository;

import io.shardingsphere.example.repository.api.entity.OrderEncrypt;
import io.shardingsphere.example.repository.api.repository.OrderEncryptRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseOrderEncryptRepository implements OrderEncryptRepository {
    
    protected static final String SQL_INSERT_T_ORDER_ENCRYPT = "INSERT INTO t_order_encrypt (order_id, user_id, md5_id, aes_id) VALUES (?, ?, ?, ?)";
    
    protected static final String SQL_DELETE_BY_MD5_ID = "DELETE FROM t_order_encrypt WHERE md5_id=?";
    
    protected static final String SQL_UPDATE_BY_AES_ID = "UPDATE t_order_encrypt SET aes_id = 11 WHERE aes_id=?";
    
    private static final String SQL_CREATE_T_ORDER_ENCRYPT = "CREATE TABLE IF NOT EXISTS t_order_item "
        + "(order_id BIGINT NOT NULL, user_id INT NOT NULL, md5_id VARCHAR(200), aes_id VARCHAR(200), aes_query_id VARCHAR(200), PRIMARY KEY (order_id))";
    
    private static final String SQL_DROP_T_ORDER_ENCRYPT = "DROP TABLE t_order_encrypt";
    
    private static final String SQL_TRUNCATE_T_ORDER_ENCRYPT = "TRUNCATE TABLE t_order_encrypt";
    
    private static final String SQL_SELECT_T_ORDER_ENCRYPT_ALL = "SELECT e.* FROM t_order o, t_order_encrypt e WHERE o.order_id = e.order_id";
    
    private static final String SQL_SELECT_T_ORDER_ENCRYPT_RANGE = "SELECT e.* FROM t_order o, t_order_encrypt e WHERE o.order_id = e.order_id AND o.user_id BETWEEN 1 AND 5";
    
    protected final void createEncryptTableNotExist(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_CREATE_T_ORDER_ENCRYPT);
    }
    
    protected final void dropEncryptTable(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_DROP_T_ORDER_ENCRYPT);
    }
    
    protected final void truncateEncryptTable(final Statement statement) throws SQLException {
        statement.executeUpdate(SQL_TRUNCATE_T_ORDER_ENCRYPT);
    }
    
    protected final void insertEncrypt(final PreparedStatement preparedStatement, final OrderEncrypt orderEncrypt) throws SQLException {
        preparedStatement.setLong(1, orderEncrypt.getOrderId());
        preparedStatement.setInt(2, orderEncrypt.getUserId());
        preparedStatement.setString(3, orderEncrypt.getMd5Id());
        preparedStatement.setString(4, orderEncrypt.getAesId());
        preparedStatement.executeUpdate();
        try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                orderEncrypt.setOrderId(resultSet.getLong(1));
            }
        }
    }
    
    protected final void deleteById(final PreparedStatement preparedStatement, final String md5Id) throws SQLException {
        preparedStatement.setString(1, md5Id);
        preparedStatement.executeUpdate();
    }
    
    protected final void updateById(final PreparedStatement preparedStatement, final String aesId) throws SQLException {
        preparedStatement.setString(1, aesId);
        preparedStatement.executeUpdate();
    }
    
    protected final List<OrderEncrypt> queryOrderEncrypt(final PreparedStatement preparedStatement) {
        List<OrderEncrypt> result = new LinkedList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OrderEncrypt orderEncrypt = new OrderEncrypt();
                orderEncrypt.setOrderId(resultSet.getLong(1));
                orderEncrypt.setUserId(resultSet.getInt(2));
                orderEncrypt.setMd5Id(resultSet.getString(3));
                orderEncrypt.setAesId(resultSet.getString(4));
                orderEncrypt.setAesQueryId(resultSet.getString(5));
                result.add(orderEncrypt);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }
    
    @Override
    public final List<OrderEncrypt> selectAll() {
        return getOrderEncrypts(SQL_SELECT_T_ORDER_ENCRYPT_ALL);
    }
    
    @Override
    public final List<OrderEncrypt> selectRange() {
        return getOrderEncrypts(SQL_SELECT_T_ORDER_ENCRYPT_RANGE);
    }
    
    public abstract List<OrderEncrypt> getOrderEncrypts(String sql);
}

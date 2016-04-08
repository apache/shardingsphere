/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.storage.impl;

import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionType;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorage;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库的事务日志存储器接口.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class DatabaseTransactionLogStorage implements TransactionLogStorage {
    
    private final SoftTransactionConfiguration transactionConfiguration;
    
    private final SoftTransactionConfiguration transactionConfig;
    
    @Override
    public void add(final TransactionLog transactionLog) {
        String sql = "INSERT INTO `transaction_log` (`id`, `transaction_type`, `data_source`, `sql`, `parameters`, `creation_time`) VALUES (?, ?, ?, ?, ?, ?);";
        try (
                Connection conn = transactionConfiguration.getTransactionLogDataSource().getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, transactionLog.getId());
            preparedStatement.setString(2, transactionLog.getTransactionType().toString());
            preparedStatement.setString(3, transactionLog.getDataSource());
            preparedStatement.setString(4, transactionLog.getSql());
            preparedStatement.setString(5, new Gson().toJson(transactionLog.getParameters()));
            preparedStatement.setLong(6, transactionLog.getCreationTime());
            preparedStatement.executeUpdate();
        } catch (final SQLException ex) {
            log.error("Save transaction log error:", ex);
        }
    }
    
    @Override
    public void remove(final String id) {
        String sql = "DELETE FROM `transaction_log` WHERE `id`=?;";
        try (
                Connection conn = transactionConfiguration.getTransactionLogDataSource().getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, id);
            preparedStatement.executeUpdate();
        } catch (final SQLException ex) {
            log.error("Delete transaction log error:", ex);
        }
    }
    
    @Override
    public List<TransactionLog> findEligibleTransactionLogs(final int size, final SoftTransactionType type) {
        List<TransactionLog> result = new ArrayList<>(size);
        String sql = "SELECT `id`, `transaction_type`, `data_source`, `sql`, `parameters`, `creation_time`, `async_delivery_try_times` "
                + "FROM `transaction_log` WHERE `async_delivery_try_times`<? AND `transaction_type`=? AND `creation_time`<? LIMIT ?;";
        try (
                Connection conn = transactionConfiguration.getTransactionLogDataSource().getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, transactionConfig.getAsyncMaxDeliveryTryTimes());
            preparedStatement.setString(2, type.toString());
            preparedStatement.setLong(3, System.currentTimeMillis() - transactionConfiguration.getAsyncMaxDeliveryTryDelayMillis());
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Gson gson = new Gson();
                    List<Object> parameters = gson.fromJson(rs.getString(5), new TypeToken<List<Object>>() { }.getType());
                    result.add(new TransactionLog(rs.getString(1), "", SoftTransactionType.valueOf(rs.getString(2)), rs.getString(3), rs.getString(4), parameters, rs.getLong(6), rs.getInt(7)));
                }
            }
        } catch (final SQLException ex) {
            log.error("Find all transaction log error:", ex);
        }
        return result;
    }
    
    @Override
    public void increaseAsyncDeliveryTryTimes(final String id) {
        String sql = "UPDATE `transaction_log` SET `async_delivery_try_times`=`async_delivery_try_times`+1 WHERE `id`=?;";
        try (
                Connection conn = transactionConfiguration.getTransactionLogDataSource().getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, id);
            preparedStatement.executeUpdate();
        } catch (final SQLException ex) {
            log.error("Update transaction log error:", ex);
        }
    }
}

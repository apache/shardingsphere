/*
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

import com.dangdang.ddframe.rdb.transaction.soft.constants.SoftTransactionType;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorage;
import com.google.common.collect.Lists;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class RdbTransactionLogStorageTest {
    
    private TransactionLogStorage storage;
    
    @Before
    public void setup() throws SQLException {
        BasicDataSource dataSource =  new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:db_trans");
        dataSource.setUsername("sa");
        createTable(dataSource);
        storage = new RdbTransactionLogStorage(dataSource);
    }
    
    @Test
    public void assertAddTransactionLogStorage() throws SQLException {
        String id = UUID.randomUUID().toString();
        String transactionId = UUID.randomUUID().toString();
        TransactionLog transactionLog = buildTransactionLog(id, transactionId);
        storage.add(transactionLog);
        assertThat(storage.findEligibleTransactionLogs(1, 1, 1L).size(), is(1));
        storage.remove(id);
    }
    
    @Test
    public void assertRemoveTransactionLogStorage() throws SQLException {
        String id = UUID.randomUUID().toString();
        String transactionId = UUID.randomUUID().toString();
        TransactionLog transactionLog = buildTransactionLog(id, transactionId);
        storage.add(transactionLog);
        storage.remove(id);
        assertThat(storage.findEligibleTransactionLogs(1, 1, 1L).size(), is(0));
    }
    
    @Test
    public void assertIncreaseAsyncDeliveryTryTimes() throws SQLException {
        String id = UUID.randomUUID().toString();
        String transactionId = UUID.randomUUID().toString();
        TransactionLog transactionLog = buildTransactionLog(id, transactionId);
        storage.add(transactionLog);
        storage.increaseAsyncDeliveryTryTimes(id);
        assertThat(storage.findEligibleTransactionLogs(1, 2, 1L).get(0).getAsyncDeliveryTryTimes(), is(1));
        storage.remove(id);
    }
    
    private TransactionLog buildTransactionLog(final String id, final String transactionId) {
        return new TransactionLog(id, transactionId, SoftTransactionType.BestEffortsDelivery,
                "ds_1", "UPDATE t_order_0 SET not_existed_column = 1 WHERE user_id = 1 AND order_id = ?", Lists.newArrayList(), 1461062858701L, 0);
    }
    
    private void createTable(final DataSource dataSource) throws SQLException {
        String dbSchema = "CREATE TABLE IF NOT EXISTS `transaction_log` ("
            + "`id` VARCHAR(40) NOT NULL, "
            + "`transaction_type` VARCHAR(30) NOT NULL, "
            + "`data_source` VARCHAR(255) NOT NULL, "
            + "`sql` TEXT NOT NULL, "
            + "`parameters` TEXT NOT NULL, "
            + "`creation_time` LONG NOT NULL, "
            + "`async_delivery_try_times` INT NOT NULL DEFAULT 0, "
            + "PRIMARY KEY (`id`));";
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.executeUpdate();
        }
    }
}

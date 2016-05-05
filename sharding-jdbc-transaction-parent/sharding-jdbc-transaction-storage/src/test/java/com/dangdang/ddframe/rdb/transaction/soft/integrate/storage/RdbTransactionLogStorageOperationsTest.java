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

package com.dangdang.ddframe.rdb.transaction.soft.integrate.storage;

import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLogStorage;
import com.dangdang.ddframe.rdb.transaction.soft.storage.impl.RdbTransactionLogStorage;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class RdbTransactionLogStorageOperationsTest extends AbstractTransactionLogStorageOperationsTest {
    
    @Test
    public void assertRdbTransactionLogStorageOperations() throws SQLException {
        BasicDataSource dataSource =  new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:db_transaction_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        createTable(dataSource);
        TransactionLogStorage storage = new RdbTransactionLogStorage(dataSource);
        assertTransactionLogStorageOperations(storage);
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

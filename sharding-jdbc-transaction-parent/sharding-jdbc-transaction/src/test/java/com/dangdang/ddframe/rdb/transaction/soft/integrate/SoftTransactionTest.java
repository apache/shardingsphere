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

package com.dangdang.ddframe.rdb.transaction.soft.integrate;

import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionManager;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.base.AbstractSoftTransactionIntegrationTest;
import com.dangdang.ddframe.rdb.transaction.soft.bed.BEDSoftTransaction;
import com.dangdang.ddframe.rdb.transaction.soft.constants.SoftTransactionType;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SoftTransactionTest extends AbstractSoftTransactionIntegrationTest {
    
    @Test
    public void bedSoftTransactionTest() throws SQLException {
        SoftTransactionManager transactionManagerFactory = new SoftTransactionManager(getSoftTransactionConfiguration(getShardingDataSource()));
        transactionManagerFactory.init();
        BEDSoftTransaction transactionManager = (BEDSoftTransaction) transactionManagerFactory.getTransaction(SoftTransactionType.BestEffortsDelivery);
        transactionManager.begin(getShardingDataSource().getConnection());
        insert();
        assertThat(select(), is(1));
        transactionManager.end();
    }
    
    private void insert() {
        String dbSchema = "insert into transaction_test(id) values (1)";
        try (
                Connection conn = getShardingDataSource().getConnection().getConnection("db_trans", SQLType.INSERT);
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
    
    private int select() {
        String dbSchema = "select * from `transaction_test`;";
        int id = 0;
        try (
                Connection conn = getShardingDataSource().getConnection().getConnection("db_trans", SQLType.SELECT);
                PreparedStatement preparedStatement = conn.prepareStatement(dbSchema)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                id = resultSet.getInt(1);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return  id;
    }
    
    private SoftTransactionConfiguration getSoftTransactionConfiguration(final ShardingDataSource dataSource) {
        SoftTransactionConfiguration result = new SoftTransactionConfiguration(dataSource);
        result.setTransactionLogDataSource(getTransactionDataSource());
        return result;
    }
}

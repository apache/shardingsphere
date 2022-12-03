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

package org.apache.shardingsphere.integration.transaction.cases.classictransfer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Classic transfer transaction integration test.
 */
@TransactionTestCase
public final class ClassicTransferTestCase extends BaseTransactionTestCase {
    
    public ClassicTransferTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        Connection connection = getDataSource().getConnection();
        executeUpdateWithLog(connection, "insert into account(transaction_id, balance) values (1,0), (2,100);");
        innerRun();
    }
    
    @SneakyThrows(InterruptedException.class)
    private void innerRun() throws SQLException {
        List<Thread> tasks = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            Thread updateThread = new UpdateTread(getDataSource());
            updateThread.start();
            tasks.add(updateThread);
            int sum = getBalanceSum();
            assertThat(String.format("Balance sum is %s, should be 100.", sum), sum, is(100));
        }
        Thread.sleep(3000);
        int sum = getBalanceSum();
        assertThat(String.format("Balance sum is %s, should be 100.", sum), sum, is(100));
        for (Thread task : tasks) {
            task.join();
        }
    }
    
    private int getBalanceSum() throws SQLException {
        int result = 0;
        try (Connection connection = getDataSource().getConnection(); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            ResultSet resultSet = statement.executeQuery("select sum(balance) as a from account where transaction_id in (1, 2)");
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
            connection.commit();
        }
        return result;
    }
    
    @AllArgsConstructor
    private static class UpdateTread extends Thread {
        
        @Getter
        private DataSource dataSource;
        
        public void run() {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                Statement statement1 = connection.createStatement();
                statement1.execute("update account set balance = balance - 1 where transaction_id = 2;");
                Statement statement2 = connection.createStatement();
                Thread.sleep(1000);
                statement2.execute("update account set balance = balance + 1 where transaction_id = 1;");
                connection.commit();
            } catch (SQLException | InterruptedException ignored) {
            }
        }
    }
}

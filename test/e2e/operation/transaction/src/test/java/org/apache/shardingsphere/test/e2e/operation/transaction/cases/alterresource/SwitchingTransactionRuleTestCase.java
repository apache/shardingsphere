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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.alterresource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.command.CommonSQLCommand;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.awaitility.Awaitility;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Switching transaction rule test case.
 */
@Slf4j
@TransactionTestCase(adapters = TransactionTestConstants.PROXY, dbTypes = TransactionTestConstants.MYSQL, transactionTypes = TransactionType.LOCAL)
public final class SwitchingTransactionRuleTestCase extends BaseTransactionTestCase {
    
    private static final int THREAD_SIZE = 1;
    
    private static final int TRANSACTION_SIZE = 1000;
    
    private static final int MAX_SWITCH_COUNT = 6;
    
    private static final AtomicBoolean IS_FINISHED = new AtomicBoolean(false);
    
    private static final AtomicInteger SWITCH_COUNT = new AtomicInteger();
    
    public SwitchingTransactionRuleTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) {
        innerRun(containerComposer);
    }
    
    @SneakyThrows(InterruptedException.class)
    private void innerRun(final TransactionContainerComposer containerComposer) {
        List<Thread> tasks = new ArrayList<>(THREAD_SIZE);
        for (int i = 0; i < THREAD_SIZE; i++) {
            Thread updateThread = new Thread(new TransactionOperationsTask(getDataSource()));
            updateThread.start();
            tasks.add(updateThread);
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new AlterTransactionRuleTask(containerComposer, getBaseTransactionITCase().getCommonSQL()));
        for (Thread each : tasks) {
            each.join();
        }
        IS_FINISHED.set(true);
        executor.shutdown();
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class AlterTransactionRuleTask implements Runnable {
        
        private final TransactionContainerComposer containerComposer;
        
        private final CommonSQLCommand commonSQL;
        
        @SneakyThrows(SQLException.class)
        @Override
        public void run() {
            while (!IS_FINISHED.get()) {
                alterLocalTransactionRule();
                Awaitility.await().atMost(20L, TimeUnit.SECONDS).pollInterval(19L, TimeUnit.SECONDS).until(() -> true);
                alterXaTransactionRule("Narayana");
                Awaitility.await().atMost(20L, TimeUnit.SECONDS).pollInterval(19L, TimeUnit.SECONDS).until(() -> true);
                if (SWITCH_COUNT.incrementAndGet() >= MAX_SWITCH_COUNT) {
                    alterLocalTransactionRule();
                    IS_FINISHED.set(true);
                    break;
                }
            }
        }
        
        private void alterLocalTransactionRule() throws SQLException {
            try (Connection connection = containerComposer.getDataSource().getConnection()) {
                if (isExpectedTransactionRule(connection, TransactionType.LOCAL, "")) {
                    return;
                }
                String alterLocalTransactionRule = commonSQL.getAlterLocalTransactionRule();
                log.info("Alter local transaction rule: {}", alterLocalTransactionRule);
                SWITCH_COUNT.getAndIncrement();
                executeWithLog(connection, alterLocalTransactionRule);
            }
            assertTrue(waitExpectedTransactionRule(TransactionType.LOCAL, "", containerComposer));
        }
        
        private void alterXaTransactionRule(final String providerType) throws SQLException {
            try (Connection connection = containerComposer.getDataSource().getConnection()) {
                if (isExpectedTransactionRule(connection, TransactionType.XA, providerType)) {
                    return;
                }
                String alterXaTransactionRule = commonSQL.getAlterXATransactionRule().replace("${providerType}", providerType);
                log.info("Alter XA transaction rule: {}", alterXaTransactionRule);
                SWITCH_COUNT.getAndIncrement();
                executeWithLog(connection, alterXaTransactionRule);
            }
            assertTrue(waitExpectedTransactionRule(TransactionType.XA, providerType, containerComposer));
        }
        
        private boolean isExpectedTransactionRule(final Connection connection, final TransactionType expectedTransType, final String expectedProviderType) throws SQLException {
            Map<String, String> transactionRuleMap = executeShowTransactionRule(connection);
            return Objects.equals(transactionRuleMap.get(TransactionTestConstants.DEFAULT_TYPE), expectedTransType.toString())
                    && Objects.equals(transactionRuleMap.get(TransactionTestConstants.PROVIDER_TYPE), expectedProviderType);
        }
        
        private Map<String, String> executeShowTransactionRule(final Connection connection) throws SQLException {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW TRANSACTION RULE;");
            Map<String, String> result = new HashMap<>();
            while (resultSet.next()) {
                String defaultType = resultSet.getString(TransactionTestConstants.DEFAULT_TYPE);
                String providerType = resultSet.getString(TransactionTestConstants.PROVIDER_TYPE);
                result.put(TransactionTestConstants.DEFAULT_TYPE, defaultType);
                result.put(TransactionTestConstants.PROVIDER_TYPE, providerType);
            }
            statement.close();
            return result;
        }
        
        private boolean waitExpectedTransactionRule(final TransactionType expectedTransType, final String expectedProviderType,
                                                    final TransactionContainerComposer containerComposer) throws SQLException {
            Awaitility.await().pollDelay(5L, TimeUnit.SECONDS).until(() -> true);
            try (Connection connection = containerComposer.getDataSource().getConnection()) {
                int waitTimes = 0;
                do {
                    if (isExpectedTransactionRule(connection, expectedTransType, expectedProviderType)) {
                        return true;
                    }
                    Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
                    waitTimes++;
                } while (waitTimes <= 3);
                return false;
            }
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class TransactionOperationsTask implements Runnable {
        
        private static final AtomicInteger ID_COUNTER = new AtomicInteger();
        
        private final DataSource dataSource;
        
        @SneakyThrows(SQLException.class)
        public void run() {
            Connection connection = dataSource.getConnection();
            for (int i = 0; i < TRANSACTION_SIZE; i++) {
                log.info("Transaction {} start", i);
                executeOneTransaction(connection);
                if (IS_FINISHED.get()) {
                    break;
                }
                log.info("Transaction {} end", i);
            }
            connection.close();
        }
        
        private static void executeOneTransaction(final Connection connection) throws SQLException {
            boolean isErrorOccured = false;
            ThreadLocalRandom random = ThreadLocalRandom.current();
            try {
                connection.setAutoCommit(false);
                int id = ID_COUNTER.incrementAndGet();
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO account(id, balance, transaction_id) VALUES(?, ?, ?)");
                insertStatement.setObject(1, id);
                insertStatement.setObject(2, id);
                insertStatement.setObject(3, id);
                insertStatement.execute();
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE account SET balance = balance - 1 WHERE id = ?");
                updateStatement.setObject(1, id);
                updateStatement.execute();
                PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM account WHERE id = ?");
                selectStatement.setObject(1, id);
                selectStatement.executeQuery();
                PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM account WHERE id = ?");
                deleteStatement.setObject(1, id);
                deleteStatement.execute();
                long time = random.nextLong(900) + 100;
                Awaitility.await().atMost(time + 10L, TimeUnit.MILLISECONDS).pollInterval(time, TimeUnit.MILLISECONDS).until(() -> true);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("Execute transaction exception occurred", ex);
                isErrorOccured = true;
                connection.rollback();
            }
            if (!isErrorOccured) {
                connection.commit();
            }
            connection.setAutoCommit(true);
        }
    }
}

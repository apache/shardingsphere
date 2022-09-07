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

package org.apache.shardingsphere.integration.transaction.engine.base;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.framework.param.TransactionParameterized;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public abstract class BaseTransactionITCase extends BaseITCase {
    
    public BaseTransactionITCase(final TransactionParameterized parameterized) throws SQLException {
        super(parameterized);
        if (isProxyAdapter(parameterized)) {
            initProxyConfig();
        } else {
            initJdbcConfig();
        }
    }
    
    private void initProxyConfig() throws SQLException {
        addResources();
        initShardingAlgorithm();
        assertTrue(waitShardingAlgorithmEffect(15));
        initTableRules();
        createTables();
    }
    
    private void initJdbcConfig() throws SQLException {
        createTables();
    }
    
    private void createTables() throws SQLException {
        Connection conn = getProxyConnection();
        createOrderTable(conn);
        createOrderItemTable(conn);
        createAccountTable(conn);
        createAddressTable(conn);
    }
    
    private void initTableRules() throws SQLException {
        Connection connection = getProxyConnection();
        createOrderTableRule(connection);
        createOrderItemTableRule(connection);
        bindingShardingRule(connection);
        createAccountTableRule(connection);
        createAddressBroadcastTableRule(connection);
    }
    
    protected void createOrderTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateOrderTableRule());
    }
    
    protected void createOrderItemTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateOrderItemTableRule());
    }
    
    protected void createAccountTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateAccountTableRule());
    }
    
    private void createAddressBroadcastTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateAddressBroadcastTableRule());
    }
    
    protected void bindingShardingRule(final Connection connection) throws SQLException {
        executeWithLog(connection, "CREATE SHARDING BINDING TABLE RULES (t_order, t_order_item)");
    }
    
    /**
     * Create account table.
     * 
     * @param connection connection 
     * @throws SQLException SQL exception
     */
    public void createAccountTable(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateAccountTable());
    }
    
    /**
     * Drop account table.
     *
     * @param connection connection 
     * @throws SQLException SQL exception
     */
    public void dropAccountTable(final Connection connection) throws SQLException {
        executeWithLog(connection, "drop table if exists account;");
    }
    
    protected void createOrderItemTable(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateOrderItemTable());
    }
    
    protected void createAddressTable(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateAddressTable());
    }
    
    protected void dropOrderItemTable(final Connection connection) throws SQLException {
        executeWithLog(connection, "DROP TABLE IF EXISTS t_order_item;");
    }
    
    protected void createOrderTable(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateOrderTable());
    }
    
    protected void dropOrderTable(final Connection connection) throws SQLException {
        executeWithLog(connection, "DROP TABLE IF EXISTS t_order;");
    }
    
    protected void assertAccountRowCount(final Connection conn, final int rowNum) {
        assertTableRowCount(conn, TransactionTestConstants.ACCOUNT, rowNum);
    }
    
    @SneakyThrows(SQLException.class)
    private void assertTableRowCount(final Connection conn, final String tableName, final int rowNum) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select * from " + tableName);
        int resultSetCount = 0;
        while (rs.next()) {
            resultSetCount++;
        }
        statement.close();
        assertThat(String.format("Recode num assert error, expect:%s, actual:%s.", rowNum, resultSetCount), resultSetCount, is(rowNum));
    }
    
    protected void alterLocalTransactionRule() throws SQLException {
        Connection connection = getProxyConnection();
        if (isExpectedTransactionRule(connection, TransactionType.LOCAL, "")) {
            return;
        }
        String alterLocalTransactionRule = getCommonSQLCommand().getAlterLocalTransactionRule();
        executeWithLog(connection, alterLocalTransactionRule);
        assertTrue(waitExpectedTransactionRule(TransactionType.LOCAL, "", 5));
    }
    
    protected void alterXaTransactionRule(final String providerType) throws SQLException {
        Connection connection = getProxyConnection();
        if (isExpectedTransactionRule(connection, TransactionType.XA, providerType)) {
            return;
        }
        String alterXaTransactionRule = getCommonSQLCommand().getAlterXATransactionRule().replace("${providerType}", providerType);
        executeWithLog(connection, alterXaTransactionRule);
        assertTrue(waitExpectedTransactionRule(TransactionType.XA, providerType, 5));
    }
    
    private boolean isExpectedTransactionRule(final Connection connection, final TransactionType expectedTransType, final String expectedProviderType) {
        Map<String, String> transactionRuleMap = executeShowTransactionRule(connection);
        return Objects.equals(transactionRuleMap.get(TransactionTestConstants.DEFAULT_TYPE), expectedTransType.toString())
                && Objects.equals(transactionRuleMap.get(TransactionTestConstants.PROVIDER_TYPE), expectedProviderType);
    }
    
    protected boolean waitExpectedTransactionRule(final TransactionType expectedTransType, final String expectedProviderType, final int maxWaitTimes) {
        ThreadUtil.sleep(5, TimeUnit.SECONDS);
        Connection connection = getProxyConnection();
        int waitTimes = 0;
        do {
            if (isExpectedTransactionRule(connection, expectedTransType, expectedProviderType)) {
                return true;
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            waitTimes++;
        } while (waitTimes <= maxWaitTimes);
        return false;
    }
    
    @SneakyThrows
    protected Map<String, String> executeShowTransactionRule(final Connection conn) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SHOW TRANSACTION RULE;");
        Map<String, String> result = new HashMap<>(1, 1);
        while (rs.next()) {
            String defaultType = rs.getString(TransactionTestConstants.DEFAULT_TYPE);
            String providerType = rs.getString(TransactionTestConstants.PROVIDER_TYPE);
            result.put(TransactionTestConstants.DEFAULT_TYPE, defaultType);
            result.put(TransactionTestConstants.PROVIDER_TYPE, providerType);
        }
        statement.close();
        return result;
    }
    
    @SneakyThrows
    protected void callTestCases(final TransactionParameterized parameterized) {
        if (TransactionTestConstants.PROXY.equalsIgnoreCase(parameterized.getAdapter())) {
            for (TransactionType each : parameterized.getTransactionTypes()) {
                if (TransactionType.LOCAL.equals(each)) {
                    log.info("Call transaction IT {}, alter transaction rule {}.", parameterized, "");
                    alterTransactionRule(each, "");
                    doCallTestCases(parameterized, each, "");
                } else if (TransactionType.XA.equals(each)) {
                    for (String eachProvider : parameterized.getProviders()) {
                        log.info("Call transaction IT {}, alter transaction rule {}.", parameterized, eachProvider);
                        alterTransactionRule(each, eachProvider);
                        doCallTestCases(parameterized, each, eachProvider);
                    }
                }
            }
        } else {
            doCallTestCases(parameterized);
        }
    }
    
    private void alterTransactionRule(final TransactionType transactionType, final String each) throws SQLException {
        if (Objects.equals(transactionType, TransactionType.LOCAL)) {
            alterLocalTransactionRule();
        } else if (Objects.equals(transactionType, TransactionType.XA)) {
            alterXaTransactionRule(each);
        }
    }
    
    private void doCallTestCases(final TransactionParameterized parameterized) {
        for (Class<? extends BaseTransactionTestCase> each : parameterized.getTransactionTestCaseClasses()) {
            log.info("Transaction IT {} -> {} test begin.", parameterized, each.getSimpleName());
            try {
                each.getConstructor(BaseTransactionITCase.class, DataSource.class).newInstance(this, getDataSource()).execute();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error(String.format("Transaction IT %s -> %s test failed", parameterized, each.getSimpleName()), ex);
                throw new RuntimeException(ex);
            }
            log.info("Transaction IT {} -> {} test end.", parameterized, each.getSimpleName());
            try {
                getDataSource().close();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    private void doCallTestCases(final TransactionParameterized parameterized, TransactionType transactionType, String provider) {
        for (Class<? extends BaseTransactionTestCase> each : parameterized.getTransactionTestCaseClasses()) {
            log.info("Call transaction IT {} -> {} -> {} -> {} test begin.", parameterized, transactionType, provider, each.getSimpleName());
            try {
                each.getConstructor(BaseTransactionITCase.class, DataSource.class).newInstance(this, getDataSource()).execute();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error(String.format("Transaction IT %s -> %s test failed", parameterized, each.getSimpleName()), ex);
                throw new RuntimeException(ex);
            }
            log.info("Call transaction IT {} -> {} -> {} -> {} test end.", parameterized, transactionType, provider, each.getSimpleName());
            try {
                getDataSource().close();
            } catch (final SQLException ignored) {
            }
        }
    }
}

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
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.framework.param.TransactionParameterized;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public abstract class BaseTransactionITCase extends BaseITCase {
    
    public BaseTransactionITCase(final TransactionParameterized parameterized) throws SQLException {
        super(parameterized);
        if (isProxyAdapter(parameterized)) {
            initProxyConfig();
        } else {
            initJDBCConfig();
        }
    }
    
    private void initProxyConfig() throws SQLException {
        addResources();
        initShardingAlgorithm();
        assertTrue(waitShardingAlgorithmEffect(15));
        initTableRules();
        createTables();
    }
    
    private void initJDBCConfig() throws SQLException {
        createTables();
    }
    
    private void createTables() throws SQLException {
        Connection conn = getProxyConnection();
        createOrderTable(conn);
        createOrderItemTable(conn);
        createAccountTable(conn);
    }
    
    private void initTableRules() throws SQLException {
        Connection connection = getProxyConnection();
        createOrderTableRule(connection);
        createOrderItemTableRule(connection);
        bindingShardingRule(connection);
        createAccountTableRule(connection);
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
    
    protected void bindingShardingRule(final Connection connection) throws SQLException {
        executeWithLog(connection, "CREATE SHARDING BINDING TABLE RULES (t_order, t_order_item)");
    }
    
    protected void createAccountTable(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateAccountTable());
    }
    
    protected void dropAccountTable(final Connection connection) throws SQLException {
        executeWithLog(connection, "DROP TABLE IF EXISTS account;");
    }
    
    protected void createOrderItemTable(final Connection connection) throws SQLException {
        executeWithLog(connection, getCommonSQLCommand().getCreateOrderItemTable());
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
        assertEquals(String.format("Recode num assert error, expect:%s, actual:%s.", rowNum, resultSetCount), resultSetCount, rowNum);
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
    
    protected void alterXaAtomikosTransactionRule() throws SQLException {
        Connection connection = getProxyConnection();
        if (isExpectedTransactionRule(connection, TransactionType.XA, "Atomikos")) {
            return;
        }
        String alterXaAtomikosTransactionRule = getCommonSQLCommand().getAlterXaAtomikosTransactionRule();
        executeWithLog(connection, alterXaAtomikosTransactionRule);
        assertTrue(waitExpectedTransactionRule(TransactionType.XA, "Atomikos", 5));
    }
    
    private boolean isExpectedTransactionRule(final Connection connection, final TransactionType expectedTransType, final String expectedProviderType) {
        Map<String, String> transactionRuleMap = executeShowTransactionRule(connection);
        return Objects.equals(transactionRuleMap.get(TransactionTestConstants.DEFAULT_TYPE), expectedTransType.toString())
                && Objects.equals(transactionRuleMap.get(TransactionTestConstants.PROVIDER_TYPE), expectedProviderType);
    }
    
    protected boolean waitExpectedTransactionRule(final TransactionType expectedTransType, final String expectedProviderType, final int maxWaitTimes) {
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
    
}

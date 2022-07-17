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

package org.apache.shardingsphere.integration.transaction.engine.mysql;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.framework.param.TransactionParameterized;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * MySQL general transaction test case with proxy container, includes multiple cases.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class MySQLProxyTransactionIT extends BaseTransactionITCase {
    
    private final TransactionParameterized parameterized;
    
    public MySQLProxyTransactionIT(final TransactionParameterized parameterized) throws SQLException {
        super(parameterized);
        this.parameterized = parameterized;
        log.info("Parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<TransactionParameterized> getParameters() {
        return getTransactionParameterizedList(MySQLProxyTransactionIT.class);
    }
    
    @Before
    @SneakyThrows
    public void before() {
        Connection conn = getProxyConnection();
        dropAccountTable(conn);
        createAccountTable(conn);
    }
    
    @Test
    @SneakyThrows
    public void assertLocalCommit() {
        alterLocalTransactionRule();
        assertCommit();
    }
    
    @Test
    @SneakyThrows
    public void assertLocalRollback() {
        alterLocalTransactionRule();
        assertRollback();
    }
    
    @Test(expected = SQLException.class)
    @SneakyThrows
    public void assertLocalSetReadOnly() {
        alterLocalTransactionRule();
        assertSetReadOnly();
    }
    
    @SneakyThrows
    public void assertSetReadOnly() {
        Connection connection = getProxyConnection();
        executeUpdateWithLog(connection, "insert into account(id,balance) values (1,0),(2,100);");
        Connection conn = getProxyConnection();
        conn.setReadOnly(true);
        Statement statement1 = conn.createStatement();
        ResultSet rs = statement1.executeQuery("select * from account;");
        while (rs.next()) {
            int id = rs.getInt("id");
            int balance = rs.getInt("balance");
            if (id == 1) {
                Assert.assertEquals(String.format("Balance is %s, should be 0.", balance), balance, 0);
            }
            if (id == 2) {
                Assert.assertEquals(String.format("Balance is %s, should be 100.", balance), balance, 100);
            }
        }
        Statement statement2 = conn.createStatement();
        statement2.execute("update account set balance=100 where id=2;");
        Assert.fail("Update ran successfully, should failed.");
    }
    
    @Test
    @SneakyThrows
    public void assertDistributedCommit() {
        alterXaAtomikosTransactionRule();
        assertCommit();
    }
    
    @Test
    @SneakyThrows
    public void assertDistributedRollback() {
        alterXaAtomikosTransactionRule();
        assertRollback();
    }
    
    @SneakyThrows(SQLException.class)
    public void assertRollback() {
        Connection conn = getProxyConnection();
        conn.setAutoCommit(false);
        assertAccountRowCount(conn, 0);
        Statement std1 = conn.createStatement();
        std1.execute("insert into account(id, balance, transaction_id) values(1, 1, 1);");
        assertAccountRowCount(conn, 1);
        conn.rollback();
        assertAccountRowCount(conn, 0);
    }
    
    @SneakyThrows(SQLException.class)
    public void assertCommit() {
        Connection conn = getProxyConnection();
        conn.setAutoCommit(false);
        assertAccountRowCount(conn, 0);
        Statement std1 = conn.createStatement();
        std1.execute("insert into account(id, balance, transaction_id) values(1, 1, 1);");
        assertAccountRowCount(conn, 1);
        conn.commit();
        assertAccountRowCount(conn, 1);
    }
    
}

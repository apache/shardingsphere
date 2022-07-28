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

package org.apache.shardingsphere.integration.transaction.cases.readonly;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Set read only transaction integration test.
 */
@Slf4j
public abstract class SetReadOnlyTestCase extends BaseTransactionTestCase {
    
    public SetReadOnlyTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @SneakyThrows
    protected void assertNotSetReadOnly() {
        Connection conn = getDataSource().getConnection();
        assertQueryBalance(conn);
        Statement queryStatement = conn.createStatement();
        queryStatement.executeUpdate("update account set balance=101 where id=2;");
        queryStatement.close();
        Statement statement3 = conn.createStatement();
        ResultSet r3 = statement3.executeQuery("select * from account where id=2");
        if (!r3.next()) {
            fail("Update run failed, should success.");
        }
        int balanceEnd = r3.getInt("balance");
        assertThat(String.format("Balance is %s, should be 101.", balanceEnd), balanceEnd, is(101));
    }
    
    protected void assertQueryBalance(final Connection conn) throws SQLException {
        Statement queryStatement = conn.createStatement();
        ResultSet rs = queryStatement.executeQuery("select * from account;");
        while (rs.next()) {
            int id = rs.getInt("id");
            int balance = rs.getInt("balance");
            if (1 == id) {
                assertThat(String.format("Balance is %s, should be 0.", balance), balance, is(0));
            }
            if (2 == id) {
                assertThat(String.format("Balance is %s, should be 100.", balance), balance, is(100));
            }
        }
    }
}

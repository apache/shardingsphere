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

import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Set read only transaction integration test.
 */
public abstract class SetReadOnlyTestCase extends BaseTransactionTestCase {
    
    public SetReadOnlyTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    void assertNotSetReadOnly() throws SQLException {
        Connection conn = getDataSource().getConnection();
        assertQueryBalance(conn);
        executeUpdateWithLog(conn, "update account set balance = 101 where id = 2;");
        ResultSet resultSet = executeQueryWithLog(conn, "select * from account where id = 2"); 
        if (!resultSet.next()) {
            fail("Should have a result.");
        }
        int balanceResult = resultSet.getInt("balance");
        assertThat(String.format("Balance is %s, should be 101.", balanceResult), balanceResult, is(101));
    }
    
    void assertQueryBalance(final Connection conn) throws SQLException {
        ResultSet resultSet = executeQueryWithLog(conn, "select * from account;");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int balance = resultSet.getInt("balance");
            if (1 == id) {
                assertThat(String.format("Balance is %s, should be 0.", balance), balance, is(0));
            }
            if (2 == id) {
                assertThat(String.format("Balance is %s, should be 100.", balance), balance, is(100));
            }
        }
    }
}

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

package org.apache.shardingsphere.test.e2e.transaction.cases.autocommit;

import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.awaitility.Awaitility;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL auto commit transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL)
public final class MySQLAutoCommitTestCase extends AutoCommitTestCase {
    
    public MySQLAutoCommitTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        if (TransactionType.LOCAL == getTransactionType()) {
            assertAutoCommit();
        }
        assertAutoCommitWithStatement();
        assertAutoCommitWithPrepareStatement();
    }
    
    private void assertAutoCommit() throws SQLException {
        // TODO Currently XA transaction does not support two transactions in the same thread at the same time
        try (
                Connection connection1 = getDataSource().getConnection();
                Connection connection2 = getDataSource().getConnection()) {
            executeWithLog(connection1, "set session transaction isolation level read committed;");
            executeWithLog(connection2, "set session transaction isolation level read committed;");
            connection1.setAutoCommit(false);
            connection2.setAutoCommit(false);
            executeWithLog(connection1, "insert into account(id, balance, transaction_id) values(1, 100, 1);");
            assertFalse(executeQueryWithLog(connection2, "select * from account;").next());
            connection1.commit();
            Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollDelay(200L, TimeUnit.MILLISECONDS).until(() -> executeQueryWithLog(connection2, "select * from account;").next());
            assertTrue(executeQueryWithLog(connection2, "select * from account;").next());
        }
    }
}

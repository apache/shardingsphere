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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.autocommit;

import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.awaitility.Awaitility;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL auto commit transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.POSTGRESQL)
public final class PostgreSQLAutoCommitTestCase extends AutoCommitTestCase {
    
    public PostgreSQLAutoCommitTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        if (TransactionType.LOCAL == getTransactionType()) {
            assertAutoCommit();
        }
        assertAutoCommitWithStatement();
        assertAutoCommitWithPreparedStatement();
        assertAutoCommitWithoutCommit();
    }
    
    private void assertAutoCommit() throws SQLException {
        try (Connection connection1 = getDataSource().getConnection(); Connection connection2 = getDataSource().getConnection()) {
            executeWithLog(connection1, "SET transaction isolation level read committed;");
            executeWithLog(connection2, "SET transaction isolation level read committed;");
            connection1.setAutoCommit(false);
            connection2.setAutoCommit(false);
            executeWithLog(connection1, "INSERT INTO account(id, balance, transaction_id) VALUES(1, 100, 1);");
            assertFalse(executeQueryWithLog(connection2, "SELECT * FROM account;").next());
            connection1.commit();
            Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollDelay(200L, TimeUnit.MILLISECONDS).until(
                    () -> executeQueryWithLog(connection2, "SELECT * FROM account;").next());
            assertTrue(executeQueryWithLog(connection2, "SELECT * FROM account;").next());
        }
    }
}

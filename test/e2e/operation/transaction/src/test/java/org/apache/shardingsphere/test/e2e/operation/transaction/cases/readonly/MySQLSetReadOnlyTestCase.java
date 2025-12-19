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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.readonly;

import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * MySQL set read only transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL)
public final class MySQLSetReadOnlyTestCase extends SetReadOnlyTestCase {
    
    public MySQLSetReadOnlyTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertSetReadOnly();
        assertNotSetReadOnly();
    }
    
    private void assertSetReadOnly() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeUpdateWithLog(connection, "INSERT INTO account(id, balance) VALUES (1, 0), (2, 100);");
        }
        try (Connection connection = getDataSource().getConnection()) {
            connection.setReadOnly(true);
            assertQueryBalance(connection);
            executeWithLog(connection, "UPDATE account SET balance = 100 WHERE id = 2;");
            fail("Update ran successfully, should failed.");
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Connection is read-only. Queries leading to data modification are not allowed."));
        }
        try (Connection connection = getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            connection.setReadOnly(true);
            executeWithLog(connection, "UPDATE account SET balance = 100 WHERE id = 2;");
            fail("Update ran successfully, should failed.");
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Connection is read-only. Queries leading to data modification are not allowed."));
        }
    }
}

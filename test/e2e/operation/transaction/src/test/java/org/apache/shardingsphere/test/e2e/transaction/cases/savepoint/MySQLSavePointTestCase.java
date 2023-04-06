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

package org.apache.shardingsphere.test.e2e.transaction.cases.savepoint;

import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * MySQL savepoint transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL)
public final class MySQLSavePointTestCase extends BaseSavePointTestCase {
    
    public MySQLSavePointTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertRollback2Savepoint();
        assertReleaseSavepoint();
        assertReleaseSavepointFailure();
    }
    
    private void assertReleaseSavepointFailure() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            executeWithLog(connection, "DELETE FROM account");
            connection.setAutoCommit(false);
            executeWithLog(connection, "INSERT INTO account(id, balance, transaction_id) VALUES(1,1,1)");
            executeWithLog(connection, "SAVEPOINT point1");
            executeWithLog(connection, "RELEASE SAVEPOINT point1");
            try {
                executeWithLog(connection, "ROLLBACK TO SAVEPOINT point1");
            } catch (final SQLException ex) {
                assertThat(ex.getMessage(), is("SAVEPOINT point1 does not exist"));
            }
            connection.rollback();
        }
    }
}

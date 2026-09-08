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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.commitrollback;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Multiple transactions within a connection integration test.
 */
@TransactionTestCase
public final class MultiTransactionInConnectionTestCase extends BaseTransactionTestCase {
    
    public MultiTransactionInConnectionTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        try (
                Connection connection = getDataSource().getConnection();
                Connection queryConnection = getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO account(id, balance, transaction_id) VALUES(?, ?, ?)")) {
            for (int each = 0; each < 8; each++) {
                connection.setAutoCommit(false);
                statement.setLong(1, each);
                statement.setFloat(2, each);
                statement.setInt(3, each);
                assertThat(statement.executeUpdate(), is(1));
                connection.commit();
                assertAccountBalances(queryConnection, IntStream.rangeClosed(0, each).toArray());
            }
            assertAccountRowCount(connection, 8);
        }
    }
}

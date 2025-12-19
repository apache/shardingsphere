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
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Multiple jdbc connections in one thread test case.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.MYSQL, transactionTypes = TransactionType.XA)
public final class MultiJDBCConnectionsTestCase extends BaseTransactionTestCase {
    
    public MultiJDBCConnectionsTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO account(id, balance, transaction_id) VALUES(?, ?, ?)");
            connection.setAutoCommit(false);
            statement.setLong(1, 1L);
            statement.setFloat(2, 1F);
            statement.setInt(3, 1);
            statement.execute();
            try (Connection connection2 = getDataSource().getConnection()) {
                connection2.createStatement().executeQuery("SELECT * FROM account");
            }
            connection.commit();
            assertAccountRowCount(connection, 1);
        }
    }
}

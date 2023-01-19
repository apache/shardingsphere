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

package org.apache.shardingsphere.test.e2e.transaction.cases.settype;

import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Transaction type holder test case.
 */
@TransactionTestCase(adapters = TransactionTestConstants.JDBC, transactionTypes = TransactionType.XA)
public final class TransactionTypeHolderTestCase extends BaseTransactionTestCase {
    
    public TransactionTypeHolderTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    protected void executeTest() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            TransactionTypeHolder.set(TransactionType.LOCAL);
            assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
            connection.setAutoCommit(false);
            connection.rollback();
        } finally {
            TransactionTypeHolder.set(TransactionType.XA);
            assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
        }
    }
}

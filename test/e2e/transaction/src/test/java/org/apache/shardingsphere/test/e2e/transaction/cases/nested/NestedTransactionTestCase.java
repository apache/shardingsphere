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

package org.apache.shardingsphere.test.e2e.transaction.cases.nested;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.AdapterContainerConstants;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.transaction.api.TransactionType;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Nested transaction test case.
 */
@TransactionTestCase(transactionTypes = TransactionType.LOCAL, adapters = AdapterContainerConstants.JDBC)
public class NestedTransactionTestCase extends BaseTransactionTestCase {
    
    public NestedTransactionTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    protected void executeTest() throws SQLException {
        ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection();
        assertFalse(connection.isHoldTransaction());
        connection.setAutoCommit(false);
        assertTrue(connection.isHoldTransaction());
        requiresNewTransaction();
        assertTrue(connection.isHoldTransaction());
        connection.commit();
    }
    
    private void requiresNewTransaction() throws SQLException {
        ShardingSphereConnection connection = (ShardingSphereConnection) getDataSource().getConnection();
        assertFalse(connection.isHoldTransaction());
        connection.setAutoCommit(false);
        assertTrue(connection.isHoldTransaction());
        connection.commit();
    }
}

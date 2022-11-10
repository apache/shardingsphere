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

package org.apache.shardingsphere.integration.transaction.cases.nested;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.AdapterContainerConstants;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Assert;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Nested transaction test case.
 */
@TransactionTestCase(transactionTypes = TransactionType.LOCAL, adapters = AdapterContainerConstants.JDBC)
public class NestedTransactionTestCase extends BaseTransactionTestCase {
    
    public NestedTransactionTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    protected void executeTest() throws SQLException {
        ShardingSphereConnection conn1 = (ShardingSphereConnection) getDataSource().getConnection();
        Assert.assertFalse(conn1.isHoldTransaction());
        conn1.setAutoCommit(false);
        Assert.assertTrue(conn1.isHoldTransaction());
        requiresNewTransaction();
        Assert.assertTrue(conn1.isHoldTransaction());
        conn1.commit();
    }
    
    private void requiresNewTransaction() throws SQLException {
        ShardingSphereConnection conn2 = (ShardingSphereConnection) getDataSource().getConnection();
        Assert.assertFalse(conn2.isHoldTransaction());
        conn2.setAutoCommit(false);
        Assert.assertTrue(conn2.isHoldTransaction());
        conn2.commit();
    }
}

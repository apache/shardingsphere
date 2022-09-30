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

package org.apache.shardingsphere.integration.transaction.cases.autocommit;

import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * MySQL auto commit transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.MYSQL})
public final class MySQLAutoCommitTestCase extends BaseTransactionTestCase {
    
    public MySQLAutoCommitTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        assertAutoCommit();
    }
    
    private void assertAutoCommit() throws SQLException {
        Connection conn1 = getDataSource().getConnection();
        Connection conn2 = getDataSource().getConnection();
        executeWithLog(conn1, "set session transaction isolation level read committed;");
        executeWithLog(conn2, "set session transaction isolation level read committed;");
        executeWithLog(conn1, "set autocommit=0;");
        executeWithLog(conn2, "begin;");
        executeWithLog(conn1, "insert into account(id, balance, transaction_id) values(1, 100, 1)");
        ResultSet emptyResultSet = executeQueryWithLog(conn2, "select * from account;");
        if (emptyResultSet.next()) {
            fail("There should not be result");
        }
        executeWithLog(conn1, "commit;");
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
        ResultSet notEmptyResultSet = executeQueryWithLog(conn2, "select * from account");
        if (!notEmptyResultSet.next()) {
            fail("There should be result");
        }
    }
}

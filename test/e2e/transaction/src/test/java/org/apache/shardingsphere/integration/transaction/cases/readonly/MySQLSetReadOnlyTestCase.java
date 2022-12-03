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

package org.apache.shardingsphere.integration.transaction.cases.readonly;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.fail;

/**
 * MySQL set read only transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.MYSQL})
@Slf4j
public final class MySQLSetReadOnlyTestCase extends SetReadOnlyTestCase {
    
    public MySQLSetReadOnlyTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        assertSetReadOnly();
        assertNotSetReadOnly();
    }
    
    private void assertSetReadOnly() throws SQLException {
        Connection connection1 = getDataSource().getConnection();
        executeUpdateWithLog(connection1, "insert into account(id, balance) values (1, 0), (2, 100);");
        Connection connection2 = getDataSource().getConnection();
        connection2.setReadOnly(true);
        assertQueryBalance(connection2);
        try {
            executeWithLog(connection2, "update account set balance = 100 where id = 2;");
            fail("Update ran successfully, should failed.");
        } catch (SQLException e) {
            log.info("Update failed for expect.");
        }
    }
}

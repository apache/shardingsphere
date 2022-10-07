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

package org.apache.shardingsphere.integration.transaction.cases.commitrollback;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * An exception occurred within the transaction integration test.
 */
@TransactionTestCase
@Slf4j
public final class ExceptionInTransactionTestCase extends BaseTransactionTestCase {
    
    public ExceptionInTransactionTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    protected void executeTest() throws SQLException {
        Connection conn = null;
        try {
            conn = getDataSource().getConnection();
            conn.setAutoCommit(false);
            assertAccountRowCount(conn, 0);
            executeWithLog(conn, "insert into account(id, balance, transaction_id) values(1, 1, 1);");
            int causedExceptionResult = 1 / 0;
            log.info("Caused exception result: {}", causedExceptionResult);
            executeWithLog(conn, "insert into account(id, balance, transaction_id) values(2, 2, 2);");
            conn.commit();
            fail("It should fail here.");
        } catch (final ArithmeticException ex) {
            assertThat(ex.getMessage(), is("/ by zero"));
        } finally {
            if (null != conn) {
                conn.rollback();
            }
        }
        Thread queryThread = new Thread(() -> {
            log.info("Execute query in new thread.");
            try (Connection connection = getDataSource().getConnection()) {
                assertAccountRowCount(connection, 0);
            } catch (final SQLException ignored) {
            }
        });
        queryThread.start();
        try {
            queryThread.join();
            log.info("ExceptionInTransactionTestCase test over.");
        } catch (final InterruptedException ignored) {
        }
    }
}

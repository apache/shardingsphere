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

package org.apache.shardingsphere.test.e2e.transaction.cases.commitrollback;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * An exception occurred within the transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.MYSQL, TransactionTestConstants.OPENGAUSS})
@Slf4j
public final class ExceptionInTransactionTestCase extends BaseTransactionTestCase {
    
    public ExceptionInTransactionTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    protected void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            connection.setAutoCommit(false);
            assertAccountRowCount(connection, 0);
            executeWithLog(connection, "insert into account(id, balance, transaction_id) values(1, 1, 1);");
            int causedExceptionResult = 1 / 0;
            log.info("Caused exception result: {}", causedExceptionResult);
            executeWithLog(connection, "insert into account(id, balance, transaction_id) values(2, 2, 2);");
            connection.commit();
            fail("It should fail here.");
        } catch (final ArithmeticException ex) {
            assertThat(ex.getMessage(), is("/ by zero"));
            if (null != connection) {
                connection.rollback();
            }
        } finally {
            if (null != connection) {
                connection.close();
            }
        }
        Thread queryThread = new Thread(() -> {
            try (Connection connection2 = getDataSource().getConnection()) {
                assertAccountRowCount(connection2, 0);
            } catch (final SQLException ignored) {
            }
        });
        queryThread.start();
        try {
            queryThread.join();
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}

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

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.postgresql.jdbc.PSQLSavepoint;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * PostgreSQL savepoint transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.POSTGRESQL)
public final class PostgreSQLSavePointTestCase extends BaseSavePointTestCase {
    
    public PostgreSQLSavePointTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertRollbackToSavepoint();
        assertReleaseSavepoint();
        assertErrors();
    }
    
    @SneakyThrows(SQLException.class)
    private void assertErrors() {
        try (Connection connection = getDataSource().getConnection()) {
            try {
                connection.setSavepoint("point");
                fail("Expect exception, but no exception report.");
            } catch (final SQLException ex) {
                assertThat(ex.getMessage(), is("Savepoint can only be used in transaction blocks."));
            }
            try {
                connection.rollback(new PSQLSavepoint("point1"));
                fail("Expect exception, but no exception report.");
            } catch (final SQLException ex) {
                // TODO can not run to get the correct result in JDBC mode.
                assertTrue(ex.getMessage().endsWith("ERROR: ROLLBACK TO SAVEPOINT can only be used in transaction blocks"));
            }
            try {
                connection.releaseSavepoint(new PSQLSavepoint("point1"));
                fail("Expect exception, but no exception report.");
            } catch (final SQLException ex) {
                // TODO can not run to get the correct result in JDBC mode.
                assertTrue(ex.getMessage().endsWith("ERROR: RELEASE SAVEPOINT can only be used in transaction blocks"));
            }
        }
    }
}

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

package org.apache.shardingsphere.integration.transaction.cases.savepoint;

import lombok.SneakyThrows;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.postgresql.jdbc.PSQLSavepoint;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * PostgreSQL savepoint transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.POSTGRESQL})
public final class PostgreSQLSavePointTestCase extends BaseSavePointTestCase {
    
    public PostgreSQLSavePointTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        assertRollback2Savepoint();
        assertReleaseSavepoint();
        assertErrors();
    }
    
    @SneakyThrows(SQLException.class)
    private void assertErrors() {
        Connection conn = getDataSource().getConnection();
        try {
            conn.setSavepoint("point");
            fail("Expect exception, but no exception report.");
        } catch (SQLException ex) {
            assertThat(ex.getMessage(), is("Savepoint can only be used in transaction blocks."));
        }
        try {
            conn.rollback(new PSQLSavepoint("point1"));
            fail("Expect exception, but no exception report.");
        } catch (SQLException ex) {
            // TODO can not run to get the correct result in JDBC mode.
            assertTrue(ex.getMessage().endsWith("ERROR: ROLLBACK TO SAVEPOINT can only be used in transaction blocks"));
        }
        try {
            conn.releaseSavepoint(new PSQLSavepoint("point1"));
            fail("Expect exception, but no exception report.");
        } catch (SQLException ex) {
            // TODO can not run to get the correct result in JDBC mode.
            assertTrue(ex.getMessage().endsWith("ERROR: RELEASE SAVEPOINT can only be used in transaction blocks"));
        }
    }
}

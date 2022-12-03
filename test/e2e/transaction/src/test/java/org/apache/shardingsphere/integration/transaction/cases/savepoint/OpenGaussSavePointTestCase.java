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

import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.opengauss.jdbc.PSQLSavepoint;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * OpenGauss savepoint transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.OPENGAUSS})
public final class OpenGaussSavePointTestCase extends BaseSavePointTestCase {
    
    public OpenGaussSavePointTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    public void executeTest() throws SQLException {
        assertRollback2Savepoint();
        assertReleaseSavepoint();
        assertErrors();
    }
    
    private void assertErrors() throws SQLException {
        Connection connection = getDataSource().getConnection();
        try {
            connection.setSavepoint("point");
            fail("Expect exception, but no exception report.");
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Cannot establish a savepoint in auto-commit mode."));
        }
        try {
            connection.rollback(new PSQLSavepoint("point1"));
            fail("Expect exception, but no exception report.");
        } catch (final SQLException ex) {
            assertTrue(ex.getMessage().endsWith("ERROR: ROLLBACK TO SAVEPOINT can only be used in transaction blocks"));
        }
        try {
            connection.releaseSavepoint(new PSQLSavepoint("point1"));
            fail("Expect exception, but no exception report.");
        } catch (final SQLException ex) {
            assertTrue(ex.getMessage().endsWith("ERROR: RELEASE SAVEPOINT can only be used in transaction blocks"));
        }
    }
}

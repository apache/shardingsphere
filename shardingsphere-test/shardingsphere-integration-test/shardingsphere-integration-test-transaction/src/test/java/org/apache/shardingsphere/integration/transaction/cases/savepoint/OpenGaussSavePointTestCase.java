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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.junit.Assert;
import org.opengauss.jdbc.PSQLSavepoint;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * OpenGauss savepoint transaction integration test.
 */
@Slf4j
@TransactionTestCase(dbTypes = {TransactionTestConstants.OPENGAUSS})
public final class OpenGaussSavePointTestCase extends BaseSavePointTestCase {
    
    public OpenGaussSavePointTestCase(final DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    @SneakyThrows
    public void executeTest() {
        assertRollback2Savepoint();
        assertReleaseSavepoint();
        assertErrors();
    }
    
    @SneakyThrows(SQLException.class)
    private void assertErrors() {
        Connection conn = getDataSource().getConnection();
        try {
            conn.setSavepoint("point");
            Assert.fail("Expect exception, but no exception report.");
        } catch (SQLException ex) {
            Assert.assertEquals("Cannot establish a savepoint in auto-commit mode.", ex.getMessage());
        }
        try {
            conn.rollback(new PSQLSavepoint("point1"));
            Assert.fail("Expect exception, but no exception report.");
        } catch (SQLException ex) {
            Assert.assertTrue(ex.getMessage().endsWith("ERROR: ROLLBACK TO SAVEPOINT can only be used in transaction blocks"));
        }
        try {
            conn.releaseSavepoint(new PSQLSavepoint("point1"));
            Assert.fail("Expect exception, but no exception report.");
        } catch (SQLException ex) {
            Assert.assertTrue(ex.getMessage().endsWith("ERROR: RELEASE SAVEPOINT can only be used in transaction blocks"));
        }
    }
}

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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.savepoint;

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.opengauss.jdbc.PSQLSavepoint;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * OpenGauss savepoint transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.OPENGAUSS)
public final class OpenGaussSavePointTestCase extends BaseSavePointTestCase {
    
    public OpenGaussSavePointTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        assertRollbackToSavepoint();
        assertReleaseSavepoint();
        assertSavepointNotInTransaction();
    }
    
    @SneakyThrows(SQLException.class)
    private void assertSavepointNotInTransaction() {
        try (Connection connection = getDataSource().getConnection()) {
            assertThrows(SQLException.class, () -> connection.setSavepoint("point"));
            assertThrows(SQLException.class, () -> connection.rollback(new PSQLSavepoint("point1")));
            assertThrows(SQLException.class, () -> connection.releaseSavepoint(new PSQLSavepoint("point1")));
        }
    }
}

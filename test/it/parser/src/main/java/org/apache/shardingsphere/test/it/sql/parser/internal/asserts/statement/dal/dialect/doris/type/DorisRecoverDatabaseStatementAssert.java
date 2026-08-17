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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisRecoverDatabaseStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.RecoverDatabaseStatementTestCase;
import org.junit.jupiter.api.Assertions;

/**
 * Recover database statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisRecoverDatabaseStatementAssert {
    
    /**
     * Assert recover database statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual recover database statement
     * @param expected expected recover database statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisRecoverDatabaseStatement actual, final RecoverDatabaseStatementTestCase expected) {
        assertDatabase(assertContext, actual, expected);
        assertDatabaseId(assertContext, actual, expected);
        assertNewDatabase(assertContext, actual, expected);
    }
    
    private static void assertDatabase(final SQLCaseAssertContext assertContext, final DorisRecoverDatabaseStatement actual, final RecoverDatabaseStatementTestCase expected) {
        if (null == expected.getDatabase()) {
            Assertions.assertNull(actual.getDatabaseName(), assertContext.getText("Actual database should not exist."));
        } else {
            Assertions.assertNotNull(actual.getDatabaseName(), assertContext.getText("Actual database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getDatabaseName(), expected.getDatabase());
        }
    }
    
    private static void assertDatabaseId(final SQLCaseAssertContext assertContext, final DorisRecoverDatabaseStatement actual, final RecoverDatabaseStatementTestCase expected) {
        if (null == expected.getDatabaseId()) {
            Assertions.assertNull(actual.getDatabaseId(), assertContext.getText("Actual database id should not exist."));
        } else {
            Assertions.assertNotNull(actual.getDatabaseId(), assertContext.getText("Actual database id should exist."));
            Assertions.assertEquals(expected.getDatabaseId(), actual.getDatabaseId().getIdentifier().getValue(), assertContext.getText("Database id assertion error: "));
        }
    }
    
    private static void assertNewDatabase(final SQLCaseAssertContext assertContext, final DorisRecoverDatabaseStatement actual, final RecoverDatabaseStatementTestCase expected) {
        if (null == expected.getNewDatabase()) {
            Assertions.assertNull(actual.getNewDatabaseName(), assertContext.getText("Actual new database should not exist."));
        } else {
            Assertions.assertNotNull(actual.getNewDatabaseName(), assertContext.getText("Actual new database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getNewDatabaseName(), expected.getNewDatabase());
        }
    }
}

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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowTransactionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowTransactionStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Show transaction statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowTransactionStatementAssert {
    
    /**
     * Assert show transaction statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show transaction statement
     * @param expected expected show transaction statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowTransactionStatement actual, final DorisShowTransactionStatementTestCase expected) {
        assertDatabase(assertContext, actual, expected);
        assertWhere(assertContext, actual, expected);
    }
    
    private static void assertDatabase(final SQLCaseAssertContext assertContext, final DorisShowTransactionStatement actual, final DorisShowTransactionStatementTestCase expected) {
        if (null != expected.getFromDatabase()) {
            assertNotNull(actual.getFromDatabase().orElse(null), assertContext.getText("Actual database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getFromDatabase().get(), expected.getFromDatabase());
        } else {
            assertNull(actual.getFromDatabase().orElse(null), assertContext.getText("Actual database should not exist."));
        }
    }
    
    private static void assertWhere(final SQLCaseAssertContext assertContext, final DorisShowTransactionStatement actual, final DorisShowTransactionStatementTestCase expected) {
        if (null != expected.getWhere()) {
            assertNotNull(actual.getWhere().orElse(null), assertContext.getText("Actual where segment should exist."));
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhere());
        } else {
            assertNull(actual.getWhere().orElse(null), assertContext.getText("Actual where segment should not exist."));
        }
    }
}

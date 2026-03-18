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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.index.MySQLShowIndexStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Show index statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLShowIndexStatementAssert {
    
    /**
     * Assert show index statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show index statement
     * @param expected expected show index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowIndexStatement actual, final MySQLShowIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertDatabase(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final MySQLShowIndexStatement actual, final MySQLShowIndexStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table segment should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertDatabase(final SQLCaseAssertContext assertContext, final MySQLShowIndexStatement actual, final MySQLShowIndexStatementTestCase expected) {
        if (null == expected.getDatabase()) {
            assertFalse(actual.getFromDatabase().isPresent(), assertContext.getText("Actual database segment should not exist."));
        } else {
            assertTrue(actual.getFromDatabase().isPresent(), assertContext.getText("Actual database segment should exist."));
            SQLSegmentAssert.assertIs(assertContext, actual.getFromDatabase().get(), expected.getDatabase());
        }
    }
}

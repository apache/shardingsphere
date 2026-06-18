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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.dialect.sqlserver.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user.SQLServerDenyUserStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.dialect.sqlserver.SQLServerDenyUserStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deny user statement assert for SQLServer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLServerDenyUserStatementAssert {
    
    /**
     * Assert deny user statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual deny user statement
     * @param expected expected deny user statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLServerDenyUserStatement actual, final SQLServerDenyUserStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final SQLServerDenyUserStatement actual, final SQLServerDenyUserStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertNotNull(actual.getTable(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertColumns(final SQLCaseAssertContext assertContext, final SQLServerDenyUserStatement actual, final SQLServerDenyUserStatementTestCase expected) {
        if (expected.getColumns().isEmpty()) {
            assertTrue(actual.getColumns().isEmpty(), assertContext.getText("Actual columns segments should not exist."));
        } else {
            ColumnAssert.assertIs(assertContext, actual.getColumns(), expected.getColumns());
        }
    }
}

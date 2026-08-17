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
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisRecoverTableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.RecoverTableStatementTestCase;
import org.junit.jupiter.api.Assertions;

/**
 * Recover table statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisRecoverTableStatementAssert {
    
    /**
     * Assert recover table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual recover table statement
     * @param expected expected recover table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisRecoverTableStatement actual, final RecoverTableStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertTableId(assertContext, actual, expected);
        assertNewTable(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DorisRecoverTableStatement actual, final RecoverTableStatementTestCase expected) {
        if (null == expected.getTable()) {
            Assertions.assertNull(actual.getTableName(), assertContext.getText("Actual table should not exist."));
        } else {
            Assertions.assertNotNull(actual.getTableName(), assertContext.getText("Actual table should exist."));
            TableAssert.assertIs(assertContext, actual.getTableName(), expected.getTable());
        }
    }
    
    private static void assertTableId(final SQLCaseAssertContext assertContext, final DorisRecoverTableStatement actual, final RecoverTableStatementTestCase expected) {
        if (null == expected.getTableId()) {
            Assertions.assertNull(actual.getTableId(), assertContext.getText("Actual table id should not exist."));
        } else {
            Assertions.assertNotNull(actual.getTableId(), assertContext.getText("Actual table id should exist."));
            Assertions.assertEquals(expected.getTableId(), actual.getTableId().getIdentifier().getValue(), assertContext.getText("Table id assertion error: "));
        }
    }
    
    private static void assertNewTable(final SQLCaseAssertContext assertContext, final DorisRecoverTableStatement actual, final RecoverTableStatementTestCase expected) {
        if (null == expected.getNewTable()) {
            Assertions.assertNull(actual.getNewTableName(), assertContext.getText("Actual new table should not exist."));
        } else {
            Assertions.assertNotNull(actual.getNewTableName(), assertContext.getText("Actual new table should exist."));
            TableAssert.assertIs(assertContext, actual.getNewTableName(), expected.getNewTable());
        }
    }
}

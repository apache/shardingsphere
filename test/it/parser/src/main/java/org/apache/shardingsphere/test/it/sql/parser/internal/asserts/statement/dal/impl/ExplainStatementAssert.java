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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.ExplainStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Explain statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExplainStatementAssert {
    
    /**
     * Assert explain statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual explain statement
     * @param expected expected explain statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ExplainStatement actual, final ExplainStatementTestCase expected) {
        if (null != expected.getSelectClause()) {
            assertNotNull(actual.getSqlStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getSqlStatement(), expected.getSelectClause());
        } else if (null != expected.getUpdateClause()) {
            assertNotNull(actual.getSqlStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getSqlStatement(), expected.getUpdateClause());
        } else if (null != expected.getInsertClause()) {
            assertNotNull(actual.getSqlStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getSqlStatement(), expected.getInsertClause());
        } else if (null != expected.getDeleteClause()) {
            assertNotNull(actual.getSqlStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getSqlStatement(), expected.getDeleteClause());
        } else if (null != expected.getCreateTableAsSelectClause()) {
            assertNotNull(actual.getSqlStatement(), assertContext.getText("Actual statement should exist."));
            SQLStatementAssert.assertIs(assertContext, actual.getSqlStatement(), expected.getCreateTableAsSelectClause());
        } else if (actual.getColumnWild().isPresent() && null != expected.getTable()) {
            assertExplainStatementColumnWild(assertContext, actual, expected);
        } else {
            assertNull(actual.getSqlStatement(), assertContext.getText("Actual statement should not exist."));
        }
    }
    
    private static void assertExplainStatementColumnWild(final SQLCaseAssertContext assertContext, final ExplainStatement actual, final ExplainStatementTestCase expected) {
        if (actual.getSimpleTable().isPresent()) {
            TableAssert.assertIs(assertContext, actual.getSimpleTable().get(), expected.getTable());
            if (actual.getColumnWild().isPresent()) {
                ColumnAssert.assertIs(assertContext, actual.getColumnWild().get(), expected.getColumn());
            } else {
                assertFalse(actual.getColumnWild().isPresent(), assertContext.getText("Actual column wild should not exist."));
            }
        } else {
            assertFalse(actual.getSimpleTable().isPresent(), assertContext.getText("Actual table should not exist."));
        }
    }
}

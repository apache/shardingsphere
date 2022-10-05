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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.MergeStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.MergeStatementTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Merge statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MergeStatementAssert {
    
    /**
     * Assert merge statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual merge statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertExpression(assertContext, actual, expected);
        assertSetClause(assertContext, actual, expected);
        assertWhereClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        if (null == expected.getSource()) {
            assertNull(assertContext.getText("Actual source should not exist."), actual.getSource());
        } else {
            TableAssert.assertIs(assertContext, actual.getSource(), expected.getSource());
        }
        if (null == expected.getTarget()) {
            assertNull(assertContext.getText("Actual target should not exist."), actual.getTarget());
        } else {
            TableAssert.assertIs(assertContext, actual.getTarget(), expected.getTarget());
        }
    }
    
    private static void assertExpression(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        if (null == expected.getExpr()) {
            assertNull(assertContext.getText("Actual expression should not exist."), actual.getExpr());
        } else {
            ExpressionAssert.assertExpression(assertContext, actual.getExpr(), expected.getExpr());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        if (null != expected.getUpdateClause()) {
            if (null == expected.getUpdateClause().getSetClause()) {
                assertNull(assertContext.getText("Actual assignment should not exist."), actual.getUpdate().getSetAssignment());
            } else {
                SetClauseAssert.assertIs(assertContext, actual.getUpdate().getSetAssignment(), expected.getUpdateClause().getSetClause());
            }
        }
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        if (null != expected.getUpdateClause()) {
            if (null == expected.getUpdateClause().getWhereClause()) {
                assertFalse(assertContext.getText("Actual update where segment should not exist."), actual.getUpdate().getWhere().isPresent());
            } else {
                assertTrue(assertContext.getText("Actual update where segment should exist."), actual.getUpdate().getWhere().isPresent());
                WhereClauseAssert.assertIs(assertContext, actual.getUpdate().getWhere().get(), expected.getUpdateClause().getWhereClause());
            }
        }
        if (null != expected.getDeleteClause()) {
            if (null == expected.getDeleteClause().getWhereClause()) {
                assertFalse(assertContext.getText("Actual delete where segment should not exist."), actual.getDelete().getWhere().isPresent());
            } else {
                assertTrue(assertContext.getText("Actual delete where segment should exist."), actual.getDelete().getWhere().isPresent());
                WhereClauseAssert.assertIs(assertContext, actual.getDelete().getWhere().get(), expected.getDeleteClause().getWhereClause());
            }
        }
    }
}

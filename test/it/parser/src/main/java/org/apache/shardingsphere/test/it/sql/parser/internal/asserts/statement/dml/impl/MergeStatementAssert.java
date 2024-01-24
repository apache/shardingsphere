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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.MergeStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertNull(actual.getSource(), assertContext.getText("Actual source should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getSource(), expected.getSource());
        }
        if (null == expected.getTarget()) {
            assertNull(actual.getTarget(), assertContext.getText("Actual target should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getTarget(), expected.getTarget());
        }
    }
    
    private static void assertExpression(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        if (null == expected.getExpr()) {
            assertNull(actual.getExpression(), assertContext.getText("Actual expression should not exist."));
        } else {
            ExpressionAssert.assertExpression(assertContext, actual.getExpression().getExpr(), expected.getExpr());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        if (null != expected.getUpdateClause()) {
            assertTrue(actual.getUpdate().isPresent(), assertContext.getText("Actual merge update statement should exist."));
            if (null == expected.getUpdateClause().getSetClause()) {
                assertNull(actual.getUpdate().get().getSetAssignment(), assertContext.getText("Actual assignment should not exist."));
            } else {
                SetClauseAssert.assertIs(assertContext, actual.getUpdate().get().getSetAssignment(), expected.getUpdateClause().getSetClause());
            }
        }
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        if (null != expected.getUpdateClause()) {
            assertTrue(actual.getUpdate().isPresent(), assertContext.getText("Actual merge update statement should exist."));
            if (null == expected.getUpdateClause().getWhereClause()) {
                assertFalse(actual.getUpdate().get().getWhere().isPresent(), assertContext.getText("Actual update where segment should not exist."));
            } else {
                assertTrue(actual.getUpdate().get().getWhere().isPresent(), assertContext.getText("Actual update where segment should exist."));
                WhereClauseAssert.assertIs(assertContext, actual.getUpdate().get().getWhere().get(), expected.getUpdateClause().getWhereClause());
            }
        }
        if (null != expected.getInsertClause() && null != expected.getInsertClause().getWhereClause() && actual.getInsert().orElse(null) instanceof OracleInsertStatement) {
            assertTrue(actual.getInsert().isPresent(), assertContext.getText("Actual merge insert statement should exist."));
            assertTrue(((OracleInsertStatement) actual.getInsert().get()).getWhere().isPresent(), assertContext.getText("Actual insert where segment should exist."));
            WhereClauseAssert.assertIs(assertContext, ((OracleInsertStatement) actual.getInsert().get()).getWhere().get(), expected.getInsertClause().getWhereClause());
        }
    }
}

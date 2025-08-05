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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.hint.WithTableHintClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.index.IndexAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.output.OutputClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedMergeWhenAndThenSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.MergeStatementTestCase;

import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        assertWithClause(assertContext, actual, expected);
        assertWithTableHintClause(assertContext, actual, expected);
        assertOutputClause(assertContext, actual, expected);
        assertWhenAndThenSegments(assertContext, actual, expected);
        assertIndexes(assertContext, actual, expected);
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        Optional<WithSegment> withSegment = actual.getWith();
        if (null == expected.getWithClause()) {
            assertFalse(withSegment.isPresent(), assertContext.getText("Actual with segment should not exist."));
        } else {
            assertTrue(withSegment.isPresent(), assertContext.getText("Actual with segment should exist."));
            WithClauseAssert.assertIs(assertContext, withSegment.get(), expected.getWithClause());
        }
    }
    
    private static void assertWithTableHintClause(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        Optional<WithTableHintSegment> withTableHintSegment = actual.getWithTableHint();
        if (null == expected.getExpectedWithTableHintClause()) {
            assertFalse(withTableHintSegment.isPresent(), assertContext.getText("Actual with table hint should not exist."));
        } else {
            assertTrue(withTableHintSegment.isPresent(), assertContext.getText("Actual with table hint segment should exist."));
            WithTableHintClauseAssert.assertIs(assertContext, withTableHintSegment.get(), expected.getExpectedWithTableHintClause());
        }
    }
    
    private static void assertOutputClause(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        Optional<OutputSegment> outputSegment = actual.getOutput();
        if (null == expected.getOutputClause()) {
            assertFalse(outputSegment.isPresent(), assertContext.getText("Actual output segment should not exist."));
        } else {
            assertTrue(outputSegment.isPresent(), assertContext.getText("Actual output segment should exist."));
            OutputClauseAssert.assertIs(assertContext, outputSegment.get(), expected.getOutputClause());
        }
    }
    
    private static void assertWhenAndThenSegments(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        Collection<MergeWhenAndThenSegment> mergeWhenAndThenSegments = actual.getWhenAndThens();
        assertThat(assertContext.getText("merge when and then segment assertion error: "), mergeWhenAndThenSegments.size(), is(expected.getMergeWhenAndThenSegments().size()));
        int count = 0;
        for (MergeWhenAndThenSegment each : mergeWhenAndThenSegments) {
            asserMergeWhenAndTheSegment(assertContext, each, expected.getMergeWhenAndThenSegments().get(count));
            count++;
        }
    }
    
    private static void asserMergeWhenAndTheSegment(final SQLCaseAssertContext assertContext, final MergeWhenAndThenSegment actual, final ExpectedMergeWhenAndThenSegment expected) {
        if (null == expected.getExpr()) {
            assertNull(actual.getAndExpr(), assertContext.getText("Actual and expression should not exist."));
        } else {
            ExpressionAssert.assertExpression(assertContext, actual.getAndExpr(), expected.getExpr());
        }
        if (null == expected.getUpdateClause()) {
            assertNull(actual.getUpdate(), assertContext.getText("Actual update statement should not exist."));
        } else {
            UpdateStatementAssert.assertIs(assertContext, actual.getUpdate(), expected.getUpdateClause());
        }
        if (null == expected.getInsertClause()) {
            assertNull(actual.getInsert(), assertContext.getText("Actual insert statement should not exist."));
        } else {
            InsertStatementAssert.assertIs(assertContext, actual.getInsert(), expected.getInsertClause());
        }
    }
    
    private static void assertIndexes(final SQLCaseAssertContext assertContext, final MergeStatement actual, final MergeStatementTestCase expected) {
        Collection<IndexSegment> indexes = actual.getIndexes();
        assertThat(assertContext.getText("index segment assertion error: "), indexes.size(), is(expected.getIndexs().size()));
        int count = 0;
        for (IndexSegment each : indexes) {
            IndexAssert.assertIs(assertContext, each, expected.getIndexs().get(count));
            count++;
        }
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
        if (null != expected.getInsertClause() && null != expected.getInsertClause().getWhereClause() && actual.getInsert().isPresent()) {
            assertTrue(actual.getInsert().get().getWhere().isPresent(), assertContext.getText("Actual insert where segment should exist."));
            WhereClauseAssert.assertIs(assertContext, actual.getInsert().get().getWhere().get(), expected.getInsertClause().getWhereClause());
        }
    }
}

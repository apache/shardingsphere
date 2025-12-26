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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.outfile.OutfileSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.groupby.GroupByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.having.HavingClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.lock.LockClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.model.ModelClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.outfile.OutfileClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.window.WindowClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.SelectStatementTestCase;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Select statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectStatementAssert {
    
    /**
     * Assert select statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual select statement
     * @param expected expected select statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        assertProjection(assertContext, actual, expected);
        assertWhereClause(assertContext, actual, expected);
        assertGroupByClause(assertContext, actual, expected);
        assertHavingClause(assertContext, actual, expected);
        assertWindowClause(assertContext, actual, expected);
        assertOrderByClause(assertContext, actual, expected);
        assertLimitClause(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
        assertLockClause(assertContext, actual, expected);
        assertWithClause(assertContext, actual, expected);
        assertCombineClause(assertContext, actual, expected);
        assertModelClause(assertContext, actual, expected);
        assertIntoClause(assertContext, actual, expected);
        assertOutfileClause(assertContext, actual, expected);
    }
    
    private static void assertWindowClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<WindowSegment> windowSegment = actual.getWindow();
        if (null == expected.getWindowClause()) {
            assertFalse(windowSegment.isPresent(), assertContext.getText("Actual window segment should not exist."));
        } else {
            assertTrue(windowSegment.isPresent(), assertContext.getText("Actual window segment should exist."));
            WindowClauseAssert.assertIs(assertContext, windowSegment.get(), expected.getWindowClause());
        }
    }
    
    private static void assertHavingClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getHavingClause()) {
            assertFalse(actual.getHaving().isPresent(), assertContext.getText("Actual having segment should not exist."));
        } else {
            assertTrue(actual.getHaving().isPresent(), assertContext.getText("Actual having segment should exist."));
            HavingClauseAssert.assertIs(assertContext, actual.getHaving().get(), expected.getHavingClause());
        }
    }
    
    private static void assertProjection(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == actual.getProjections() && 0 == expected.getProjections().getSize()) {
            return;
        }
        ProjectionAssert.assertIs(assertContext, actual.getProjections(), expected.getProjections());
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getFrom()) {
            assertFalse(actual.getFrom().isPresent(), assertContext.getText("Actual simple-table should not exist."));
        } else {
            assertTrue(actual.getFrom().isPresent(), assertContext.getText("Actual from segment should exist."));
            TableAssert.assertIs(assertContext, actual.getFrom().get(), expected.getFrom());
        }
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getWhereClause()) {
            assertFalse(actual.getWhere().isPresent(), assertContext.getText("Actual where segment should not exist."));
        } else {
            assertTrue(actual.getWhere().isPresent(), assertContext.getText("Actual where segment should exist."));
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhereClause());
        }
    }
    
    private static void assertGroupByClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getGroupByClause()) {
            assertFalse(actual.getGroupBy().isPresent(), assertContext.getText("Actual group by segment should not exist."));
        } else {
            assertTrue(actual.getGroupBy().isPresent(), assertContext.getText("Actual group by segment should exist."));
            GroupByClauseAssert.assertIs(assertContext, actual.getGroupBy().get(), expected.getGroupByClause());
        }
    }
    
    private static void assertOrderByClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getOrderByClause()) {
            assertFalse(actual.getOrderBy().isPresent(), assertContext.getText("Actual order by segment should not exist."));
        } else {
            assertTrue(actual.getOrderBy().isPresent(), assertContext.getText("Actual order by segment should exist."));
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderByClause());
        }
    }
    
    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<LimitSegment> limitSegment = actual.getLimit();
        if (null == expected.getLimitClause()) {
            assertFalse(limitSegment.isPresent(), assertContext.getText("Actual limit segment should not exist."));
        } else {
            assertTrue(limitSegment.isPresent(), assertContext.getText("Actual limit segment should exist."));
            LimitClauseAssert.assertOffset(assertContext, limitSegment.get().getOffset().orElse(null), expected.getLimitClause().getOffset());
            LimitClauseAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimitClause());
        }
    }
    
    private static void assertLockClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<LockSegment> actualLock = actual.getLock();
        if (null == expected.getLockClause()) {
            assertFalse(actualLock.isPresent(), assertContext.getText("Actual lock segment should not exist."));
        } else {
            assertTrue(actualLock.isPresent(), assertContext.getText("Actual lock segment should exist."));
            LockClauseAssert.assertIs(assertContext, actualLock.get(), expected.getLockClause());
        }
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<WithSegment> withSegment = actual.getWith();
        if (null == expected.getWithClause()) {
            assertFalse(withSegment.isPresent(), assertContext.getText("Actual with segment should not exist."));
        } else {
            assertTrue(withSegment.isPresent(), assertContext.getText("Actual with segment should exist."));
            WithClauseAssert.assertIs(assertContext, withSegment.get(), expected.getWithClause());
        }
    }
    
    private static void assertCombineClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<CombineSegment> combineSegment = actual.getCombine();
        if (null == expected.getCombineClause()) {
            assertFalse(combineSegment.isPresent(), assertContext.getText("Actual combine segment should not exist."));
        } else {
            assertTrue(combineSegment.isPresent(), assertContext.getText("Actual combine segment should exist."));
            assertThat(assertContext.getText("Combine type assertion error: "), combineSegment.get().getCombineType().name(), is(expected.getCombineClause().getCombineType()));
            SQLSegmentAssert.assertIs(assertContext, combineSegment.get(), expected.getCombineClause());
            assertIs(assertContext, combineSegment.get().getLeft().getSelect(), expected.getCombineClause().getLeft());
            assertIs(assertContext, combineSegment.get().getRight().getSelect(), expected.getCombineClause().getRight());
        }
    }
    
    private static void assertModelClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<ModelSegment> modelSegment = actual.getModel();
        if (null == expected.getModelClause()) {
            assertFalse(modelSegment.isPresent(), assertContext.getText("Actual model segment should not exist."));
        } else {
            assertTrue(modelSegment.isPresent(), assertContext.getText("Actual model segment should exist."));
            ModelClauseAssert.assertIs(assertContext, modelSegment.get(), expected.getModelClause());
        }
    }
    
    private static void assertIntoClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<TableSegment> intoSegment = actual.getInto();
        if (null == expected.getIntoClause()) {
            assertFalse(intoSegment.isPresent(), assertContext.getText("Actual into segment should not exist."));
        } else {
            assertTrue(intoSegment.isPresent(), assertContext.getText("Actual into segment should exist."));
            TableAssert.assertIs(assertContext, intoSegment.get(), expected.getIntoClause());
        }
    }
    
    private static void assertOutfileClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<OutfileSegment> outfileSegment = actual.getOutfile();
        if (null == expected.getOutfileClause()) {
            assertFalse(outfileSegment.isPresent(), assertContext.getText("Actual outfile segment should not exist."));
        } else {
            assertTrue(outfileSegment.isPresent(), assertContext.getText("Actual outfile segment should exist."));
            OutfileClauseAssert.assertIs(assertContext, outfileSegment.get(), expected.getOutfileClause());
        }
    }
}

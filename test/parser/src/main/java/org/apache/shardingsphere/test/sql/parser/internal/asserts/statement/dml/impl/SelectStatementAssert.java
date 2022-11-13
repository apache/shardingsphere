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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.groupby.GroupByClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.having.HavingClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.lock.LockClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.model.ModelClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.dml.SelectStatementTestCase;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    }
    
    private static void assertWindowClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(actual);
        if (null == expected.getWindowClause()) {
            assertFalse(assertContext.getText("Actual window segment should not exist."), windowSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual window segment should exist."), windowSegment.isPresent());
            SQLSegmentAssert.assertIs(assertContext, windowSegment.get(), expected.getWindowClause());
        }
    }
    
    private static void assertHavingClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getHavingClause()) {
            assertFalse(assertContext.getText("Actual having segment should not exist."), actual.getHaving().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual having segment should exist."), actual.getHaving().isPresent());
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
            assertNull(assertContext.getText("Actual simple-table should not exist."), actual.getFrom());
        } else {
            TableAssert.assertIs(assertContext, actual.getFrom(), expected.getFrom());
        }
        if (actual instanceof MySQLSelectStatement) {
            if (null == expected.getSimpleTable()) {
                assertFalse(assertContext.getText("Actual simple-table should not exist."), ((MySQLSelectStatement) actual).getTable().isPresent());
            } else {
                Optional<SimpleTableSegment> table = ((MySQLSelectStatement) actual).getTable();
                TableAssert.assertIs(assertContext, table.orElse(null), expected.getSimpleTable());
            }
        }
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getWhereClause()) {
            assertFalse(assertContext.getText("Actual where segment should not exist."), actual.getWhere().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual where segment should exist."), actual.getWhere().isPresent());
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhereClause());
        }
    }
    
    private static void assertGroupByClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getGroupByClause()) {
            assertFalse(assertContext.getText("Actual group by segment should not exist."), actual.getGroupBy().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual group by segment should exist."), actual.getGroupBy().isPresent());
            GroupByClauseAssert.assertIs(assertContext, actual.getGroupBy().get(), expected.getGroupByClause());
        }
    }
    
    private static void assertOrderByClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == expected.getOrderByClause()) {
            assertFalse(assertContext.getText("Actual order by segment should not exist."), actual.getOrderBy().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual order by segment should exist."), actual.getOrderBy().isPresent());
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderByClause());
        }
    }
    
    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(actual);
        if (null == expected.getLimitClause()) {
            assertFalse(assertContext.getText("Actual limit segment should not exist."), limitSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual limit segment should exist."), limitSegment.isPresent());
            LimitClauseAssert.assertOffset(assertContext, limitSegment.get().getOffset().orElse(null), expected.getLimitClause().getOffset());
            LimitClauseAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimitClause());
        }
    }
    
    private static void assertLockClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<LockSegment> actualLock = SelectStatementHandler.getLockSegment(actual);
        if (null == expected.getLockClause()) {
            assertFalse(assertContext.getText("Actual lock segment should not exist."), actualLock.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual lock segment should exist."), actualLock.isPresent());
            LockClauseAssert.assertIs(assertContext, actualLock.get(), expected.getLockClause());
        }
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(actual);
        if (null == expected.getWithClause()) {
            assertFalse(assertContext.getText("Actual with segment should not exist."), withSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual with segment should exist."), withSegment.isPresent());
            WithClauseAssert.assertIs(assertContext, withSegment.get(), expected.getWithClause());
        }
    }
    
    private static void assertCombineClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<CombineSegment> combineSegment = actual.getCombine();
        if (null == expected.getCombineClause()) {
            assertFalse(assertContext.getText("Actual combine segment should not exist."), combineSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual combine segment should exist."), combineSegment.isPresent());
            assertThat(assertContext.getText("Combine type assertion error: "), combineSegment.get().getCombineType().name(), is(expected.getCombineClause().getCombineType()));
            SQLSegmentAssert.assertIs(assertContext, combineSegment.get(), expected.getCombineClause());
            assertIs(assertContext, combineSegment.get().getSelectStatement(), expected.getCombineClause().getSelectClause());
        }
    }
    
    private static void assertModelClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<ModelSegment> modelSegment = SelectStatementHandler.getModelSegment(actual);
        if (null == expected.getModelClause()) {
            assertFalse(assertContext.getText("Actual model segment should not exist."), modelSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual model segment should exist."), modelSegment.isPresent());
            ModelClauseAssert.assertIs(assertContext, modelSegment.get(), expected.getModelClause());
        }
    }
}

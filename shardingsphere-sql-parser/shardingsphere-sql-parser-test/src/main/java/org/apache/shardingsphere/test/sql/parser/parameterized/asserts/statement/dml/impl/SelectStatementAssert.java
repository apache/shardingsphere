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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.groupby.GroupByClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.lock.LockClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.SelectStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
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
        assertOrderByClause(assertContext, actual, expected);
        assertLimitClause(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
        assertLockClause(assertContext, actual, expected);
    }
    
    private static void assertProjection(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null == actual.getProjections() && 0 == expected.getProjections().getSize()) {
            return;
        }
        ProjectionAssert.assertIs(assertContext, actual.getProjections(), expected.getProjections());
    }

    private static void assertTable(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null != expected.getFrom()) {
            TableAssert.assertIs(assertContext, actual.getFrom(), expected.getFrom());
        } else {
            assertFalse(assertContext.getText("Actual from should not exist."), null != actual.getFrom());
        }
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null != expected.getWhereClause()) {
            assertTrue(assertContext.getText("Actual where segment should exist."), actual.getWhere().isPresent());
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhereClause());
        } else {
            assertFalse(assertContext.getText("Actual where segment should not exist."), actual.getWhere().isPresent());
        }
    }
    
    private static void assertGroupByClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null != expected.getGroupByClause()) {
            assertTrue(assertContext.getText("Actual group by segment should exist."), actual.getGroupBy().isPresent());
            GroupByClauseAssert.assertIs(assertContext, actual.getGroupBy().get(), expected.getGroupByClause());
        } else {
            assertFalse(assertContext.getText("Actual group by segment should not exist."), actual.getGroupBy().isPresent());
        }
    }
    
    private static void assertOrderByClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        if (null != expected.getOrderByClause()) {
            assertTrue(assertContext.getText("Actual order by segment should exist."), actual.getOrderBy().isPresent());
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderByClause());
        } else {
            assertFalse(assertContext.getText("Actual order by segment should not exist."), actual.getOrderBy().isPresent());
        }
    }
    
    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(actual);
        if (null != expected.getLimitClause()) {
            assertTrue(assertContext.getText("Actual limit segment should exist."), limitSegment.isPresent());
            LimitClauseAssert.assertOffset(assertContext, limitSegment.get().getOffset().orElse(null), expected.getLimitClause().getOffset());
            LimitClauseAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimitClause());
        } else {
            assertFalse(assertContext.getText("Actual limit segment should not exist."), limitSegment.isPresent());
        }
    }

    private static void assertLockClause(final SQLCaseAssertContext assertContext, final SelectStatement actual, final SelectStatementTestCase expected) {
        Optional<LockSegment> actualLock = SelectStatementHandler.getLockSegment(actual);
        if (null != expected.getLockClause()) {
            assertTrue(assertContext.getText("Actual lock segment should exist."), actualLock.isPresent());
            LockClauseAssert.assertIs(assertContext, actualLock.get(), expected.getLockClause());
        } else {
            assertFalse(assertContext.getText("Actual lock segment should not exist."), actualLock.isPresent());
        }
    }
}

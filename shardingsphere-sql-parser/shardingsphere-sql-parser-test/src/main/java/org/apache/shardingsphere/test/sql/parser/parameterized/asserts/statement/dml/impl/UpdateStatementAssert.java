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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.UpdateStatementHandler;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.UpdateStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Update statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateStatementAssert {
    
    /**
     * Assert update statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual update statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertSetClause(assertContext, actual, expected);
        assertWhereClause(assertContext, actual, expected);
        assertOrderByClause(assertContext, actual, expected);
        assertLimitClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        if (null != expected.getTable()) {
            TableAssert.assertIs(assertContext, actual.getTableSegment(), expected.getTable());
        } else {
            assertFalse(assertContext.getText("Actual from should not exist."), null != actual.getTableSegment());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        SetClauseAssert.assertIs(assertContext, actual.getSetAssignment(), expected.getSetClause());
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        if (null != expected.getWhereClause()) {
            assertTrue(assertContext.getText("Actual where segment should exist."), actual.getWhere().isPresent());
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhereClause());
        } else {
            assertFalse(assertContext.getText("Actual where segment should not exist."), actual.getWhere().isPresent());
        }
    }

    private static void assertOrderByClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        Optional<OrderBySegment> orderBySegment = UpdateStatementHandler.getOrderBySegment(actual);
        if (null != expected.getOrderByClause()) {
            assertTrue(assertContext.getText("Actual order by segment should exist."), orderBySegment.isPresent());
            OrderByClauseAssert.assertIs(assertContext, orderBySegment.get(), expected.getOrderByClause());
        } else {
            assertFalse(assertContext.getText("Actual order by segment should not exist."), orderBySegment.isPresent());
        }
    }

    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        Optional<LimitSegment> limitSegment = UpdateStatementHandler.getLimitSegment(actual);
        if (null != expected.getLimitClause()) {
            assertTrue(assertContext.getText("Actual limit segment should exist."), limitSegment.isPresent());
            LimitClauseAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimitClause());
        } else {
            assertFalse(assertContext.getText("Actual limit segment should not exist."), limitSegment.isPresent());
        }
    }
}
